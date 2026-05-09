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
import com.example.wardrobeai.logic.RedBlackTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedBlackTreeView extends View {

    public interface OnNodeTappedListener {
        void onNodeTapped(String title, String details);
    }

    private OnNodeTappedListener listener;

    public void setOnNodeTappedListener(OnNodeTappedListener l) {
        this.listener = l;
    }

    private final RedBlackTree.VisNode root;
    private final Paint nodePaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint statBgPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint statTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final float NODE_RADIUS  = 60f;
    private static final float LEVEL_HEIGHT = 160f;
    private static final float NODE_SPACING = 20f;
    private static final float TAP_SLOP     = 10f;

    private final Matrix matrix = new Matrix();
    private float lastTouchX, lastTouchY;
    private float downX, downY;
    private ScaleGestureDetector scaleDetector;

    private final Map<RedBlackTree.VisNode, float[]> positions = new HashMap<>();
    private int inorderIndex;

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
    private float pendingCenterX = Float.NaN;
    private float pendingCenterY = Float.NaN;

    // ── Selection & pulse ────────────────────────────────────────────────────
    private RedBlackTree.VisNode selectedNode = null;
    private RedBlackTree.VisNode pulsingNode  = null;
    private float pulseScale = 1f;
    private ValueAnimator pulseAnimator;

    public void clearSelection() {
        selectedNode = null;
        invalidate();
    }

    // ─────────────────────────────────────────────────────────────────────────

    public RedBlackTreeView(Context context, RedBlackTree rbt) {
        super(context);
        this.root = rbt.getVisTree();

        edgePaint.setColor(Color.GRAY);
        edgePaint.setStrokeWidth(4f);
        edgePaint.setStyle(Paint.Style.STROKE);

        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setColor(Color.rgb(255, 210, 40));
        ringPaint.setStrokeWidth(6f);

        statBgPaint.setStyle(Paint.Style.FILL);
        statBgPaint.setColor(Color.argb(180, 20, 20, 20));

        statTextPaint.setTextSize(28f);
        statTextPaint.setColor(Color.WHITE);
        statTextPaint.setTextAlign(Paint.Align.LEFT);

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

    // ── Center-focus animation ────────────────────────────────────────────────

    /**
     * Smoothly translates the canvas so that the given canvas-space point
     * ends up at the center of the view.
     *
     * How it works: map the canvas point through the current matrix to get
     * its current screen position, then compute the delta to screen center,
     * and animate that delta incrementally so the matrix shift is smooth.
     */
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

        for (Map.Entry<RedBlackTree.VisNode, float[]> entry : positions.entrySet()) {
            float[] pos = entry.getValue();
            float dx = canvasX - pos[0];
            float dy = canvasY - pos[1];
            if (Math.sqrt(dx * dx + dy * dy) <= NODE_RADIUS && listener != null) {
                RedBlackTree.VisNode node = entry.getKey();
                selectedNode = node;
                startPulse(node);
                pendingCenterX = pos[0];
                pendingCenterY = pos[1];
                listener.onNodeTapped(node.item.getName(), buildDetails(node));
                return;
            }
        }
    }

    private void startPulse(RedBlackTree.VisNode node) {
        if (pulseAnimator != null) pulseAnimator.cancel();
        pulsingNode = node;
        pulseAnimator = ValueAnimator.ofFloat(1f, 1.4f, 1f);
        pulseAnimator.setDuration(300);
        pulseAnimator.addUpdateListener(a -> {
            pulseScale = (float) a.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                pulsingNode = null;
                pulseScale  = 1f;
                invalidate();
            }
        });
        pulseAnimator.start();
    }

    // ── Stats helpers ─────────────────────────────────────────────────────────

    private int treeHeight(RedBlackTree.VisNode n) {
        if (n == null) return 0;
        return 1 + Math.max(treeHeight(n.left), treeHeight(n.right));
    }

    private int blackHeight(RedBlackTree.VisNode n) {
        if (n == null) return 0;
        return blackHeight(n.left) + (n.isRed ? 0 : 1);
    }

    private String[] buildStatsLines() {
        if (root == null) return new String[]{"Empty tree"};
        return new String[]{
                "Nodes:        " + positions.size(),
                "Height:       " + treeHeight(root),
                "Black-height: " + blackHeight(root)
        };
    }

    // ── Detail text helpers ───────────────────────────────────────────────────

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

    private String buildDetails(RedBlackTree.VisNode node) {
        ClothingItem item  = node.item;
        String nodeColor   = node.isRed ? "red" : "black";
        String leftName    = node.left  != null ? node.left.item.getName()  : "none";
        String rightName   = node.right != null ? node.right.item.getName() : "none";
        String category    = item.getCategory().name().charAt(0) + item.getCategory().name().substring(1).toLowerCase();
        String style       = item.getStyle().name().charAt(0) + item.getStyle().name().substring(1).toLowerCase();

        return item.getName() + " is stored as a " + nodeColor + " node. "
                + "Items in the tree are sorted alphabetically, so its left subtree holds items "
                + "that come before it (" + leftName + ") and its right subtree holds items "
                + "that come after (" + rightName + ").\n\n"
                + "Category: " + category + "\n"
                + "Style: "    + style    + "\n"
                + "Colors: "   + colorList(item.getColors());
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (root == null) return;

        inorderIndex = 0;
        positions.clear();
        assignPositions(root, 0);

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        for (float[] pos : positions.values()) {
            minX = Math.min(minX, pos[0]);
            maxX = Math.max(maxX, pos[0]);
        }
        float offsetX = (w - (maxX - minX)) / 2f - minX;
        matrix.reset();
        matrix.postTranslate(offsetX, NODE_RADIUS + 20);

        if (!animationStarted) startEntryAnimation();
    }

    private void assignPositions(RedBlackTree.VisNode node, int depth) {
        if (node == null) return;
        assignPositions(node.left, depth + 1);
        positions.put(node, new float[]{
                inorderIndex * (NODE_RADIUS * 2 + NODE_SPACING),
                depth * LEVEL_HEIGHT
        });
        inorderIndex++;
        assignPositions(node.right, depth + 1);
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (root == null) {
            textPaint.setColor(Color.GRAY);
            canvas.drawText("No items in wardrobe", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }
        canvas.save();
        canvas.concat(matrix);
        edgePaint.setAlpha((int) (animProgress * 255));
        drawEdges(canvas, root);
        drawNodes(canvas, root);
        canvas.restore();

        drawStatsOverlay(canvas);
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

    private void drawEdges(Canvas canvas, RedBlackTree.VisNode node) {
        if (node == null) return;
        float[] pos = positions.get(node);
        if (pos == null) return;
        if (node.left != null) {
            float[] lPos = positions.get(node.left);
            if (lPos != null) canvas.drawLine(pos[0], pos[1], lPos[0], lPos[1], edgePaint);
            drawEdges(canvas, node.left);
        }
        if (node.right != null) {
            float[] rPos = positions.get(node.right);
            if (rPos != null) canvas.drawLine(pos[0], pos[1], rPos[0], rPos[1], edgePaint);
            drawEdges(canvas, node.right);
        }
    }

    private void drawNodes(Canvas canvas, RedBlackTree.VisNode node) {
        if (node == null) return;
        drawNodes(canvas, node.left);
        drawNodes(canvas, node.right);

        float[] pos = positions.get(node);
        if (pos == null) return;

        float r = NODE_RADIUS * animProgress;
        if (node == pulsingNode) r *= pulseScale;

        if (node == selectedNode) {
            canvas.drawCircle(pos[0], pos[1], r + 10f, ringPaint);
        }

        Bitmap photo = photoBitmaps.get(node.item.getId());
        if (photo != null) {
            drawPhotoNode(canvas, photo, pos[0], pos[1], r);
        } else {
            nodePaint.setStyle(Paint.Style.FILL);
            nodePaint.setColor(node.isRed ? Color.rgb(200, 30, 30) : Color.rgb(30, 30, 30));
            canvas.drawCircle(pos[0], pos[1], r, nodePaint);
        }

        nodePaint.setStyle(Paint.Style.STROKE);
        nodePaint.setColor(Color.WHITE);
        nodePaint.setStrokeWidth(2f);
        canvas.drawCircle(pos[0], pos[1], r, nodePaint);

        if (animProgress > 0.5f && photo == null) {
            android.graphics.drawable.Drawable icon = androidx.core.content.ContextCompat
                    .getDrawable(getContext(), node.item.getCategory().getIconRes());
            if (icon != null) {
                int half = (int)(18 * animProgress);
                icon.setBounds((int)pos[0] - half, (int)pos[1] - half - 14,
                        (int)pos[0] + half, (int)pos[1] + half - 14);
                icon.setTint(Color.WHITE);
                icon.draw(canvas);
            }
            textPaint.setColor(Color.WHITE);
            String name = node.item.getName();
            if (name.length() > 8) name = name.substring(0, 7) + "…";
            canvas.drawText(name, pos[0], pos[1] + 34f, textPaint);
        }
    }

    /**
     * Draws a bitmap center-cropped into a circle.
     * When photo support is wired up, this is the only method that needs
     * changing -- everything else already calls it.
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
}