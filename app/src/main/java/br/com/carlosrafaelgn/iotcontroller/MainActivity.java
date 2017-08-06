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
package br.com.carlosrafaelgn.iotcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import br.com.carlosrafaelgn.iotdcp.IoTClient;
import br.com.carlosrafaelgn.iotdcp.IoTDevice;
import br.com.carlosrafaelgn.iotdcp.IoTMessage;
import br.com.carlosrafaelgn.iotcontroller.ui.IoTUI;
import br.com.carlosrafaelgn.iotcontroller.ui.BgProgressBar;
import br.com.carlosrafaelgn.iotcontroller.ui.UI;

public class MainActivity extends AppCompatActivity implements IoTClient.Observer {
	private IoTClient client;
	private LinearLayout panelDevices;
	private BgProgressBar progressBar;
	private boolean alertVisible;
	private HashMap<IoTDevice, View> viewsByDevice;
	private ArrayList<String> pendingErrorAlerts;

	private void updateProgressBar() {
		if (client != null && progressBar != null)
			progressBar.setVisibility(client.isWaitingForResponses() ? View.VISIBLE : View.GONE);
	}

	private boolean isConnected() {
		try {
			final ConnectivityManager mngr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo info = mngr.getActiveNetworkInfo();
			return (info != null && info.isConnected());
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return false;
	}

	private void showNextAlert() {
		alertVisible = false;
		if (pendingErrorAlerts.size() > 0)
			showErrorAlert(pendingErrorAlerts.remove(0));
	}

	private void showErrorAlert(String message) {
		if (alertVisible) {
			pendingErrorAlerts.add(message);
			return;
		}

		alertVisible = true;
		UI.prepareDialogAndShow(UI.createDialogBuilder(this)
			.setMessage(message)
			.setTitle(R.string.oops)
			.setNegativeButton(R.string.ok, null)
			.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					showNextAlert();
				}
			})
			.create());
	}

	private void handleResponse(IoTDevice device, int responseCode) {
		if (client == null || device == null)
			return;
		final String message;
		switch (responseCode) {
		case IoTMessage.ResponseTimeout:
			message = getString(R.string.timeout, device.name);
			break;
		case IoTMessage.ResponseOK:
			// nothing to be done
			return;
		case IoTMessage.ResponseUnknownClient:
			// apparently, we need to perform a handshake (again)
			device.handshake();
			message = getString(R.string.unknown_client, device.name);
			break;
		default:
			final int messageId;
			switch (responseCode) {
			case IoTMessage.ResponseDeviceError:
				messageId = R.string.device_error;
				break;
			case IoTMessage.ResponseUnsupportedMessage:
				messageId = R.string.unsupported_message;
				break;
			case IoTMessage.ResponsePayloadTooLarge:
				messageId = R.string.payload_too_large;
				break;
			case IoTMessage.ResponseInvalidPayload:
				messageId = R.string.invalid_payload;
				break;
			case IoTMessage.ResponseEndOfPacketNotFound:
				messageId = R.string.eop_not_found;
				break;
			case IoTMessage.ResponseWrongPassword:
				messageId = R.string.wrong_password;
				break;
			case IoTMessage.ResponsePasswordReadOnly:
				messageId = R.string.password_read_only;
				break;
			case IoTMessage.ResponseCannotChangePasswordNow:
				messageId = R.string.cannot_change_password_now;
				break;
			case IoTMessage.ResponseInvalidInterface:
				messageId = R.string.invalid_interface;
				break;
			case IoTMessage.ResponseInvalidInterfaceCommand:
				messageId = R.string.invalid_interface_command;
				break;
			case IoTMessage.ResponseInvalidInterfaceProperty:
				messageId = R.string.invalid_interface_property;
				break;
			case IoTMessage.ResponseInterfacePropertyReadOnly:
				messageId = R.string.interface_property_read_only;
				break;
			case IoTMessage.ResponseInterfacePropertyWriteOnly:
				messageId = R.string.interface_property_write_only;
				break;
			case IoTMessage.ResponseInvalidInterfacePropertyValue:
				messageId = R.string.invalid_interface_property_value;
				break;
			case IoTMessage.ResponseTryAgainLater:
				messageId = R.string.try_again_later;
				break;
			default:
				messageId = R.string.unknown_error;
				break;
			}
			message = getString(R.string.device_responded, device.name, getString(messageId));
			break;
		}
		showErrorAlert(message);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		UI.initialize(this);

		final Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		panelDevices = (LinearLayout)findViewById(R.id.panelDevices);
		progressBar = (BgProgressBar)findViewById(R.id.progressBar);

		viewsByDevice = new HashMap<>(16);
		pendingErrorAlerts = new ArrayList<>(16);

		try {
			client = new IoTClient(getApplication());
			client.setObserver(this);
			client.scanDevices();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}

		updateProgressBar();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		panelDevices = null;
		progressBar = null;
		pendingErrorAlerts = null;
		if (viewsByDevice != null) {
			for (IoTDevice device : viewsByDevice.keySet())
				device.goodBye();
			viewsByDevice = null;
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				// Give some time for the messages to be sent
				// (I know this is not a warranty...)
			}
		}
		if (client != null) {
			client.destroy();
			client = null;
		}
		System.exit(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			if (client != null)
				client.scanDevices();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onException(IoTClient client, Throwable ex) {
		if (client != this.client)
			return;
		updateProgressBar();
		if (ex instanceof SocketException)
			showErrorAlert(getText(isConnected() ? R.string.network_error : R.string.no_network_connection).toString());
		else
			showErrorAlert(ex.getMessage());
	}

	@Override
	public void onMessageSent(IoTClient client, IoTDevice device, int message) {
		updateProgressBar();
	}

	@Override
	public void onQueryDevice(IoTClient client, IoTDevice device) {
		if (client != this.client || device == null) {
			// device == null means the client has just stopped scanning for devices
			updateProgressBar();
			return;
		}
		device.setLocalPassword("Teste");
		final View view = IoTUI.createViewForDevice(this, panelDevices, device);
		if (view != null) {
			viewsByDevice.put(device, view);
			device.handshake();
			updateProgressBar();
		}
	}

	@Override
	public void onChangePassword(IoTClient client, IoTDevice device, int responseCode, String password) {
		if (client != this.client)
			return;
		updateProgressBar();
		if (responseCode != IoTMessage.ResponseOK)
			handleResponse(device, responseCode);
		else
			device.setLocalPassword(password);
	}

	@Override
	public void onHandshake(IoTClient client, IoTDevice device, int responseCode) {
		if (client != this.client)
			return;
		if (responseCode != IoTMessage.ResponseOK) {
			updateProgressBar();
			handleResponse(device, responseCode);
		} else {
			device.updateAllProperties();
			updateProgressBar();
		}
	}

	@Override
	public void onPing(IoTClient client, IoTDevice device, int responseCode) {
		handleResponse(device, responseCode);
	}

	@Override
	public void onReset(IoTClient client, IoTDevice device, int responseCode) {
		handleResponse(device, responseCode);
	}

	@Override
	public void onGoodBye(IoTClient client, IoTDevice device, int responseCode) {
		handleResponse(device, responseCode);
	}

	@Override
	public void onExecute(IoTClient client, IoTDevice device, int responseCode, int interfaceIndex, int command) {
		if (client != this.client)
			return;
		updateProgressBar();
		if (responseCode != IoTMessage.ResponseOK) {
			handleResponse(device, responseCode);
			return;
		}
		final View view = viewsByDevice.get(device);
		if (view != null)
			IoTUI.updateViewOnExecute(view, device, responseCode, interfaceIndex, command);
	}

	@Override
	public void onGetProperty(IoTClient client, IoTDevice device, int responseCode, int interfaceIndex, int propertyIndex) {
		if (client != this.client)
			return;
		updateProgressBar();
		if (responseCode != IoTMessage.ResponseOK) {
			handleResponse(device, responseCode);
			return;
		}
		final View view = viewsByDevice.get(device);
		if (view != null)
			IoTUI.updateViewOnPropertyChange(view, device, responseCode, interfaceIndex, propertyIndex);
	}

	@Override
	public void onSetProperty(IoTClient client, IoTDevice device, int responseCode, int interfaceIndex, int propertyIndex) {
		if (client != this.client)
			return;
		updateProgressBar();
		if (responseCode != IoTMessage.ResponseOK) {
			handleResponse(device, responseCode);
			return;
		}
		final View view = viewsByDevice.get(device);
		if (view != null)
			IoTUI.updateViewOnPropertyChange(view, device, responseCode, interfaceIndex, propertyIndex);
	}
}
