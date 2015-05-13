package com.vitman.touchlayout.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;
import com.vitman.touchlayout.app.activity.MainActivity;

/**
 * Created by Victor Artemjev on 07.05.2015.
 */
public class TouchRelativeLayout extends RelativeLayout {

    private static final String LOG_TAG = TouchRelativeLayout.class.getSimpleName();
    private static final int INVALID_POINTER_ID = -1;
    private static final float MIN_ZOOM = 1f;
    private static final float MAX_ZOOM = 2f;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    private float mFocusX;
    private float mFocusY;

    private int mActivePointerId = INVALID_POINTER_ID;

    private float mPosX;
    private float mPosY;

    private float mLastTouchX;
    private float mLastTouchY;

    public TouchRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    public TouchRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TouchRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
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
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        TouchRelativeLayout.this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mScaleDetector.onTouchEvent(motionEvent);

                final int action = motionEvent.getAction();
                switch (action & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN: {
                        final float x = motionEvent.getX();
                        final float y = motionEvent.getY();

                        mLastTouchX = x;
                        mLastTouchY = y;
                        mActivePointerId = motionEvent.getPointerId(0);
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        final int pointerIndex = motionEvent.findPointerIndex(mActivePointerId);
                        final float x = motionEvent.getX(pointerIndex);
                        final float y = motionEvent.getY(pointerIndex);

                        if (!mScaleDetector.isInProgress()) {
                            final float dx = x - mLastTouchX;
                            final float dy = y - mLastTouchY;

//                            mPosX += dx;
                            mPosX += getFixDragTrans(dx, getWidth(), getScaledWidth());
//                            mPosY += dy;
                            mPosY += getFixDragTrans(dy, getHeight(), getScaledHeight());

                             // X
                            if (mPosX > (getScaledWidth() - getWidth()) / 2) {
                                mPosX = (getScaledWidth() - getWidth()) / 2;
                            } else if (mPosX < (getScaledWidth() - getWidth()) / 2 - getScaledWidth() + getWidth()) {
                                mPosX = (getScaledWidth() - getWidth()) / 2 - getScaledWidth() + getWidth();
                            }

                            // Y
                            if (mPosY > (getScaledHeight() - getHeight()) / 2) {
                                mPosY = (getScaledHeight() - getHeight()) / 2;
                            } else if (mPosY < (getScaledHeight() - getHeight()) / 2 - getScaledHeight() + getHeight()) {
                                mPosY = (getScaledHeight() - getHeight()) / 2 - getScaledHeight() + getHeight();
                            }
                            TouchRelativeLayout.this.invalidate();
                        }

                        mLastTouchX = x;
                        mLastTouchY = y;
                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        mActivePointerId = INVALID_POINTER_ID;
                        break;
                    }

                    case MotionEvent.ACTION_CANCEL: {
                        mActivePointerId = INVALID_POINTER_ID;
                        break;
                    }

                    case MotionEvent.ACTION_POINTER_UP: {
                        final int pointerIndex = (motionEvent.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                        final int pointerId = motionEvent.getPointerId(pointerIndex);
                        if (pointerId == mActivePointerId) {
                            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                            mLastTouchX = motionEvent.getX(newPointerIndex);
                            mLastTouchY = motionEvent.getY(newPointerIndex);
                            mActivePointerId = motionEvent.getPointerId(newPointerIndex);
                        }
                        break;
                    }
                }
                return true;
            }
        });
    }

    private float getScaledWidth() {
        return getWidth() * mScaleFactor;
    }

    private float getScaledHeight() {
        return getHeight() * mScaleFactor;
    }

    private float getFixDragTrans(float delta, float originalSize, float scaleSize) {
        if (scaleSize <= originalSize) {
            return 0f;
        }
        return delta;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mFocusX = detector.getFocusX();
            mFocusY = detector.getFocusY();
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float newScaleFactor = mScaleFactor * detector.getScaleFactor();

             // X
            float sizeX = getWidth() * (newScaleFactor - mScaleFactor) / 2;
            float rankFocusX = mFocusX / (float) getWidth();
            mPosX -= (sizeX * 2 * rankFocusX) - sizeX;

            // Y
            Log.e(LOG_TAG, "original height - " + MainActivity.fitMapHeight);
            Log.e(LOG_TAG, "layout height - " + getHeight());
            Log.e(LOG_TAG, "" + (getHeight() - MainActivity.fitMapHeight));
            float size = (getHeight() - MainActivity.fitMapHeight) / 2;

            float scaledSize = size * mScaleFactor;
            Log.e(LOG_TAG, "scaledSize - " + scaledSize);

            float sizeY;
            if (mPosY < 0) {
                sizeY = (getHeight() * (newScaleFactor - mScaleFactor) / 2);
            } else {
                sizeY = (getHeight() * (newScaleFactor - mScaleFactor) / 2);
            }

            float rankFocusY = mFocusY / (float) getHeight();
            mPosY -= (sizeY * 2 * rankFocusY) - sizeY;

            mScaleFactor = newScaleFactor;
            mScaleFactor = Math.max(MIN_ZOOM, Math.min(mScaleFactor, MAX_ZOOM));

            TouchRelativeLayout.this.invalidate();

            return true;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Rect bounds = canvas.getClipBounds();
        int centerX = bounds.centerX();
        int centerY = bounds.centerY();
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(mScaleFactor, mScaleFactor, centerX, centerY);
        Log.e(LOG_TAG, "Y - " + mPosY);
        canvas.translate(mPosX / mScaleFactor, mPosY / mScaleFactor);

        super.dispatchDraw(canvas);
        canvas.restore();
    }
}
