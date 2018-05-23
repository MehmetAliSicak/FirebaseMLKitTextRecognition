
package com.mas.textrecognition;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.camera2.CameraCharacteristics;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashSet;
import java.util.Set;

/*GraphicOverlay sınıfı View sınıfından türetilmelidir.
Bu sınıfı arayüzde bir kontrol olarak kullanacağız.
Temel amacı, metinlerin gösterileceği çizim
alanını oluşturmaktır. Kameradan alınan görüntü
üzerine uygulanacktır. */
public class GraphicOverlay extends View {
    private final Object lock = new Object();
    private int previewWidth;
    private float widthScaleFactor = 1.0f;
    private int previewHeight;
    private float heightScaleFactor = 1.0f;
    private int facing = CameraCharacteristics.LENS_FACING_BACK;
    private Set<Graphic> graphics = new HashSet<>();

    /**
     * Uygulamaya özgü bir grafik sınıfı oluşturduk.
     * Bu sınıfın amacı, resimde tespit edilen metinlerin
     * gösterilmesini sağlamaktır. Grafik sınıfı ile
     * mevcut resmin üzerinde çizim yapılır.
     */
    public abstract static class Graphic {
        private GraphicOverlay overlay;

        public Graphic(GraphicOverlay overlay) {
            this.overlay = overlay;
        }

        /**
         * Grafik canvas nesnesi ile çizilecektir. */
        public abstract void draw(Canvas canvas);

        /**
         * Cihaz ölçeği baz alınarak scaleX belirlenir.
         */
        public float scaleX(float horizontal) {
            return horizontal * overlay.widthScaleFactor;
        }

        /**
         * Cihaz ölçeği baz alınarak scaleY belirlenir.
         */
        public float scaleY(float vertical) {
            return vertical * overlay.heightScaleFactor;
        }

        /**
         * Uygulama Context'i gönderilir.
         */
        public Context getApplicationContext() {
            return overlay.getContext().getApplicationContext();
        }

        /**
         * x koordinatı
         */
        public float translateX(float x) {
            if (overlay.facing == CameraCharacteristics.LENS_FACING_FRONT) {
                return overlay.getWidth() - scaleX(x);
            } else {
                return scaleX(x);
            }
        }

        /**
         * y koordinatı
         */
        public float translateY(float y) {
            return scaleY(y);
        }

        public void postInvalidate() {
            overlay.postInvalidate();
        }
    }

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Mevcut grafiklerin silinmesini sağlar.
     */
    public void clear() {
        synchronized (lock) {
            graphics.clear();
        }
        postInvalidate();
    }

    /**
     * Tanımladığımız Overlay alanına Grafik eklemyi sağlar.
     */
    public void add(Graphic graphic) {
        synchronized (lock) {
            graphics.add(graphic);
        }
        postInvalidate();
    }

    /**
     * Tanımladığımız Overlay alanından Grafik silmeyi sağlar.
     */
    public void remove(Graphic graphic) {
        synchronized (lock) {
            graphics.remove(graphic);
        }
        postInvalidate();
    }

    /**
     * Kamera özellikleri ayarlanır (yükseklik, genişlik, yön)
     */
    public void setCameraInfo(int previewWidth, int previewHeight, int facing) {
        synchronized (lock) {
            this.previewWidth = previewWidth;
            this.previewHeight = previewHeight;
            this.facing = facing;
        }
        postInvalidate();
    }

    /**
     * Overlay çizmeyi sağlar. Resimde bulunan metinlerin gösterimi sağlanır.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (lock) {
            if ((previewWidth != 0) && (previewHeight != 0)) {
                widthScaleFactor = (float) canvas.getWidth() / (float) previewWidth;
                heightScaleFactor = (float) canvas.getHeight() / (float) previewHeight;
            }

            for (Graphic graphic : graphics) {
                graphic.draw(canvas);
            }
        }
    }
}
