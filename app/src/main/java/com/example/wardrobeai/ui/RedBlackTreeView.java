package com.example.wardrobeai.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

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
    private final Paint nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final float NODE_RADIUS = 60f;
    private static final float LEVEL_HEIGHT = 160f;
    private static final float NODE_SPACING = 20f;
    private static final float TAP_SLOP = 10f;


    private final Matrix matrix = new Matrix();
    private float lastTouchX, lastTouchY;
    private float downX, downY;
    private ScaleGestureDetector scaleDetector;

    private final Map<RedBlackTree.VisNode, float[]> positions = new HashMap<>();
    private int inorderIndex;

    public RedBlackTreeView(Context context, RedBlackTree rbt) {
        super(context);
        this.root = rbt.getVisTree();

        edgePaint.setColor(Color.GRAY);
        edgePaint.setStrokeWidth(4f);
        edgePaint.setStyle(Paint.Style.STROKE);

        textPaint.setTextSize(24f);
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

    private void handleTap(float screenX, float screenY) {
        // invert the matrix to convert screen coords -> canvas coords
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
                String title = node.item.getName();
                String details = buildDetails(node);
                listener.onNodeTapped(title, details);
                return;
            }
        }
    }

    private String colorName(String hex) {
        for (com.example.wardrobeai.data.ClothingColor c : com.example.wardrobeai.data.ClothingColor.values()) {
            if (c.getHex().equalsIgnoreCase(hex)) return c.name().charAt(0) + c.name().substring(1).toLowerCase();
        }
        return hex;
    }

    private String colorList(List<String> hexColors) {
        List<String> names = new ArrayList<>();
        for (String hex : hexColors) names.add(colorName(hex));
        return String.join(", ", names);
    }

    private String buildDetails(RedBlackTree.VisNode node) {
        ClothingItem item = node.item;
        String nodeColor = node.isRed ? "red" : "black";
        String leftName  = node.left  != null ? node.left.item.getName()  : "none";
        String rightName = node.right != null ? node.right.item.getName() : "none";

        String category = item.getCategory().name().charAt(0)
                + item.getCategory().name().substring(1).toLowerCase();
        String style = item.getStyle().name().charAt(0)
                + item.getStyle().name().substring(1).toLowerCase();

        return item.getName() + " is stored as a " + nodeColor + " node. "
                + "Items in the tree are sorted alphabetically, so its left subtree holds items "
                + "that come before it (" + leftName + ") and its right subtree holds items "
                + "that come after (" + rightName + ").\n\n"
                + "Category: " + category + "\n"
                + "Style: " + style + "\n"
                + "Colors: " + colorList(item.getColors());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (root == null) return;

        inorderIndex = 0;
        positions.clear();
        assignPositions(root, 0);

        // center the tree horizontally
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        for (float[] pos : positions.values()) {
            minX = Math.min(minX, pos[0]);
            maxX = Math.max(maxX, pos[0]);
        }
        float offsetX = (w - (maxX - minX)) / 2f - minX;
        matrix.reset();
        matrix.postTranslate(offsetX, NODE_RADIUS + 20);
    }

    // Inorder traversal assigns x positions left to right, depth gives y
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
        drawEdges(canvas, root);
        drawNodes(canvas, root);
        canvas.restore();
    }

    // Draw edges in a separate pass so they appear behind nodes
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

        // fill
        nodePaint.setStyle(Paint.Style.FILL);
        nodePaint.setColor(node.isRed ? Color.rgb(200, 30, 30) : Color.rgb(30, 30, 30));
        canvas.drawCircle(pos[0], pos[1], NODE_RADIUS, nodePaint);

        // border
        nodePaint.setStyle(Paint.Style.STROKE);
        nodePaint.setColor(Color.WHITE);
        nodePaint.setStrokeWidth(2f);
        canvas.drawCircle(pos[0], pos[1], NODE_RADIUS, nodePaint);

        // label
        textPaint.setColor(Color.WHITE);
        String name = node.item.getName();
        if (name.length() > 8) name = name.substring(0, 7) + "…";
        canvas.drawText(name, pos[0], pos[1] + 8f, textPaint);
    }
}