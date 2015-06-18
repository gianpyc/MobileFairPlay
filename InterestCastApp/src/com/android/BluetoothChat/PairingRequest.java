package com.android.BluetoothChat;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PairingRequest extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent){
	if (intent.getAction().equals("ACTION_PAIRING_REQUEST")) {
	        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

//	        byte[] pinBytes = BluetoothDevice.convertPinToBytes("1234");
//	        device.setPin(pinBytes);
	    }
	 }
	}
