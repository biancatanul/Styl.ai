package com.example.wardrobeai.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.example.wardrobeai.logic.BinomialHeap;

import java.util.HashMap;
import java.util.Map;

public class BinomialHeapView extends View {

    private final BinomialHeap.Node heapHead;
    private final Paint nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final float NODE_RADIUS = 60f;
    private static final float LEVEL_HEIGHT = 160f;
    private static final float TREE_SPACING = 100f;

    private final Matrix matrix = new Matrix();
    private float lastTouchX, lastTouchY;
    private ScaleGestureDetector scaleDetector;

    private final Map<BinomialHeap.Node, float[]> positions = new HashMap<>();

    public BinomialHeapView(Context context, BinomialHeap heap) {
        super(context);
        this.heapHead = heap.getHead();

        edgePaint.setColor(Color.GRAY);
        edgePaint.setStrokeWidth(4f);
        edgePaint.setStyle(Paint.Style.STROKE);

        textPaint.setTextSize(24f);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);

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
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                lastTouchX = event.getX();
                lastTouchY = event.getY();
            } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE
                    && event.getPointerCount() == 1) {
                matrix.postTranslate(event.getX() - lastTouchX, event.getY() - lastTouchY);
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                invalidate();
            }
            return true;
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        positions.clear();
        float xOffset = NODE_RADIUS + 20;
        BinomialHeap.Node tree = heapHead;
        while (tree != null) {
            assignPositions(tree, xOffset, 0);
            int width = treeWidth(tree);
            xOffset += width * (NODE_RADIUS * 2 + 20) + TREE_SPACING;
            tree = tree.getSibling();
        }
        matrix.reset();
        matrix.postTranslate(0, NODE_RADIUS + 20);
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

        // assign children first, left to right
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

        // center root over its children, or just place it at startX if it's a leaf
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
            nodePaint.setStyle(Paint.Style.FILL);
            nodePaint.setColor(Color.rgb(60, 100, 180));
            canvas.drawCircle(pos[0], pos[1], NODE_RADIUS, nodePaint);

            nodePaint.setStyle(Paint.Style.STROKE);
            nodePaint.setColor(Color.WHITE);
            nodePaint.setStrokeWidth(2f);
            canvas.drawCircle(pos[0], pos[1], NODE_RADIUS, nodePaint);

            textPaint.setColor(Color.WHITE);
            String label = "s:" + n.score;
            canvas.drawText(label, pos[0], pos[1] - 8f, textPaint);
            String name = n.outfit.getName();
            if (name.length() > 8) name = name.substring(0, 7) + "…";
            canvas.drawText(name, pos[0], pos[1] + 16f, textPaint);
        }
        drawNodes(canvas, n.getChild());
        drawNodes(canvas, n.getSibling());
    }
}