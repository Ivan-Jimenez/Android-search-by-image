package mx.ivancastro.android_search_by_image;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import mx.ivancastro.android_search_by_image.common.FrameMetaData;
import mx.ivancastro.android_search_by_image.common.GraphicOverlay;
import mx.ivancastro.android_search_by_image.common.VisionImageProcessor;

/**
 * Subclasses need to implement {@link #onSuccess(Object, FrameMetaData, GraphicOverlay)} to define
 * what they what they want to do with the detection results and {@link #detectInImage(FirebaseVisionImage)}
 * to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
public abstract class VisionProcessorBase<T> implements VisionImageProcessor {
    // whether we should ignore process(). This is usually caused by feeding input data faster than
    // the model can handle
    private final AtomicBoolean shouldThrottle = new AtomicBoolean(false);

    public VisionProcessorBase () {}

    @Override
    public void process (ByteBuffer data, final FrameMetaData frameMetaData,
                         final GraphicOverlay graphicOverlay) {

        if (shouldThrottle.get()) return;

        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setWidth(frameMetaData.getWidth())
                .setHeight(frameMetaData.getHeight())
                .setRotation(frameMetaData.getRotation())
                .build();

        detectInVisonImage(
                FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetaData, graphicOverlay);
    }

    /**
     * Detects features from given Bitmap
     * @param bitmap
     * @param graphicOverlay
     */
    @Override
    public void process (Bitmap bitmap, final GraphicOverlay graphicOverlay) {
        if (shouldThrottle.get()) return;
        detectInVisonImage(FirebaseVisionImage.fromBitmap(bitmap), null, graphicOverlay);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void process (Image image, int rotation, final GraphicOverlay graphicOverlay) {
        if (shouldThrottle.get()) return;
        // This is for overlay display's usage
        FrameMetaData frameMetaData = new FrameMetaData.Builder()
                .setWidth(image.getWidth())
                .setHeight(image.getHeight())
                .build();

        FirebaseVisionImage fbImage = FirebaseVisionImage.fromMediaImage(image, rotation);
        detectInVisonImage(fbImage, frameMetaData, graphicOverlay);
    }

    /**
     * Detects feature from given media.Image
     * @param image
     * @param metadata
     * @param graphicOverlay
     *
     * @return created FirebaseVisionImage
     */
    private void detectInVisonImage (FirebaseVisionImage image, final FrameMetaData metadata,
                                     final GraphicOverlay graphicOverlay) {
        detectInImage(image)
                .addOnSuccessListener(results -> {
                    shouldThrottle.set(false);
                    VisionProcessorBase.this.onSuccess(results, metadata, graphicOverlay);
                })
                .addOnFailureListener ((e) -> {
                    shouldThrottle.set(false);
                    VisionProcessorBase.this.onFailure(e);
                });
        // Begin throttling until frame off input has been processed, either onSuccess or
        // onFailure
        shouldThrottle.set(true);
    }

    @Override
    public void stop () {}

    protected  abstract Task<T> detectInImage (FirebaseVisionImage image);

    protected abstract void onSuccess ( @NonNull T results,
                                        @NonNull FrameMetaData frameMetaData,
                                        @NonNull GraphicOverlay graphicOverlay);

    protected abstract void onFailure (@NonNull Exception e);
}
