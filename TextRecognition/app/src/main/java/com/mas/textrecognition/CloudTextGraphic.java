package com.mas.textrecognition;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.google.firebase.ml.vision.cloud.text.FirebaseVisionCloudText;

import java.util.List;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class CloudTextGraphic extends GraphicOverlay.Graphic {
    private static final int TEXT_COLOR = Color.GREEN;
    private static final float TEXT_SIZE = 60.0f;
    private static final float STROKE_WIDTH = 5.0f;

    private final Paint rectPaint;
    private final Paint textPaint;
    private final FirebaseVisionCloudText.Word word;
    private final GraphicOverlay overlay;

    public CloudTextGraphic(GraphicOverlay overlay, FirebaseVisionCloudText.Word word) {
        super(overlay);

        this.word = word;
        this.overlay = overlay;

        rectPaint = new Paint();
        rectPaint.setColor(TEXT_COLOR);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(STROKE_WIDTH);

        textPaint = new Paint();
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(TEXT_SIZE);
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        if (word == null) {
            throw new IllegalStateException("Attempting to draw a null text.");
        }

        float x = overlay.getWidth() / 4.0f;
        float y = overlay.getHeight() / 4.0f;

        StringBuilder wordStr = new StringBuilder();
        Rect wordRect = word.getBoundingBox();
        canvas.drawRect(wordRect, rectPaint);
        List<FirebaseVisionCloudText.Symbol> symbols = word.getSymbols();
        for (int m = 0; m < symbols.size(); m++) {
            wordStr.append(symbols.get(m).getText());
        }
        canvas.drawText(wordStr.toString(), wordRect.left, wordRect.bottom, textPaint);
    }
}
