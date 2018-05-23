package com.mas.textrecognition;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

/*MainActivity sınıfına CameraKitEventListener arayüzü uygulanır.
* Bununla beraber onEvent, onError, onImage ve onVideo metotlarını
* eklemeniz gerekir.*/
public class MainActivity extends AppCompatActivity implements CameraKitEventListener, View.OnClickListener {

    /*Değişken tanımlama*/
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
     * Arayüzde bulunan kontrollere erişim sağlanır.
     */
    private void getControls() {
        mCameraView = findViewById(R.id.cameraView);
        mCameraButton = findViewById(R.id.btnOnDevice);
        mCameraButtonCloud = findViewById(R.id.btnCloud);
        mGraphicOverlay = findViewById(R.id.graphicOverlay);
    }

    /*
     * Kontrollere event listener tanımlanır.
     */
    private void setEventListener() {
        mCameraView.addCameraKitListener(this);
        mCameraButton.setOnClickListener(this);
        mCameraButtonCloud.setOnClickListener(this);
    }

    /*
     * Butonların click olayını alan metodumuz
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //cameraBtn
            case R.id.btnOnDevice:
                mDevice = true;
                break;
            //cameraBtnCloud
            case R.id.btnCloud:
                mDevice = false;
                break;

        }

        /*
        * Kameradan resim alınır.
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
        * Kameradan çekilen resmi bitmap olarak alan kodlar.
        */
        mBitmap = cameraKitImage.getBitmap();
        mBitmap = Bitmap.createScaledBitmap(mBitmap, mCameraView.getWidth(), mCameraView.getHeight(), false);
        mCameraView.stop();
        if (mDevice)
            //On-device tabanlı metin tanıma
            runTextRecognition(mBitmap);
        else
            //Cloud tabanlı metin tanıma
            runCloudTextRecognition(mBitmap);

    }

    @Override
    public void onVideo(CameraKitVideo cameraKitVideo) {

    }

    /*
    * text recognition detector ile resimde bulunan metinlerin algılanması sağlanır.
    * Eğer resimde metin tespit edilirse, processTextRecognitionResult isimli metot çağrılır.
    */
    private void runTextRecognition(Bitmap bitmap) {

        /*Bitmap resimdeki metni algılamak için resimden,
        * FirebaseVisionImage nesnesi oluşturmamız gerekiyor*/
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        /*FirebaseVisionTextDetector: Resimde bulunan karakterleri tanımak için kullanılır.*/
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();

        /*Aşağıdaki kod ile metinlerin algılanması işlemi icra edilir.*/
        detector.detectInImage(image).addOnSuccessListener(
                new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText texts) {
                //Metin tespit edilirse,
                processTextRecognitionResult(texts);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Metin tespit edilmediği zaman,
                e.printStackTrace();
            }
        });
    }

    /*
    * On-device modellerini kullanarak resimde tespit edilen metinleri ayrıştırıp
    * ekrana çizmeyi sağlar.
    * */
    private void processTextRecognitionResult(FirebaseVisionText texts) {
        //getBlocks ile gelen metin bloğu liste olarak alınır.
        List<FirebaseVisionText.Block> blocks = texts.getBlocks();
        if (blocks.size() == 0) {
            return;
        }
        mGraphicOverlay.clear();

        //blok olarak alınan metinler ayrıştırılır ve ekranda gösterilir.
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
    * DocumentTextDetector ile resimde bulunan metinlerin algılanması sağlanır.
    * Eğer resimde metin tespit edilirse, processCloudTextRecognitionResult isimli metot çağrılır.
    */
    private void runCloudTextRecognition(Bitmap bitmap) {

        /*FirebaseVisionCloudDetectorOptions: Bulut modellerini kullanarak
        * metin algılama yapabilmek için temel yapılandırmalar burada yapılır.
        * setModelType(): Varsayılan olarak STABLE_MODEL değerine sahiptir.
        * Bunu kullandığınızda stabil olan bulut modeli kullanılır. Eğer
        * LATEST_MODEL seçilirse yayınlanan en son model kullanılır. Son modeller
        * günceol olmaları bakımdan daha iyi sonuçlar verebilir.
        *
        * setMaxResults(): Maksimum sonuç sayısı belirlenir. Varsayılan 10'dur.
        *
        * build(): FirebaseVisionCloudDetectorOptions nesnesi oluşturmayı sağlar.*/
        FirebaseVisionCloudDetectorOptions options =
                new FirebaseVisionCloudDetectorOptions.Builder()
                        .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                        .setMaxResults(15)
                        .build();

        mCameraButtonCloud.setEnabled(false);

        /*Bitmap resimdeki metni algılamak için resimden,
        * FirebaseVisionImage nesnesi oluşturmamız gerekiyor*/
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        /*FirebaseVisionCloudDocumentTextDetector: Tanımladığımız yapılandırma ayarlarını
        * kullanarak görüntünün Google Cloud ML'ye gönderilmesini sağlar. Özellikle yoğun
        * metinler ile çalışırken bu yöntemi kullanmanızı tavsiye ediyoruz.
        * Bunun yerine FirebaseVisionCloudTextDetector sınıfını da kullanabilirsiniz.
        * */
        FirebaseVisionCloudDocumentTextDetector detector = FirebaseVision.getInstance()
                .getVisionCloudDocumentTextDetector(options);

        /*Resimde bulunan metinlerin algılanmasını sağlar.*/
        detector.detectInImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionCloudText>() {
                            @Override
                            public void onSuccess(FirebaseVisionCloudText texts) {
                                mCameraButtonCloud.setEnabled(true);
                                //Metin tespit edilirse,
                                processCloudTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // işlem başarısız olduğunda
                                mCameraButtonCloud.setEnabled(true);
                                e.printStackTrace();
                            }
                        });
    }

    /*
    * Cloud modellerini kullanarak resimde tespit edilen metinleri ayrıştırıp
    * ekrana çizmeyi sağlar.
    * */
    private void processCloudTextRecognitionResult(FirebaseVisionCloudText text) {

        if (text == null) {
            return;
        }

        mGraphicOverlay.clear();
        /*FirebaseVisionCloudText: Resimde tanınan metnin tutulduğu yapıdır.*/
        List<FirebaseVisionCloudText.Page> pages = text.getPages();

        //blok olarak alınan metinler ayrıştırılır ve ekranda gösterilir.
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
        mCameraView.start();
    }

    @Override
    public void onPause() {
        mCameraView.stop();
        super.onPause();
    }


}
