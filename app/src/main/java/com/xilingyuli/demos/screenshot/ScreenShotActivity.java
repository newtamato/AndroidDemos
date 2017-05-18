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
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.xilingyuli.demos.R;
import com.xilingyuli.demos.utils.FileUtil;

import java.nio.ByteBuffer;

import static android.graphics.PixelFormat.RGBA_8888;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScreenShotActivity extends Activity {

    static final int SCREEN_CAPTURE_PERMISSION = 101;

    private DisplayMetrics metrics;
    private int width, height;

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader = null;

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_shot);
        checkScreenShotPermission();
        button = (Button) findViewById(R.id.screen_shot);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setVisibility(View.GONE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        screenShot();
                    }
                }).start();
            }
        });
    }

    /**
     * 申请截屏相关权限
     * */
    protected void checkScreenShotPermission() {
        FileUtil.requestWritePermission(this);
        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), SCREEN_CAPTURE_PERMISSION);
    }

    /**
     * 初始化截屏相关设置
     * MediaProjectionManager -> MediaProjection -> VirtualDisplay
     * */
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

        //将屏幕画面放入ImageReader关联的Surface中
        imageReader = ImageReader.newInstance(width, height, RGBA_8888, 1);
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

    /**
     * 进行截屏
     * */
    protected boolean screenShot()
    {
        Image image = imageReader.acquireLatestImage();  //获取缓冲区中的图像，关键代码
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setVisibility(View.VISIBLE);
            }
        });
        if(image==null)
            return false;

        //Image -> Bitmap
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int rowStride = planes[0].getRowStride();  //Image中的行宽，大于Bitmap所设的真实行宽
        byte[] oldBuffer = new byte[rowStride*height];
        buffer.get(oldBuffer);
        byte[] newBuffer = new byte[width*4*height];

        Bitmap bitmap = Bitmap.createBitmap(metrics, width, height, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < height; ++i) {
            System.arraycopy(oldBuffer,i*rowStride,newBuffer,i*width*4,width*4);  //跳过多余的行宽部分，关键代码
        }
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(newBuffer));  //用byte数组填充bitmap，关键代码
        image.close();

        return FileUtil.saveImage(""+width+"×"+height+"-ScreenShot.png",bitmap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(virtualDisplay!=null)
            virtualDisplay.release();
    }
}
