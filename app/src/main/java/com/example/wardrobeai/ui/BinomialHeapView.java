package com.example.wardrobeai.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.example.wardrobeai.data.ClothingItem;
import com.example.wardrobeai.logic.BinomialHeap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinomialHeapView extends View {

    private static final float NODE_RADIUS = 60f;
    private static final float LEVEL_HEIGHT = 160f;
    private static final float TREE_SPACING = 120f;
    private static final float BOX_PADDING = 40f;
    private static final float TAP_SLOP = 10f;
    private final BinomialHeap.Node heapHead;
    private final Paint nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint boxLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix matrix = new Matrix();
    private final Map<BinomialHeap.Node, float[]> positions = new HashMap<>();
    private final Map<BinomialHeap.Node, String> outfitLabels = new HashMap<>();
    private OnNodeTappedListener listener;
    private float lastTouchX, lastTouchY;
    private float downX, downY;
    private ScaleGestureDetector scaleDetector;
    private float animProgress = 0f;
    private boolean animationStarted = false;
    private ValueAnimator entryAnimator;
    private BinomialHeap.Node selectedNode = null;
    private BinomialHeap.Node pulsingNode = null;
    private float pulseScale = 1f;
    private ValueAnimator pulseAnimator;
    public BinomialHeapView(Context context, BinomialHeap heap) {
        super(context);
        this.heapHead = heap.getHead();

        edgePaint.setColor(Color.rgb(180, 180, 180));
        edgePaint.setStrokeWidth(4f);
        edgePaint.setStyle(Paint.Style.STROKE);

        textPaint.setTextSize(26f);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);

        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setColor(Color.rgb(120, 120, 120));
        boxPaint.setStrokeWidth(3f);
        boxPaint.setPathEffect(new DashPathEffect(new float[]{18f, 10f}, 0f));

        boxLabelPaint.setTextSize(30f);
        boxLabelPaint.setColor(Color.rgb(200, 200, 200));
        boxLabelPaint.setTextAlign(Paint.Align.LEFT);
        boxLabelPaint.setFakeBoldText(true);

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setColor(Color.rgb(255, 210, 40));
        ringPaint.setStrokeWidth(6f);

        int[] counter = {1};
        assignLabels(heapHead, counter);

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
        selectedNode = null;
        invalidate();
    }

    private void assignLabels(BinomialHeap.Node n, int[] counter) {
        if (n == null) return;
        outfitLabels.put(n, "#" + counter[0]++);
        assignLabels(n.getChild(), counter);
        assignLabels(n.getSibling(), counter);
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

        for (Map.Entry<BinomialHeap.Node, float[]> entry : positions.entrySet()) {
            float[] pos = entry.getValue();
            float dx = canvasX - pos[0];
            float dy = canvasY - pos[1];
            if (Math.sqrt(dx * dx + dy * dy) <= NODE_RADIUS && listener != null) {
                BinomialHeap.Node node = entry.getKey();
                selectedNode = node;
                startPulse(node);
                String label = outfitLabels.getOrDefault(node, "?");
                listener.onNodeTapped(label, buildDetails(node));
                return;
            }
        }
    }

    private void startPulse(BinomialHeap.Node node) {
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
                pulseScale = 1f;
                invalidate();
            }
        });
        pulseAnimator.start();
    }

    private String colorName(String hex) {
        for (com.example.wardrobeai.data.ClothingColor c : com.example.wardrobeai.data.ClothingColor.values()) {
            if (c.getHex().equalsIgnoreCase(hex))
                return c.name().charAt(0) + c.name().substring(1).toLowerCase();
        }
        return hex;
    }

    private String buildDetails(BinomialHeap.Node node) {
        List<ClothingItem> outfitItems = node.outfit.getItems();
        int itemCount = outfitItems.size();
        int maxScore = itemCount * (itemCount - 1) / 2;

        StringBuilder sb = new StringBuilder();
        sb.append("Outfit ").append(outfitLabels.getOrDefault(node, "?"))
                .append(" scored ").append(node.score).append("/").append(maxScore)
                .append(" compatibility points");

        if (node.score == maxScore) {
            sb.append(" -- every pair of items works together.");
        } else {
            sb.append(" -- some pairs are not fully compatible.");
        }

        sb.append(" This is a B").append(node.getDegree())
                .append(" binomial tree, meaning it has ")
                .append((int) Math.pow(2, node.getDegree()))
                .append(" node(s) in its subtree.\n\n");

        sb.append("Items:\n");
        for (ClothingItem item : outfitItems) {
            String category = item.getCategory().name().charAt(0)
                    + item.getCategory().name().substring(1).toLowerCase();
            sb.append("  ").append(item.getName())
                    .append(" (").append(category).append(")\n");
        }

        return sb.toString().trim();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        positions.clear();
        float xOffset = NODE_RADIUS + BOX_PADDING + 20;
        BinomialHeap.Node tree = heapHead;
        while (tree != null) {
            assignPositions(tree, xOffset, 0);
            int width = treeWidth(tree);
            xOffset += width * (NODE_RADIUS * 2 + 20) + TREE_SPACING;
            tree = tree.getSibling();
        }
        matrix.reset();
        matrix.postTranslate(0, NODE_RADIUS + BOX_PADDING + 40);

        if (!animationStarted) startEntryAnimation();
    }

    private int treeWidth(BinomialHeap.Node n) {
        if (n == null) return 0;
        int width = 1;
        BinomialHeap.Node child = n.getChild();
        while (child != null) {
            width += treeWidth(child);
            child = child.getSibling();
        }
        return width;
    }

    private float assignPositions(BinomialHeap.Node n, float startX, int depth) {
        if (n == null) return startX;
        float childX = startX;
        BinomialHeap.Node child = n.getChild();
        float firstChildX = -1, lastChildX = -1;
        while (child != null) {
            childX = assignPositions(child, childX, depth + 1);
            float[] childPos = positions.get(child);
            if (firstChildX < 0) firstChildX = childPos[0];
            lastChildX = childPos[0];
            child = child.getSibling();
        }
        float rootX;
        if (firstChildX >= 0) {
            rootX = (firstChildX + lastChildX) / 2f;
        } else {
            rootX = startX;
            childX = startX + NODE_RADIUS * 2 + 20;
        }
        positions.put(n, new float[]{rootX, depth * LEVEL_HEIGHT});
        return childX;
    }

    private void collectTreePositions(BinomialHeap.Node n, List<float[]> out) {
        if (n == null) return;
        float[] pos = positions.get(n);
        if (pos != null) out.add(pos);
        BinomialHeap.Node child = n.getChild();
        while (child != null) {
            collectTreePositions(child, out);
            child = child.getSibling();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (heapHead == null) {
            textPaint.setColor(Color.GRAY);
            canvas.drawText("No outfits to display", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }
        canvas.save();
        canvas.concat(matrix);

        // bounding boxes fade in with animProgress
        int boxAlpha = (int) (animProgress * 255);
        boxPaint.setAlpha(boxAlpha);
        boxLabelPaint.setAlpha(boxAlpha);

        BinomialHeap.Node tree = heapHead;
        while (tree != null) {
            List<float[]> treePositions = new ArrayList<>();
            collectTreePositions(tree, treePositions);
            if (!treePositions.isEmpty()) {
                float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
                float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
                for (float[] p : treePositions) {
                    minX = Math.min(minX, p[0]);
                    maxX = Math.max(maxX, p[0]);
                    minY = Math.min(minY, p[1]);
                    maxY = Math.max(maxY, p[1]);
                }
                RectF box = new RectF(
                        minX - NODE_RADIUS - BOX_PADDING,
                        minY - NODE_RADIUS - BOX_PADDING,
                        maxX + NODE_RADIUS + BOX_PADDING,
                        maxY + NODE_RADIUS + BOX_PADDING
                );
                canvas.drawRoundRect(box, 24f, 24f, boxPaint);
                canvas.drawText("B" + tree.getDegree(),
                        box.left + 12f,
                        box.top + boxLabelPaint.getTextSize() + 4f,
                        boxLabelPaint);
            }
            tree = tree.getSibling();
        }

        edgePaint.setAlpha((int) (animProgress * 255));
        drawEdges(canvas, heapHead);
        drawNodes(canvas, heapHead);
        canvas.restore();
    }

    private void drawEdges(Canvas canvas, BinomialHeap.Node n) {
        if (n == null) return;
        float[] pos = positions.get(n);
        BinomialHeap.Node child = n.getChild();
        while (child != null) {
            float[] childPos = positions.get(child);
            if (pos != null && childPos != null)
                canvas.drawLine(pos[0], pos[1], childPos[0], childPos[1], edgePaint);
            drawEdges(canvas, child);
            child = child.getSibling();
        }
        drawEdges(canvas, n.getSibling());
    }

    private void drawNodes(Canvas canvas, BinomialHeap.Node n) {
        if (n == null) return;

        float[] pos = positions.get(n);
        if (pos != null) {
            float r = NODE_RADIUS * animProgress;
            if (n == pulsingNode) r *= pulseScale;

            // yellow selection ring
            if (n == selectedNode) {
                canvas.drawCircle(pos[0], pos[1], r + 10f, ringPaint);
            }

            nodePaint.setStyle(Paint.Style.FILL);
            nodePaint.setColor(Color.rgb(60, 100, 180));
            canvas.drawCircle(pos[0], pos[1], r, nodePaint);

            nodePaint.setStyle(Paint.Style.STROKE);
            nodePaint.setColor(Color.WHITE);
            nodePaint.setStrokeWidth(2f);
            canvas.drawCircle(pos[0], pos[1], r, nodePaint);

            if (animProgress > 0.5f) {
                textPaint.setColor(Color.WHITE);
                String label = outfitLabels.getOrDefault(n, "?");
                canvas.drawText(label, pos[0], pos[1] - 6f, textPaint);
                canvas.drawText("s:" + n.score, pos[0], pos[1] + 22f, textPaint);
            }
        }

        drawNodes(canvas, n.getChild());
        drawNodes(canvas, n.getSibling());
    }

    public interface OnNodeTappedListener {
        void onNodeTapped(String title, String details);
    }
}