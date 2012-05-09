package com.android.benchmark;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.benchmark.cputest.CPUBenchmark;
import com.android.benchmark.hardwareinformation.HardwareInformation;
import com.android.benchmark.internettest.InternetTestBenchmark;
import com.android.benchmark.iostoragetest.IOStorageBenchmark;

public class MainActivity extends Activity
{
	/*
	 * Hints: Plus all results use some formula to calculate the
	 * "Benchmark Score"
	 * 
	 * This class select by default all Benchmark tests, however user can decide
	 * whether it wants to start which benchmark tests.
	 */
	
	private Button harwareInfoButton;
	private Button testCPUButton;
	private Button testIOStorageButton;
	private Button speedTest;
	private Button testAllButton;

	private static final int MENU_RESULTS = Menu.FIRST; 
	private static final int MENU_HISTORY = Menu.FIRST +1; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.main);
		Installation.id(this);
		
		BenchmarkApplication application = (BenchmarkApplication) getApplication();
		application.setTestStatus(false);
		
		this.harwareInfoButton = (Button) findViewById(R.id.hardwareInfoButton);
		this.harwareInfoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						HardwareInformation.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intent);
			}
		});

		this.testCPUButton = (Button) findViewById(R.id.testCPUButton);
		this.testCPUButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						CPUBenchmark.class);
				startActivity(intent);
			}
		});

		this.testIOStorageButton = (Button) findViewById(R.id.testIOStorageButton);
		this.testIOStorageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						IOStorageBenchmark.class);
				startActivity(intent);
			}
		});

		this.speedTest = (Button) findViewById(R.id.speedTest);
		this.speedTest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						InternetTestBenchmark.class);
				startActivity(intent);
			}
		});
		
		this.testAllButton = (Button) findViewById(R.id.testAllButton);
		this.testAllButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BenchmarkApplication application = (BenchmarkApplication) getApplication();
		        application.setTestStatus(true);
		        Intent intent = new Intent(MainActivity.this, HardwareInformation.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intent);
			}
		});
		

		Log.d("mobile", "Manufactor: " + Build.MANUFACTURER );
		Log.d("mobile", "Brand: " + Build.BRAND );
		Log.d("mobile", "Model: " + Build.MODEL );
		Log.d("mobile", "Device: " + Build.DEVICE );
	
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_RESULTS, 0, "Results").setIcon(R.drawable.stats);
        menu.add(0, MENU_HISTORY, 0, "History").setIcon(R.drawable.stats);
        return true;
    }
	
	 @Override
	    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		 	Intent intent;
	        switch (item.getItemId()) {
	            case MENU_RESULTS:
	    			 intent = new Intent(MainActivity.this, ResultsActivity.class);
	    			 startActivity(intent);
	    			 return true;	
	            case MENU_HISTORY:
	    			 intent = new Intent(MainActivity.this, BenchmarkHistoryActivity.class);
	    			 startActivity(intent);
	    			 return true;	           
	        }
	        return super.onMenuItemSelected(featureId, item);
	    }    
	
	@Override
	protected void onRestart() {
		super.onRestart();
		// ignore
	}

	@Override
	protected void onStart() {
		super.onStart();
		// ignore
	}

	@Override
	protected void onResume() {
		super.onResume();
		// start benchmark
	}

	@Override
	protected void onPause() {
		super.onPause();
		// pause benchmark
	}

	@Override
	protected void onStop() {
		super.onStop();
		// app focus lost, program stops
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// program exits
	}

}