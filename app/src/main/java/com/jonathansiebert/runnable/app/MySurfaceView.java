package com.jonathansiebert.runnable.app;

import java.lang.Runnable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.content.res.AssetManager;
import java.io.BufferedInputStream;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

public class MySurfaceView extends SurfaceView
        implements SurfaceHolder.Callback, Runnable {
    private Thread mThread = null;
    private Context mContext;
    private SurfaceHolder mSurfaceHolder;

    private boolean mRunning;

    Bitmap bitmap;

    private SoundPool soundPool;
    private int soundID;
    boolean loaded = false;


    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        mRunning = false;

        AssetManager assetManager = context.getAssets();
        BitmapFactory bitmapFactory = new BitmapFactory();
        try {
            bitmap =
            bitmapFactory.decodeStream(new BufferedInputStream(assetManager.open("runningcat.png")));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        // Load the sound
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                loaded = true;
            }
        });
        soundID = soundPool.load(mContext, R.raw.test, 1);
    }

    @Override
    public void run() {
        int f = 0;
        int frame = 0;
        int h = 0;
        int v = 0;
        Rect source = new Rect(0, 0, 512, 256);
        Rect target = new Rect(0, 0, 720, 360);
        while(mRunning) {
            Canvas c = null;
            try {
                c = mSurfaceHolder.lockCanvas(null);
                if(c != null) {
                    synchronized (mSurfaceHolder) {
                        c.drawColor(Color.GREEN);
                        h = frame % 2;
                        v = frame / 2;
                        source.set(h * 512, v * 256, h * 512 + 512, v * 256 + 256);
                        c.drawBitmap(bitmap, source, target, null);
                        f += 1;
                        if(f >= 6) {
                            frame += 1;
                            f = 0;
                        }
                        if(frame >= 8) frame = 0;

                        // Getting the user sound settings
                        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                        float actualVolume = (float) audioManager
                                .getStreamVolume(AudioManager.STREAM_MUSIC);
                        float maxVolume = (float) audioManager
                                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        float volume = actualVolume / maxVolume;
                        // Is the sound loaded already?
                        if (loaded) {
                            soundPool.play(soundID, volume, volume, 1, 0, 1f);
                            loaded = false;
                        }
                    }
                }
            } finally {
                if(c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mRunning = true;
        mThread = new Thread(this);
        mThread.start();
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        mRunning = false;
        while(retry) {
            try {
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public SurfaceHolder getHolder() {
        // TODO Auto-generated method stub
        return super.getHolder();
    }
}