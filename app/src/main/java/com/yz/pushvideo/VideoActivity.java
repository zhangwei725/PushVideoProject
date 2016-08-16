package com.yz.pushvideo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.yz.pushvideo.camera.CameraSurfaceView;
import com.yz.pushvideo.utils.PushManager;

public class VideoActivity extends AppCompatActivity {
    PushManager manager;

    CameraSurfaceView csv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        csv = (CameraSurfaceView) findViewById(R.id.cvs);
        manager = new PushManager.Bulider(this, csv).bulider();
        manager.startRecording();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }


    @Override
    public void onBackPressed() {
        manager.stopRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
