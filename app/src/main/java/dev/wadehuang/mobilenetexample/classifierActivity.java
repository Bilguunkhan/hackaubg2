package dev.wadehuang.mobilenetexample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.util.List;

import dev.wadehuang.mobilenetexample.images.ImageClassifier;
import dev.wadehuang.mobilenetexample.images.Recognition;

public class classifierActivity extends Activity {
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private ImageClassifier imageClassifier;
    private List<Recognition> resultL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classifier);
        this.imageView = (ImageView)this.findViewById(R.id.capturedImage);
        Button photoButton = (Button) this.findViewById(R.id.button1);
        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
    }
    public Translation getTranslation() {


        final Translation[] translation= new Translation[1];
        Thread t= new Thread(){
            public void run(){
                Translate translate = TranslateOptions.getDefaultInstance().getService();
                // The text to translate
                String text2 = "Hello, world!";

                // Translates some text into Russian
                translation[0] =
                        translate.translate(
                                text2,
                                Translate.TranslateOption.sourceLanguage("en"),
                                Translate.TranslateOption.targetLanguage("ru"));

            }

        };
        t.start();
        try{
            t.join();
        }
        catch (InterruptedException e){
            System.out.print("Got interrupted");
        }
        return translation[0];

    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);

            Bitmap bm = Bitmap.createScaledBitmap(photo, 224, 224, true);
            imageClassifier = new ImageClassifier(this);
            resultL = imageClassifier.recognizeImage(bm);
            TextView text = (TextView) findViewById(R.id.resultList);
            Recognition object = resultL.get(0);

            Translation ts = getTranslation();
            System.out.println("translated");
            text.setText(ts.getTranslatedText());
        }
    }

}
