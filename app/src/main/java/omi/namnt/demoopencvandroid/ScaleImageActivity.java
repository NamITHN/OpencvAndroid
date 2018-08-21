package omi.namnt.demoopencvandroid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

public class ScaleImageActivity extends AppCompatActivity implements View.OnTouchListener {

    ImageView imgScale;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale_image);
        imgScale = this.findViewById(R.id.img_zoom);


    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
     //   scaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

   /* class MyGesture extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        float mScaleFactor = 1.0F;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f,
                    Math.min(mScaleFactor, 10.0f));
            imgScale.setScaleX(mScaleFactor);
            imgScale.setScaleY(mScaleFactor);
            return true;
        }


    }*/
}
