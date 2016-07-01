package com.image.rok.imageplusplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import com.savagelook.android.UrlJsonAsyncTask;

public class LoginActivity extends AppCompatActivity {
    private final static String LOGIN_API_ENDPOINT_URL = "https://imageplusplus.herokuapp.com/api_signin";
    private SharedPreferences mPreferences;
    private String mUserUsername;
    private String mUserPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
    }

    public void login(View button) {
        EditText userEmailField = (EditText) findViewById(R.id.userUsername);
        mUserUsername = userEmailField.getText().toString();
        EditText userPasswordField = (EditText) findViewById(R.id.userPassword);
        mUserPassword = userPasswordField.getText().toString();

        if (mUserUsername.length() == 0 || mUserPassword.length() == 0) {
            Toast.makeText(this, "Please fill out all the fields!",
                    Toast.LENGTH_LONG).show();
            return;
        } else {
            LoginTask loginTask = new LoginTask(LoginActivity.this);
            loginTask.setMessageLoading("Authenticating...");
            loginTask.execute(LOGIN_API_ENDPOINT_URL);
        }
    }

    public void bypass(View button){
        Intent intent = new Intent(this, WelcomeActivity.class);
        Bundle b = new Bundle();
        b.putString("user", "rokzidarn");
        intent.putExtras(b);
        startActivity(intent);
    }

//--------------------------------------------------------------------------------------------------

    private class LoginTask extends UrlJsonAsyncTask {
        public LoginTask(Context context) {
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
                    userObj.put("username", mUserUsername);
                    userObj.put("password", mUserPassword);
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
                    json.put("info", "Email and/or password are invalid. Retry!");
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
                    Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);

                    Bundle b = new Bundle();
                    b.putString("user", mUserUsername);
                    b.putString("id", id);
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
