package com.android.broadcast;

import java.lang.reflect.Method;
import java.util.Random;

import com.android.BluetoothChat.BluetoothChatService;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetooth;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class PairingRequest extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// The auto-pairing is done only if the other device is in listen mode
		if (BluetoothChatService.autoPairing == true)
		{
			String action = intent.getAction();
			Random randomGenerator = new Random();

			if (intent.getAction().contains("PAIRING_REQUEST")) {
				try {
					Thread.sleep(0);
					//BluetoothDevice localBluetoothDevice = (BluetoothDevice)intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

					int randomInt = randomGenerator.nextInt(999999);
					byte[] pinBytes = convertPinToBytes(""+randomInt);

					IBluetooth ib = getIBluetooth();


					//String addr = DeviceListActivity.ib.getAddress();
					String addr;
					//addr = DeviceListActivity.ib.getAddress();
					addr =  device.getAddress();
					System.out.println("Addr: "+addr);
					System.out.println("Pin: "+randomInt);
					System.out.println("PinBytes: "+pinBytes);
					System.out.println("ib: "+ib);

					ib.setPin(addr, pinBytes);

				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static IBluetooth getIBluetooth() {
		IBluetooth ibt = null;

		try {

			Class c2 = Class.forName("android.os.ServiceManager");

			Method m2 = c2.getDeclaredMethod("getService",String.class);
			IBinder b = (IBinder) m2.invoke(null, "bluetooth");

			Class c3 = Class.forName("android.bluetooth.IBluetooth");

			Class[] s2 = c3.getDeclaredClasses();

			Class c = s2[0];
			Method m = c.getDeclaredMethod("asInterface",IBinder.class);
			m.setAccessible(true);
			ibt = (IBluetooth) m.invoke(null, b);


		} catch (Exception e) {
			Log.e("flowlab", "Erroraco!!! " + e.getMessage());
		}

		return ibt;
	}

	public static byte[] convertPinToBytes(String pin) {
		if (pin == null) {
			return null;
		}
		byte[] pinBytes;
		try {
			pinBytes = pin.getBytes("UTF8");
		} catch (Exception uee) {
			//	Log.e(TAG, "UTF8 not supported?!?");  // this should not happen
			return null;
		}
		if (pinBytes.length <= 0 || pinBytes.length > 16) {
			return null;
		}
		return pinBytes;
	}

}
