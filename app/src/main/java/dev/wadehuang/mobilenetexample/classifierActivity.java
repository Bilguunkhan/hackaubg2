package dev.wadehuang.mobilenetexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.api.client.googleapis.auth.clientlogin.ClientLogin;
import com.google.api.client.http.HttpResponse;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.Instant;
import org.threeten.bp.temporal.ChronoUnit;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

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


    public String getTranslation2(final String sentence2,final String languageCode){

        final String[] result=new String[1];
        Thread t= new Thread(){
            public void run(){
                result[0]=sendRequest(sentence2,languageCode);
            }

        };
        t.start();
        try{
            t.join();
        }
        catch (InterruptedException e){
            System.out.print("Got interrupted");
        }
        return result[0];
    }



    StringBuilder stringBuilder;
    AzureAuthToken token = new AzureAuthToken("24b53dbc03e24ec1b87ef2280c2adf9d");
    public String sendRequest(String sentenceToTranslate,String languageCode){
        try{

            // 1. Declare a URL Connection
            CookieHandler.setDefault( new CookieManager( null, CookiePolicy.ACCEPT_ALL ) );
            URL url= new URL("https://api.microsofttranslator.com/V2/Http.svc/Translate");
            Map<String, String> queryParameters= new HashMap<>();
            queryParameters.put("appid", "");
            queryParameters.put("text", sentenceToTranslate);
            queryParameters.put("from", "en");
            queryParameters.put("to", languageCode);

            String postParameters= createQueryStringForParameters(queryParameters);
            url=new URL(url.toString()+"?"+postParameters);

            System.out.println(url);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Ocp-Apim-Subscription-Key","24b53dbc03e24ec1b87ef2280c2adf9d");

            // 2. Open InputStream to connection
            conn.connect();
            InputStream in = conn.getInputStream();
            // 3. Download and decode the string response using builder
            stringBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            System.out.println(token.getAccessToken());
            //return readFullyAsString(conn.getInputStream(), "UTF-8");
            String result=stringBuilder.toString();
            result = result.substring(result.indexOf(">") + 1);
            result = result.substring(0, result.indexOf("<"));
            return result;
        }
        catch(Exception e){
            return "Some error occurred "+e.toString();
        }
    }

    public MediaPlayer sendSpeechRequest(String sentenceToTranslate,String languageCode){
        try{

            // 1. Declare a URL Connection
            CookieHandler.setDefault( new CookieManager( null, CookiePolicy.ACCEPT_ALL ) );
            URL url= new URL("https://api.microsofttranslator.com/V2/Http.svc/Speak");
            Map<String, String> queryParameters= new HashMap<>();
            queryParameters.put("appid", "Bearer "+token.getAccessToken());
            queryParameters.put("text", sentenceToTranslate);
            queryParameters.put("format", "audio/mp3");
            queryParameters.put("language", languageCode);

            String postParameters= createQueryStringForParameters(queryParameters);
            url=new URL(url.toString()+"?"+postParameters);

            System.out.println(url);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            //conn.setRequestProperty("Ocp-Apim-Subscription-Key","24b53dbc03e24ec1b87ef2280c2adf9d");

            // 2. Open InputStream to connection
            conn.connect();

            /*
            InputStream in = conn.getInputStream();
            // 3. Download and decode the string response using builder
            stringBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            String result=stringBuilder.toString();
            result = result.substring(result.indexOf(">") + 1);
            result = result.substring(0, result.indexOf("<"));*/

            MediaPlayer media=new MediaPlayer();
            media.setAudioStreamType(AudioManager.STREAM_MUSIC);
            media.setDataSource(url.toString());
            //mediaPlayer.Play();
            return media;
        }
        catch(Exception e){
            System.out.println("Some error occurred "+e.toString());
        }
        return new MediaPlayer();
    }

    // The types specified here are the input data type, the progress type, and the result type
// Subclass AsyncTask to execute the network request
// String == URL, Void == Progress Tracking, String == Response Received
    private class NetworkAsyncTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... strings) {
            // Some long-running task like downloading an image.
            // ... code shown above to send request and retrieve string builder
            return stringBuilder.toString();
        }

        protected void onPostExecute(String result) {
            // This method is executed in the UIThread
            // with access to the result of the long running task
            // DO SOMETHING WITH STRING RESPONSE
            System.out.print("Get response: "+result);
        }
    }

    private void downloadResponseFromNetwork() {
        // 4. Wrap in AsyncTask and execute in background thread
        new NetworkAsyncTask().execute("http://google.com");
    }


    public class AzureAuthToken {

        //Name of header used to pass the subscription key to the token service
        public static final String OcpApimSubscriptionKeyHeader = "Ocp-Apim-Subscription-Key";

        //when to refresh the token
        public static final int TokenCacheDurationMins = 8;

        /// URL of the token service
        private String  _serviceUrl= "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";

        /// Gets the subscription key.
        private String _subscriptionKey;

        //Cache the value of the last valid token obtained from the token service.
        private String _storedTokenValue = "";

        // When the last valid token was obtained.
        private Instant _storedTokenTime = Instant.MIN;


        public AzureAuthToken(String subscriptionKey)
        {
            this._subscriptionKey = subscriptionKey;
        }

        public String getAccessToken()
        {
            if (  this._storedTokenTime.until(Instant.now(), ChronoUnit.MINUTES) < TokenCacheDurationMins)
            {
                return this._storedTokenValue;
            }
            try
            {
                String charset = StandardCharsets.UTF_8.name();
                URL url = new URL(this._serviceUrl);

                HttpsURLConnection  connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty(OcpApimSubscriptionKeyHeader, this._subscriptionKey);
                connection.setDoOutput(true);
                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                out.close();

                int responseCode = connection.getResponseCode();
                if ( responseCode == HttpURLConnection.HTTP_OK)
                {
                    // OK
                    try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), charset)))
                    {
                        StringBuffer res = new StringBuffer();
                        String line;
                        while ((line = reader.readLine()) != null)
                        {
                            res.append(line);
                        }

                        this._storedTokenValue = res.toString();
                        this._storedTokenTime = Instant.now();
                        return this._storedTokenValue;
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

    }


    private static final char PARAMETER_DELIMITER = '&';
    private static final char PARAMETER_EQUALS_CHAR = '=';
    public static String createQueryStringForParameters(Map<String, String> parameters) {
        StringBuilder parametersAsQueryString = new StringBuilder();
        if (parameters != null) {
            boolean firstParameter = true;

            for (String parameterName : parameters.keySet()) {
                if (!firstParameter) {
                    parametersAsQueryString.append(PARAMETER_DELIMITER);
                }

                parametersAsQueryString.append(parameterName)
                        .append(PARAMETER_EQUALS_CHAR)
                        .append(URLEncoder.encode(
                                parameters.get(parameterName)));

                firstParameter = false;
            }
        }
        return parametersAsQueryString.toString();
    }

    public String readFullyAsString(InputStream inputStream, String encoding) throws IOException {
        return readFully(inputStream).toString(encoding);
    }

    public String getLanguageCode(String language){
        switch (language){
            case "English": {
                return "en";
            }
            case "French": {
                return "fr";
            }
            case "Spanish":{
                return "es";
            }
            case "German":{
                return "de";
            }
            case "Bulgarian":{
                return "bg";
            }
            case "Russian":{
                return "ru";
            }
            case "Chinese":{
                return "zh-CHS";
            }
            case "Italian":{
                return "it";
            }
        }
        return "en";
    }

    private ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);

            Bitmap bm = Bitmap.createScaledBitmap(photo, 224, 224, true);
            imageClassifier = new ImageClassifier(this);
            resultL = imageClassifier.recognizeImage(bm);
            TextView text = (TextView) findViewById(R.id.resultList);
            TextView text2 = (TextView) findViewById(R.id.resultList2);

            Spinner mySpinner=(Spinner) findViewById(R.id.lang_spinner);
            String languageFromSpinner = mySpinner.getSelectedItem().toString();
            String languageCode=getLanguageCode(languageFromSpinner);

            mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    TextView text = (TextView) findViewById(R.id.resultList);
                    TextView text2= (TextView) findViewById(R.id.resultList2);
                    Spinner mySpinner=(Spinner) findViewById(R.id.lang_spinner);
                    String languageCode=getLanguageCode(mySpinner.getSelectedItem().toString());
                    String translatedText= getTranslation2(text.getText().toString(),languageCode);

                    text2.setText(translatedText);

                    MediaPlayer media= sendSpeechRequest(translatedText,languageCode);
                    try{
                        media.prepare();
                        media.start();
                    }
                    catch (Exception ex){

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                    // todo
                }
            });

            Recognition object = resultL.get(0);

            String finalResult = getTranslation2(object.getTitle().toString(),languageCode);

            text.setText(object.getTitle().toString());
            text2.setText(finalResult);

            MediaPlayer media= sendSpeechRequest(finalResult,languageCode);
            try{
                media.prepare();
                media.start();
            }
            catch (Exception ex){

            }
        }
    }


}

