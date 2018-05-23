package com.mas.textrecognition;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import com.google.firebase.ml.vision.text.FirebaseVisionText;

/**
 * GraphicOverlay.Graphic sınıfından türetilen
 * ve temel amacı resimde tespit edilen metin blokları
 * için text boyutu, ID ve dikdörtgen bir alan çizmeyi
 * sağlamaktır. Cihazdaki modelleri kullanır.
 */
public class TextGraphic extends GraphicOverlay.Graphic {

    //metin rengi
    private static final int TEXT_COLOR = Color.YELLOW;
    //metin boyutu
    private static final float TEXT_SIZE = 100.0f;
    //Dikdörtgen kutu çerçeve kalınlığı
    private static final float STROKE_WIDTH = 10.0f;

    //Dikdörtgen kutu
    private final Paint rectPaint;
    //metin bloğu
    private final Paint textPaint;
    //Firebase ile algılana metin bloğu
    private final FirebaseVisionText.Element element;

    /*Kurucu metot oluşturduk.
    * FirebaseVisionText ile gelen metin bloklarını alır.
    * Metin blokları için dikdörtgen bir alan çizmeyi,
    * metin bloklarının rengini ve büyüklüğünü
    * ayarlamayı sağlar.*/
    public TextGraphic(GraphicOverlay overlay, FirebaseVisionText.Element element) {
        super(overlay);

        this.element = element;

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
        /*rectPaint için belirlenen özellikler kullanılarak
        Dikdörtgen alan çizmeyi sağlar*/
        RectF rect = new RectF(element.getBoundingBox());
        canvas.drawRect(rect, rectPaint);

        /*textPaint için belirlenen özellikleri kullanarak
        dikdörtgen alan içine metni çizmeyi sağlar.*/
        canvas.drawText(element.getText(), rect.left, rect.bottom, textPaint);
    }
}
