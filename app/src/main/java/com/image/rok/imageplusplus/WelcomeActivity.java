package com.image.rok.imageplusplus;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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

import java.io.ByteArrayOutputStream;

public class WelcomeActivity extends AppCompatActivity {

    Button btnCamera;
    ImageView imageView;
    RadioGroup rg;
    private String encodedImage;
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

        if (imageName.length() == 0 || value.length() == 0) {
            Toast.makeText(this, "Please fill out all the fields!",
                    Toast.LENGTH_LONG).show();
            return;
        } else {
            //upload
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmapImage = (Bitmap) data.getExtras().get("data");
        imageView.setImageBitmap(bitmapImage);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        //Log.e("ENCODED IMAGE", encodedImage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
