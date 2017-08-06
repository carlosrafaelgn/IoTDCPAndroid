//
// FPlayAndroid is distributed under the FreeBSD License
//
// Copyright (c) 2013-2014, Carlos Rafael Gimenes das Neves
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those
// of the authors and should not be interpreted as representing official policies,
// either expressed or implied, of the FreeBSD Project.
//
// https://github.com/carlosrafaelgn/FPlayAndroid
//
package br.com.carlosrafaelgn.iotcontroller.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import br.com.carlosrafaelgn.iotcontroller.R;

public final class UI {
	static final int STATE_PRESSED = 1;
	static final int STATE_FOCUSED = 2;

	private static final class DisplayInfo {
		int usableScreenWidth, usableScreenHeight, screenWidth, screenHeight;
		boolean isLandscape, isLargeScreen, isLowDpiScreen;
		DisplayMetrics displayMetrics;

		private void initializeScreenDimensions(Display display, DisplayMetrics outDisplayMetrics) {
			display.getMetrics(outDisplayMetrics);
			screenWidth = outDisplayMetrics.widthPixels;
			screenHeight = outDisplayMetrics.heightPixels;
			usableScreenWidth = screenWidth;
			usableScreenHeight = screenHeight;
		}

		@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		private void initializeScreenDimensions14(Display display, DisplayMetrics outDisplayMetrics) {
			try {
				screenWidth = (Integer)Display.class.getMethod("getRawWidth").invoke(display);
				screenHeight = (Integer)Display.class.getMethod("getRawHeight").invoke(display);
			} catch (Throwable ex) {
				initializeScreenDimensions(display, outDisplayMetrics);
				return;
			}
			display.getMetrics(outDisplayMetrics);
			usableScreenWidth = outDisplayMetrics.widthPixels;
			usableScreenHeight = outDisplayMetrics.heightPixels;
		}

		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		private void initializeScreenDimensions17(Display display, DisplayMetrics outDisplayMetrics) {
			display.getMetrics(outDisplayMetrics);
			usableScreenWidth = outDisplayMetrics.widthPixels;
			usableScreenHeight = outDisplayMetrics.heightPixels;
			display.getRealMetrics(outDisplayMetrics);
			screenWidth = outDisplayMetrics.widthPixels;
			screenHeight = outDisplayMetrics.heightPixels;
		}

		void getInfo(Context context) {
			final Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			displayMetrics = new DisplayMetrics();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
				initializeScreenDimensions17(display, displayMetrics);
			else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				initializeScreenDimensions14(display, displayMetrics);
			else
				initializeScreenDimensions(display, displayMetrics);
			//improved detection for tablets, based on:
			//http://developer.android.com/guide/practices/screens_support.html#DeclaringTabletLayouts
			//(There is also the solution at http://stackoverflow.com/questions/11330363/how-to-detect-device-is-android-phone-or-android-tablet
			//but the former link says it is deprecated...)
			//*** I decided to treat screens >= 500dp as large screens because there are
			//lots of 7" phones/tablets with resolutions starting at around 533dp ***
			final int _500dp = (int)((500.0f * displayMetrics.density) + 0.5f);
			isLandscape = (screenWidth >= screenHeight);
			isLargeScreen = ((screenWidth >= _500dp) && (screenHeight >= _500dp));
			isLowDpiScreen = (displayMetrics.densityDpi < 160);
		}
	}

	static final Rect rect = new Rect();
	static boolean isLandscape, isLargeScreen, isLowDpiScreen;
	static int _1dp, _4dp, _22sp, _18sp, _14sp, _22spBox, defaultCheckIconSize, _18spBox, _14spBox, _22spYinBox, _18spYinBox, _14spYinBox, _LargeItemsp, _LargeItemspBox, _LargeItemspYinBox, controlLargeMargin, controlMargin, controlSmallMargin, controlXtraSmallMargin, dialogMargin, dialogDropDownVerticalMargin,
		strokeSize, thickDividerSize, defaultControlContentsSize, defaultControlSize, usableScreenWidth, usableScreenHeight, screenWidth, screenHeight, densityDpi;

	static float density, scaledDensity;

	static final Paint fillPaint;
	static final TextPaint textPaint;

	static {
		fillPaint = new Paint();
		fillPaint.setDither(false);
		fillPaint.setAntiAlias(false);
		fillPaint.setStyle(Paint.Style.FILL);
		textPaint = new TextPaint();
		textPaint.setDither(false);
		textPaint.setAntiAlias(true);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setTypeface(Typeface.DEFAULT);
		textPaint.setTextAlign(Paint.Align.LEFT);
		textPaint.setColor(0xff000000);
		textPaint.measureText("IoT");
	}

	@SuppressWarnings("deprecation")
	public static void initialize(Activity activityContext) {
		final DisplayInfo info = new DisplayInfo();
		info.getInfo(activityContext);
		density = info.displayMetrics.density;
		densityDpi = info.displayMetrics.densityDpi;
		scaledDensity = info.displayMetrics.scaledDensity;
		screenWidth = info.screenWidth;
		screenHeight = info.screenHeight;
		usableScreenWidth = info.usableScreenWidth;
		usableScreenHeight = info.usableScreenHeight;
		isLargeScreen = info.isLargeScreen;
		isLandscape = info.isLandscape;
		isLowDpiScreen = info.isLowDpiScreen;

		//apparently, the display metrics returned by Resources.getDisplayMetrics()
		//is not the same as the one returned by Display.getMetrics()/getRealMetrics()
		final float sd = activityContext.getResources().getDisplayMetrics().scaledDensity;
		if (sd > 0)
			scaledDensity = sd;
		else if (scaledDensity <= 0)
			scaledDensity = 1.0f;

		_1dp = dpToPxI(1);
		strokeSize = (_1dp + 1) >> 1;
		thickDividerSize = dpToPxI(1.5f);
		if (thickDividerSize < 2) thickDividerSize = 2;
		if (thickDividerSize <= _1dp) thickDividerSize = _1dp + 1;
		_4dp = dpToPxI(4);
		_22sp = spToPxI(22);
		_18sp = spToPxI(18);
		_14sp = spToPxI(14);
		controlLargeMargin = dpToPxI(16);
		controlMargin = controlLargeMargin >> 1;
		controlSmallMargin = controlLargeMargin >> 2;
		controlXtraSmallMargin = controlLargeMargin >> 3;
		dialogMargin = controlLargeMargin;
		dialogDropDownVerticalMargin = (dialogMargin * 3) >> 1;
		defaultControlContentsSize = dpToPxI(32);
		defaultControlSize = defaultControlContentsSize + (controlMargin << 1);
		defaultCheckIconSize = dpToPxI(24); //both descent and ascent of iconsTypeface are 0!

		textPaint.setTypeface(Typeface.DEFAULT);
		final Paint.FontMetrics fm = textPaint.getFontMetrics();
		textPaint.setTextSize(_22sp);
		textPaint.getFontMetrics(fm);
		_22spBox = (int)(fm.descent - fm.ascent + 0.5f);
		_22spYinBox = _22spBox - (int)(fm.descent);
		textPaint.setTextSize(_18sp);
		textPaint.getFontMetrics(fm);
		_18spBox = (int)(fm.descent - fm.ascent + 0.5f);
		_18spYinBox = _18spBox - (int)(fm.descent);
		textPaint.setTextSize(_14sp);
		textPaint.getFontMetrics(fm);
		_14spBox = (int)(fm.descent - fm.ascent + 0.5f);
		_14spYinBox = _14spBox - (int)(fm.descent);
		if (isLargeScreen) {
			_LargeItemsp = _22sp;
			_LargeItemspBox = _22spBox;
			_LargeItemspYinBox = _22spYinBox;
		} else {
			_LargeItemsp = _18sp;
			_LargeItemspBox = _18spBox;
			_LargeItemspYinBox = _18spYinBox;
		}
	}

	static int dpToPxI(float dp) {
		return (int)((dp * density) + 0.5f);
	}

	static int spToPxI(float sp) {
		return (int)((sp * scaledDensity) + 0.5f);
	}

	static int measureText(String text, int size) {
		if (text == null)
			return 0;
		textPaint.setTextSize(size);
		return (int)(textPaint.measureText(text) + 0.5f);
	}

	static void drawText(Canvas canvas, String text, int color, int size, int x, int y) {
		textPaint.setColor(color);
		textPaint.setTextSize(size);
		canvas.drawText(text, x, y, textPaint);
	}

	static void drawText(Canvas canvas, String text, int start, int end, int color, int size, int x, int y) {
		textPaint.setColor(color);
		textPaint.setTextSize(size);
		canvas.drawText(text, start, end, x, y, textPaint);
	}

	static void fillRect(Canvas canvas, int fillColor) {
		fillPaint.setColor(fillColor);
		canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, fillPaint);
	}

	static void strokeRect(Canvas canvas, int strokeColor, int thickness) {
		fillPaint.setColor(strokeColor);
		final int l = rect.left, t = rect.top, r = rect.right, b = rect.bottom;
		canvas.drawRect(l, t, r, t + thickness, fillPaint);
		canvas.drawRect(l, b - thickness, r, b, fillPaint);
		canvas.drawRect(l, t + thickness, l + thickness, b - thickness, fillPaint);
		canvas.drawRect(r - thickness, t + thickness, r, b - thickness, fillPaint);
	}

	static int handleStateChanges(int state, boolean pressed, boolean focused, View view) {
		boolean r = false;
		final boolean op = ((state & STATE_PRESSED) != 0), of = ((state & STATE_FOCUSED) != 0);
		if (op != pressed) {
			if (pressed)
				state |= STATE_PRESSED;
			else
				state &= ~STATE_PRESSED;
			r = true;
		}
		if (of != focused) {
			if (focused)
				state |= STATE_FOCUSED;
			else
				state &= ~STATE_FOCUSED;
			r = true;
		}
		if (r)
			view.invalidate();
		return state;
	}

	@SuppressWarnings("deprecation")
	public static BgEditText createDialogEditText(Context context, int id, CharSequence text, CharSequence contentDescription, int inputType) {
		final BgEditText editText = new BgEditText(context);
		if (id != 0)
			editText.setId(id);
		final Resources resources = context.getResources();
		final int colorAccent = resources.getColor(R.color.colorAccent);
		final int colorDivider = resources.getColor(R.color.colorDivider);
		final int colorBgText = resources.getColor(R.color.colorBgText);
		editText.setSingleLine((inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) == 0);
		editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, _18sp);
		editText.setTextColor(colorBgText);
		editText.setCursorColor(colorAccent);
		editText.setColors(colorDivider, colorAccent);
		editText.setContentDescription(contentDescription);
		editText.setInputType(inputType);
		if (text != null)
			editText.setText(text);
		return editText;
	}

	public static AlertDialog.Builder createDialogBuilder(Context context) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.BgDialog);
		builder.setCancelable(true);
		return builder;
	}

	public static AlertDialog prepareDialogAndShow(final AlertDialog dialog) {
		dialog.setCanceledOnTouchOutside(true);
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onShow(DialogInterface d) {
				if (dialog.getWindow() != null)
					dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			}
		});
		dialog.show();
		return dialog;
	}

	public static AlertDialog prepareDialogAndShow(final AlertDialog dialog, final View.OnClickListener positiveListener, final View.OnClickListener negativeListener, final View.OnClickListener neutralListener) {
		dialog.setCanceledOnTouchOutside(true);
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onShow(DialogInterface d) {
				if (dialog.getWindow() != null)
					dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
				Button btn;
				if (positiveListener != null && (btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)) != null)
					btn.setOnClickListener(positiveListener);
				if (negativeListener != null && (btn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)) != null)
					btn.setOnClickListener(negativeListener);
				if (neutralListener != null && (btn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)) != null)
					btn.setOnClickListener(neutralListener);
			}
		});
		dialog.show();
		return dialog;
	}
}
