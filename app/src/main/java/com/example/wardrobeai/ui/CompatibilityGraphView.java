package com.example.wardrobeai.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.example.wardrobeai.data.ClothingItem;
import com.example.wardrobeai.logic.CompatibilityGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompatibilityGraphView extends View {

    public interface OnNodeTappedListener {
        void onNodeTapped(String title, String details);
    }

    private OnNodeTappedListener listener;

    public void setOnNodeTappedListener(OnNodeTappedListener l) {
        this.listener = l;
    }

    private final List<ClothingItem> items;
    private final CompatibilityGraph graph;
    private final Map<String, float[]> positions = new HashMap<>();

    private final Paint nodePaint          = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint          = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint          = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint          = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mostConnectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint statBgPaint        = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint statTextPaint      = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final float NODE_RADIUS = 60f;
    private static final float TAP_SLOP    = 10f;

    private final Matrix matrix = new Matrix();
    private float lastTouchX, lastTouchY;
    private float downX, downY;
    private ScaleGestureDetector scaleDetector;

    // ── Photos ────────────────────────────────────────────────────────────────
    // When photo functionality is added, call setPhoto(itemId, bitmap).
    // Nodes with a photo will render as a circular crop instead of a color fill.
    private final Map<String, Bitmap> photoBitmaps = new HashMap<>();

    public void setPhoto(String itemId, Bitmap bmp) {
        photoBitmaps.put(itemId, bmp);
        invalidate();
    }

    // ── Animation ────────────────────────────────────────────────────────────
    private float animProgress = 0f;
    private boolean animationStarted = false;
    private ValueAnimator entryAnimator;
    private ValueAnimator centerAnimator;

    // ── Selection & pulse ────────────────────────────────────────────────────
    private String selectedId = null;
    private String pulsingId  = null;
    private float  pulseScale = 1f;
    private ValueAnimator pulseAnimator;
    private float pendingCenterX = Float.NaN;
    private float pendingCenterY = Float.NaN;
    private boolean layoutDone = false;

    // ── Most-connected node ───────────────────────────────────────────────────
    private String mostConnectedId   = null;
    private String mostConnectedName = "none";

    public void clearSelection() {
        selectedId = null;
        invalidate();
    }

    // ─────────────────────────────────────────────────────────────────────────

    public CompatibilityGraphView(Context context, List<ClothingItem> items, CompatibilityGraph graph) {
        super(context);
        this.items = items;
        this.graph = graph;

        edgePaint.setColor(Color.rgb(150, 150, 150));
        edgePaint.setStrokeWidth(3f);
        edgePaint.setStyle(Paint.Style.STROKE);

        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setColor(Color.rgb(255, 210, 40));
        ringPaint.setStrokeWidth(6f);

        mostConnectedPaint.setStyle(Paint.Style.STROKE);
        mostConnectedPaint.setColor(Color.rgb(255, 140, 0));
        mostConnectedPaint.setStrokeWidth(5f);

        statBgPaint.setStyle(Paint.Style.FILL);
        statBgPaint.setColor(Color.argb(180, 20, 20, 20));

        statTextPaint.setTextSize(28f);
        statTextPaint.setColor(Color.WHITE);
        statTextPaint.setTextAlign(Paint.Align.LEFT);

        computeMostConnected();

        scaleDetector = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        matrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(),
                                detector.getFocusX(), detector.getFocusY());
                        invalidate();
                        return true;
                    }
                });

        setOnTouchListener((v, event) -> {
            scaleDetector.onTouchEvent(event);
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX      = event.getX();
                    downY      = event.getY();
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() == 1) {
                        matrix.postTranslate(event.getX() - lastTouchX, event.getY() - lastTouchY);
                        lastTouchX = event.getX();
                        lastTouchY = event.getY();
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    float dx = event.getX() - downX;
                    float dy = event.getY() - downY;
                    if (Math.sqrt(dx * dx + dy * dy) < TAP_SLOP) {
                        handleTap(event.getX(), event.getY());
                    }
                    break;
            }
            return true;
        });
    }

    // ── Most-connected ────────────────────────────────────────────────────────

    private void computeMostConnected() {
        Map<String, List<String>> adj = graph.getAdjacencyList();
        int maxEdges = -1;
        for (ClothingItem item : items) {
            List<String> neighbors = adj.get(item.getId());
            int count = neighbors != null ? neighbors.size() : 0;
            if (count > maxEdges) {
                maxEdges         = count;
                mostConnectedId   = item.getId();
                mostConnectedName = item.getName();
            }
        }
    }

    // ── Center-focus animation ────────────────────────────────────────────────

    private void animateCenterTo(float canvasX, float canvasY) {
        float[] screenPt = {canvasX, canvasY};
        matrix.mapPoints(screenPt);
        float targetDx = getWidth()  / 2f - screenPt[0];
        float targetDy = getHeight() / 2f - screenPt[1];

        if (centerAnimator != null) centerAnimator.cancel();
        final float[] applied = {0f, 0f};
        centerAnimator = ValueAnimator.ofFloat(0f, 1f);
        centerAnimator.setDuration(400);
        centerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        centerAnimator.addUpdateListener(a -> {
            float t  = (float) a.getAnimatedValue();
            float dx = targetDx * t - applied[0];
            float dy = targetDy * t - applied[1];
            applied[0] = targetDx * t;
            applied[1] = targetDy * t;
            matrix.postTranslate(dx, dy);
            invalidate();
        });
        centerAnimator.start();
    }
    public void executePendingCenter() {
        if (!Float.isNaN(pendingCenterX)) {
            animateCenterTo(pendingCenterX, pendingCenterY);
            pendingCenterX = Float.NaN;
            pendingCenterY = Float.NaN;
        }
    }

    // ── Entry animation ───────────────────────────────────────────────────────

    private void startEntryAnimation() {
        animationStarted = true;
        entryAnimator = ValueAnimator.ofFloat(0f, 1f);
        entryAnimator.setDuration(700);
        entryAnimator.setInterpolator(new OvershootInterpolator(0.6f));
        entryAnimator.addUpdateListener(a -> {
            animProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        entryAnimator.start();
    }

    // ── Tap ───────────────────────────────────────────────────────────────────

    private void handleTap(float screenX, float screenY) {
        Matrix inverse = new Matrix();
        matrix.invert(inverse);
        float[] point = {screenX, screenY};
        inverse.mapPoints(point);
        float canvasX = point[0];
        float canvasY = point[1];

        for (ClothingItem item : items) {
            float[] pos = positions.get(item.getId());
            if (pos == null) continue;
            float dx = canvasX - pos[0];
            float dy = canvasY - pos[1];
            if (Math.sqrt(dx * dx + dy * dy) <= NODE_RADIUS && listener != null) {
                selectedId = item.getId();
                startPulse(item.getId());
                pendingCenterX = pos[0];
                pendingCenterY = pos[1];
                listener.onNodeTapped(item.getName(), buildDetails(item));
                return;
            }
        }
    }

    private void startPulse(String itemId) {
        if (pulseAnimator != null) pulseAnimator.cancel();
        pulsingId = itemId;
        pulseAnimator = ValueAnimator.ofFloat(1f, 1.4f, 1f);
        pulseAnimator.setDuration(300);
        pulseAnimator.addUpdateListener(a -> {
            pulseScale = (float) a.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                pulsingId  = null;
                pulseScale = 1f;
                invalidate();
            }
        });
        pulseAnimator.start();
    }

    // ── Color helpers ─────────────────────────────────────────────────────────

    private int nodeColor(ClothingItem item) {
        if (item.getColors() != null && !item.getColors().isEmpty()) {
            try {
                return Color.parseColor(item.getColors().get(0));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return Color.rgb(30, 140, 100);
    }

    private int textColorFor(int bgColor) {
        double lum = (0.299 * Color.red(bgColor)
                + 0.587 * Color.green(bgColor)
                + 0.114 * Color.blue(bgColor)) / 255.0;
        return lum > 0.5 ? Color.BLACK : Color.WHITE;
    }

    private String colorName(String hex) {
        for (com.example.wardrobeai.data.ClothingColor c : com.example.wardrobeai.data.ClothingColor.values()) {
            if (c.getHex().equalsIgnoreCase(hex))
                return c.name().charAt(0) + c.name().substring(1).toLowerCase();
        }
        return hex;
    }

    private String colorList(List<String> hexColors) {
        List<String> names = new ArrayList<>();
        for (String hex : hexColors) names.add(colorName(hex));
        return String.join(", ", names);
    }

    // ── Stats helpers ─────────────────────────────────────────────────────────

    private String[] buildStatsLines() {
        Map<String, List<String>> adj = graph.getAdjacencyList();
        int edgeCount = 0;
        for (List<String> neighbors : adj.values()) edgeCount += neighbors.size();
        edgeCount /= 2;
        return new String[]{
                "Nodes: " + items.size(),
                "Edges: " + edgeCount,
                "Hub:   " + mostConnectedName
        };
    }

    // ── Detail text ───────────────────────────────────────────────────────────

    private String buildDetails(ClothingItem item) {
        Map<String, List<String>> adjacency = graph.getAdjacencyList();
        List<String> neighborIds  = adjacency.get(item.getId());
        int neighborCount         = neighborIds != null ? neighborIds.size() : 0;
        String category = item.getCategory().name().charAt(0) + item.getCategory().name().substring(1).toLowerCase();
        String style    = item.getStyle().name().charAt(0)    + item.getStyle().name().substring(1).toLowerCase();
        String colors   = colorList(item.getColors());

        boolean isNeutral = false;
        for (String hex : item.getColors()) {
            String name = colorName(hex).toLowerCase();
            if (name.equals("black") || name.equals("white") || name.equals("gray")) {
                isNeutral = true;
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(item.getName()).append(" is a ").append(style.toLowerCase())
                .append(" ").append(category.toLowerCase()).append(" in ").append(colors).append(". ");
        if (isNeutral) sb.append("Its neutral color means it pairs with almost any item regardless of style. ");
        if (item.getId().equals(mostConnectedId)) sb.append("This is the most compatible item in your wardrobe. ");
        sb.append("It is compatible with ").append(neighborCount).append(" item(s) in your wardrobe.");

        if (neighborIds != null && !neighborIds.isEmpty()) {
            sb.append("\n\nCompatible with:\n");
            for (String neighborId : neighborIds) {
                for (ClothingItem other : items) {
                    if (other.getId().equals(neighborId)) {
                        String otherCat = other.getCategory().name().charAt(0)
                                + other.getCategory().name().substring(1).toLowerCase();
                        sb.append("  - ").append(other.getName())
                                .append(" (").append(otherCat).append(")\n");
                        break;
                    }
                }
            }
        }

        return sb.toString().trim();
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (items.isEmpty()) return;
        if (layoutDone) return;
        layoutDone = true;
        float cx     = w / 2f;
        float cy     = h / 2f;
        float radius = Math.min(w, h) / 2f - NODE_RADIUS - 40f;

        for (int i = 0; i < items.size(); i++) {
            double angle = 2 * Math.PI * i / items.size();
            positions.put(items.get(i).getId(), new float[]{
                    (float) (cx + radius * Math.cos(angle)),
                    (float) (cy + radius * Math.sin(angle))
            });
        }

        matrix.reset();
        if (!animationStarted) startEntryAnimation();
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (items.isEmpty()) {
            textPaint.setColor(Color.GRAY);
            canvas.drawText("No items in wardrobe", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        canvas.save();
        canvas.concat(matrix);

        edgePaint.setAlpha((int) (animProgress * 255));
        Map<String, List<String>> adjacency = graph.getAdjacencyList();
        for (ClothingItem item : items) {
            float[] from = positions.get(item.getId());
            if (from == null) continue;
            List<String> neighbors = adjacency.get(item.getId());
            if (neighbors == null) continue;
            for (String neighborId : neighbors) {
                float[] to = positions.get(neighborId);
                if (to != null && item.getId().compareTo(neighborId) < 0)
                    canvas.drawLine(from[0], from[1], to[0], to[1], edgePaint);
            }
        }

        for (ClothingItem item : items) {
            float[] pos = positions.get(item.getId());
            if (pos == null) continue;

            float r = NODE_RADIUS * animProgress;
            if (item.getId().equals(pulsingId)) r *= pulseScale;

            // most-connected ring (outer, orange-gold, always visible)
            if (item.getId().equals(mostConnectedId)) {
                mostConnectedPaint.setAlpha((int) (animProgress * 255));
                canvas.drawCircle(pos[0], pos[1], r + 20f, mostConnectedPaint);
            }

            // selection ring (inner, yellow, only when tapped)
            if (item.getId().equals(selectedId)) {
                canvas.drawCircle(pos[0], pos[1], r + 10f, ringPaint);
            }

            Bitmap photo = photoBitmaps.get(item.getId());
            if (photo != null) {
                drawPhotoNode(canvas, photo, pos[0], pos[1], r);
            } else {
                int fillColor = nodeColor(item);
                nodePaint.setStyle(Paint.Style.FILL);
                nodePaint.setColor(fillColor);
                canvas.drawCircle(pos[0], pos[1], r, nodePaint);

                if (animProgress > 0.5f) {
                    textPaint.setColor(textColorFor(fillColor));
                    String name = item.getName();
                    if (name.length() > 8) name = name.substring(0, 7) + "…";
                    canvas.drawText(name, pos[0], pos[1] + 8f, textPaint);
                }
            }

            nodePaint.setStyle(Paint.Style.STROKE);
            nodePaint.setColor(Color.WHITE);
            nodePaint.setStrokeWidth(2f);
            canvas.drawCircle(pos[0], pos[1], r, nodePaint);
        }

        canvas.restore();
        drawStatsOverlay(canvas);
    }

    /**
     * Draws a bitmap center-cropped into a circle.
     * When photo support is wired up, this is the only method that needs
     * changing -- everything else already calls it via photoBitmaps.
     */
    private void drawPhotoNode(Canvas canvas, Bitmap bmp, float cx, float cy, float r) {
        int srcSize = Math.min(bmp.getWidth(), bmp.getHeight());
        Rect src = new Rect(
                (bmp.getWidth()  - srcSize) / 2,
                (bmp.getHeight() - srcSize) / 2,
                (bmp.getWidth()  + srcSize) / 2,
                (bmp.getHeight() + srcSize) / 2);

        Path clip = new Path();
        clip.addCircle(cx, cy, r, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clip);
        canvas.drawBitmap(bmp, src, new RectF(cx - r, cy - r, cx + r, cy + r), null);
        canvas.restore();
    }

    private void drawStatsOverlay(Canvas canvas) {
        String[] lines   = buildStatsLines();
        float padding    = 16f;
        float lineHeight = statTextPaint.getTextSize() + 6f;

        float maxWidth = 0;
        for (String line : lines) maxWidth = Math.max(maxWidth, statTextPaint.measureText(line));

        float cardW = maxWidth + padding * 2;
        float cardH = lines.length * lineHeight + padding * 2 - 6f;

        canvas.drawRoundRect(new RectF(20f, 20f, 20f + cardW, 20f + cardH), 16f, 16f, statBgPaint);

        float textY = 20f + padding + statTextPaint.getTextSize();
        for (String line : lines) {
            canvas.drawText(line, 20f + padding, textY, statTextPaint);
            textY += lineHeight;
        }
    }
}