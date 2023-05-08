package com.example.may8dialogflowdetectintenttext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static Handler handler = new Handler();
    public static TextView tvResponse;
    EditText txtInput;

    DetectIntent detectIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getting internet permission if not provided by default
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.INTERNET}, 200);
        }

        tvResponse = findViewById(R.id.tvResponse);
        txtInput = findViewById(R.id.txtInput);

        detectIntent = new DetectIntent();
    }


    public void btnSendMessagePressed(View view) {
        if(txtInput.getText().length() > 0){

            Context context = this;

            try {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            detectIntent.detectIntent(txtInput.getText().toString(), "en-US", context);

                            Log.e("Runnable", "Thread: "+Thread.currentThread().getName());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                t.start();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}