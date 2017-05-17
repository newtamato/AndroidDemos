package com.xilingyuli.demos.screenshot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.xilingyuli.demos.R;
import com.xilingyuli.demos.utils.FileUtil;

import java.nio.ByteBuffer;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScreenShotActivity extends Activity {

    static final int SCREEN_CAPTURE_PERMISSION = 101;

    private DisplayMetrics metrics;
    private int width, height;

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader = null;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_shot);
        checkScreenShotPermission();
        findViewById(R.id.screen_shot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        screenShot();
                    }
                });
            }
        });
    }

    protected void checkScreenShotPermission() {
        FileUtil.requestWritePermission(this);
        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), SCREEN_CAPTURE_PERMISSION);
    }

    protected void screenShotPrepare() {
        if(mediaProjection==null)
            return;

        Display display = ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        Point point = new Point();
        display.getRealSize(point);
        width = point.x;
        height = point.y;
        resize();

        imageReader = ImageReader.newInstance(width, height, 0x1, 1);
        virtualDisplay = mediaProjection.createVirtualDisplay("ScreenShotDemo",
                width, height, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null/*Callbacks*/, null/*Handler*/);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCREEN_CAPTURE_PERMISSION:
                mediaProjection = projectionManager.getMediaProjection(resultCode, data);
                screenShotPrepare();
                break;
        }
    }

    protected boolean screenShot()
    {
        if(virtualDisplay==null)
            return false;
        Image image = imageReader.acquireLatestImage();
        if(image==null)
            return false;

        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();

        int w = rowStride/pixelStride;
        int h = w*height/width;

        Bitmap bitmap = Bitmap.createBitmap(metrics, w, h, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        image.close();
        return FileUtil.saveImage(""+w+"×"+h+"test.png",bitmap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(virtualDisplay!=null)
            virtualDisplay.release();
    }

    private void resize(){
        /*int w = width;
        int h = height;
        while (h!=0){
            if(w>h){
                w = w+h;
                h = w-h;
                w = w-h;
            }
            h = h%w;
        }

        width/=w;
        height/=w;

        //w向上取整
        int res = 1;
        while (w!=0)
        {
            w = w>>1;
            res = res<<1;
        }

        width *= res;
        height *= res;*/

        //TODO:经测试得出，大于设定大小，原因未知
        width = 128*9;
        height = 128*16;
    }
}
