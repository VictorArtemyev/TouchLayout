package com.vitman.touchlayout.app.layout;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by Victor Artemjev on 07.05.2015.
 */
public class TouchRelativeLayout extends RelativeLayout {

    private static final String LOG_TAG = TouchRelativeLayout.class.getSimpleName();

    // Instance state
    private static final String BUNDLE_INSTANCE_STATE = "instanceState";
    private static final String BUNDLE_MAP_WIDTH = "mapWidth";
    private static final String BUNDLE_MAP_HEIGHT = "mapHeight";
    private static final String BUNDLE_SCALE_FACTOR = "scaleFactor";
    private static final String BUNDLE_POSITION_X = "positionX";
    private static final String BUNDLE_POSITION_Y = "positionY";

    private static final int INVALID_POINTER_ID = -1;
    private static final float MIN_ZOOM = 1f;
    private static final float MAX_ZOOM = 2f;
    private static final String BUNDLE_SCREEN_ORIENTATION = "screenOrientation";

    private float mMapWidth;
    private float mMapHeight;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    private float mFocusX;
    private float mFocusY;

    private int mActivePointerId = INVALID_POINTER_ID;

    private float mPosX;
    private float mPosY;

    private float mLastTouchX;
    private float mLastTouchY;

    private int mScreenOrientation;

    private Context mContext;

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

//    @Override
//    protected Parcelable onSaveInstanceState() {
//        Bundle bundle = new Bundle();
//        bundle.putParcelable(BUNDLE_INSTANCE_STATE, super.onSaveInstanceState());
//        bundle.putFloat(BUNDLE_MAP_WIDTH, mMapWidth);
//        bundle.putFloat(BUNDLE_MAP_HEIGHT, mMapHeight);
//        bundle.putFloat(BUNDLE_POSITION_X, mPosX);
//        bundle.putFloat(BUNDLE_POSITION_Y, mPosY);
//        bundle.putFloat(BUNDLE_SCALE_FACTOR, mScaleFactor);
//        bundle.putInt(BUNDLE_SCREEN_ORIENTATION, mScreenOrientation);
//        return bundle;
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Parcelable state) {
//        if (state instanceof Bundle) {
//            Bundle bundle = (Bundle) state;
//            mMapWidth = bundle.getFloat(BUNDLE_MAP_WIDTH);
//            mMapHeight = bundle.getFloat(BUNDLE_MAP_HEIGHT);
//            mScaleFactor = bundle.getFloat(BUNDLE_SCALE_FACTOR);
//            mPosX = bundle.getFloat(BUNDLE_POSITION_X);
//            mPosY = bundle.getFloat(BUNDLE_POSITION_Y);
//            mScreenOrientation = bundle.getInt(BUNDLE_SCREEN_ORIENTATION);
//            super.onRestoreInstanceState(bundle.getParcelable(BUNDLE_INSTANCE_STATE));
//            return;
//        }
//        super.onRestoreInstanceState(state);
//    }

    private void init(Context context) {
        mContext = context;
        mScreenOrientation = getScreenOrientation();
//        fixInstantStateByScreenOrientation();
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
                            mPosX += getFixDragTrans(dx, getWidth(), getScaledWidth());
                            mPosY += getFixDragTrans(dy, getHeight(), getScaledHeight());

                            fixTransByScreenOrientation();

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

//    private void fixInstantStateByScreenOrientation() {
//        if (mScreenOrientation == getScreenOrientation()) {
//            return;
//        }
//        mScreenOrientation = getScreenOrientation();
//        mPosX = 0f;
//        mPosY = 0f;
//        mScaleFactor = MIN_ZOOM;
//    }

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

    private float getScaledEmptyRegionByMapWidth() {
        return mMapWidth * mScaleFactor;
    }

    private float getScaledEmptySpaceByMapHeight() {
        return mMapHeight * mScaleFactor;
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

            //TODO refactor
            // X
            float sizeX = getWidth() * (newScaleFactor - mScaleFactor) / 2;
            float rankFocusX = mFocusX / (float) getWidth();
            mPosX -= (sizeX * 2 * rankFocusX) - sizeX;

            // Y
            float sizeY = getHeight() * (newScaleFactor - mScaleFactor) / 2;
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
//        Rect bounds = canvas.getClipBounds();
//        int centerX = bounds.centerX();
//        int centerY = bounds.centerY();
//        canvas.save(Canvas.MATRIX_SAVE_FLAG);
//        canvas.scale(mScaleFactor, mScaleFactor, centerX, centerY);
//        canvas.translate(mPosX / mScaleFactor, mPosY / mScaleFactor);
        applyScaleAndTranslation();
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    private View child() {
        return getChildAt(0);
    }

    private void applyScaleAndTranslation() {
        child().setScaleX(mScaleFactor);
        child().setScaleY(mScaleFactor);
//        child().setPivotX(getWidth() / 2);
//        child().setPivotY(getHeight() / 2);
//        child().setTranslationX(mPosX / mScaleFactor);
//        child().setTranslationY(mPosY / mScaleFactor);
        child().setTranslationX(mPosX);
        child().setTranslationY(mPosY);
    }

    public void setMapWidth(float width) {
        mMapWidth = width;
    }

    public void setMapHeight(float height) {
        mMapHeight = height;
    }

    private void fixTransByScreenOrientation() {
        if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            fixTransByLandscape();
        } else if (getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            fixTransByPortrait();
        }
    }

    private int getScreenOrientation() {
        return mContext.getResources().getConfiguration().orientation;
    }

    // TODO refactor
    private void fixTransByLandscape() {
        // X
        float emptySpaceWidth = getScaledEmptyRegionByMapWidth();
        if (emptySpaceWidth > getWidth()) {
            float shift = (getScaledWidth() - getWidth()) / 2 - (getWidth() - mMapWidth) / 2 * mScaleFactor;
            if (mPosX > shift) {
                mPosX = shift;
            } else if (mPosX < -shift) {
                mPosX = -shift;
            } else {
                mPosX = 0;
            }
        }

        // Y
        if (mPosY > (getScaledHeight() - getHeight()) / 2) {
            mPosY = (getScaledHeight() - getHeight()) / 2;
        } else if (mPosY < (getScaledHeight() - getHeight()) / 2 - getScaledHeight() + getHeight()) {
            mPosY = (getScaledHeight() - getHeight()) / 2 - getScaledHeight() + getHeight();
        }

    }

    private void fixTransByPortrait() {
        // Y
        float emptySpaceHeight = getScaledEmptySpaceByMapHeight();
        if (emptySpaceHeight > getHeight()) {
            float shift = (getScaledHeight() - getHeight()) / 2 - (getHeight() - mMapHeight) / 2 * mScaleFactor;
            if (mPosY > shift)
                mPosY = shift;
            else if (mPosY < -shift)
                mPosY = -shift;
        } else {
            mPosY = 0;
        }

        // X
        if (mPosX > (getScaledWidth() - getWidth()) / 2) {
            mPosX = (getScaledWidth() - getWidth()) / 2;
        } else if (mPosX < (getScaledWidth() - getWidth()) / 2 - getScaledWidth() + getWidth()) {
            mPosX = (getScaledWidth() - getWidth()) / 2 - getScaledWidth() + getWidth();
        }
    }
}
