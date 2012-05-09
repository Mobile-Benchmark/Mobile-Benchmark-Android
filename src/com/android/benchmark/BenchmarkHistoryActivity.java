package com.android.benchmark;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.benchmark.fetcher.URLFetcher;
import com.android.benchmark.hardwareinformation.ListViewCustomAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class BenchmarkHistoryActivity extends Activity
{
	private static final String TAG = "mobile";
	
	private static SimpleDateFormat xsd = new SimpleDateFormat(
			"yyyy-MM-dd'T'kk:mm:ss");
	private static SimpleDateFormat xsn = new SimpleDateFormat(
			"yyyy-MM-dd kk:mm:ss");
	private static final String dialogMessage = "Collecting Historical data";
	
	private ListView listView;
	private ListViewCustomAdapter arrayAdapter;
	private JSONArray arrayResults;
	private FetchingHistoryTask fetchingHistoryTask;
	private ProgressDialog progressDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.benchmarkhistoryactivity);

		this.listView = (ListView) findViewById(R.id.ListView);
		this.progressDialog = new ProgressDialog(this);
		this.progressDialog.setMessage(dialogMessage);
		
		arrayAdapter = new ListViewCustomAdapter(this);

		fetchingHistoryTask = (FetchingHistoryTask) getLastNonConfigurationInstance();

		if (fetchingHistoryTask == null)
		{
			fetchingHistoryTask = new FetchingHistoryTask(this);
			fetchingHistoryTask.execute();
		}
		else
		{
			fetchingHistoryTask.attach(this);
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance()
	{    	
		if(fetchingHistoryTask != null)
		{
			fetchingHistoryTask.detach();
		}
		
		return (fetchingHistoryTask);
	}
	
	@Override
	protected void onPause()
	{
    	super.onPause();
		progressDialog.dismiss();
	}

	private class FetchingHistoryTask extends AsyncTask<String, String, Void>
	{
		private BenchmarkHistoryActivity activity;
		private boolean isFinished = false;

		public FetchingHistoryTask(BenchmarkHistoryActivity activity)
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
				String phoneID = activity.getPhoneID();
				if (!phoneID.equals(""))
				{
					phoneID = phoneID.substring(0, phoneID.length() - 1);
					JSONObject json = URLFetcher
							.connectCustom("BenchmarksFromPhoneID/" + phoneID);

					if (URLFetcher.isValidRequest())
					{
						JSONObject list = json.getJSONObject("list");
						Object object = list.get("Benchmark");

						java.util.Date date = null;

						if (object.getClass().equals(JSONObject.class))
						{
							JSONObject objectResult = list
									.getJSONObject("Benchmark");
							String datetime = (String) objectResult
									.get("timestamp");
							date = xsd.parse(datetime);
							activity.arrayAdapter.add(
									"Date: " + xsn.format(date),
									"Total Benchmark Score: "
											+ objectResult.get("total_score"),
									R.drawable.stats);
							arrayResults = new JSONArray();
							arrayResults.put(objectResult);
						}
						else if (object.getClass().equals(JSONArray.class))
						{
							arrayResults = list.getJSONArray("Benchmark");
							for (int i = 0; i < arrayResults.length(); i++)
							{
								String datetime = (String) arrayResults
										.getJSONObject(i).get("timestamp");
								date = xsd.parse(datetime);
								activity.arrayAdapter.add("Date: " + xsn.format(date),
										"Total Benchmark Score: "
												+ arrayResults.getJSONObject(i)
														.get("total_score"),
										R.drawable.stats);
							}
						}
					}
					else
					{
						activity.arrayAdapter.add("", "Geen testen gevonden",
								R.drawable.unchecked);
					}
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			catch (ParseException e)
			{
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
				activity.listView.setAdapter(activity.arrayAdapter);
	
				activity.listView.setOnItemClickListener(new OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> a, View v, int position,
							long id)
					{
						BenchmarkApplication application = (BenchmarkApplication) getApplication();
						try
						{
							application.setBenchmarkKey(arrayResults
									.getJSONObject(position).get("key").toString());
						}
						catch (JSONException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Intent intent = new Intent(BenchmarkHistoryActivity.this,
								BenchmarkHistoryResult.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						activity.startActivity(intent);
					}
				});				
				
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
		    
		void attach(BenchmarkHistoryActivity activity) 
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
	

	private String getPhoneID()
	{
		String id = "";
		String deviceID = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.ANDROID_ID);
		id = Installation.getID();
		if (deviceID != null)
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
		catch (UnsupportedEncodingException e)
		{
			Log.e("mobile", "SHA1: " + e.getMessage());
		}
		catch (NoSuchAlgorithmException e)
		{
			Log.e("mobile", "SHA1: " + e.getMessage());
		}
		catch (InvalidKeyException e)
		{
			Log.e("mobile", "SHA1: " + e.getMessage());
		}
		return Base64.encodeToString(bytes, Base64.DEFAULT + Base64.URL_SAFE);
	}

}
