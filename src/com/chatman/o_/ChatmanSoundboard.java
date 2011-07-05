package com.chatman.o_;

// Shake code from http://stackoverflow.com/questions/2317428/android-i-want-to-shake-it


import java.io.File;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatmanSoundboard extends ListActivity {
	private static final int DEFAULT_SOUND = 0;
	private static final int DEFAULT_THEME = 0;
	private static final float SHAKE_SENSITIVITY = 2.5f;
	
    // Variables for playing sounds
	private String[] soundNames;
	private String[] soundFiles = new String[] {"chatman.mp3", 
			"dota.mp3", "ghostnappa.mp3", "ghostnappa.mp3"};
	private String[] themes;
	private int currTheme;
	private MediaPlayer player;

	// Variables for detecting shakes
    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources res = getResources();
    	player = new MediaPlayer();
    	
    	// Load in filenames and labels
    	soundFiles = res.getStringArray(R.array.soundFiles);
        soundNames = res.getStringArray(R.array.soundNames);
        
        // Load in names of themes and set based on user preference or default
        themes = res.getStringArray(R.array.themes);
        currTheme = getPreferences(MODE_PRIVATE).getInt("theme", DEFAULT_THEME);

        // Generate list with labels
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, soundNames));
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        // Set to play appropriate sound on click
        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id)
          {
        	  if (id < soundFiles.length)
        	  {
        		  if (!playSound((int)id))
        			  // Sound failed, show text message
        		      Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
        		              Toast.LENGTH_SHORT).show();

        	  }
          }
        });
        
        // Initialize shake detector
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }
    
    // Play a sound based on id
    private boolean playSound(int id)
    {
		  try
		  {
			  player.reset();
			  // Uses the them and filename to find file
			  AssetFileDescriptor fd = getAssets().openFd(
					  String.format("%s%c%s", 
							  themes[currTheme], File.separatorChar, 
							  soundFiles[(int)id])
					);
			  player.setDataSource(fd.getFileDescriptor(), 
					  fd.getStartOffset(), fd.getLength());
			  fd.close();
			  // Play sound
			  player.prepare();
			  player.start();
			  return true;
		  }
		  catch (Exception e)
		  {
			  return false;
		  }
    }
    
    // Handle selecting a theme using the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu:
            chooseTheme();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	// Defaults to current and display a dialog to chose theme
    private void chooseTheme()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.themePrompt);
    	builder.setSingleChoiceItems(themes, currTheme, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
    	        currTheme = item;
    	        getPreferences(MODE_PRIVATE).edit().putInt("theme", currTheme).commit();
    	        dialog.dismiss();
    	    }
    	});
    	builder.create().show();
    }
    
    
    // Sensor event listener for detecting shakes
    private final SensorEventListener mSensorListener = new SensorEventListener() {
    public void onSensorChanged(SensorEvent se) {
    	float x = se.values[0];
    	float y = se.values[1];
    	float z = se.values[2];
    	mAccelLast = mAccelCurrent;
    	mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
    	float delta = mAccelCurrent - mAccelLast;
    	mAccel = mAccel * 0.9f + delta; // perform low-cut filter
    	if (mAccel > SHAKE_SENSITIVITY)
    		playSound(DEFAULT_SOUND);
      }

      public void onAccuracyChanged(Sensor sensor, int accuracy) {
      }
    };

    @Override
    protected void onResume() {
    	super.onResume();
    	mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
    	mSensorManager.unregisterListener(mSensorListener);
    	super.onStop();
    }
}