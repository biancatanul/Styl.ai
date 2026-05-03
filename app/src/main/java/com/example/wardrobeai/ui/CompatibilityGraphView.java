package com.example.wardrobeai.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.example.wardrobeai.data.ClothingItem;
import com.example.wardrobeai.logic.CompatibilityGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompatibilityGraphView extends View {

    private static final float NODE_RADIUS = 60f;
    private static final float TAP_SLOP = 10f;
    private final List<ClothingItem> items;
    private final CompatibilityGraph graph;
    private final Map<String, float[]> positions = new HashMap<>();
    private final Paint nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix matrix = new Matrix();
    private OnNodeTappedListener listener;
    private float lastTouchX, lastTouchY;
    private float downX, downY;
    private ScaleGestureDetector scaleDetector;
    private float animProgress = 0f;
    private boolean animationStarted = false;
    private ValueAnimator entryAnimator;
    private String selectedId = null;  // item ID of the selected node
    private String pulsingId = null;
    private float pulseScale = 1f;
    private ValueAnimator pulseAnimator;
    public CompatibilityGraphView(Context context, List<ClothingItem> items, CompatibilityGraph graph) {
        super(context);
        this.items = items;
        this.graph = graph;

        edgePaint.setColor(Color.rgb(150, 150, 150));
        edgePaint.setStrokeWidth(3f);
        edgePaint.setStyle(Paint.Style.STROKE);

        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setColor(Color.rgb(255, 210, 40));
        ringPaint.setStrokeWidth(6f);

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
                    downX = event.getX();
                    downY = event.getY();
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

    public void setOnNodeTappedListener(OnNodeTappedListener l) {
        this.listener = l;
    }

    public void clearSelection() {
        selectedId = null;
        invalidate();
    }

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
                pulsingId = null;
                pulseScale = 1f;
                invalidate();
            }
        });
        pulseAnimator.start();
    }

    private int nodeColor(ClothingItem item) {
        if (item.getColors() != null && !item.getColors().isEmpty()) {
            String hex = item.getColors().get(0);
            try {
                // ClothingColor hex values already include '#', e.g. "#ff2626"
                return Color.parseColor(hex);
            } catch (IllegalArgumentException ignored) {
                // malformed hex -- fall through to default
            }
        }
        return Color.rgb(30, 140, 100);
    }

    private int textColorFor(int bgColor) {
        double luminance = (0.299 * Color.red(bgColor)
                + 0.587 * Color.green(bgColor)
                + 0.114 * Color.blue(bgColor)) / 255.0;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
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

    private String buildDetails(ClothingItem item) {
        Map<String, List<String>> adjacency = graph.getAdjacencyList();
        List<String> neighborIds = adjacency.get(item.getId());
        int neighborCount = neighborIds != null ? neighborIds.size() : 0;

        String category = item.getCategory().name().charAt(0)
                + item.getCategory().name().substring(1).toLowerCase();
        String style = item.getStyle().name().charAt(0)
                + item.getStyle().name().substring(1).toLowerCase();
        String colors = colorList(item.getColors());

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

        if (isNeutral) {
            sb.append("Its neutral color means it pairs with almost any item regardless of style. ");
        }

        sb.append("It is compatible with ").append(neighborCount).append(" item(s) in your wardrobe.");

        if (neighborIds != null && !neighborIds.isEmpty()) {
            sb.append("\n\nCompatible with:\n");
            for (String neighborId : neighborIds) {
                for (ClothingItem other : items) {
                    if (other.getId().equals(neighborId)) {
                        String otherCategory = other.getCategory().name().charAt(0)
                                + other.getCategory().name().substring(1).toLowerCase();
                        sb.append("  - ").append(other.getName())
                                .append(" (").append(otherCategory).append(")\n");
                        break;
                    }
                }
            }
        }

        return sb.toString().trim();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (items.isEmpty()) return;

        float cx = w / 2f;
        float cy = h / 2f;
        float radius = Math.min(w, h) / 2f - NODE_RADIUS - 40f;

        for (int i = 0; i < items.size(); i++) {
            double angle = 2 * Math.PI * i / items.size();
            float x = (float) (cx + radius * Math.cos(angle));
            float y = (float) (cy + radius * Math.sin(angle));
            positions.put(items.get(i).getId(), new float[]{x, y});
        }

        matrix.reset();
        if (!animationStarted) startEntryAnimation();
    }

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

        // edges
        edgePaint.setAlpha((int) (animProgress * 255));
        Map<String, List<String>> adjacency = graph.getAdjacencyList();
        for (ClothingItem item : items) {
            float[] from = positions.get(item.getId());
            if (from == null) continue;
            List<String> neighbors = adjacency.get(item.getId());
            if (neighbors == null) continue;
            for (String neighborId : neighbors) {
                float[] to = positions.get(neighborId);
                if (to == null) continue;
                if (item.getId().compareTo(neighborId) < 0)
                    canvas.drawLine(from[0], from[1], to[0], to[1], edgePaint);
            }
        }

        // nodes
        for (ClothingItem item : items) {
            float[] pos = positions.get(item.getId());
            if (pos == null) continue;

            float r = NODE_RADIUS * animProgress;
            if (item.getId().equals(pulsingId)) r *= pulseScale;

            // yellow selection ring
            if (item.getId().equals(selectedId)) {
                canvas.drawCircle(pos[0], pos[1], r + 10f, ringPaint);
            }

            int fillColor = nodeColor(item);

            nodePaint.setStyle(Paint.Style.FILL);
            nodePaint.setColor(fillColor);
            canvas.drawCircle(pos[0], pos[1], r, nodePaint);

            nodePaint.setStyle(Paint.Style.STROKE);
            nodePaint.setColor(Color.WHITE);
            nodePaint.setStrokeWidth(2f);
            canvas.drawCircle(pos[0], pos[1], r, nodePaint);

            if (animProgress > 0.5f) {
                textPaint.setColor(textColorFor(fillColor));
                String name = item.getName();
                if (name.length() > 8) name = name.substring(0, 7) + "…";
                canvas.drawText(name, pos[0], pos[1] + 8f, textPaint);
            }
        }

        canvas.restore();
    }

    public interface OnNodeTappedListener {
        void onNodeTapped(String title, String details);
    }
}