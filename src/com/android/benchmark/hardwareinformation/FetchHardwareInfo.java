package com.android.benchmark.hardwareinformation;

import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

class FetchHardwareInfo extends AsyncTask<String, String, Void>
{
	private static final String TAG = "mobile";
	private HardwareInformation activity = null;
	
	private static final double scoreTotalFactor = 0.001;
	private static final double scoreCPUhzFactor = 3;
	private static final double scoreCPUscalingFactor = 0.0013;
	private static final double scoreCPUBogoMipsFactor = 4;
	private static final double scoreResolutionFactor = 1;
	private static final double scoreRefreshRateFactor = 1;
	private static final double scoreDPIFactor = 2;
	
	private double scoreTotal;
	private double scoreCPUhz;
	private double scoreCPUscaling;
	private double scoreCPUBogoMips;
	private double scoreResolution;
	private double scoreRefreshRate;
	private double scoreDPI;
	
	private int progress;

	public FetchHardwareInfo(HardwareInformation activity)
	{
		attach(activity);
	}

	private double calculateScore()
	{
		scoreTotal += scoreCPUhz * scoreCPUhzFactor;
		scoreTotal += scoreCPUhzFactor * scoreCPUscalingFactor;
		scoreTotal += scoreCPUBogoMips * scoreCPUBogoMipsFactor;
		scoreTotal += scoreResolution * scoreResolutionFactor;
		scoreTotal += scoreRefreshRate * scoreRefreshRateFactor;
		scoreTotal += scoreDPI * scoreDPIFactor;
		scoreTotal = scoreTotal * scoreTotalFactor;
		return scoreTotal;
	}
	
	private void sleepMs(Integer time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {

		}
	}
	
	private void getMaxCPU() {
		try {
			scoreCPUhz = HardwareInformation.getCPUFrequencyMax();
			Log.d(TAG, "" + scoreCPUhz);
			publishProgress("Looking for Max CPU Hz:", "CPU: "
					+ scoreCPUhz / 1000 + " MHz");
			
		} catch (Exception e1) {
			scoreCPUhz = 0;
			publishProgress("Looking for Max CPU Hz:",
					"Failed");
			Log.d(TAG, e1.getMessage());
		}
	}

	private void getMaxCPUFrequency() 
	{
		try {
			scoreCPUscaling = HardwareInformation.getCPUFrequencyMaxScaling(); 
			publishProgress("Looking for Max CPU scaling", "CPU Scaling: "
					+ scoreCPUscaling);
			Log.d(TAG, "" + scoreCPUscaling);
		} catch (Exception e1) {
			scoreCPUscaling = 0;
			publishProgress("Looking for Max CPU scaling:",
					"Failed");
			Log.d(TAG, e1.getMessage());
		}
	}

	private void getMaxBogoMips() 
	{
		try {
			scoreCPUBogoMips = (int)HardwareInformation.getCPUBogoMips();
			publishProgress("Looking for CPU BogoMips:", "CPU BogoMips: "
					+ scoreCPUBogoMips);
			Log.d(TAG, "" + scoreCPUBogoMips);
		} catch (Exception e1) {
			scoreCPUBogoMips = 0;
			publishProgress("Looking for CPU BogoMips:",
					"Failed");
			Log.d(TAG, e1.getMessage());
		}
	}

	private void getScreenResolutionRefreshRate()
	{
		WindowManager windowManager = activity.getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		scoreResolution = width + height;
		publishProgress("Looking for Screenresolution:", "Screen Resolution: "
				+ width + "x" + height);
		Log.d(TAG, "" + width + " " + height); //Pixels

		sleepMs(100);

		publishProgress("Looking for Refresh rate:", "Screen Refreshrate : "
				+ display.getRefreshRate() + " Hz");
		Log.d(TAG, "refreshrate: " + display.getRefreshRate());
	}

	private void getScreenDPI() 
	{
		DisplayMetrics dm = new DisplayMetrics();
		if(dm != null)
		{
			activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
	
			// these will return the actual dpi horizontally and vertically
			float xDpi = dm.xdpi;
			float yDpi = dm.ydpi;
			scoreDPI = (int)(xDpi + yDpi);
			publishProgress("Looking for Screen DPI", "Screen XDPI : " + xDpi
					+ " Screen YDPI : " + yDpi);
		}
	}

	@Override
	protected Void doInBackground(String... args)
	{
		// Max CPU
		getMaxCPU();
		sleepMs(100);

		// getCPUFrequencyMaxScaling
		getMaxCPUFrequency();
		sleepMs(100);

		// Bogo mips
		getMaxBogoMips();
		sleepMs(100);

		// screen resolution
		getScreenResolutionRefreshRate();
		sleepMs(100);
		
		// screen dpi
		getScreenDPI();
		return null;
	}

	@Override
	protected void onProgressUpdate(String... progressString)
	{
		if (activity==null) 
		{
			Log.w(TAG, "onProgressUpdate() skipped -- no activity");
		}
		else
		{
			if (progressString.length == 2) 
			{
				progress += 10;
				activity.setProgressPercent(progressString[0], progressString[1], progress);
			}
		}
	}

	protected void onPostExecute(Void test) 
	{
		if (activity==null)
		{
			Log.w(TAG, "onPostExecute() skipped -- no activity");
		}
		else 
		{
			activity.testingDone(calculateScore());
		}
	}
	
	void detach()
	{
		activity = null;
	}
	
    void attach(HardwareInformation activity)
    {
    	this.activity = activity;
    }
    
	int getProgress() 
	{
		return(progress);
	}
}