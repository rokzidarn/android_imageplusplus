package com.image.rok.imageplusplus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.savagelook.android.UrlJsonAsyncTask;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WelcomeActivity extends AppCompatActivity {

    Button btnCamera;
    ImageView imageView;
    RadioGroup rg;
    Bitmap bitmap = null;
    private final static String UPLOAD_API_ENDPOINT_URL = "https://imageplusplus.herokuapp.com/api_upload";

    private String mEncodedImage;
    private String mImageName;
    private String mUserId;
    private boolean mImagePrivate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Bundle b = this.getIntent().getExtras();
        String user = b.getString("user");
        mUserId = b.getString("id");

        TextView myAwesomeTextView = (TextView)findViewById(R.id.usernameText);
        myAwesomeTextView.setText(user);

        btnCamera=(Button)findViewById(R.id.btnCamera);
        imageView=(ImageView)findViewById(R.id.imageView);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
            }
        });
    }

    public void upload(View button) {
        EditText imageName = (EditText) findViewById(R.id.imageName);
        mImageName = imageName.getText().toString();

        rg = (RadioGroup) findViewById(R.id.radioGroup);
        final String value = ((RadioButton)findViewById(rg.getCheckedRadioButtonId() )).getText().toString();
        mImagePrivate = false;
        if(value.equals("Private")){
            mImagePrivate = true;
        }

        if (imageName.length() == 0 || value.length() == 0 || bitmap == null) {
            Toast.makeText(this, "Please fill out all the fields!",
                    Toast.LENGTH_LONG).show();
            return;
        }
        else if(imageName.length() > 24){
            Toast.makeText(this, "Image name field must not be longer than 32 characters!",
                    Toast.LENGTH_LONG).show();
        }
        else if(!isOnline()){
            Toast.makeText(this, "Please connect to the internet!",
                    Toast.LENGTH_LONG).show();
        }
        else {
            UploadTask uploadTask = new UploadTask(WelcomeActivity.this);
            uploadTask.setMessageLoading("Uploading...");
            uploadTask.execute(UPLOAD_API_ENDPOINT_URL);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
            if (resultCode == RESULT_CANCELED) {
                //disable back button in camera
            }
            if (resultCode == RESULT_OK){
                super.onActivityResult(requestCode, resultCode, data);
                Bitmap bitmapImage = (Bitmap) data.getExtras().get("data");

                Uri imageUri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("URI to Bitmap problem!", "" + e);
                }
                //imageView.setImageBitmap(bitmap);

                if(bitmap != null) {
                    if ((bitmap.getWidth() > 3264) || bitmap.getHeight() > 3264) {
                        bitmap = getResizedBitmap(bitmap, bitmap.getWidth() / 3, bitmap.getHeight() / 3);
                    }
                    imageView.setImageBitmap(bitmap);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageBytes = baos.toByteArray();
                    mEncodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    Log.e("ENCODED IMAGE", mEncodedImage);
                }
            }
        }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    //--------------------------------------------------------------------------------------------------

    private class UploadTask extends UrlJsonAsyncTask {
        public UploadTask(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject userObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();

            try {
                try {
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");
                    userObj.put("id", mUserId);
                    userObj.put("image", mEncodedImage);
                    userObj.put("name", mImageName);
                    userObj.put("private", mImagePrivate);
                    holder.put("session", userObj);
                    StringEntity se = new StringEntity(holder.toString());
                    post.setEntity(se);

                    // setup the request headers
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-Type", "application/json");

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute(post, responseHandler);
                    json = new JSONObject(response);

                } catch (HttpResponseException e) {
                    e.printStackTrace();
                    Log.e("ClientProtocol", "" + e);
                    json.put("info", "Error!"); // na serverju se že obdeluje
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IO", "" + e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON", "" + e);
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getBoolean("success")) {
                    String id = json.getJSONObject("data").getString("user_data");
                    String username = json.getJSONObject("data").getString("user_name");
                    Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);

                    Bundle b = new Bundle();
                    b.putString("id", id);
                    b.putString("user", username);
                    intent.putExtras(b);

                    startActivity(intent);
                    finish();
                }
                Toast.makeText(context, json.getString("info"), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }
}
