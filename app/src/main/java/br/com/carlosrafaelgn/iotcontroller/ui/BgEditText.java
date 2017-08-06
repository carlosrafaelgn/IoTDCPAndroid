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
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.AppCompatEditText;
import android.text.Layout;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewDebug.ExportedProperty;
import android.widget.TextView;

import java.lang.reflect.Field;

public final class BgEditText extends AppCompatEditText {
	private int state, colorNormal, colorFocused, extraTopPaddingForLastWidth, lastMeasuredWidth, textSize, textBox, textY, textMargin;
	private String contentDescription;
	private int[] contentDescriptionLineEndings;

	public BgEditText(Context context) {
		super(context);
		init();
	}

	public BgEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BgEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		super.setBackgroundResource(0);
		super.setDrawingCacheEnabled(false);
		super.setGravity(Gravity.BOTTOM);
		super.setPadding(0, 0, 0, UI.thickDividerSize << 1);
		setSmallContentDescription(false);
		textMargin = (UI.isLargeScreen ? UI.controlMargin : UI.controlSmallMargin);
	}

	public void setSmallContentDescription(boolean small) {
		if (small) {
			textSize = UI._14sp;
			textBox = UI._14spBox;
			textY = UI._14spYinBox;
		} else {
			textSize = UI._18sp;
			textBox = UI._18spBox;
			textY = UI._18spYinBox;
		}
	}

	@SuppressWarnings("deprecation")
	public void setCursorColor(int color) {
		try {
			//http://stackoverflow.com/a/26543290/3569421
			final Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
			fCursorDrawableRes.setAccessible(true);
			final int mCursorDrawableRes = fCursorDrawableRes.getInt(this);
			final Field fEditor = TextView.class.getDeclaredField("mEditor");
			fEditor.setAccessible(true);
			final Object editor = fEditor.get(this);
			final Class<?> clazz = editor.getClass();
			final Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
			fCursorDrawable.setAccessible(true);
			final Drawable[] drawables = new Drawable[2];
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				drawables[0] = getContext().getDrawable(mCursorDrawableRes);
				drawables[1] = getContext().getDrawable(mCursorDrawableRes);
			} else {
				drawables[0] = getContext().getResources().getDrawable(mCursorDrawableRes);
				drawables[1] = getContext().getResources().getDrawable(mCursorDrawableRes);
			}
			drawables[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
			drawables[1].setColorFilter(color, PorterDuff.Mode.SRC_IN);
			fCursorDrawable.set(editor, drawables);
		} catch (Throwable ex) {
			//just ignore
		}
	}

	public void setColors(int colorNormal, int colorFocused) {
		this.colorNormal = colorNormal;
		this.colorFocused = colorFocused;
	}

	@Override
	public int getPaddingLeft() {
		return 0;
	}

	@Override
	public int getPaddingTop() {
		return 0;
	}

	@Override
	public int getPaddingRight() {
		return 0;
	}

	@Override
	public int getPaddingBottom() {
		return 0;
	}

	@Override
	public void setPadding(int left, int top, int right, int bottom) {
	}

	@Override
	public void setContentDescription(CharSequence contentDescription) {
		this.contentDescription = ((contentDescription == null || contentDescription.length() == 0)? null : contentDescription.toString());
		extraTopPaddingForLastWidth = 0;
		lastMeasuredWidth = 0;
		contentDescriptionLineEndings = null;
		super.setContentDescription(this.contentDescription);
		requestLayout();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void setBackground(Drawable background) {
		super.setBackground(null);
	}

	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void setBackgroundDrawable(Drawable background) {
		super.setBackgroundDrawable(null);
	}

	@Override
	public void setBackgroundResource(int resid) {
		super.setBackgroundResource(0);
	}

	@Override
	public void setBackgroundColor(int color) {
		super.setBackgroundResource(0);
	}

	@Override
	public Drawable getBackground() {
		return null;
	}

	@Override
	@ExportedProperty(category = "drawing")
	public boolean isOpaque() {
		return false;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		state = (UI.STATE_FOCUSED & UI.handleStateChanges(state, isPressed(), isFocused(), this));
	}

	private int countLines(int width) {
		final StaticLayout layout = new StaticLayout(contentDescription, UI.textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		final int lines = layout.getLineCount();
		contentDescriptionLineEndings = new int[lines];
		for (int i = 0; i < lines; i++)
			contentDescriptionLineEndings[i] = layout.getLineEnd(i);
		return lines;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = getMeasuredWidth();
		int extraTopPadding;

		if (lastMeasuredWidth != width) {
			//add our bottom border + enough room for the description at the top

			extraTopPadding = 0;

			if (contentDescription != null) {
				UI.textPaint.setTextSize(textSize);
				if ((int)UI.textPaint.measureText(contentDescription) <= width) {
					contentDescriptionLineEndings = null;
					extraTopPadding += textBox + textMargin;
				} else {
					extraTopPadding += (textBox * countLines(width)) + textMargin;
				}
			}

			extraTopPaddingForLastWidth = extraTopPadding;
		} else {
			extraTopPadding = extraTopPaddingForLastWidth;
		}

		setMeasuredDimension(width, getMeasuredHeight() + extraTopPadding);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		getDrawingRect(UI.rect);

		if (extraTopPaddingForLastWidth > 0) {
			UI.rect.top += textY;
			if (contentDescriptionLineEndings != null) {
				UI.drawText(canvas, contentDescription, 0, contentDescriptionLineEndings[0], colorFocused, textSize, UI.rect.left, UI.rect.top);
				for (int i = 1; i < contentDescriptionLineEndings.length; i++) {
					UI.rect.top += textBox;
					UI.drawText(canvas, contentDescription, contentDescriptionLineEndings[i - 1], contentDescriptionLineEndings[i], colorFocused, textSize, UI.rect.left, UI.rect.top);
				}
			} else if (contentDescription != null) {
				UI.drawText(canvas, contentDescription, colorFocused, textSize, UI.rect.left, UI.rect.top);
			}
		}

		UI.rect.top = UI.rect.bottom - (state == 0 ? UI.strokeSize : UI.thickDividerSize);
		UI.fillRect(canvas, state == 0 ? colorNormal : colorFocused);

		super.onDraw(canvas);
	}

	@Override
	protected void onDetachedFromWindow() {
		contentDescription = null;
		super.onDetachedFromWindow();
	}
}
