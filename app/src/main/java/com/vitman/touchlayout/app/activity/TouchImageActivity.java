package com.vitman.touchlayout.app.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import com.vitman.touchlayout.app.R;
import com.vitman.touchlayout.app.view.TouchImageView;

/**
 * Created by Victor Artemjev on 08.05.2015.
 */
public class TouchImageActivity extends Activity {

    private static final String LOG_TAG = TouchImageActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_image);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayWidth = size.x;
        int displayHeight = size.y;

        // original map size
        Bitmap originalMapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map_qub_office, options);
        final float originalMapWidth = originalMapBitmap.getWidth();
        final float originalMapHeight = originalMapBitmap.getHeight();
        Log.e(LOG_TAG, "Original size map: width - " + originalMapWidth + " height - " + originalMapHeight);


        Bitmap scaledMapBitmap = scaleToFitWidth(originalMapBitmap, displayWidth);
        final float fitMapWidth = scaledMapBitmap.getWidth();
        final float fitMapHeight = scaledMapBitmap.getHeight();
        Log.e(LOG_TAG, "Fit width size map: width - " + fitMapWidth + " height - " + fitMapHeight);

        TouchImageView mMapImageView = (TouchImageView) findViewById(R.id.image);
        mMapImageView.setImageBitmap(scaledMapBitmap);
    }

    public static Bitmap scaleToFitWidth(Bitmap b, int width) {
        float factor = width / (float) b.getWidth();
        return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
    }
}
