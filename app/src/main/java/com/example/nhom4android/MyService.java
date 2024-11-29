package com.example.nhom4android;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.example.myapplication.R;

public class MyService extends Service {
    MediaPlayer mymusic;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    // Hàm dùng để khởi tạo đối tượng mà service quản lý
    @Override
    public void onCreate() {
        super.onCreate();
        mymusic = MediaPlayer.create(MyService.this, R.raw.music);
        mymusic.setLooping(true); // Lập lại bài hát
    }
    // Hàm để khởi tạo service quản lý
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mymusic.isPlaying()){
            mymusic.pause();
        }
        else {
            mymusic.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }
    // Hàm dùng để dừng đối tượng mà service quản lý
    @Override
    public void onDestroy() {
        super.onDestroy();
        mymusic.stop();
    }
}