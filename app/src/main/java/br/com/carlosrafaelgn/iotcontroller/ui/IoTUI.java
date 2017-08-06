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

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;

import br.com.carlosrafaelgn.iotcontroller.R;
import br.com.carlosrafaelgn.iotdcp.IoTDevice;
import br.com.carlosrafaelgn.iotdcp.IoTInterface;
import br.com.carlosrafaelgn.iotdcp.IoTInterfaceOnOff;
import br.com.carlosrafaelgn.iotdcp.IoTInterfaceOnOffSimple;
import br.com.carlosrafaelgn.iotdcp.IoTInterfaceOpenClose;
import br.com.carlosrafaelgn.iotdcp.IoTInterfaceOpenCloseStop;
import br.com.carlosrafaelgn.iotdcp.IoTMessage;
import br.com.carlosrafaelgn.iotdcp.IoTProperty;

public final class IoTUI {
	public static View createViewForDevice(Activity activity, ViewGroup parent, IoTDevice device) {
		final LayoutInflater layoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final CardView cardView = new CardView(activity);

		layoutInflater.inflate(R.layout.device_container, cardView, true);
		final DeviceContainer container = (DeviceContainer)cardView.getChildAt(0);
		((AppCompatTextView)container.findViewById(R.id.txtDeviceName)).setText(device.name);
		container.setDevice(device);

		for (int i = 0; i < device.ioTInterfaceCount(); i++)
			createViewForInterface(activity, layoutInflater, container, device.ioTInterface(i));

		final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.bottomMargin = (int)activity.getResources().getDimension(R.dimen.card_margin);
		parent.addView(cardView, params);

		return cardView;
	}

	private static void createViewForInterface(Activity activity, LayoutInflater layoutInflater, DeviceContainer container, IoTInterface ioTInterface) {
		switch (ioTInterface.type) {
		case IoTInterface.TypeSensor:
			// TODO: implement this
			break;
		case IoTInterface.TypeOnOff:
			createViewForInterfaceOnOff(activity, layoutInflater, container, ioTInterface);
			break;
		case IoTInterface.TypeOnOffSimple:
			createViewForInterfaceOnOffSimple(activity, layoutInflater, container, ioTInterface);
			break;
		case IoTInterface.TypeOpenClose:
			// TODO: implement this
			break;
		case IoTInterface.TypeOpenCloseStop:
			// TODO: implement this
			break;
		}
	}

	private static void createViewForInterfaceOnOff(Activity activity, LayoutInflater layoutInflater, DeviceContainer container, IoTInterface ioTInterface) {
		final IoTInterfaceOnOff onOff = (IoTInterfaceOnOff)ioTInterface;
		final int state = onOff.state.getValueByte();

		layoutInflater.inflate(R.layout.interface_onoff, container, true);

		final LinearLayout parent = (LinearLayout)container.getChildAt(container.getChildCount() - 1);
		((AppCompatTextView)parent.findViewById(R.id.txtTitle)).setText(ioTInterface.name);

		final View.OnClickListener clickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.btnOn:
				case R.id.btnOnOn:
					onOff.executeOn();
					break;
				case R.id.btnOff:
				case R.id.btnOffOff:
					onOff.executeOff();
					break;
				}
			}
		};

		AppCompatButton btn = ((AppCompatButton)parent.findViewById(R.id.btnOn));
		btn.setOnClickListener(clickListener);
		btn.setVisibility(state == IoTInterfaceOnOff.StateOn ? View.GONE : View.VISIBLE);
		btn = ((AppCompatButton)parent.findViewById(R.id.btnOnOn));
		btn.setOnClickListener(clickListener);
		btn.setVisibility(state == IoTInterfaceOnOff.StateOn ? View.VISIBLE : View.GONE);

		btn = ((AppCompatButton)parent.findViewById(R.id.btnOff));
		btn.setOnClickListener(clickListener);
		btn.setVisibility(state == IoTInterfaceOnOff.StateOff ? View.GONE : View.VISIBLE);
		btn = ((AppCompatButton)parent.findViewById(R.id.btnOffOff));
		btn.setOnClickListener(clickListener);
		btn.setVisibility(state == IoTInterfaceOnOff.StateOff ? View.VISIBLE : View.GONE);

		createViewForProperties(activity, layoutInflater, parent, ioTInterface, 1);
	}

	private static void createViewForInterfaceOnOffSimple(Activity activity, LayoutInflater layoutInflater, DeviceContainer container, IoTInterface ioTInterface) {
		final IoTInterfaceOnOffSimple onOffSimple = (IoTInterfaceOnOffSimple)ioTInterface;
		final int state = onOffSimple.state.getValueByte();

		layoutInflater.inflate(R.layout.interface_onoffsimple, container, true);

		final LinearLayout parent = (LinearLayout)container.getChildAt(container.getChildCount() - 1);
		((AppCompatTextView)parent.findViewById(R.id.txtTitle)).setText(ioTInterface.name);

		final View.OnClickListener clickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onOffSimple.executeOnOff();
			}
		};

		AppCompatButton btn = ((AppCompatButton)parent.findViewById(R.id.btnOnOffOff));
		btn.setOnClickListener(clickListener);
		btn.setVisibility(state == IoTInterfaceOnOff.StateOn ? View.GONE : View.VISIBLE);

		btn = ((AppCompatButton)parent.findViewById(R.id.btnOnOffOn));
		btn.setOnClickListener(clickListener);
		btn.setVisibility(state == IoTInterfaceOnOff.StateOn ? View.VISIBLE : View.GONE);

		createViewForProperties(activity, layoutInflater, parent, ioTInterface, 1);
	}

	private static void createViewForProperties(Activity activity, LayoutInflater layoutInflater, LinearLayout parent, IoTInterface ioTInterface, int firstProperty) {
		boolean needsDivider = (firstProperty > 0);

		for (; firstProperty < ioTInterface.propertyCount(); firstProperty++) {
			final IoTProperty property = ioTInterface.property(firstProperty);
			switch (property.unitNum) {
			case IoTProperty.UnitRGB:
				if (needsDivider)
					createDivider(layoutInflater, parent);
				else
					needsDivider = true;
				createViewForRGBProperty(activity, layoutInflater, parent, property);
				break;

			case IoTProperty.UnitEnum:
				if (needsDivider)
					createDivider(layoutInflater, parent);
				else
					needsDivider = true;
				createViewForEnumProperty(activity, layoutInflater, parent, property);
				break;

			// TODO: implement other property types
			}
		}
	}

	private static void createDivider(LayoutInflater layoutInflater, LinearLayout parent) {
		layoutInflater.inflate(R.layout.divider, parent, true);
	}

	private static void createViewForRGBProperty(final Activity activity, LayoutInflater layoutInflater, LinearLayout parent, final IoTProperty property) {
		layoutInflater.inflate(R.layout.property_rgb, parent, true);

		final RelativeLayout panel = (RelativeLayout)parent.getChildAt(parent.getChildCount() - 1);
		((AppCompatTextView)panel.findViewById(R.id.txtTitleColor)).setText(property.name);

		final AppCompatButton btn = ((AppCompatButton)panel.findViewById(R.id.btnChange));
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ColorPickerView.showDialog(activity, property.getValueRGB(), null, false, new ColorPickerView.OnColorPickerViewListener() {
					@Override
					public void onColorPicked(ColorPickerView colorPickerView, View parentView, int color) {
						property.setValueRGB(color);
					}
				});
			}
		});
	}

	private static void createViewForEnumProperty(Activity activity, LayoutInflater layoutInflater, LinearLayout parent, final IoTProperty property) {
		layoutInflater.inflate(R.layout.property_enum, parent, true);

		final RelativeLayout panel = (RelativeLayout)parent.getChildAt(parent.getChildCount() - 1);
		((AppCompatTextView)panel.findViewById(R.id.txtTitleEnum)).setText(property.name);

		final AppCompatSpinner spinner = ((AppCompatSpinner)panel.findViewById(R.id.spinnerEnum));
		ArrayAdapter<IoTProperty.Enum> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, property.getEnums());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			private boolean firstTime = true;

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (firstTime) {
					// Spinner calls onItemSelected() just by
					// calling setOnItemSelectedListener()
					firstTime = false;
					return;
				}
				final List<IoTProperty.Enum> enums = property.getEnums();
				if (position < 0 || position >= enums.size())
					return;
				final IoTProperty.Enum e = enums.get(position);
				// This is just to avoid setting the property again when
				// calling spinner.setSelection()
				if (!e.equals(property.getValueEnum()))
					property.setValueEnum(e);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	public static void updateViewOnExecute(View view, IoTDevice device, int responseCode, int interfaceIndex, int command) {
		final IoTInterface ioTInterface;

		if (view == null ||
			responseCode != IoTMessage.ResponseOK ||
			interfaceIndex < 0 ||
			interfaceIndex >= device.ioTInterfaceCount() ||
			(ioTInterface = device.ioTInterface(interfaceIndex)) == null)
			return;

		switch (ioTInterface.type) {
		// IoTInterface.TypeSensor has no predefined commands
		//case IoTInterface.TypeSensor:
		//	break;
		case IoTInterface.TypeOnOff:
			switch (command) {
			case IoTInterfaceOnOff.CommandOff:
			case IoTInterfaceOnOff.CommandOn:
				updateViewOnOff(view, ((IoTInterfaceOnOff)ioTInterface).state);
			}
			break;
		case IoTInterface.TypeOnOffSimple:
			switch (command) {
			case IoTInterfaceOnOffSimple.CommandOnOff:
				updateViewOnOffSimple(view, ((IoTInterfaceOnOffSimple)ioTInterface).state);
			}
			break;
		case IoTInterface.TypeOpenClose:
			switch (command) {
			case IoTInterfaceOpenClose.CommandClose:
			case IoTInterfaceOpenClose.CommandOpen:
				updateViewOpenClose(view, ((IoTInterfaceOpenClose)ioTInterface).state);
			}
			break;
		case IoTInterface.TypeOpenCloseStop:
			switch (command) {
			case IoTInterfaceOpenCloseStop.CommandClose:
			case IoTInterfaceOpenCloseStop.CommandOpen:
			case IoTInterfaceOpenCloseStop.CommandStop:
				updateViewOpenCloseStop(view, ((IoTInterfaceOpenCloseStop)ioTInterface).state);
			}
			break;
		}
	}

	public static void updateViewOnPropertyChange(View view, IoTDevice device, int responseCode, int interfaceIndex, int propertyIndex) {
		final IoTInterface ioTInterface;

		if (view == null ||
			responseCode != IoTMessage.ResponseOK ||
			interfaceIndex < 0 ||
			interfaceIndex >= device.ioTInterfaceCount() ||
			(ioTInterface = device.ioTInterface(interfaceIndex)) == null ||
			propertyIndex < 0 ||
			propertyIndex >= ioTInterface.propertyCount())
			return;

		switch (ioTInterface.type) {
		case IoTInterface.TypeSensor:
			updateViewCommon(view, ioTInterface.property(propertyIndex));
			break;
		case IoTInterface.TypeOnOff:
			if (propertyIndex == IoTInterfaceOnOff.PropertyState)
				updateViewOnOff(view, ioTInterface.property(propertyIndex));
			else
				updateViewCommon(view, ioTInterface.property(propertyIndex));
			break;
		case IoTInterface.TypeOnOffSimple:
			if (propertyIndex == IoTInterfaceOnOffSimple.PropertyState)
				updateViewOnOffSimple(view, ioTInterface.property(propertyIndex));
			else
				updateViewCommon(view, ioTInterface.property(propertyIndex));
			break;
		case IoTInterface.TypeOpenClose:
			if (propertyIndex == IoTInterfaceOpenClose.PropertyState)
				updateViewOpenClose(view, ioTInterface.property(propertyIndex));
			else
				updateViewCommon(view, ioTInterface.property(propertyIndex));
			break;
		case IoTInterface.TypeOpenCloseStop:
			if (propertyIndex == IoTInterfaceOpenCloseStop.PropertyState)
				updateViewOpenCloseStop(view, ioTInterface.property(propertyIndex));
			else
				updateViewCommon(view, ioTInterface.property(propertyIndex));
			break;
		}
	}

	private static void updateViewOnOff(View parent, IoTProperty property) {
		final int state = property.getValueByte();

		View btn = parent.findViewById(R.id.btnOn);
		if (btn != null) btn.setVisibility(state == IoTInterfaceOnOff.StateOn ? View.GONE : View.VISIBLE);

		btn = parent.findViewById(R.id.btnOnOn);
		if (btn != null) btn.setVisibility(state == IoTInterfaceOnOff.StateOn ? View.VISIBLE : View.GONE);

		btn = parent.findViewById(R.id.btnOff);
		if (btn != null) btn.setVisibility(state == IoTInterfaceOnOff.StateOff ? View.GONE : View.VISIBLE);

		btn = parent.findViewById(R.id.btnOffOff);
		if (btn != null) btn.setVisibility(state == IoTInterfaceOnOff.StateOff ? View.VISIBLE : View.GONE);
	}

	private static void updateViewOnOffSimple(View parent, IoTProperty property) {
		final int state = property.getValueByte();

		View btn = parent.findViewById(R.id.btnOnOffOff);
		if (btn != null) btn.setVisibility(state == IoTInterfaceOnOff.StateOn ? View.GONE : View.VISIBLE);

		btn = parent.findViewById(R.id.btnOnOffOn);
		if (btn != null) btn.setVisibility(state == IoTInterfaceOnOff.StateOn ? View.VISIBLE : View.GONE);
	}

	private static void updateViewOpenClose(View parent, IoTProperty property) {
		// TODO: implement this
	}

	private static void updateViewOpenCloseStop(View parent, IoTProperty property) {
		// TODO: implement this
	}

	private static void updateViewCommon(View parent, IoTProperty property) {
		switch (property.unitNum) {
		case IoTProperty.UnitRGB:
			final AppCompatTextView txtColor = (AppCompatTextView)parent.findViewById(R.id.txtColor);
			if (txtColor != null) txtColor.setBackgroundColor(property.getValueRGB());
			break;

		case IoTProperty.UnitEnum:
			final AppCompatSpinner spinner = (AppCompatSpinner)parent.findViewById(R.id.spinnerEnum);
			if (spinner != null) spinner.setSelection(property.getEnums().indexOf(property.getValueEnum()));
			break;

		// TODO: implement other property types
		}
	}
}
