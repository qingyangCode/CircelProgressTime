package com.yq.circlepress;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 仿iphone带进度的进度条，线程安全的View，可直接在线程中更新进度
 * @author Yangqing
 *
 */
public class RoundProgressBar extends View {
	/**
	 * 画笔对象的引用
	 */
	private final Paint paint;

	/**
	 * 圆环的颜色
	 */
	private int roundColor;

	/**
	 * 圆环进度的颜色
	 */
	private int roundProgressColor;

	/**
	 * 中间进度百分比的字符串的颜色
	 */
	private int textColor;

	/**
	 * 中间进度百分比的字符串的字体
	 */
	private float textSize;

	/**
	 * 圆环的宽度
	 */
	private float roundWidth;

	/**
	 * 圆环进度条的宽度
	 */
	private final float roundProgressWidth;

	/**
	 * 最大进度
	 */
	private long max;

	/**
	 * 当前进度
	 */
	private long progress;
	/**
	 * 是否显示中间的进度
	 */
	private final boolean textIsDisplayable;

	/**
	 * 进度的风格，实心或者空心
	 */
	private final int style;

	/***结束时间*/
	private long endTime;

	public static final int STROKE = 0;
	public static final int FILL = 1;

	private boolean mTickerStopped;
	private Handler mHandler;
	private Runnable mTicker;
	public static long distanceTime;

	private long currentTime;
	private boolean first = true;

	public RoundProgressBar(Context context) {
		this(context, null);
	}

	public RoundProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		paint = new Paint();

		TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar);

		// 获取自定义属性和默认值
		roundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundColor, Color.RED);
		roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor, Color.GREEN);
		textColor = mTypedArray.getColor(R.styleable.RoundProgressBar_textColor, Color.GREEN);
		textSize = mTypedArray.getDimension(R.styleable.RoundProgressBar_textSize, 15);
		roundWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 5);
		roundProgressWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundProgressWidth, 5);
		max = mTypedArray.getInteger(R.styleable.RoundProgressBar_max, 100);
		textIsDisplayable = mTypedArray.getBoolean(R.styleable.RoundProgressBar_textIsDisplayable, true);
		style = mTypedArray.getInt(R.styleable.RoundProgressBar_style, 0);

		mTypedArray.recycle();
	}

	/**
	 * Clock end time from now on.
	 * 
	 * @param endTime
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
		startTimer();
	}

	private void startTimer() {
		mTickerStopped = false;
		mHandler = new Handler();
		/**
		 * requests a tick on the next hard-second boundary
		 */
		mTicker = new Runnable() {

			@Override
			public void run() {
				if (mTickerStopped)
					return;

				// 获取控件加载时的系统时间
				currentTime = System.currentTimeMillis();
				distanceTime = endTime - currentTime;
				distanceTime /= 1000;

				// 第一次加载时的差异时间为圆环的最大值
				if (first) {
					first = false;
					setMax(distanceTime);
				}

				long now = SystemClock.uptimeMillis();
				long next = now + (1000 - now % 1000);
				mHandler.postAtTime(mTicker, next);
				// 设置每一秒的系统进程
				if (distanceTime >= 0)
					setProgress(distanceTime);
				else {
					mTickerStopped = true;
					mHandler.removeCallbacks(null);
				}
			}
		};
		mTicker.run();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		/**
		 * 画最外层的大圆环
		 */
		int centre = getWidth() / 2; // 获取圆心的x坐标
		int radius = (int) (centre - (roundProgressWidth == 0 ? roundWidth : roundProgressWidth) / 2); // 圆环的半径
		paint.setColor(roundColor); // 设置圆环的颜色
		paint.setStyle(Paint.Style.STROKE); // 设置空心
		paint.setStrokeWidth(roundWidth); // 设置圆环的宽度
		paint.setAntiAlias(true); // 消除锯齿
		canvas.drawCircle(centre, centre, radius, paint); // 画出圆环

		Log.e("log", centre + "");

		/**
		 * 画进度百分比
		 */
		paint.setStrokeWidth(0);
		paint.setColor(textColor);
		paint.setTextSize(textSize);
		paint.setTypeface(Typeface.DEFAULT_BOLD); // 设置字体

//		int percent = (int) (((float) progress / (float) max) * 100); // 中间的进度百分比，先转换成float在进行除法运算，不然都为0

		if (textIsDisplayable && style == STROKE) {
			String times = "00:00";
			if (distanceTime == 0 || distanceTime < 0) {
				// 倒计时已结束
			} else {
				times = dealTime(distanceTime);
			}
			float textWidth = paint.measureText(times); // 测量字体宽度，我们需要根据字体的宽度设置在圆环中间
			canvas.drawText(times, centre - textWidth / 2, centre + textSize / 2, paint); // 画出进度百分比
		}

		/**
		 * 画圆弧 ，画圆环的进度
		 */

		// 设置进度是实心还是空心
		paint.setStrokeWidth(roundProgressWidth); // 设置圆环的宽度
		paint.setColor(roundProgressColor); // 设置进度的颜色
		RectF oval = new RectF(centre - radius, centre - radius, centre + radius, centre + radius); // 用于定义的圆弧的形状和大小的界限

		switch (style) {
		case STROKE:
			paint.setStyle(Paint.Style.STROKE);
			canvas.drawArc(oval, -90, 360 * distanceTime / max, false, paint); // 根据进度画圆弧
			break;
		case FILL:
			paint.setStyle(Paint.Style.FILL_AND_STROKE);
			if (progress != 0)
				canvas.drawArc(oval, 0, 360 * progress / max, true, paint); // 根据进度画圆弧
			break;
		}

	}

	public synchronized long getMax() {
		return max;
	}

	/**
	 * deal time string
	 * 
	 * @param time
	 * @return
	 */
	public static String dealTime(long time) {
		StringBuffer returnString = new StringBuffer();
		long minutes = ((time % (24 * 60 * 60)) % (60 * 60)) / 60;
		long second = ((time % (24 * 60 * 60)) % (60 * 60)) % 60;
		String minutesStr = timeStrFormat(String.valueOf(minutes));
		String secondStr = timeStrFormat(String.valueOf(second));
		returnString.append(minutesStr).append(":").append(secondStr);
		return returnString.toString();
	}

	/**
	 * format time
	 * 
	 * @param timeStr
	 * @return
	 */
	private static String timeStrFormat(String timeStr) {
		switch (timeStr.length()) {
		case 1:
			timeStr = "0" + timeStr;
			break;
		}
		return timeStr;
	}

	/**
	 * 设置进度的最大值
	 * @param max
	 */
	public synchronized void setMax(long max) {
		if (max < 0) {
			throw new IllegalArgumentException("max not less than 0");
		}
		this.max = max;
	}

	/**
	 * 获取进度.需要同步
	 * @return
	 */
	public synchronized long getProgress() {
		return progress;
	}

	/**
	 * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
	 * 刷新界面调用postInvalidate()能在非UI线程刷新
	 * @param progress
	 */
	public synchronized void setProgress(long progress) {
		if (progress < 0) {
			throw new IllegalArgumentException("progress not less than 0");
		}
		if (progress > max) {
			progress = max;
		}
		if (progress <= max) {
			this.progress = progress;
			postInvalidate();
		}

	}

	public int getCricleColor() {
		return roundColor;
	}

	public void setCricleColor(int cricleColor) {
		this.roundColor = cricleColor;
	}

	public int getCricleProgressColor() {
		return roundProgressColor;
	}

	public void setCricleProgressColor(int cricleProgressColor) {
		this.roundProgressColor = cricleProgressColor;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public float getTextSize() {
		return textSize;
	}

	public void setTextSize(float textSize) {
		this.textSize = textSize;
	}

	public float getRoundWidth() {
		return roundWidth;
	}

	public void setRoundWidth(float roundWidth) {
		this.roundWidth = roundWidth;
	}

}
