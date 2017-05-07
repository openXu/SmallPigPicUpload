package com.openxu.pigpic.util;

import android.content.Context;
import android.graphics.Paint;

public class DensityUtil {
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 将px值转换为sp值，保证文字大小不变
	 *
	 * @param pxValue
	 * @return
	 */
	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * 将sp值转换为px值，保证文字大小不变
	 *
	 * @param spValue
	 * @return
	 */
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}


	/**
	 * @param paint
	 * @param str
	 * @return 返回指定笔和指定字符串的长度
	 */
	public static float getFontlength(Paint paint, String str) {
		return paint.measureText(str);
	}
	/**
	 * @return 返回指定笔的文字高度
	 */
	public static float getFontHeight(Paint paint)  {
		Paint.FontMetrics fm = paint.getFontMetrics();
		return fm.descent - fm.ascent;
	}
	/**
	 * @return 返回指定笔离文字顶部的基准距离
	 * @add yujiangtao 16/8/5
	 */
	public static float getFontLeading(Paint paint)  {
		Paint.FontMetrics fm = paint.getFontMetrics();
		return fm.leading- fm.ascent;
	}

}
