package dev.wadehuang.mobilenetexample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import dev.wadehuang.mobilenetexample.images.ImageClassifier;
import dev.wadehuang.mobilenetexample.images.ImageHelper;
import dev.wadehuang.mobilenetexample.images.Recognition;
import dev.wadehuang.mobilenetexample.views.CameraPreviewFragment;


public class MainActivity extends Activity
        implements CameraPreviewFragment.CameraPreviewListener {

    private static final String TAG = "MainActivity";
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final int PERMISSIONS_REQUEST = 1;
    private static final boolean DEBUG_MODE = false;

    private ListView resultListView;
    private ImageView previewImageView;

    private ArrayAdapter<Recognition> resultListAdapter;

    private boolean computing;
    private ImageClassifier imageClassifier;
    private Bitmap bitmap;
    private Bitmap croppedBitmap;
    private int sensorOrientation;
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private List<Recognition> resultList;

    private Runnable updateResult = new Runnable() {
        @Override
        public void run() {
            resultListAdapter.clear();
            resultListAdapter.addAll(resultList);


            if (DEBUG_MODE) {
                previewImageView.setImageDrawable(new BitmapDrawable(getResources(), croppedBitmap));
            }

            computing = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (hasPermission()) {
            if (null == savedInstanceState) {
                init();
            }
        } else {
            requestPermission();
        }
    }

    private void init() {
        resultListView = (ListView) findViewById(R.id.resultList);
        previewImageView = (ImageView) findViewById(R.id.previewImage);

        resultListAdapter = new ArrayAdapter<>(this, R.layout.item_recogition);
        resultListView.setAdapter(this.resultListAdapter);

        imageClassifier = new ImageClassifier(this);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, CameraPreviewFragment.newInstance(this))
                .commit();

        if (DEBUG_MODE) {
            previewImageView.setVisibility(View.VISIBLE);
        } else {
            previewImageView.setVisibility(View.GONE);
        }
    }


    @Override
    public void onPreviewReadied(Size size, int cameraRotation) {
        bitmap = Bitmap.createBitmap(size.getWidth(), size.getHeight(), Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(ImageClassifier.INPUT_SIZE, ImageClassifier.INPUT_SIZE, Bitmap.Config.ARGB_8888);

        final Display display = getWindowManager().getDefaultDisplay();
        final int screenOrientation = display.getRotation();

        sensorOrientation = cameraRotation + screenOrientation;

        frameToCropTransform =
                ImageHelper.getTransformationMatrix(
                        size.getHeight(), size.getWidth(),
                        ImageClassifier.INPUT_SIZE, ImageClassifier.INPUT_SIZE,
                        sensorOrientation, true);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        if (computing)
            return;
        ;

        Image image = null;

        try {

            image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            computing = true;

            ImageHelper.imageToBitmap(image, bitmap);

            final Canvas canvas = new Canvas(croppedBitmap);
            canvas.drawBitmap(bitmap, frameToCropTransform, null);

            image.close();

            resultList = imageClassifier.recognizeImage(croppedBitmap);

            runOnUiThread(updateResult);
        } catch (final Exception e) {
            Log.e(TAG, "recognizeImage", e);
        } finally {
            if (image != null) {
                image.close();
            }

            computing = false;
        }
    }

    //region Permission
    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
                Toast.makeText(MainActivity.this, "Camera permission is required for this example", Toast.LENGTH_LONG).show();
            }

            requestPermissions(new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();
            } else {
                requestPermission();
            }
        }
    }
    public void classifyActivity(View view){
        Intent intent = new Intent(this, classifierActivity.class);
        startActivity(intent);
    }
    //endregion
}
