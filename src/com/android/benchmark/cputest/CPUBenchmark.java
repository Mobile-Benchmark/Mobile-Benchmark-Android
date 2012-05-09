package com.android.benchmark.cputest;

import com.android.benchmark.BenchmarkApplication;
import com.android.benchmark.R;
import com.android.benchmark.internettest.InternetTestBenchmark;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class CPUBenchmark extends Activity
{
	/* 
	 * Hints: 
	 * Solver (example: FPU, SSE2, SSE3) and/or Renderer module (FPU/MMX, SSE3/Extended MMX)
	 */ 
	
	private static final String TAG = "mobile";
	private static final double CPU_FACTOR = 0.3;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private TextView statusText;
	private SingleThreadPICalculation singleThreadTask = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cpu_benchmark);
         	
    	progressDialog = new ProgressDialog(this);
    	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	progressDialog.setMessage("Calculating pi...");
    	progressDialog.setCancelable(false);
    	progressDialog.setProgress(0);
    	
    	this.titleText = (TextView) findViewById(R.id.titleText);
        this.statusText = (TextView) findViewById(R.id.statusText);
        this.titleText.setText("Single Thread Benchmark");
        this.statusText.setText("Running...");
        
    	singleThreadTask=(SingleThreadPICalculation)getLastNonConfigurationInstance();
    	    
		if (singleThreadTask==null)
		{
			markAsRunning();
			singleThreadTask = new SingleThreadPICalculation(this);
			singleThreadTask.execute();
		}
		else
		{
			singleThreadTask.attach(this);
			setProgressPercentSingleThread(singleThreadTask.getProgress());
		
			if (singleThreadTask.getProgress()>=100)
			{
				markAsDone();
			}
			else
			{
				markAsRunning();
			}
		}
    }
    
    @Override
    public Object onRetainNonConfigurationInstance()
    {
    	singleThreadTask.detach();

    	return(singleThreadTask);
    }
    
    @Override
    protected void onPause()
    {
    	super.onPause();
    	markAsDone();
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
    	savedInstanceState.putString("titleText", (String) titleText.getText());
    	savedInstanceState.putString("statusText", (String) statusText.getText());
    	savedInstanceState.putInt("progress", singleThreadTask.getProgress());
    	super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) 
    {
    	super.onRestoreInstanceState(savedInstanceState);    	
        this.titleText.setText(savedInstanceState.getString("titleText"));
        this.statusText.setText(savedInstanceState.getString("statusText"));
        setProgressPercentSingleThread(savedInstanceState.getInt("progress"));
    }    

    private void markAsRunning()
    {
    	progressDialog.show();
    }
    
	private void markAsDone() 
	{
		progressDialog.dismiss();
	}
    
    private void setProgressPercentSingleThread(int progress) 
    {
    	progressDialog.setProgress(progress);
	}
    
    
	public void testingDone(double testResult)
	{
		BenchmarkApplication application = (BenchmarkApplication) getApplication();
		application.setCPUBenchmarkScore( (int) (testResult * CPU_FACTOR));
        if(application.getTestStatus())
        {
			Intent intent = new Intent(CPUBenchmark.this,InternetTestBenchmark.class);
			startActivity(intent);
        }
	}

	protected class SingleThreadPICalculation extends AsyncTask<String, String, Double>
	{	
		CPUBenchmark activity = null;
		int cnt = 0;
		int progress = 0;
    	long start;
    	
    	SingleThreadPICalculation(CPUBenchmark activity)
    	{
    		attach(activity);
    	}
    	
		protected Double doInBackground(String... arg0) 
		{  
			double pi = 0; 	
	    	double y = 1;

	    	final int lps = 90000000*2; //90000000

    		publishProgress();
	    	start = System.nanoTime();	    	
	    	for(int x=1; x < lps; x+=2) 
	    	{
	    		pi = pi + (y/x);
	    		y = -y;
	    		cnt++;
	    		if((cnt % 9000000) == 0)
	    		{
	    			publishProgress();
	    		}
	    	}
			return pi * 4;
		}
		
		protected void onProgressUpdate(String... unused)
		{   
			if (activity==null) 
			{
				Log.w(TAG, "onProgressUpdate() skipped -- no activity");
			}
			else
			{
				progress += 10;
				activity.setProgressPercentSingleThread(progress);
			}
	    }
		
		protected void onPostExecute(Double pi)
	    {
			if (activity==null)
			{
				Log.w(TAG, "onPostExecute() skipped -- no activity");
			}
			else 
			{
				activity.markAsDone();
				double duration = (System.nanoTime() - start) / 1000000.0;
				activity.statusText.setText("Results:\nDuration: " + duration + "ms" + "\nTotale cycles: " + cnt + "\nAnswer: " + pi);
				activity.testingDone(duration);
			}
	    }
		
		void detach()
		{
			activity = null;
		}
		    
		void attach(CPUBenchmark activity) 
		{
			this.activity = activity;
		}

		int getProgress() 
		{
			return(progress);
		}
	}
    
    //Multiple Threads CPU Benchmark (Parallel), if supported multi-core
    @SuppressWarnings("unused")
	private void startMultiCPUBenchMark()
    {
    	
    }
}
