package com.xilingyuli.demos.animation;

import android.content.ClipData;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xilingyuli.demos.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DragActivity extends AppCompatActivity {

    @BindView(R.id.imageView)
    ImageView imageView;

    @BindView(R.id.relativeLayout)
    RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag);
        ButterKnife.bind(this);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN)
                {
                    ClipData data = ClipData.newPlainText("","");
                    //noinspection deprecation
                    v.startDrag(data,new View.DragShadowBuilder(v),null,0);
                    //return true;
                }
                return false;
            }
        });
        layout.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                int action = event.getAction();
                switch (action){
                    case DragEvent.ACTION_DRAG_STARTED:
                        imageView.setVisibility(View.GONE);
                        break;
                    case DragEvent.ACTION_DROP:
                        imageView.setX(event.getX()-imageView.getWidth()/2);
                        imageView.setY(event.getY()-imageView.getHeight()/2);
                        imageView.setVisibility(View.VISIBLE);
                        break;
                }
                return true;
            }
        });
    }
}
