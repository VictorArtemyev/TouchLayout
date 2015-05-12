package com.vitman.touchlayout.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by Victor Artemjev on 30.04.2015.
 */
public class ZoomMapRelativeLayout extends RelativeLayout
        implements ScaleGestureDetector.OnScaleGestureListener {

    private enum Mode {
        NONE, DRAG, ZOOM
    }

    private static final String LOG_TAG = ZoomMapRelativeLayout.class.getSimpleName();

    private static final float MIN_ZOOM = 1f;
    private static final float MAX_ZOOM = 2f;

    // scale event
    private float mScaleFactor = 1f;
    private float mLastScaleFactor = 0f;
    private float mPivotX = 0f;
    private float mPivotY = 0f;

    // move event
    private PointF mStartPoint = new PointF(0f, 0f);
    private PointF mPrevPoint = new PointF(0f, 0f);
    private float mDx = 0f;
    private float mDy = 0f;

    private int mapImageWidth;
    private int mapImageHeight;

    private Mode mMode = Mode.NONE;

    public ZoomMapRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    public ZoomMapRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomMapRelativeLayout(Context context, AttributeSet attrs,
                                 int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    private void init(Context context) {

        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(context, ZoomMapRelativeLayout.this);
        ZoomMapRelativeLayout.this.setOnTouchListener(new OnTouchListener() {

            // How much to translate the canvas
            PointF lastPoint = new PointF(0f, 0f);
            float deltaX;
            float deltaY;

            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                scaleGestureDetector.onTouchEvent(motionEvent);

                // Where the finger first  touches the screen
                PointF currentPoint = new PointF(motionEvent.getX(), motionEvent.getY());

                float currentX = motionEvent.getX();
                float currentY = motionEvent.getY();
                float lastX = mPrevPoint.x;
                float lastY = mPrevPoint.y;

                if (mMode == Mode.NONE || mMode == Mode.DRAG) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
//                            lastPoint.set(currentPoint);

                            mStartPoint.set((currentX - lastX), (currentY - lastY));
                            mMode = Mode.DRAG;
                            break;

                        case MotionEvent.ACTION_MOVE:
                            if (mMode == Mode.DRAG) {
//                                float deltaX = currentPoint.x - lastPoint.x;
//                                float deltaY = currentPoint.y - lastPoint.y;
                                deltaX = currentX - mStartPoint.x;
                                deltaY = currentY - mStartPoint.y;
                                float fixTransX = getFixDragTrans(deltaX, getWidth(), getScaledWidth());
                                float fixTransY = getFixDragTrans(deltaY, getHeight(), getScaledHeight());
                                Log.e(LOG_TAG, "FIX TRANS: " + "X: " + fixTransX + " Y: " + fixTransY + " "+(getScaledWidth()-getWidth()));


                                move(fixTransX, fixTransY);
//                                ZoomMapRelativeLayout.this.move(deltaX, deltaY);
//                                fixTrans(fixTransX, fixTransY);
//                                lastPoint.set(currentPoint);
                            }
                            break;

                        case MotionEvent.ACTION_POINTER_UP:
                        case MotionEvent.ACTION_UP:
                            mMode = Mode.NONE;
                            mPrevPoint.set(deltaX, deltaY);
                            break;
                    }
                }
                return true;
            }
        });
    }

    private float mStartingSpan = 0f;
    private float mStartFocusX = 0f;
    private float mStartFocusY = 0f;

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        mStartingSpan = detector.getCurrentSpan();
        Log.e(LOG_TAG, "Start focusX - " + mStartFocusX + " focusY - " + mStartFocusY);
        mStartFocusX = detector.getFocusX();
        mStartFocusY = detector.getFocusY();
        Log.e(LOG_TAG, "Start focusX - " + mStartFocusX + " focusY - " + mStartFocusY);
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Log.d(LOG_TAG, "on scale");
        float currentSpan = detector.getCurrentSpan();
        float scaleFactor = currentSpan / mStartingSpan;
        if (mLastScaleFactor == 0 || (Math.signum(scaleFactor) == Math.signum(mLastScaleFactor))) {
            scaleFactor = (Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM)));
            mLastScaleFactor = scaleFactor;
        } else {
            mLastScaleFactor = 0f;
        }
        ZoomMapRelativeLayout.this.scale(scaleFactor, mStartFocusX, mStartFocusY);
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    private float getScaledWidth() {
        return getWidth() * mScaleFactor;
    }

    private float getScaledHeight() {
        return getHeight() * mScaleFactor;
    }

    private float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0f;
        }
        return delta;
    }

    private void fixTrans(float transX, float transY) {

        Log.e(LOG_TAG, "[Fix trans]: scale width - " + getScaledWidth() + ", scale height - " + getScaledHeight());
        Log.e(LOG_TAG, "[Fix trans]: width - " + getWidth() + ", height - " + getHeight());

        float fixTransX = getFixTrans(transX, getWidth(), getScaledWidth());
        float fixTransY = getFixTrans(transY, getHeight(), getScaledHeight());

        if (fixTransX != 0) {
            mDx = fixTransX;
        }

        if (fixTransY != 0) {
            mDy = fixTransY;
        }
        this.invalidate();
    }

    private float getFixTrans(float trans, float originalSize, float scaleSize) {
        float minTrans;
        float maxTrans;

        if (scaleSize <= originalSize) {
            minTrans = 0f;
            maxTrans = originalSize - scaleSize;
        } else {
            minTrans = originalSize - scaleSize;
            maxTrans = 0f;
        }

        if (trans < minTrans) {
            return -trans + minTrans;
        }

        if (trans > maxTrans) {
            return -trans + maxTrans;
        }
        return 0f;
    }

    //=========================================================================//

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Log.d(LOG_TAG, "[dispatch draw]: scaleFactor - " + mScaleFactor +
                ", pivotX - " + mPivotX + ", pivotY - " + mPivotY);

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(mScaleFactor, mScaleFactor, mPivotX, mPivotY);
        canvas.translate(mDx, mDy);

        super.dispatchDraw(canvas);
        canvas.restore();
    }

    public void move(float dx, float dy) {
        mDx = dx;
        mDy = dy;
        ZoomMapRelativeLayout.this.invalidate();
    }

    public void scale(float scaleFactor, float pivotX, float pivotY) {
        mScaleFactor = scaleFactor;
        mPivotX = pivotX;
        mPivotY = pivotY;
        ZoomMapRelativeLayout.this.invalidate();
    }

    public void restore() {
        mScaleFactor = 1f;
        ZoomMapRelativeLayout.this.invalidate();
    }
}
