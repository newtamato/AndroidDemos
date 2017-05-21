package com.xilingyuli.demos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.xilingyuli.demos.animation.DragActivity;
import com.xilingyuli.demos.screenshot.ScreenShotActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Intent intent = new Intent(this, ScreenShotActivity.class);
        Intent intent = new Intent(this, DragActivity.class);
        startActivity(intent);
        finish();
    }
}
