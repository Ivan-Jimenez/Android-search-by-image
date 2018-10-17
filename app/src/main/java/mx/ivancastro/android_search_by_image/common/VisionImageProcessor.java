package mx.ivancastro.android_search_by_image.common;

import android.graphics.Bitmap;
import android.media.Image;

import com.google.firebase.ml.common.FirebaseMLException;

import java.nio.ByteBuffer;

/**
 * Interface to process the images with different detectors.
 */
public interface VisionImageProcessor {

    /**
     * Process the images with the underlying machine learning models.
     * @param data
     * @param frameMetaData
     * @param graphicOverlay
     * @throws FirebaseMLException
     */
    void process (ByteBuffer data, FrameMetaData frameMetaData, GraphicOverlay graphicOverlay)
        throws FirebaseMLException;

    /**
     * Process the bitmap images
     * @param bitmap
     * @param graphicOverlay
     */
    void process (Bitmap bitmap, GraphicOverlay graphicOverlay);

    /**
     * Process the images
     * @param bitmap
     * @param rotation
     * @param graphicOverlay
     */
    void process (Image bitmap, int rotation, GraphicOverlay graphicOverlay);

    /**
     * Stops the underlying machine learning model and release resources.
     */
    void stop ();
}