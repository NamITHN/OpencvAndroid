package omi.namnt.demoopencvandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnTouchListener {

    private static int SELECT_GALLERY_IMAGE = 1;
    private ImageView imgTemp, imgInput;
    private ImageView imgOut, imgBlur, imgGaussian, imgMedian;
    ScaleGestureDetector scaleGestureDetector;
    int flipCode = 0;
    Bitmap srcImgRotate, srcImg;


    static {
        OpenCVLoader.initDebug();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scaleGestureDetector = new ScaleGestureDetector(this, new MyGesture());

        Button btnOpen = this.findViewById(R.id.btn_open);
        Button btnClose = this.findViewById(R.id.btn_save);
        Button btnCamera = this.findViewById(R.id.btn_takeaphoto);
        Button btnReset=this.findViewById(R.id.btn_reset);

        ImageView imgRotation = this.findViewById(R.id.img_rotation);
        imgTemp = this.findViewById(R.id.img_tempOut);
        imgInput = this.findViewById(R.id.img_temp);
        imgOut = this.findViewById(R.id.img_out);
        imgBlur = this.findViewById(R.id.img_blur);
        imgGaussian = this.findViewById(R.id.img_gaussian);
        imgMedian = this.findViewById(R.id.img_median);

        imgTemp.setOnTouchListener(this);
        btnOpen.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        imgRotation.setOnClickListener(this);
        imgOut.setOnClickListener(this);
        imgBlur.setOnClickListener(this);
        imgMedian.setOnClickListener(this);
        imgGaussian.setOnClickListener(this);



        Drawable src = getResources().getDrawable(R.drawable.test);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) src;
        srcImg = bitmapDrawable.getBitmap();
        srcImgRotate = bitmapDrawable.getBitmap();
        refresh();

    }

    private void refresh() {
        thresshold(imgOut,true);
        blur(imgBlur,true);
        median(imgMedian,true);
        imgGaussian(imgGaussian,true);
    }

    private void imgGaussian(ImageView imgGaussian,Boolean isRefresh) {
        /*Drawable src = getResources().getDrawable(R.drawable.test);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) src;
        Bitmap srcImg = bitmapDrawable.getBitmap();*/

        Bitmap srcImgCopy = Bitmap.createBitmap(srcImg);
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcImgCopy, srcMat);
        Mat desMat = new Mat();
        Imgproc.GaussianBlur(srcMat, desMat, new Size(45, 45), 50);
        Bitmap desImg = Bitmap.createBitmap(srcImgCopy.getWidth(), srcImgCopy.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(desMat, desImg);
        if(!isRefresh){
            srcImgRotate = Bitmap.createBitmap(desImg);
        }
        imgGaussian.setImageBitmap(desImg);
    }

    private void median(ImageView imgMedian,Boolean isRefresh) {
       /* Drawable src = getResources().getDrawable(R.drawable.test);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) src;
        Bitmap srcImg = bitmapDrawable.getBitmap();*/

        Bitmap srcImgCopy = Bitmap.createBitmap(srcImg);
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcImgCopy, srcMat);
        Mat desMat = new Mat();
        Imgproc.Scharr(srcMat, desMat, Imgproc.CV_SCHARR, 0, 1);

        Bitmap desImg = Bitmap.createBitmap(srcImgCopy.getWidth(), srcImgCopy.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(desMat, desImg);
        if(!isRefresh){
            srcImgRotate = Bitmap.createBitmap(desImg);
        }

        imgMedian.setImageBitmap(desImg);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_open:
                openImageFromSource();
                break;
            case R.id.btn_takeaphoto:
                takeAPhoto();
                break;
            case R.id.btn_save:
                saveImage();
                break;
            case R.id.img_rotation:
                rotation(imgTemp);
                break;
            case R.id.img_blur:
                blur(imgTemp, false);
                break;
            case R.id.img_out:
                thresshold(imgTemp, false);
                break;
            case R.id.img_gaussian:
                imgGaussian(imgTemp,false);
                break;
            case R.id.img_median:
                median(imgTemp,false);
                break;
            case R.id.btn_reset:
                resetImage();
                break;
            default:
                break;
        }
    }

    private void resetImage() {
        srcImgRotate = Bitmap.createBitmap(srcImg);
        imgTemp.setImageBitmap(srcImg);
    }

    private void rotation(ImageView imgTemp) {
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcImgRotate, srcMat);
        Mat dst = new Mat();
        Core.flip(srcMat, dst, flipCode);
        if (flipCode == -1) {
            flipCode = 0;
        } else if (flipCode == 0) {
            flipCode = 1;
        } else {
            flipCode = -1;
        }
        Bitmap desImg = Bitmap.createBitmap(srcImgRotate.getWidth(), srcImgRotate.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(dst, desImg);
        imgTemp.setImageBitmap(desImg);
    }

    private void saveImage() {

    }

    private void takeAPhoto() {
    }

    private void thresshold(ImageView imgOut, boolean isRefresh) {
//        Drawable src = getResources().getDrawable(R.drawable.test);
//        BitmapDrawable bitmapDrawable = (BitmapDrawable) src;
//        Bitmap srcImg = bitmapDrawable.getBitmap();

        Bitmap image = Bitmap.createBitmap(srcImg);
        Mat inputMat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8U/*.CV_8UC1*/);
        Utils.bitmapToMat(image, inputMat);
        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_RGB2GRAY);
        Mat outputMat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8U);
        Imgproc.adaptiveThreshold(inputMat, outputMat, 125, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 17, 2);
        Bitmap output = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(outputMat, output);
        if(!isRefresh){
            srcImgRotate = Bitmap.createBitmap(output);
        }
        imgOut.setImageBitmap(output);
    }


    private void blur(ImageView imgBlur, boolean isRefresh) {
       /* Drawable src = getResources().getDrawable(R.drawable.test);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) src;
        Bitmap srcImg = bitmapDrawable.getBitmap();*/

        Bitmap srcImgCopy = Bitmap.createBitmap(srcImg);
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcImgCopy, srcMat);
        Mat desMat = new Mat();
        Imgproc.blur(srcMat, desMat, new Size(51, 51), new Point(20, 20));
        Bitmap desImg = Bitmap.createBitmap(srcImgCopy.getWidth(), srcImgCopy.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(desMat, desImg);
        if(!isRefresh){
            srcImgRotate = Bitmap.createBitmap(desImg);
        }
        imgBlur.setImageBitmap(desImg);
    }

    private void openImageFromSource() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, SELECT_GALLERY_IMAGE);
                        } else {
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        scaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == SELECT_GALLERY_IMAGE) {
            Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData(), 300, 300);
            imgInput.setImageBitmap(bitmap);
            imgTemp.setImageBitmap(bitmap);
            srcImgRotate = Bitmap.createBitmap(bitmap);
            srcImg = bitmap;
            refresh();
        }
    }

    class MyGesture extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        float mScaleFactor = 1.0F;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f,
                    Math.min(mScaleFactor, 10.0f));
            imgTemp.setScaleX(mScaleFactor);
            imgTemp.setScaleY(mScaleFactor);
            return true;
        }


    }

}

