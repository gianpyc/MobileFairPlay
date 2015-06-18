package com.android.tab;

import java.io.IOException;
import java.util.Vector;

import com.android.R;
import com.android.BluetoothChat.FairplayMobileApp;
import com.android.BluetoothChat.BluetoothChat;
import com.android.BluetoothChat.BluetoothChatService;
import com.android.file.Read;
import com.android.file.Write;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.os.Handler;

public class TabSfida extends Activity {
	// public static String i;
	public String topicvector = "";

	public static boolean ready = false;

	public String separatore = ";";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView textView = new TextView(this);
		textView.setText("Verifica Amicizia");
		setContentView(R.layout.sfida);

		// startActivity(new Intent(TabSfida.this, BluetoothChat.class));
		final CheckBox check1 = (CheckBox) findViewById(R.id.check1);
		final CheckBox check2 = (CheckBox) findViewById(R.id.check2);
		final CheckBox check3 = (CheckBox) findViewById(R.id.check3);
		final CheckBox check4 = (CheckBox) findViewById(R.id.check4);
		Button sfida = (Button) findViewById(R.id.ok);

		sfida.setOnClickListener(new OnClickListener() {
			// gestione dei checkbox
			public void onClick(View v) {
				check1.getText().toString();

				if (check1.isChecked() || check2.isChecked()
						|| check3.isChecked() || check4.isChecked()) {
					synchronized (BluetoothChatService.sync) {
						if (check1.isChecked()) {
							topicvector = topicvector
									+ check1.getText().toString() + separatore;

						}
						if (check2.isChecked()) {

							topicvector = topicvector
									+ check2.getText().toString() + separatore;

						}
						if (check3.isChecked()) {

							topicvector = topicvector
									+ check3.getText().toString() + separatore;

						}
						if (check4.isChecked()) {

							topicvector = topicvector
									+ check4.getText().toString() + separatore;

						}

						System.out.println("interesse selezionato: "
								+ topicvector);
						// Message msg = mHandler
						// .obtainMessage(BluetoothChat.SEND,topicvector);
						// mHandler.sendMessage(msg);
						Intent intent = getIntent();
						intent.putExtra("result", topicvector);
						setResult(RESULT_OK, intent);
						try {
							ready = true;
							System.out.println("SIGNAL");
							BluetoothChatService.sync.notify();
						} catch (Exception e) {
							e.printStackTrace();
						}
						topicvector = "";
						finish();

					}

				} else {
					Dialog locationError = new AlertDialog.Builder(
							TabSfida.this).setIcon(0).setTitle("Errore")
							.setPositiveButton(R.string.ok, null)
							.setMessage("Seleziona almeno un interesse.")
							.create();
					locationError.show();
				}
			}
		});
	}

	public static boolean isReady() {
		return ready;
	}

	// public static void resetTopicVector()
	// {
	// topicvector="";
	// }

	public static void resetReady() {
		ready = false;
	}

}
