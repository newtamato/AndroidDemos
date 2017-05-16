package com.xilingyuli.demos.screenshot;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.xilingyuli.demos.R;
import com.xilingyuli.demos.utils.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.OnClick;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScreenShotActivity extends Activity {

    static final int SCREEN_CAPTURE_PERMISSION = 101;

    private MediaProjectionManager mProjectionManager;

    ImageReader mImageReader = null;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_shot);
        checkScreenShotPermission();
        findViewById(R.id.screen_shot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenShotPrepare();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        screenShot();
                    }
                },1000);
            }
        });
    }

    protected void checkScreenShotPermission() {
        FileUtil.requestWritePermission(this);
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), SCREEN_CAPTURE_PERMISSION);
    }

    @SuppressWarnings("deprecation")
    protected void screenShotPrepare() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mScreenDensity = metrics.densityDpi;
        int mDisplayWidth = getWindowManager().getDefaultDisplay().getWidth();
        int mDisplayHeight = getWindowManager().getDefaultDisplay().getHeight();
        mImageReader = ImageReader.newInstance(mDisplayWidth, mDisplayHeight, 0x1, 2); //ImageFormat.RGB_565
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenShotDemo",
                mDisplayWidth, mDisplayHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null/*Callbacks*/, null/*Handler*/);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            /*case WRITE_PERMISSION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    checkScreenShotPermission();
                }
                break;*/
            case SCREEN_CAPTURE_PERMISSION:
                mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
                break;
        }
    }

    protected void screenShot()
    {
        if(mVirtualDisplay==null)
            return;
        Image image = mImageReader.acquireLatestImage();
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0,width, height);
        image.close();
        File fileImage = null;
        if (bitmap != null) {
            FileUtil.saveImage("test.png",bitmap);
            Toast.makeText(this,"保存完成",Toast.LENGTH_SHORT).show();
            mVirtualDisplay.release();
        }
    }
}
