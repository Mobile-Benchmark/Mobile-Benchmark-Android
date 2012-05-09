package com.android.benchmark;


import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONObject;

import com.android.benchmark.fetcher.URLFetcher;
import com.android.benchmark.hardwareinformation.ListViewCustomAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class ResultsActivity extends Activity
{
	private static final String TAG = "mobile";
	
	protected static final String TEST_CPU_SINGLETHREAD = "CPU Single Thread - PI Calculation";
	protected static final String TEST_IO_INTERN = "Intern IO Storage";
	protected static final String TEST_IO_EXTERN = "Extern IO Storage";
	protected static final String TEST_HARDWARE = "Hardware Information";
	protected static final String TEST_INTERNET_SPEED = "Internet speed test";
	private static final String dialogMessage = "Submitting score";
	
	private ListViewCustomAdapter arrayAdapter;
	private Button backButton;
	private Button submitButton;
	private ListView listView;
	private SubmitScoreTask submitScoreTask;
	private ProgressDialog progressDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.results);
		
		this.listView = (ListView) findViewById(R.id.ListView);
		this.progressDialog = new ProgressDialog(this);
		this.progressDialog.setMessage(dialogMessage);
		
		this.submitButton = (Button) findViewById(R.id.submitButton);
		this.submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				submitScores();
			}
		});
		
		this.backButton = (Button) findViewById(R.id.backButton);
		this.backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ResultsActivity.this , MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intent);
			}
		});
		
    	submitScoreTask=(SubmitScoreTask)getLastNonConfigurationInstance();    	
    	if (submitScoreTask != null)
		{
    		submitScoreTask.attach(this);
		}
    	
		arrayAdapter = new ListViewCustomAdapter(this);
		getResults();
		listView.setAdapter(arrayAdapter);
		
	}
	
    @Override
    public Object onRetainNonConfigurationInstance()
    {
    	if(submitScoreTask != null)
    	{
    		submitScoreTask.detach();
    	}

    	return(submitScoreTask);
    }
    
	private void submitScores()
	{
		BenchmarkApplication application = (BenchmarkApplication) getApplication();

		if(application.getSubmittedScore() != application.getTotalBenchMarkScore() || application.getTotalBenchMarkScore() == 0 )
		{		
			if(application.getGlobalTestStatus())
			{
				submitScoreTask = new SubmitScoreTask(this);
				submitScoreTask.execute();				
			}
			else
			{
				Toast toast = Toast.makeText(getApplicationContext(), "All tests are required to submit a benchmark score", Toast.LENGTH_SHORT);	
				toast.show();
			}
		}
		else
		{
			Toast toast = Toast.makeText(getApplicationContext(), "Already submitted your benchmark", Toast.LENGTH_SHORT);	
			toast.show();
		}
	}
	
	private void getResults()
	{
		BenchmarkApplication application = (BenchmarkApplication) getApplication();
		
		int globalScore = application.getTotalBenchMarkScore();
		if(application.getGlobalTestStatus())
		{
			arrayAdapter.add("Total Score:", "" + globalScore, R.drawable.checked);
		}
		else
		{
			arrayAdapter.add("Total Score:", "" + globalScore, R.drawable.unchecked);
		}
		
		int hardwareScore = application.getHardwareBenchMarkScore();
		if(hardwareScore != 0)
		{
			arrayAdapter.add("Hardware Score:", "" + hardwareScore, R.drawable.checked);
		}
		else
		{
			arrayAdapter.add("Hardware Score:", "" + hardwareScore, R.drawable.unchecked);
		}
		
		int ioInternScore = application.getIOInternBenchmarkScore();
		if(ioInternScore != 0)
		{
			arrayAdapter.add("IO Intern Benchmark Score:", "" + ioInternScore, R.drawable.checked);
		}
		else
		{
			arrayAdapter.add("IO Intern Benchmark Score:", "" + ioInternScore, R.drawable.unchecked);
		}
		
		int ioExternScore = application.getIOExternBenchmarkScore();
		if(ioExternScore != 0)
		{
			arrayAdapter.add("IO Extern Benchmark Score:", "" + ioExternScore, R.drawable.checked);
		}
		else
		{
			arrayAdapter.add("IO Extern Benchmark Score:", "" + ioExternScore, R.drawable.unchecked);
		}
		
		
		int cpuScore = application.getCPUBenchmarkScore();
		if(cpuScore != 0)
		{
			arrayAdapter.add("CPU Single Thread - PI Calculation:", "" + cpuScore, R.drawable.checked);
		}
		else
		{
			arrayAdapter.add("CPU Single Thread - PI Calculation:", "" + cpuScore, R.drawable.unchecked);
		}
		
		int speedScore = application.getSpeedBenchmarkScore();
		if(speedScore != 0)
		{
			arrayAdapter.add("Internet Benchmark Score:", "" + speedScore, R.drawable.checked);
		}
		else
		{
			arrayAdapter.add("Internet Benchmark Score:", "" + speedScore, R.drawable.unchecked);
		}		
	}

	private String getPhoneID()
	{	
		String id = "";
		String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
		id = Installation.getID();
		if(deviceID != null)
		{
			id += deviceID;
		}
		
		return getSha1(id, "SHA-1");
	}
	
	private String getSha1(String s, String keyString)
	{
		SecretKeySpec key;
		byte[] bytes = null;
		
		try 
		{
			key = new SecretKeySpec((keyString).getBytes("UTF-8"), "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(key);	
			bytes = mac.doFinal(s.getBytes("UTF-8"));

		}
		catch (UnsupportedEncodingException e) {
			Log.e("mobile", "SHA1: " + e.getMessage());
		}
		catch (NoSuchAlgorithmException e) {
			Log.e("mobile", "SHA1: " + e.getMessage());
		}
		catch (InvalidKeyException e) {
			Log.e("mobile", "SHA1: " + e.getMessage());
		}
		return Base64.encodeToString(bytes, Base64.DEFAULT + Base64.URL_SAFE);
	}

	@Override
	public void onResume() {
		super.onResume();
	
	}

	@Override
	public void onPause() {
		super.onPause();	
		progressDialog.dismiss();
	}
	
	private class SubmitScoreTask extends AsyncTask<String, String, Void> 
	{
		private ResultsActivity activity;
		private boolean isFinished = false;
		private boolean isSuccessful = false;
		BenchmarkApplication application = (BenchmarkApplication) getApplication();
		
		public SubmitScoreTask(ResultsActivity activity)
		{
			attach(activity);
		}
		
		protected void onPreExecute() 
		{
			isFinished = false;
        }
		
		protected Void doInBackground(String... args) 
		{		
			String phoneID = activity.getPhoneID();
			if(!phoneID.equals(""))
			{
				try {
				phoneID = phoneID.substring(0, phoneID.length() - 1);
				Log.d("mobile", " (original) phoneID: " + phoneID);
				
				JSONArray testresults = new JSONArray();

				JSONObject result = new JSONObject();
				result.put("test", TEST_CPU_SINGLETHREAD);
				result.put("score", application.getCPUBenchmarkScore() );
				result.put("successful", true);
				testresults.put(result);
				
				result = new JSONObject();
				result.put("test", TEST_HARDWARE);
				result.put("score", application.getHardwareBenchMarkScore());
				result.put("successful", true);
				testresults.put(result);
				
				result = new JSONObject();
				result.put("test", TEST_IO_EXTERN);
				result.put("score", application.getIOExternBenchmarkScore());
				result.put("successful", true);
				testresults.put(result);
				
				result = new JSONObject();
				result.put("test", TEST_IO_INTERN);
				result.put("score", application.getIOInternBenchmarkScore() );
				result.put("successful", true);
				testresults.put(result);
				
				result = new JSONObject();
				result.put("test", TEST_INTERNET_SPEED);
				result.put("score", application.getSpeedBenchmarkScore() );
				result.put("successful", true);
				testresults.put(result);
				
				JSONObject mq = new JSONObject();
			    
				mq.put("phoneid", phoneID);
				mq.put("device", Build.MODEL);
				mq.put("brand", Build.DEVICE);
				mq.put("total_score", application.getTotalBenchMarkScore());
				mq.put("results", testresults);

				Log.d("mobile", "json: " + mq.toString());
				URLFetcher.makeCustomRequest("PostTestResultsWithPhoneKey/", mq);
					
				if(URLFetcher.isValidRequest())
				{
					isSuccessful = true;
				}
				
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				
			}
			return null;
		}
		
		protected void onProgressUpdate(String... progress) {
		
		}
		
		protected void onPostExecute(Void test) 
		{
			isFinished = true;
			
			if(isSuccessful)
			{				
				application.setSubmittedScore(application.getTotalBenchMarkScore());
				
	            AlertDialog.Builder alertbox = new AlertDialog.Builder(activity);
	            alertbox.setTitle("Succeeded");
	            alertbox.setMessage("Result successfully submitted");
	            alertbox.setPositiveButton("OK", null);
	            alertbox.setCancelable(true);
	            alertbox.show();
			}
			else
			{
	            AlertDialog.Builder alertbox = new AlertDialog.Builder(activity);
	            alertbox.setTitle("Failed");
	            alertbox.setMessage("Something went wrong... Try again.");
	            alertbox.setPositiveButton("OK", null);
	            alertbox.setCancelable(true);
	            alertbox.show();			
			}
			
			if (activity==null)
			{
				Log.w(TAG, "onPostExecute() skipped -- no activity");
			}
			else 
			{
				if (activity.progressDialog.isShowing()) 
				{
					activity.progressDialog.dismiss();
				}				
			}
	    }
		
		void detach()
		{
			activity = null;
		}
		    
		void attach(ResultsActivity activity) 
		{
			this.activity = activity;		
			
			if(!isFinished)
			{
				if(this.activity.progressDialog != null)
				{
					if(!this.activity.progressDialog.isShowing())
					{
						this.activity.progressDialog.show();
					}
				}
			}
		}
	}
}
