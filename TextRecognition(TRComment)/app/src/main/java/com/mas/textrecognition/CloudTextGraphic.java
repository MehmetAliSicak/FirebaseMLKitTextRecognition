package com.mas.textrecognition;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.google.firebase.ml.vision.cloud.text.FirebaseVisionCloudText;

import java.util.List;

/**
 * GraphicOverlay.Graphic sınıfından türetilen
 * ve temel amacı resimde tespit edilen metin blokları
 * için text boyutu, ID ve dikdörtgen bir alan çizmeyi
 * sağlamaktır. Buluttaki modelleri kullanır.
 */
public class CloudTextGraphic extends GraphicOverlay.Graphic {
    //metin rengi
    private static final int TEXT_COLOR = Color.GREEN;
    //metin boyutu
    private static final float TEXT_SIZE = 60.0f;
    //Dikdörtgen kutu çerçeve kalınlığı
    private static final float STROKE_WIDTH = 5.0f;

    //Dikdörtgen kutu
    private final Paint rectPaint;
    //metin bloğu
    private final Paint textPaint;
    //Firebase Cloud ile algılana metin bloğu.
    private final FirebaseVisionCloudText.Word word;
    private final GraphicOverlay overlay;

    /*Kurucu metot oluşturduk.
    * FirebaseVisionCloudText ile gelen metin bloklarını alır.
    * Metin blokları için dikdörtgen bir alan çizmeyi,
    * metin bloklarının rengini ve büyüklüğünü
    * ayarlamayı sağlar.*/
    public CloudTextGraphic(GraphicOverlay overlay, FirebaseVisionCloudText.Word word) {
        super(overlay);

        this.word = word;
        this.overlay = overlay;

        //Dikdörtgen alanın özellekleri nesneye uygulanır.
        rectPaint = new Paint();
        rectPaint.setColor(TEXT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        //Metin özellikleri nesneye uygulanır.
        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);

        //draw() metodunu çağırır.
        postInvalidate();
    }

    /**
     * Metin için belirlenen özellikleri
     * kullanarak metin bloğu çizmeyi sağlar
     */
    @Override
    public void draw(Canvas canvas) {

        //Tespit edilen metinleri tutacağımız nesnemiz.
        StringBuilder wordStr = new StringBuilder();

        /*rectPaint için belirlenen özellikler kullanılarak
        Dikdörtgen alan çizmeyi sağlar*/
        Rect wordRect = word.getBoundingBox();
        canvas.drawRect(wordRect, rectPaint);

        /*Cloud modellerinden elde edilen metinleri ayrıştırıp
        * wordStr nesnesine eklemeyi sağlayan for döngüsü */
        List<FirebaseVisionCloudText.Symbol> symbols = word.getSymbols();
        for (int m = 0; m < symbols.size(); m++) {
            wordStr.append(symbols.get(m).getText());
        }

        /*Metinler resim üzerine çizilir.*/
        canvas.drawText(wordStr.toString(), wordRect.left, wordRect.bottom, textPaint);
    }
}
