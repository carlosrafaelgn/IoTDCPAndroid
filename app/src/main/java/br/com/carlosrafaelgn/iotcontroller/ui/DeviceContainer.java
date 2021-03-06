//
// IoTDCPJava is distributed under the FreeBSD License
//
// Copyright (c) 2017, Carlos Rafael Gimenes das Neves
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice, this
//   list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above copyright notice,
//   this list of conditions and the following disclaimer in the documentation
//   and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// https://github.com/carlosrafaelgn/IoTDCPJava
//
package br.com.carlosrafaelgn.iotcontroller.ui;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import br.com.carlosrafaelgn.iotcontroller.R;
import br.com.carlosrafaelgn.iotdcp.IoTDevice;

public class DeviceContainer extends LinearLayout {
	private IoTDevice device;
	private AppCompatTextView txtPassword;

	public DeviceContainer(Context context) {
		super(context);
	}

	public DeviceContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DeviceContainer(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	void initialize(IoTDevice device, final IoTUI.PasswordClickListener listener) {
		this.device = device;

		if (!device.isPasswordProtected())
			return;

		txtPassword = findViewById(R.id.txtPassword);

		final AppCompatButton btnPassword = findViewById(R.id.btnPassword);
		btnPassword.setVisibility(VISIBLE);
		btnPassword.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null)
					listener.onPasswordClick(DeviceContainer.this);
			}
		});
	}

	public void updateDeviceName() {
		final AppCompatTextView txtDeviceName = findViewById(R.id.txtDeviceName);
		if (txtDeviceName != null)
			txtDeviceName.setText(device.name);
	}

	public void showWrongPasswordMessage(boolean show) {
		if (txtPassword != null)
			txtPassword.setVisibility(show ? VISIBLE : GONE);
	}

	public boolean isWrongPassword() {
		return (txtPassword != null && txtPassword.getVisibility() == VISIBLE);
	}

	public IoTDevice getDevice() {
		return device;
	}
}
