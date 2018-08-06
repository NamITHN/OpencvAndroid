package omi.namnt.demoopencvandroid;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgTemp;
    private ImageView imgOut,imgBlur,imgGaussian,imgMedian;

    static {
        OpenCVLoader.initDebug();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnOpen = this.findViewById(R.id.btn_open);
        Button btnClose = this.findViewById(R.id.btn_save);
        Button btnTest = this.findViewById(R.id.btn_takeaphoto);

        ImageView imgThresshold = this.findViewById(R.id.img_thresshold);
        imgTemp = this.findViewById(R.id.img_temp);
        imgOut=this.findViewById(R.id.img_out);
        imgBlur=this.findViewById(R.id.img_blur);
        imgGaussian=this.findViewById(R.id.img_gaussian);
        imgMedian=this.findViewById(R.id.img_median);

        btnOpen.setOnClickListener(this);
        btnTest.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        imgThresshold.setOnClickListener(this);
        imgOut.setOnClickListener(this);
        imgBlur.setOnClickListener(this);
        imgMedian.setOnClickListener(this);
        imgGaussian.setOnClickListener(this);

        thresshold(imgOut);
         blur(imgBlur);
       // median(imgMedian);
        imgGaussian(imgGaussian);
    }

    private void imgGaussian(ImageView imgGaussian) {
        Drawable src = getResources().getDrawable(R.drawable.test);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) src;
        Bitmap srcImg = bitmapDrawable.getBitmap();
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcImg, srcMat);
        Mat desMat = new Mat();
        Imgproc.GaussianBlur(srcMat, desMat, new Size(45, 45),50);
        Bitmap desImg = Bitmap.createBitmap(srcImg);
        Utils.matToBitmap(desMat, desImg);
        imgGaussian.setImageBitmap(desImg);
    }

    private void median(ImageView imgMedian) {
        Drawable src = getResources().getDrawable(R.drawable.test);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) src;
        Bitmap srcImg = bitmapDrawable.getBitmap();
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcImg, srcMat);
        Mat desMat = new Mat();
        Imgproc.Scharr(srcMat, desMat, Imgproc.CV_SCHARR, 0, 1);

        Bitmap desImg = Bitmap.createBitmap(srcImg);
        Utils.matToBitmap(desMat, desImg);
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
            case R.id.img_thresshold:
                thresshold(imgTemp);
                break;
            case R.id.img_blur:
                blur(imgTemp);
                break;
            case R.id.img_out:
                thresshold(imgTemp);
                break;
            case R.id.img_gaussian:
                imgGaussian(imgTemp);
                break;
            case R.id.img_median:
                median(imgTemp);
            default:
                break;
        }
    }

    private void saveImage() {

    }

    private void takeAPhoto() {
    }

    private void thresshold(ImageView imgOut) {
        Drawable src = getResources().getDrawable(R.drawable.test);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) src;
        Bitmap image = bitmapDrawable.getBitmap();
        Mat inputMat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8U/*.CV_8UC1*/);
        Utils.bitmapToMat(image, inputMat);
        Imgproc.cvtColor(inputMat,inputMat,Imgproc.COLOR_RGB2GRAY);
        Mat inputMat2 = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8U);
        Mat outputMat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8U);
        Imgproc.adaptiveThreshold(inputMat, outputMat, 125, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 17, 2);
        // Photo.fastNlMeansDenoisingColored(inputMat, outputMat)
        Bitmap output = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(outputMat, output);
        imgOut.setImageBitmap(output);
    }

    private void setDefaultImage() {

        Drawable src = getResources().getDrawable(R.drawable.android);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) src;
        Bitmap srcImg = bitmapDrawable.getBitmap();

        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcImg, srcMat);
        Mat desMat = new Mat();

       String xmlFile="lbpcascade_frontalface.xml";
       //String s=getResources().getAssets().toString()+"/lbpcascade_frontalface.xml";
        Toast.makeText(this, ""+xmlFile, Toast.LENGTH_SHORT).show();
        CascadeClassifier classifier = new CascadeClassifier(xmlFile);
        MatOfRect faceDetections = new MatOfRect();
        classifier.detectMultiScale(srcMat, faceDetections);
        System.out.println(String.format("Detected %s faces",faceDetections.toArray().length));
        // Drawing boxes
        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(
                    srcMat,                                               // where to draw the box
                    new Point(rect.x, rect.y),                            // bottom left
                    new Point(rect.x + rect.width, rect.y + rect.height), // top right
                    new Scalar(0, 0, 255),
                    3                                                     // RGB colour
            );
        }
        Bitmap desImg = Bitmap.createBitmap(srcImg);
        Utils.matToBitmap(desMat, desImg);
        imgTemp.setImageBitmap(desImg);
    }

    private void blur(ImageView imgBlur) {
        Drawable src = getResources().getDrawable(R.drawable.test);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) src;
        Bitmap srcImg = bitmapDrawable.getBitmap();
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcImg, srcMat);
        Mat desMat = new Mat();
        Imgproc.blur(srcMat, desMat, new Size(51, 51),new Point(20,20));
        Bitmap desImg = Bitmap.createBitmap(srcImg);
        Utils.matToBitmap(desMat, desImg);
        imgBlur.setImageBitmap(desImg);
    }

    private void openImageFromSource() {
    }
}
