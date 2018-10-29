package omi.namnt.demoopencvandroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnTouchListener {

    private static int SELECT_GALLERY_IMAGE = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    private ImageView imgTemp, imgInput;
    private ImageView imgOut, imgBinary, imgGaussian, imgMedian, imgGray,imgFlip,imgMedianblur,imgThressHold,imgText;
    ScaleGestureDetector scaleGestureDetector;
    int flipCode = 1;
    int orientation=0;
    double x,y;
    int size=3;
    boolean isText=false;
    ArrayList<Point> points=new ArrayList<>();
    Bitmap srcImgRotate, srcImg;
    CoordinatorLayout coordinatorLayout;


    static {
        OpenCVLoader.initDebug();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOpen = this.findViewById(R.id.btn_open);
        Button btnClose = this.findViewById(R.id.btn_save);
        Button btnCamera = this.findViewById(R.id.btn_takeaphoto);
        Button btnReset = this.findViewById(R.id.btn_reset);

        ImageView imgRotation = this.findViewById(R.id.img_rotation);
        imgMedianblur=this.findViewById(R.id.img_medianblur);
        imgTemp = this.findViewById(R.id.img_tempOut);
        imgInput = this.findViewById(R.id.img_temp);
        imgOut = this.findViewById(R.id.img_out);
        imgGaussian = this.findViewById(R.id.img_gaussian);
        imgMedian = this.findViewById(R.id.img_median);
        coordinatorLayout = this.findViewById(R.id.coordinator);
        imgGray = this.findViewById(R.id.img_gray);
        imgBinary = this.findViewById(R.id.img_binary);
        imgFlip=this.findViewById(R.id.img_flip);
        imgThressHold=this.findViewById(R.id.img_threshold);
        imgText=this.findViewById(R.id.img_text);



        btnOpen.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        imgRotation.setOnClickListener(this);
        imgOut.setOnClickListener(this);
        imgBinary.setOnClickListener(this);
        imgMedian.setOnClickListener(this);
        imgGaussian.setOnClickListener(this);
        imgGray.setOnClickListener(this);
        imgFlip.setOnClickListener(this);
        imgMedianblur.setOnClickListener(this);
        imgThressHold.setOnClickListener(this);
        imgTemp.setOnTouchListener(this);
        imgText.setOnClickListener(this);


        scaleGestureDetector = new ScaleGestureDetector(this, new MyGesture());
        Drawable src = getResources().getDrawable(R.drawable.test);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) src;
        srcImg = bitmapDrawable.getBitmap();
        srcImgRotate = bitmapDrawable.getBitmap();
        refresh();

    }

    private void refresh() {
        edgeDetection1(imgOut, true);
        edgeDetection2(imgMedian, true);
        threshold(imgThressHold,true);
        gaussianBlur(imgGaussian, true);
        medianBlur(imgMedianblur, true);
        colorGray(imgGray, true);
        colorBinary(imgBinary, true);
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
            case R.id.btn_reset:
                resetImage();
                break;
            //Chuyển đổi ảnh màu
            case R.id.img_binary:
                colorBinary(imgTemp, false);
                break;
            case R.id.img_gray:
                colorGray(imgTemp, false);
                break;
             // Tách biên
            case R.id.img_out:
                edgeDetection1(imgTemp, false);
                break;
            case R.id.img_median:
                edgeDetection2(imgTemp, false);
                break;
                //Làm mờ
            case R.id.img_gaussian:
                gaussianBlur(imgTemp, false);
                break;

            case R.id.img_medianblur:
                medianBlur(imgTemp, false);
                break;
                //Nhị phân hóa ảnh
            case R.id.img_threshold:
                threshold(imgTemp, false);
                break;
                //xoay ảnh
            case R.id.img_rotation:
                rotation(imgTemp);
                break;

                //lật ảnh
            case R.id.img_flip:
                flip(imgTemp);
                break;
                //chèn text
            case R.id.img_text:
                isText=!isText;
                if(isText){
                    imgText.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_text_black));
                }else {
                    imgText.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_text));
                }


                break;

            default:
                break;
        }
    }



    private void putTextDialog(final Point point) {
         final LovelyTextInputDialog lovelyTextInputDialog=   new LovelyTextInputDialog(this);
        lovelyTextInputDialog.setTopColorRes(R.color.colorAccent)
                .setTitle("Mời bạn nhập vào nội dung")
                .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                    @Override
                    public void onTextInputConfirmed(String text) {
                        putText(text,point);
                        lovelyTextInputDialog.dismiss();
                    }
                })
                .show();
    }

    private void colorBinary(ImageView imgTemp, boolean isRefresh) {
        Bitmap srcImgCopy = Bitmap.createBitmap(srcImg);
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcImgCopy, srcMat);
        Mat desMat = new Mat();
        Imgproc.cvtColor(srcMat, desMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(srcMat, desMat, 100, 255, Imgproc.THRESH_BINARY);
        Bitmap desImg = Bitmap.createBitmap(srcImgCopy.getWidth(), srcImgCopy.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(desMat, desImg);
        if (!isRefresh) {
            srcImgRotate = Bitmap.createBitmap(desImg);
        }
        imgTemp.setImageBitmap(desImg);

    }

    private void colorGray(ImageView imgTemp, boolean isRefresh) {
        Bitmap srcImgCopy = Bitmap.createBitmap(srcImg);
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcImgCopy, srcMat);
        Mat desMat = new Mat();
        Imgproc.cvtColor(srcMat, desMat, Imgproc.COLOR_RGB2GRAY);
        Bitmap desImg = Bitmap.createBitmap(srcImgCopy.getWidth(), srcImgCopy.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(desMat, desImg);
        if (!isRefresh) {
            srcImgRotate = Bitmap.createBitmap(desImg);
        }
        imgTemp.setImageBitmap(desImg);
    }
    private void edgeDetection1(ImageView imgOut, boolean isRefresh) {
        Bitmap image = Bitmap.createBitmap(srcImg);
        Mat inputMat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8U/*.CV_8UC1*/);
        Utils.bitmapToMat(image, inputMat);
        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_RGB2GRAY);
        Mat outputMat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8U);
        Imgproc.Canny(inputMat, outputMat, 60, 60*3);
        Bitmap output = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(outputMat, output);
        if (!isRefresh) {
            srcImgRotate = Bitmap.createBitmap(output);
        }
        imgOut.setImageBitmap(output);
    }
    private void edgeDetection2(ImageView imgMedian, Boolean isRefresh) {

        Bitmap srcImgCopy = Bitmap.createBitmap(srcImg);
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcImgCopy, srcMat);
        Mat desMat = new Mat();
        Imgproc.Scharr(srcMat, desMat, Imgproc.CV_SCHARR, 0, 1);
        Bitmap desImg = Bitmap.createBitmap(srcImgCopy.getWidth(), srcImgCopy.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(desMat, desImg);
        if (!isRefresh) {
            srcImgRotate = Bitmap.createBitmap(desImg);
        }

        imgMedian.setImageBitmap(desImg);
    }
    private void threshold(ImageView imgThressHold, boolean isRefresh) {
        Bitmap image = Bitmap.createBitmap(srcImg);
        Mat inputMat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8U/*.CV_8UC1*/);
        Utils.bitmapToMat(image, inputMat);
        Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_RGB2GRAY);
        Mat outputMat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8U);
        Imgproc.adaptiveThreshold(inputMat, outputMat, 125, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 17, 2);
        Bitmap output = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(outputMat, output);
        if (!isRefresh) {
            srcImgRotate = Bitmap.createBitmap(output);
        }
        imgThressHold.setImageBitmap(output);
    }

    private void gaussianBlur(ImageView imgGaussian, Boolean isRefresh) {
        Bitmap srcImgCopy = Bitmap.createBitmap(srcImg);
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcImgCopy, srcMat);
        Mat desMat = new Mat();
        Imgproc.GaussianBlur(srcMat, desMat, new Size(45, 45), 50);
        Bitmap desImg = Bitmap.createBitmap(srcImgCopy.getWidth(), srcImgCopy.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(desMat, desImg);
        if (!isRefresh) {
            srcImgRotate = Bitmap.createBitmap(desImg);
        }
        imgGaussian.setImageBitmap(desImg);
    }
    private void medianBlur(ImageView imgTemp, boolean isRefresh) {
        Bitmap srcImgCopy = Bitmap.createBitmap(srcImg);
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcImgCopy, srcMat);
        Mat desMat = new Mat();
        Imgproc.medianBlur(srcMat, desMat, 15);
        Bitmap desImg = Bitmap.createBitmap(srcImgCopy.getWidth(), srcImgCopy.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(desMat, desImg);
        if (!isRefresh) {
            srcImgRotate = Bitmap.createBitmap(desImg);
        }
        imgTemp.setImageBitmap(desImg);
    }

    private void flip(ImageView imgTemp) {
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcImgRotate, srcMat);
        Mat dst = new Mat();
        Core.flip(srcMat, dst, flipCode);
        if (flipCode == -1) {
            flipCode = 0;
        } else if (flipCode == 0) {
            flipCode = -1;
        } else if(flipCode==1) {
            flipCode=0;
           resetImage();
        }

        Bitmap desImg = Bitmap.createBitmap(srcImgRotate.getWidth(), srcImgRotate.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(dst, desImg);
        imgTemp.setImageBitmap(desImg);
    }

    private void rotation(ImageView imgTemp) {
        Matrix matrix=new Matrix();
        switch (orientation){
            case 0:
                orientation=90;
                matrix.setRotate(orientation);
                break;
            case 90:
                orientation=180;
                matrix.setRotate(orientation);
                break;
            case 180:
                orientation=270;
                matrix.setRotate(orientation);
                break;
            case 270:
                orientation=360;
                matrix.setRotate(orientation);
                break;
            case 360:
                orientation=0;
                matrix.setRotate(orientation);
                break;
        }
        Bitmap srcImgCopy = Bitmap.createBitmap(srcImg);
        Bitmap rotateBitmap=Bitmap.createBitmap(srcImgCopy,0,0,srcImgCopy.getWidth(),srcImgCopy.getHeight(),matrix,true);
        imgTemp.setImageBitmap(rotateBitmap);
    }

    private void putText(String mesage,Point point){

        Bitmap srcImgCopy = Bitmap.createBitmap(srcImg);
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcImgCopy, srcMat);
        Imgproc.putText (srcMat,mesage, point, Core.FONT_HERSHEY_SIMPLEX , 1, new Scalar(0, 0, 0), 4);
        Bitmap desImg = Bitmap.createBitmap(srcImgCopy.getWidth(), srcImgCopy.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(srcMat, desImg);
        imgTemp.setImageBitmap(desImg);

    }


    private void saveImage() {

        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final String path = BitmapUtils.insertImage(getContentResolver(), ((BitmapDrawable) imgTemp.getDrawable()).getBitmap(), System.currentTimeMillis() + "_profile.jpg", null);
                            if (!TextUtils.isEmpty(path)) {
                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Image saved to gallery!", Snackbar.LENGTH_LONG)
                                        .setAction("OPEN", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                openImage(path);
                                            }
                                        });

                                snackbar.show();
                            } else {
                                Snackbar snackbar = Snackbar
                                        .make(coordinatorLayout, "Unable to save image!", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
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

    private void takeAPhoto() {

        Dexter.withActivity(this).withPermissions(Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
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
    public boolean onTouch(View view, final MotionEvent motionEvent) {
      //  Toast.makeText(this, ""+(int)motionEvent.getX()+","+(int)motionEvent.getY(), Toast.LENGTH_SHORT).show();
        x = motionEvent.getX();
        y = motionEvent.getY();
        if(isText){
            points.add(new Point(x,y));
            if(points.size()>=size){
                putTextDialog(points.get(0));
                points =new ArrayList<>();
            }
        }

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
        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imgInput.setImageBitmap(imageBitmap);
            imgTemp.setImageBitmap(imageBitmap);
            srcImgRotate = Bitmap.createBitmap(imageBitmap);
            srcImg = imageBitmap;
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

    private void openImage(String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path), "image/*");
        startActivity(intent);
    }
    private void resetImage() {
        srcImgRotate = Bitmap.createBitmap(srcImg);
        imgTemp.setImageBitmap(srcImg);
    }
}

