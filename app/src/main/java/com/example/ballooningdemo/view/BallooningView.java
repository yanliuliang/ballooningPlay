package com.example.ballooningdemo.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.ballooningdemo.R;

/**
 * @Description:自定义气球
 * @Author: dick
 * @CreateDate: 2022/12/12 
 * @Version:
 */
public class BallooningView  extends RelativeLayout {
    private LifeValueListener listener;
    private static ImageView imageView;
    private int lifeValue =3;//生命值，默认为3
    public  BallooningView(Context context) {
        super(context);
        initView(context);
    }

    /**
     * 初始化界面
     * @param context
     */
    private void initView(Context context) {
        View view  = LayoutInflater.from(context).inflate(R.layout.view_ball,null);
        this.addView(view);
        this.setBackgroundColor(context.getResources().getColor(R.color.ballooningColor1));
        this.setOnClickListener(v -> {
            lifeValue--;
            Log.d("BallooningView", "onClick: 当前球的生命值："+lifeValue);
            if (lifeValue==0){
                Log.d("BallooningView", "onClick: 该球被消除");
                listener.leftOver();
            }
        });
    }

    /**
     * 设置生命值
     * @param lifeValue
     */
    public void setLifeValue(int lifeValue){
        this.lifeValue =lifeValue;
    }
    public void setBallClick(LifeValueListener listener){
        this.listener = listener;
    }
    public interface LifeValueListener{
        void leftOver();
    }

}
