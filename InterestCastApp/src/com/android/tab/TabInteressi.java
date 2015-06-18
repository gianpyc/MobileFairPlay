package com.android.tab;

import com.android.R;
import com.android.BluetoothChat.FairplayMobileApp;
import com.android.BluetoothChat.BluetoothChat;
import com.android.file.Read;
import com.android.file.Write;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.io.File;
import java.util.Vector;

public class TabInteressi extends Activity implements OnSeekBarChangeListener {
	/** Called when the activity is first created. */
	private Button salva;
	private Button carica;
	private Button quit;
	public String[] topic = { "Music", "Cinema", "Sport", "Book", "Animals",
			"Cars", "MotorBikes", "Technology", "Televison", "Travelling" };
	private static final String TAG = "interessi";
	private Vector valoriFile;
	public int[] controlloint = new int[10];
	private Vector valoriDaScrivere;
	private Vector valoriDaScrivere2;
	private String separatore = "=";
	private int seekProgress;
	private SeekBar bar; // declare seekbar object variable
	// declare text label objects
	private TextView textProgress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* First Tab Content */
		EditText EditText = new EditText(this);
		EditText.setText("Modifica Interessi");
		setContentView(R.layout.interessi);
		bar = (SeekBar) findViewById(R.id.seekBar1); // make seekbar object
		bar.setOnSeekBarChangeListener(this); // set seekbar listener.
		textProgress = (TextView) findViewById(R.id.sogliaAmicizia);
		File sd = Environment.getExternalStorageDirectory();
		File f = new File(sd, "/interest/config/interessi.txt");
		if (!f.exists()) {
			setvalori();
		}

		getvalori();
		salva = (Button) findViewById(R.id.salva1);
		salva.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				// Write.f.delete();
				valoriDaScrivere = new Vector();

				EditText view3 = (EditText) findViewById(R.id.Music);
				String music = view3.getText().toString();
				valoriDaScrivere.add(topic[0] + separatore + music);
				controlloint[0] = Integer.parseInt(music);

				EditText view = (EditText) findViewById(R.id.Cinema);
				String cinema = view.getText().toString();
				valoriDaScrivere.add(topic[1] + separatore + cinema);
				controlloint[1] = Integer.parseInt(cinema);

				EditText view2 = (EditText) findViewById(R.id.Sport);
				String sport = view2.getText().toString();
				valoriDaScrivere.add(topic[2] + separatore + sport);
				controlloint[2] = Integer.parseInt(sport);

				EditText view4 = (EditText) findViewById(R.id.Book);
				String book = view4.getText().toString();
				valoriDaScrivere.add(topic[3] + separatore + book);
				controlloint[3] = Integer.parseInt(book);

				EditText view5 = (EditText) findViewById(R.id.Animals);
				String animals = view5.getText().toString();
				valoriDaScrivere.add(topic[4] + separatore + animals);
				controlloint[4] = Integer.parseInt(animals);

				EditText view6 = (EditText) findViewById(R.id.Cars);
				String cars = view6.getText().toString();
				valoriDaScrivere.add(topic[5] + separatore + cars);
				controlloint[5] = Integer.parseInt(cars);

				EditText view7 = (EditText) findViewById(R.id.Motorbikes);
				String motorbikes = view7.getText().toString();
				valoriDaScrivere.add(topic[6] + separatore + motorbikes);
				controlloint[6] = Integer.parseInt(motorbikes);

				EditText view8 = (EditText) findViewById(R.id.Technology);
				String technology = view8.getText().toString();
				valoriDaScrivere.add(topic[7] + separatore + technology);
				controlloint[7] = Integer.parseInt(technology);

				EditText view9 = (EditText) findViewById(R.id.Television);
				String television = view9.getText().toString();
				valoriDaScrivere.add(topic[8] + separatore + television);
				controlloint[8] = Integer.parseInt(television);

				EditText view10 = (EditText) findViewById(R.id.Travelling);
				String travelling = view10.getText().toString();
				valoriDaScrivere.add(topic[9] + separatore + travelling);
				controlloint[9] = Integer.parseInt(travelling);

				valoriDaScrivere.add("Friendship_threshold" + separatore
						+ seekProgress);

				boolean controllo = false;
				for (int i = 0; i < controlloint.length; i++) {
					if (controlloint[i] > 100) {
						controllo = true;

					}

				}
				if (controllo)
					showDialog(CONTROLLO);
				else {
					Write scriviInteressi = new Write(valoriDaScrivere,
							"/interest/config/interessi.txt", false);
					showDialog(SALVATAGGIO);
				}
			}

		});
		Button Quit = (Button) findViewById(R.id.quit);
		Quit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				showDialog(QUIT);
			}

		});

	}

	public void getvalori() {

		Log.d(TAG, "getvalori()");
		Read read = new Read("/interest/config/interessi.txt");
		valoriFile = read.getReadInterest();
		EditText view = (EditText) findViewById(R.id.Music);
		view.setText((String) valoriFile.get(0));
		EditText view1 = (EditText) findViewById(R.id.Cinema);
		view1.setText((String) valoriFile.get(1));
		EditText view2 = (EditText) findViewById(R.id.Sport);
		view2.setText((String) valoriFile.get(2));
		EditText view3 = (EditText) findViewById(R.id.Book);
		view3.setText((String) valoriFile.get(3));
		EditText view4 = (EditText) findViewById(R.id.Animals);
		view4.setText((String) valoriFile.get(4));
		EditText view5 = (EditText) findViewById(R.id.Cars);
		view5.setText((String) valoriFile.get(5));
		EditText view6 = (EditText) findViewById(R.id.Motorbikes);
		view6.setText((String) valoriFile.get(6));
		EditText view7 = (EditText) findViewById(R.id.Technology);
		view7.setText((String) valoriFile.get(7));
		EditText view8 = (EditText) findViewById(R.id.Television);
		view8.setText((String) valoriFile.get(8));
		EditText view9 = (EditText) findViewById(R.id.Travelling);
		view9.setText((String) valoriFile.get(9));
		System.out.println("seek: " + valoriFile.get(10));
		bar.setProgress(Integer.parseInt("" + valoriFile.get(10)));
	}

	public void setvalori() {

		valoriDaScrivere2 = new Vector();

		valoriDaScrivere2.add(topic[0] + separatore + 10);
		valoriDaScrivere2.add(topic[1] + separatore + 10);
		valoriDaScrivere2.add(topic[2] + separatore + 10);
		valoriDaScrivere2.add(topic[3] + separatore + 10);
		valoriDaScrivere2.add(topic[4] + separatore + 10);
		valoriDaScrivere2.add(topic[5] + separatore + 10);
		valoriDaScrivere2.add(topic[6] + separatore + 10);
		valoriDaScrivere2.add(topic[7] + separatore + 10);
		valoriDaScrivere2.add(topic[8] + separatore + 10);
		valoriDaScrivere2.add(topic[9] + separatore + 10);
		valoriDaScrivere2.add("Friendship_threshold" + separatore + 10);

		Write scriviInteressi2 = new Write(valoriDaScrivere2,
				"/interest/config/interessi.txt", false);

	}

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		seekProgress = progress;
		// change progress text label with current seekbar value
		textProgress.setText(": " + seekProgress);
		// change action text label to changing

	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		seekBar.setSecondaryProgress(seekBar.getProgress());

	}

	private static final int SALVATAGGIO = 0;
	private static final int CONTROLLO = 1;
	private static final int QUIT = 2;

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case SALVATAGGIO: {
			AlertDialog errorDialog = new AlertDialog.Builder(this)
					.setMessage("Saved new interests")
					.setCancelable(false)
					.setNeutralButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									dialog.dismiss();
									finish();
									// startActivity(new
									// Intent(TabInteressi.this,
									// BenvenutoChat.class));
								}
							}).create();
			dialog = errorDialog;
			break;
		}
		case CONTROLLO: {
			AlertDialog errorDialog = new AlertDialog.Builder(this)
					.setMessage("Wrong value inserted")
					.setCancelable(false)
					.setNeutralButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
									// startActivity(new
									// Intent(TabInteressi.this,
									// TabInteressi.class));
								}
							}).create();
			dialog = errorDialog;
			break;
		}
		case QUIT: {
			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);

			// set the message to display
			alertbox.setMessage("Do you really want to quit?");

			// set a positive/yes button and create a listener
			alertbox.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {

						// do something when the button is clicked
						public void onClick(DialogInterface arg0, int arg1) {
							finish();
						}
					});

			// set a negative/no button and create a listener
			alertbox.setNegativeButton("No",
					new DialogInterface.OnClickListener() {

						// do something when the button is clicked
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}
					});

			// display box
			alertbox.show();
		}

		default:
			dialog = super.onCreateDialog(id);
			break;
		}
		return dialog;
	}

}