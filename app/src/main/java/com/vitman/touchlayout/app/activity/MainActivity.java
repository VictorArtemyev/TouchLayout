package com.vitman.touchlayout.app.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;
import com.vitman.touchlayout.app.R;
import com.vitman.touchlayout.app.view.TouchClubMapLayout;

public class MainActivity extends Activity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        float height = scaledMapBitmap.getHeight();
        float width = scaledMapBitmap.getWidth();

        Log.e(LOG_TAG, "Fit width size map: width - " + height + " height - " + width);

        TouchClubMapLayout layout = (TouchClubMapLayout) findViewById(R.id.club_map_layout);

        ImageView mMapImageView = (ImageView) layout.findViewById(R.id.club_map_holder_imageView);
        mMapImageView.setImageBitmap(scaledMapBitmap);
        layout.setMapHeight(height);
        layout.setMapWidth(width);
    }

    public static Bitmap scaleToFitWidth(Bitmap b, int width) {
        float factor = width / (float) b.getWidth();
        return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
    }
}
