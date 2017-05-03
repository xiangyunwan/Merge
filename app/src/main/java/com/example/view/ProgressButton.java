package com.example.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class ProgressButton extends ImageButton {

	protected Paint mPaint = new Paint();
	protected int mReachedBarColor = 0xFF1BBC9B;
	protected int mUnReachedBarColor = 0XFFFFC607;

	protected int progressBarHeight = 5;// 描边
	private int mRadius = 0;// 半径
	private int progress = 0; // 进度
	private int max = 0;// 进度最大值

	public ProgressButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ProgressButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mPaint.setStyle(Style.STROKE);
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setStrokeCap(Cap.ROUND);
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
		invalidate();
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getMax() {
		return max;
	}

	public void setProgressBarHeight(int progressBarHeight) {
		this.progressBarHeight = progressBarHeight;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int realWidth = Math.min(width, height);
		// progressBarHeight = realWidth / 20;
		mRadius = (realWidth - getPaddingLeft() - getPaddingRight()) / 2;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (getProgress() > 0) {
			mPaint.setStyle(Style.STROKE);
			mPaint.setColor(mUnReachedBarColor);
			mPaint.setStrokeWidth(progressBarHeight);
			canvas.drawCircle(mRadius, mRadius, mRadius - progressBarHeight,
					mPaint);
			// draw reached bar
			mPaint.setColor(Color.rgb(255, 40, 0));
			mPaint.setStrokeWidth(progressBarHeight);
			float sweepAngle1 = (getMax() - getProgress()) * 1.0f / getMax()
					* 360;
			canvas.drawArc(new RectF(progressBarHeight, progressBarHeight,
							mRadius * 2 - progressBarHeight, mRadius * 2
							- progressBarHeight), -90, sweepAngle1, false,
					mPaint);
			canvas.restore();
		}
	}

}
