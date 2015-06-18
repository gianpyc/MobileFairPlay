package com.android.profile;

import com.android.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class ProfileActivity extends Activity {
	private ImageView img;
	private Button scegli_button;
	private Button salva_button;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        img =  (ImageView) findViewById(R.id.profile_img);
        scegli_button = (Button) findViewById(R.id.choose);
        scegli_button.setOnClickListener(settingsclick);
        salva_button = (Button) findViewById(R.id.salva_button);
        salva_button.setOnClickListener(salvaclick);
        
        EditText  mEdit   = (EditText)findViewById(R.id.name_box);
        mEdit.setText(settings.getString("profile_name", ""));
        mEdit   = (EditText)findViewById(R.id.surname_box);
        mEdit.setText(settings.getString("profile_surname", ""));
        String img_url = settings.getString("profile_img", null);
        if(img_url!=null){
        	BitmapFactory.Options bfOptions = new BitmapFactory.Options();
			bfOptions.inDither = false; // Disable Dithering mode
			bfOptions.inPurgeable = true; // Tell to gc that whether it needs
											// free memory, the Bitmap can be
											// cleared
			bfOptions.inInputShareable = true; // Which kind of reference will
												// be used to recover the Bitmap
												// data after being clear, when
												// it will be used in the future
			bfOptions.inTempStorage = new byte[32 * 1024];
			bfOptions.inSampleSize = 2;

			img.setImageBitmap(BitmapFactory.decodeFile(img_url, bfOptions));
			img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }


	OnClickListener settingsclick = new OnClickListener() {
		public void onClick(View viewParam) {
			Intent intent = new Intent(ProfileActivity.this,
					LoadIMGAsink.class);
			startActivityForResult(intent, 1);
		}
	};
	
	OnClickListener salvaclick = new OnClickListener() {
		public void onClick(View viewParam) {
			EditText  mEdit   = (EditText)findViewById(R.id.name_box);
			String name = mEdit.getText().toString();
			mEdit   = (EditText)findViewById(R.id.surname_box);
			String surname = mEdit.getText().toString();
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			Editor editor = settings.edit();
			editor.putString("profile_name", name);
			editor.putString("profile_surname", surname);
			editor.commit();
			
			System.out.println("saved: "+name+" "+surname);
			finish();
		}
	};
	
	@Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == 1) {
			String msg = data.getStringExtra("imagePath");
			
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			Editor editor = settings.edit();
			editor.putString("profile_img", msg);
			editor.commit();
			System.out.println("saved: "+msg);
			
			BitmapFactory.Options bfOptions = new BitmapFactory.Options();
			bfOptions.inDither = false; // Disable Dithering mode
			bfOptions.inPurgeable = true; // Tell to gc that whether it needs
											// free memory, the Bitmap can be
											// cleared
			bfOptions.inInputShareable = true; // Which kind of reference will
												// be used to recover the Bitmap
												// data after being clear, when
												// it will be used in the future
			bfOptions.inTempStorage = new byte[32 * 1024];
			bfOptions.inSampleSize = 2;

			img.setImageBitmap(BitmapFactory.decodeFile(msg, bfOptions));
			img.setScaleType(ImageView.ScaleType.FIT_CENTER);
		}
	 }
}
