package com.example.ballooningdemo.manager;

import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.Lifecycle;

import com.example.ballooningdemo.util.DensityUtil;
import com.example.ballooningdemo.view.BallooningView;

import java.util.ArrayList;

public class ActionManager {
    private ValueAnimator valueAnimator;

    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    private Sensor accSensor;
    private boolean isStart = false;
    private volatile int sensorTime = 0;
    private CountDownTimer countDownTimer;
    private  BallooningView imageView;
    private  Context mContext;


    public  ActionManager getInstance(Context context, BallooningView view) {
        imageView =view;
        mContext = context;
        return this;
    }

    /**
     * 将图片的大小 位置塞到同个方法
     * 注！！！！！！！
     * ActionManager初始化之后必须设置的属性 不然会出现问题
     * @param size
     * @param leftMargin
     * @param topMargin
     */
    public  void setImageView(Float size,Float leftMargin, Float topMargin) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)imageView.getLayoutParams();
        layoutParams.width = DensityUtil.dp2px(mContext,size);
        layoutParams.height = DensityUtil.dp2px(mContext,size);
        layoutParams.leftMargin = DensityUtil.dp2px(mContext,leftMargin);
        layoutParams.topMargin = DensityUtil.dp2px(mContext,topMargin);
        imageView.setLayoutParams(layoutParams);
    }


    public void initAnimal(boolean right, boolean down) {
        if (valueAnimator != null) {
            valueAnimator.cancel();
            valueAnimator = null;
        }
        int radians = 45;
        int startX = (int) (imageView.getLeft() + imageView.getTranslationX());
        int startY = (int) (imageView.getTop() + imageView.getTranslationY());
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        int height = mContext.getResources().getDisplayMetrics().heightPixels;
        int endX = 0;
        int endY = 0;
        Log.d("onAnimationEnd", "onAnimationEnd: 方向 " + (right ? "右面" : "左面") + (down ? "下面" : "上面") + "  startX :" + startX + "  startY :" + startY + "  width :" + width + "  height :" + height);
        if (down) {
            endX = (int) (Math.tan(Math.toRadians(radians)) * (height - startY));
            Log.d("onAnimationEnd", "onAnimationEnd: endX " + endX);
            if (endX > width) {
                if (right) {
                    endX = width - imageView.getWidth();
                } else {
                    endX = 0;
                }
                endY = (int) (startY + (width - imageView.getWidth()) / (Math.tan(Math.toRadians(radians))));
            } else {
                endY = height - imageView.getHeight();
                if (!right) {
                    endX = width - endX - imageView.getWidth();
                } else {
                    endX = endX - imageView.getWidth();
                }
                if (endX < 0) {
                    endX = 0;
                }
            }
        } else {
            endX = (int) (Math.tan(Math.toRadians(radians)) * (startY + imageView.getWidth()));
            Log.d("onAnimationEnd", "onAnimationEnd: endX " + endX);
            if (endX >= width) {
                if (right) {
                    endX = width - imageView.getWidth();
                } else {
                    endX = 0;
                }
                endY = (int) (startY - (width - imageView.getWidth()) / (Math.tan(Math.toRadians(radians))));
            } else {
                endY = 0;
                if (!right) {
                    endX = width - endX - imageView.getWidth();
                } else {
                    endX = endX - imageView.getWidth();
                }
                if (endX < 0) {
                    endX = 0;
                }
            }
        }

        Log.d("onAnimationEnd", "onAnimationEnd: startX " + startX + "  startY:" + startY + "  endX:" + endX + "  endY:" + endY);

        ArrayList<PointF> values = new ArrayList<>();
        values.add(new PointF(startX, startY));
        values.add(new PointF(endX, endY));
        valueAnimator = new ValueAnimator();
        double distance = Math.sqrt((endX - startX) * (endX - startX) + (endY - startY) * (endY - startY));
        int duration = (int) ((5000 / width) * distance);
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setObjectValues(values.toArray());
        valueAnimator.setEvaluator((TypeEvaluator<PointF>) (fraction, startValue, endValue) -> {
            PointF point = new PointF();
            point.x = startValue.x + (endValue.x - startValue.x) * fraction;
            point.y = startValue.y + (endValue.y - startValue.y) * fraction;
            return point;
        });

        valueAnimator.start();
        valueAnimator.addUpdateListener(animation -> {
            PointF point = (PointF) animation.getAnimatedValue();
            if (imageView != null && point != null) {
                imageView.setX(point.x);
                imageView.setY(point.y);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                int left = (int) (imageView.getLeft() + imageView.getTranslationX());
                int top = (int) (imageView.getTop() + imageView.getTranslationY());
                int right = (int) (imageView.getRight() + imageView.getTranslationX());
                int bottom = (int) (imageView.getBottom() + imageView.getTranslationY());

                Log.d("onAnimationEnd", "onAnimationEnd: left " + left + "  top:" + top + "  right:" + right + "  bottom:" + bottom);
                //               Log.d("onAnimationEnd", "onAnimationEnd: width "+width+ "  height:"+height);
                if (right == width) {
                    initAnimal(false, top > startY);
                } else if (top == 0) {
                    initAnimal(left > startX, true);

                } else if (left == 0) {
                    initAnimal(true, top > startY);
                } else if (bottom == height) {
                    initAnimal(left > startX, false);
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
    public void initEvent(Context context) {
        //先获得传感器管理器
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        //获得加速度传感器
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //获得振动器
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //Log.i("AAA", "onSensorChanged: ");
                if (((ComponentActivity)context).getLifecycle().getCurrentState() != Lifecycle.State.RESUMED) {
                    return;
                }
                //设置触发摇一摇的条件
                //获得x,y,z方向的变化
                float[] values = event.values;
                float valueX = values[0]; //空间中X的变化
                float valueY = values[1]; //空间中Y的变化
                float valueZ = values[2]; //空间中Z的变化
                if (valueX > 15 || valueY > 15 || valueZ > 15) {//触发条件
                    Log.d("onSensorChanged", "onSensorChanged: " + "");
                    sensorTime++;
                    if (!isStart) {
                        isStart = true;
                        countDownTimer = new CountDownTimer(1000, 500) {
                            @Override
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish() {
                                countDownTimer.cancel();
                                countDownTimer = null;
                                sensorTime = 0;

                            }
                        };
                        countDownTimer.start();
                    }
                }
            }


            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensorManager.registerListener(sensorEventListener, accSensor, SENSOR_DELAY_NORMAL);

    }
}
