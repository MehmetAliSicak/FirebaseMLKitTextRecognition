package com.mas.textrecognition;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.text.FirebaseVisionCloudDocumentTextDetector;
import com.google.firebase.ml.vision.cloud.text.FirebaseVisionCloudText;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraKitEventListener, View.OnClickListener {

    CameraView mCameraView;
    Button mCameraButton, mCameraButtonCloud;
    GraphicOverlay mGraphicOverlay;
    Bitmap mBitmap;
    boolean mDevice = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getControls();
        setEventListener();
    }

    /*
     * Access to controls is provided
     */
    private void getControls() {
        mCameraView = findViewById(R.id.camView);
        mCameraButton = findViewById(R.id.cameraBtn);
        mCameraButtonCloud = findViewById(R.id.cameraBtnCloud);
        mGraphicOverlay = findViewById(R.id.graphic_overlay);
    }

    /*
     * Event listener for controls is defined
     */
    private void setEventListener() {
        mCameraView.addCameraKitListener(this);
        mCameraButton.setOnClickListener(this);
        mCameraButtonCloud.setOnClickListener(this);
    }

    /*
     * Buttons click event
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //if cameraBtn is clicked
            case R.id.cameraBtn:
                mDevice = true;
                break;
            //if cameraBtnCloud is clicked
            case R.id.cameraBtnCloud:
                mDevice = false;
                break;

        }

        /*
        * picture taken from camera
        */
        mGraphicOverlay.clear();
        mCameraView.start();
        mCameraView.captureImage();

    }

    @Override
    public void onEvent(CameraKitEvent cameraKitEvent) {

    }

    @Override
    public void onError(CameraKitError cameraKitError) {

    }

    @Override
    public void onImage(CameraKitImage cameraKitImage) {
        /*
        * The image from the camera is taken as a bitmap
        */
        mBitmap = cameraKitImage.getBitmap();
        mBitmap = Bitmap.createScaledBitmap(mBitmap, mCameraView.getWidth(), mCameraView.getHeight(), false);
        mCameraView.stop();
        if (mDevice)
            //Make text recognition on device
            runTextRecognition(mBitmap);
        else
            //Make text recognition on cloud
            runCloudTextRecognition(mBitmap);

    }

    @Override
    public void onVideo(CameraKitVideo cameraKitVideo) {

    }

    /*
    * Make text recognition on device and text recognition detector.
    * if the image contains text, processTextRecognitionResult is call
    */
    private void runTextRecognition(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();

        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText texts) {
                processTextRecognitionResult(texts);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                e.printStackTrace();
            }
        });
    }

    /*
    * processTextRecognitionResult allows you to parse existing text  and display them in your app.
    * */
    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.Block> blocks = texts.getBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(getApplicationContext(), "onDevice: No text found", Toast.LENGTH_SHORT).show();
            return;
        }
        mGraphicOverlay.clear();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                    mGraphicOverlay.add(textGraphic);

                }
            }
        }
    }

    /*
    * Make text recognition on cloud. This method takes some time. if the image contains text, processCloudTextRecognitionResult is call
    */
    private void runCloudTextRecognition(Bitmap bitmap) {
        FirebaseVisionCloudDetectorOptions options =
                new FirebaseVisionCloudDetectorOptions.Builder()
                        .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                        .setMaxResults(15)
                        .build();
        mCameraButtonCloud.setEnabled(false);
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionCloudDocumentTextDetector detector = FirebaseVision.getInstance()
                .getVisionCloudDocumentTextDetector(options);
        detector.detectInImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionCloudText>() {
                            @Override
                            public void onSuccess(FirebaseVisionCloudText texts) {
                                mCameraButtonCloud.setEnabled(true);
                                processCloudTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                mCameraButtonCloud.setEnabled(true);
                                e.printStackTrace();
                            }
                        });
    }

    /*
    * processCloudTextRecognitionResult allows you to parse existing text  and display them in your app.
    * */
    private void processCloudTextRecognitionResult(FirebaseVisionCloudText text) {
        // Task completed successfully
        if (text == null) {
            Toast.makeText(getApplicationContext(), "onCloud: No text found", Toast.LENGTH_SHORT).show();
            return;
        }
        mGraphicOverlay.clear();
        List<FirebaseVisionCloudText.Page> pages = text.getPages();
        for (int i = 0; i < pages.size(); i++) {
            FirebaseVisionCloudText.Page page = pages.get(i);
            List<FirebaseVisionCloudText.Block> blocks = page.getBlocks();
            for (int j = 0; j < blocks.size(); j++) {
                List<FirebaseVisionCloudText.Paragraph> paragraphs = blocks.get(j).getParagraphs();
                for (int k = 0; k < paragraphs.size(); k++) {
                    FirebaseVisionCloudText.Paragraph paragraph = paragraphs.get(k);
                    List<FirebaseVisionCloudText.Word> words = paragraph.getWords();
                    for (int l = 0; l < words.size(); l++) {
                        GraphicOverlay.Graphic cloudTextGraphic = new CloudTextGraphic(mGraphicOverlay, words.get(l));
                        mGraphicOverlay.add(cloudTextGraphic);
                    }
                }
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        //when user interacting with the activity
        mCameraView.start();
    }

    @Override
    public void onPause() {
        //when the user leaves the activity
        mCameraView.stop();
        super.onPause();
    }


}
