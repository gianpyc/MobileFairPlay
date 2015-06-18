package com.android.tab;

import java.util.Vector;

import com.android.R;
import com.android.file.Write;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class TabBarManger extends TabActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab);
		boolean exists;
		Vector valori = null;
		/* TabHost will have Tabs */
		TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);

		/*
		 * TabSpec used to create a new tab. By using TabSpec only we can able
		 * to setContent to the tab. By using TabSpec setIndicator() we can set
		 * name to tab.
		 */

		/* tid1 is firstTabSpec Id. Its used to access outside. */
		TabSpec firstTabSpec = tabHost.newTabSpec("tid1");
		TabSpec secondTabSpec = tabHost.newTabSpec("tid2");

		/* TabSpec setIndicator() is used to set name for the tab. */
		/* TabSpec setContent() is used to set content for a particular tab. */
		firstTabSpec.setIndicator("Interessi").setContent(
				new Intent(this, TabInteressi.class));
		secondTabSpec.setIndicator("Sfida").setContent(
				new Intent(this, TabSfida.class));

		/* Add tabSpec to the TabHost to display. */
		tabHost.addTab(firstTabSpec);
		tabHost.addTab(secondTabSpec);
		// switch for tab
		/*
		 * Write write = new Write(valori); exists=write.getexits();
		 * 
		 * if (exists) { tabHost.setCurrentTabByTag("tid2"); } else {
		 * tabHost.setCurrentTabByTag("tid1"); }
		 */

	}
}
