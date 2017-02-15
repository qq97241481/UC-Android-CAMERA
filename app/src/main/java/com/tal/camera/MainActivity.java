package com.tal.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import static com.tal.camera.Config.screenHeight;
import static com.tal.camera.Config.screenWidth;


/**
 * Created by 郭强 on 2017/2/9.
 */
public class MainActivity extends Activity {

    private Button cameraBtn;
    private Button pictureBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Display display =  this.getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        cameraBtn = (Button) findViewById(R.id.cameraBut);
        pictureBtn = (Button) findViewById(R.id.pictureBut);

        pictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("camera","picture");
                intent.setClass(MainActivity.this , TALAndroid.class);
                startActivity(intent);
            }
        });
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("camera","video");
                intent.setClass(MainActivity.this,TALAndroid.class);
                startActivity(intent);
            }
        });
    }
}

