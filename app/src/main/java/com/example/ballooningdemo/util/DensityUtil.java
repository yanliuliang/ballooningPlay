package com.example.ballooningdemo.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * 屏幕尺寸工具类
 */
public class DensityUtil {
    /**
     * dp转px
     */
    public static int dp2px(Context context, Float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, context.getResources().getDisplayMetrics());
    }

    /**
     * px转dp
     */
    public static Float px2dp(Context context, Float pxVal) {
        float scale = context.getResources().getDisplayMetrics().density;
        return pxVal / scale;
    }

    /**
     * px转sp
     */
    public static Float px2sp(Context context, int pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return pxValue / fontScale + 0.5f;
    }

    /**
     * 获取状态栏高度
     */
    public static int getStateBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            return context.getResources().getDimensionPixelSize(resourceId);
        else {
            return 0;
        }
    }

    /**
     * 获取屏幕高度
     * @return
     */
    public static int getScreenHeight( Context context){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }
    /**
     * 获取屏幕宽度
     * @return
     */
    public static int getScreenWidth(Context context){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }
}
