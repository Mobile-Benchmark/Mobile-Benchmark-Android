package com.android.benchmark.internettest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.android.benchmark.BenchmarkApplication;
import com.android.benchmark.R;
import com.android.benchmark.ResultsActivity;

public class InternetTestBenchmark extends Activity {

	private static final String TAG = InternetTestBenchmark.class.getSimpleName();
	private static final int EXPECTED_SIZE_IN_BYTES = 1048576;// 1MB 1024*1024

	private static final double EDGE_THRESHOLD = 176.0;
	private static final double BYTE_TO_KILOBIT = 0.0078125;
	private static final double KILOBIT_TO_MEGABIT = 0.0009765625;
	private static final double SPEED_FACTOR_WIFI = 1;
	private static final double SPEED_FACTOR_MOBILE = 4;
	
	private TextView mTxtSpeed;
	private TextView mTxtConnectionSpeed;
	private TextView mTxtProgress;
	private TextView mTxtNetwork;

	private double averageSpeed;
	private int netType;
	private ConnectivityManager mConnectivity;
	private final int MSG_UPDATE_STATUS = 0;
	private final int MSG_UPDATE_CONNECTION_TIME = 1;
	private final int MSG_COMPLETE_STATUS = 2;

	private final static int UPDATE_THRESHOLD = 300;
	private Thread internetSpeedTestThread = null;
	private MainHander mainHandler = null;
	private MainWorker mainWorker = null;
	
	private DecimalFormat mDecimalFormater;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDecimalFormater = new DecimalFormat("##.##");
		
		// Request the progress bar to be shown in the title
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.speedtest);
		
		mConnectivity =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		//Get type network Mobile or Wi-Fi
		NetworkInfo info = mConnectivity.getActiveNetworkInfo();
		if( info != null)
		{
			netType = info.getType();
		}
		mainHandler=(MainHander)getLastNonConfigurationInstance();
		bindListeners();
	}

    @Override
    public Object onRetainNonConfigurationInstance()
    {
    	mainHandler.detach();
    	return(mainHandler);
    }
	
	@Override
	public void onPause() {
		super.onPause();
		overridePendingTransition(0, 0);
	}
	
	
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {    	
    	savedInstanceState.putString("mTxtSpeed", (String) mTxtSpeed.getText());
    	savedInstanceState.putString("mTxtConnectionSpeed", (String) mTxtConnectionSpeed.getText());
    	savedInstanceState.putString("mTxtProgress", (String) mTxtProgress.getText());
    	savedInstanceState.putString("mTxtNetwork", (String) mTxtNetwork.getText());
    	super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) 
    {
    	super.onRestoreInstanceState(savedInstanceState);    	
        this.mTxtSpeed.setText(savedInstanceState.getString("mTxtSpeed"));
        this.mTxtConnectionSpeed.setText(savedInstanceState.getString("mTxtConnectionSpeed"));
        this.mTxtProgress.setText(savedInstanceState.getString("mTxtProgress"));
        this.mTxtNetwork.setText(savedInstanceState.getString("mTxtNetwork"));
    }   
	
	public void testingDone() {
		BenchmarkApplication application = (BenchmarkApplication) getApplication();			
		application.setSpeedBenchmarkScore(calculateScore());
        if(application.getTestStatus())
        {
			Intent intent = new Intent(InternetTestBenchmark.this, ResultsActivity.class);
			startActivity(intent);
        }
	}
	
	public int calculateScore()
	{
		double result = 0;
		if(netType == ConnectivityManager.TYPE_WIFI)
		{
			result = averageSpeed * SPEED_FACTOR_WIFI;
		}
		else if (netType == ConnectivityManager.TYPE_MOBILE)
		{
			result = averageSpeed * SPEED_FACTOR_MOBILE;
		}
		return (int)result;
	}
	
	/**
	 * Setup event handlers and bind variables to values from xml
	 */
	private void bindListeners() {
		this.mTxtSpeed = (TextView) findViewById(R.id.speed);
		this.mTxtConnectionSpeed = (TextView) findViewById(R.id.connectionspeeed);
		this.mTxtProgress = (TextView) findViewById(R.id.progress);
		this.mTxtNetwork = (TextView) findViewById(R.id.networktype);
		setProgressBarVisibility(true);
		mTxtSpeed.setText("Test started");
		mTxtNetwork.setText(R.string.network_detecting);
		
		if(internetSpeedTestThread == null && mainHandler == null && mainWorker == null)
		{
			mainHandler = new MainHander(this);
			mainWorker = new MainWorker(mainHandler);
			internetSpeedTestThread = new Thread(mainWorker);
			internetSpeedTestThread.start();			
		}
		else
		{
			mainHandler.attach(this);
		}
	}

	private class MainHander extends Handler
	{
		private InternetTestBenchmark activity;
		
		MainHander(InternetTestBenchmark activity)
		{
			attach(activity); 
		}
		
		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_STATUS:
				final SpeedInfo info1 = (SpeedInfo) msg.obj;
				activity.mTxtSpeed.setText(String.format(
						getResources().getString(R.string.update_speed),
						mDecimalFormater.format(info1.kilobits)));
				// Title progress is in range 0..10000
				activity.setProgress(100 * msg.arg1);
				activity.mTxtProgress.setText(String.format(
						getResources().getString(R.string.update_downloaded),
						msg.arg2, EXPECTED_SIZE_IN_BYTES));
				break;
			case MSG_UPDATE_CONNECTION_TIME:
				activity.mTxtConnectionSpeed.setText(String.format(getResources()
						.getString(R.string.update_connectionspeed), msg.arg1));
				break;
			case MSG_COMPLETE_STATUS:
				final SpeedInfo info2 = (SpeedInfo) msg.obj;
				activity.mTxtSpeed.setText(String.format(
						getResources().getString(
								R.string.update_downloaded_complete), msg.arg1,
						info2.kilobits));
				averageSpeed = info2.kilobits ;
				activity.mTxtProgress.setText(String.format(
						getResources().getString(R.string.update_downloaded),
						msg.arg1, EXPECTED_SIZE_IN_BYTES));

				if (networkType(info2.kilobits) == 1) {
					activity.mTxtNetwork.setText(R.string.network_3g);
				} else {
					activity.mTxtNetwork.setText(R.string.network_edge);
				}
				testingDone();
				setProgressBarVisibility(false);
				break;
			default:
				super.handleMessage(msg);
			}
		}
		
		void detach()
		{
			activity = null;
		}
		    
		void attach(InternetTestBenchmark activity) 
		{
			this.activity = activity;
		}
	};

	/**
	 * Our Slave worker that does actually all the work
	 */
	private class MainWorker implements Runnable
	{
		private MainHander mHandler;
		
		public MainWorker(MainHander mHandler) {
			this.mHandler = mHandler;
		}

		@Override
		public void run() {
			InputStream stream = null;
			try {
				int bytesIn = 0;
				String downloadFileUrl = "http://download.thinkbroadband.com/1MB.zip";
				long startCon = System.currentTimeMillis();
				URL url = new URL(downloadFileUrl);
				URLConnection con = url.openConnection();
				con.setUseCaches(false);
				long connectionLatency = System.currentTimeMillis() - startCon;
				stream = con.getInputStream();

				Message msgUpdateConnection = Message.obtain(mHandler,
						MSG_UPDATE_CONNECTION_TIME);
				msgUpdateConnection.arg1 = (int) connectionLatency;
				mHandler.sendMessage(msgUpdateConnection);

				long start = System.currentTimeMillis();
				@SuppressWarnings("unused")
				int currentByte = 0;
				long updateStart = System.currentTimeMillis();
				long updateDelta = 0;
				int bytesInThreshold = 0;

				while ((currentByte = stream.read()) != -1) {
					bytesIn++;
					bytesInThreshold++;
					if (updateDelta >= UPDATE_THRESHOLD) {
						int progress = (int) ((bytesIn / (double) EXPECTED_SIZE_IN_BYTES) * 100);
						Message msg = Message.obtain(mHandler,
								MSG_UPDATE_STATUS,
								calculate(updateDelta, bytesInThreshold));
						msg.arg1 = progress;
						msg.arg2 = bytesIn;
						mHandler.sendMessage(msg);
						// Reset
						updateStart = System.currentTimeMillis();
						bytesInThreshold = 0;
					}
					updateDelta = System.currentTimeMillis() - updateStart;
				}

				long downloadTime = (System.currentTimeMillis() - start);
				// Prevent AritchmeticException
				if (downloadTime == 0) {
					downloadTime = 1;
				}

				Message msg = Message.obtain(mHandler, MSG_COMPLETE_STATUS,
						calculate(downloadTime, bytesIn));
				msg.arg1 = bytesIn;
				mHandler.sendMessage(msg);
			} catch (MalformedURLException e) {
				Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			} finally {
				try {
					if (stream != null) {
						stream.close();
					}
				} catch (IOException e) {
					// Suppressed
				}
			}

		}
	};

	/**
	 * Get Network type from download rate
	 * 
	 * @return 0 for Edge and 1 for 3G
	 */
	private int networkType(final double kbps) {
		int type = 1;// 3G
		// Check if its EDGE
		if (kbps < EDGE_THRESHOLD) {
			type = 0;
		}
		return type;
	}



	/**
	 * 
	 * 1 byte = 0.0078125 kilobits 1 kilobits = 0.0009765625 megabit
	 * 
	 * @param downloadTime
	 *            in miliseconds
	 * @param bytesIn
	 *            number of bytes downloaded
	 * @return SpeedInfo containing current speed
	 */
	private SpeedInfo calculate(final long downloadTime, final long bytesIn) {
		SpeedInfo info = new SpeedInfo();
		// from mil to sec
		long bytespersecond = (bytesIn / downloadTime) * 1000;
		double kilobits = bytespersecond * BYTE_TO_KILOBIT;
		double megabits = kilobits * KILOBIT_TO_MEGABIT;
		info.downspeed = bytespersecond;
		info.kilobits = kilobits;
		info.megabits = megabits;

		return info;
	}

	/**
	 * Transfer Object
	 * 
	 * @author devil
	 * 
	 */
	private static class SpeedInfo {
		public double kilobits = 0;
		@SuppressWarnings("unused")
		public double megabits = 0;
		@SuppressWarnings("unused")
		public double downspeed = 0;
	}

	// Private fields

}