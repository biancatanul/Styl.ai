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
import com.example.wardrobeai.logic.CompatibilityGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompatibilityGraphView extends View {

    private final List<ClothingItem> items;
    private final CompatibilityGraph graph;
    private final Map<String, float[]> positions = new HashMap<>();

    private final Paint nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final float NODE_RADIUS = 60f;

    private final Matrix matrix = new Matrix();
    private float lastTouchX, lastTouchY;
    private ScaleGestureDetector scaleDetector;

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
        if (items.isEmpty()) return;

        // place nodes evenly around a circle
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

        // draw edges first
        Map<String, List<String>> adjacency = graph.getAdjacencyList();
        for (ClothingItem item : items) {
            float[] from = positions.get(item.getId());
            if (from == null) continue;
            List<String> neighbors = adjacency.get(item.getId());
            if (neighbors == null) continue;
            for (String neighborId : neighbors) {
                float[] to = positions.get(neighborId);
                if (to == null) continue;
                // only draw each edge once
                if (item.getId().compareTo(neighborId) < 0)
                    canvas.drawLine(from[0], from[1], to[0], to[1], edgePaint);
            }
        }

        // draw nodes on top
        for (ClothingItem item : items) {
            float[] pos = positions.get(item.getId());
            if (pos == null) continue;

            nodePaint.setStyle(Paint.Style.FILL);
            nodePaint.setColor(Color.rgb(30, 140, 100));
            canvas.drawCircle(pos[0], pos[1], NODE_RADIUS, nodePaint);

            nodePaint.setStyle(Paint.Style.STROKE);
            nodePaint.setColor(Color.WHITE);
            nodePaint.setStrokeWidth(2f);
            canvas.drawCircle(pos[0], pos[1], NODE_RADIUS, nodePaint);

            String name = item.getName();
            if (name.length() > 8) name = name.substring(0, 7) + "…";
            textPaint.setColor(Color.WHITE);
            canvas.drawText(name, pos[0], pos[1] + 8f, textPaint);
        }

        canvas.restore();
    }
}