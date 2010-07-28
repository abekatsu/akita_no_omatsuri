package com.damburisoft.android.yamalocationsrv;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class YamaMapOverlay extends Overlay {
    private GeoPoint mPoint;
    private Drawable mDrawable = null;

    private int mRadius = 12;
    private int mOffsetX;
    private int mOffsetY;

    public YamaMapOverlay(GeoPoint gp) {
        mPoint = gp;
    }

    public YamaMapOverlay(Drawable drawable, GeoPoint gp) {
        mDrawable = drawable;
        mPoint = gp;

        mOffsetX = 0 - drawable.getMinimumWidth() / 2;
        mOffsetY = 0 - drawable.getMinimumHeight() / 2;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);
        // 地図の描画時に、shadow=true, shadow=falseと2回呼び出される
        if (shadow) {
            // TODO
        } else {
            if (mDrawable == null) {
                drawCircleOnMap(canvas, mapView);
            } else {
                drawIconOnMap(canvas, mapView);
            }

        }
    }

    private void drawIconOnMap(Canvas canvas, MapView mapView) {
        Projection projection = mapView.getProjection();
        Point point = new Point();
        projection.toPixels(mPoint, point);
        point.offset(mOffsetX, mOffsetY);

        canvas.drawBitmap(((BitmapDrawable) mDrawable).getBitmap(), point.x,
                point.y, null);
    }

    private void drawCircleOnMap(Canvas canvas, MapView mapView) {
        // 地図上の場所と、描画用のCanvasの座標の変換

        Projection projection = mapView.getProjection();
        Point point = new Point();
        projection.toPixels(mPoint, point);
        point.offset(-mRadius / 2, -mRadius / 2);

        // Draw point body and outer ring
        Paint p = new Paint();

        p.setStyle(Style.FILL);
        p.setARGB(88, 0, 0, 224);
        p.setStrokeWidth(1);
        RectF spot = new RectF(point.x - mRadius, point.y - mRadius, point.x
                + mRadius, point.y + mRadius);
        canvas.drawOval(spot, p);

        p.setARGB(255, 0, 0, 224);
        p.setStyle(Style.STROKE);
        canvas.drawCircle(point.x, point.y, mRadius, p);

    }

}
