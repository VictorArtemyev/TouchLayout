package com.vitman.touchlayout.app.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.vitman.touchlayout.app.R;
import com.vitman.touchlayout.app.view.TouchRelativeLayout;

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

        // Pin
        ImageView pinView = new ImageView(MainActivity.this);
        pinView.setScaleType(ImageView.ScaleType.FIT_XY);
        pinView.setImageResource(R.drawable.ic_pin);
        // Layout params for pin
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(
//                        RelativeLayout.LayoutParams.WRAP_CONTENT,
//                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                        80, 80);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//        layoutParams.leftMargin = 100;
//        layoutParams.topMargin = 200;

        pinView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "CLICK!", Toast.LENGTH_SHORT).show();
            }
        });

        TouchRelativeLayout layout = (TouchRelativeLayout) findViewById(R.id.club_map_layout);
        layout.setMapHeight(height);
        layout.setMapWidth(width);

        RelativeLayout holder = (RelativeLayout) findViewById(R.id.holder);
        ImageView mMapImageView = (ImageView) holder.findViewById(R.id.club_map_holder_imageView);
        mMapImageView.setImageBitmap(scaledMapBitmap);
        holder.addView(pinView, layoutParams);
    }

    public static Bitmap scaleToFitWidth(Bitmap b, int width) {
        float factor = width / (float) b.getWidth();
        return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
    }
}
