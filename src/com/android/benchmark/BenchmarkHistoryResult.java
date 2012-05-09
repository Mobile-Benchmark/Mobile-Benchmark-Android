package com.android.benchmark;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.benchmark.fetcher.URLFetcher;
import com.android.benchmark.hardwareinformation.ListViewCustomAdapter;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class BenchmarkHistoryResult extends ListActivity
{
	private static final String TAG = "mobile";
	private static final String dialogMessage = "Collecting results data";
	
	private ListViewCustomAdapter arrayAdapter;
	
	private FetchingHistoryResultTask fetchingHistoryResultTask;
	private ProgressDialog progressDialog;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.arrayAdapter = new ListViewCustomAdapter(this);
		
		this.progressDialog = new ProgressDialog(this);
		this.progressDialog.setMessage(dialogMessage);
		
		fetchingHistoryResultTask = (FetchingHistoryResultTask) getLastNonConfigurationInstance();

		if (fetchingHistoryResultTask == null)
		{
			fetchingHistoryResultTask = new FetchingHistoryResultTask(this);
			fetchingHistoryResultTask.execute();
		}
		else
		{
			fetchingHistoryResultTask.attach(this);
		}
    }
	
    @Override
    protected void onResume() 
    {	
    	super.onResume();
    	
    }
  
	@Override
	public Object onRetainNonConfigurationInstance()
	{    	
		if(fetchingHistoryResultTask != null)
		{
			fetchingHistoryResultTask.detach();
		}
		
		return (fetchingHistoryResultTask);
	}
	
	@Override
	protected void onPause()
	{
    	super.onPause();
		progressDialog.dismiss();
	}
    
	private class FetchingHistoryResultTask extends AsyncTask<String, String, Void>
	{
		private BenchmarkHistoryResult activity;
		private boolean isFinished = false;

		public FetchingHistoryResultTask(BenchmarkHistoryResult activity)
		{
			attach(activity);
		}

		protected void onPreExecute()
		{
			isFinished = false;
		}

		protected Void doInBackground(String... args)
		{
			try
			{
		    	BenchmarkApplication application = (BenchmarkApplication) getApplication();
		    	String benchmarkKey = application.getBenchmarkKey();
		    	
		 		
				JSONObject json = URLFetcher.connectCustom("TestResultsFromBenchmarkKey/" + benchmarkKey);
				JSONObject list = json.getJSONObject("list");
				JSONArray arrayResults = list.getJSONArray("TestResult");
				for(int i = 0; i < arrayResults.length(); i++)
				{
					activity.arrayAdapter.add(arrayResults.getJSONObject(i).get("name").toString(), "Score: " + arrayResults.getJSONObject(i).get("score"), R.drawable.checked);
				}
				Log.d("mobile", " (original) json: " +json.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}

		protected void onProgressUpdate(String... progress)
		{

		}

		protected void onPostExecute(Void test)
		{	
			isFinished = true;
			
			if (activity==null)
			{
				Log.w(TAG, "onPostExecute() skipped -- no activity");
			}
			else 
			{
				activity.setListAdapter(arrayAdapter);
				
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
		    
		void attach(BenchmarkHistoryResult activity) 
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
