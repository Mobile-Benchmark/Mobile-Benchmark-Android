package com.android.benchmark.hardwareinformation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.MatchResult;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;

import com.android.benchmark.BenchmarkApplication;
import com.android.benchmark.R;
import com.android.benchmark.iostoragetest.IOStorageBenchmark;

public class HardwareInformation extends Activity
{
	/*
	 * This class gets all remaining valuable hardware information of the system
	 * (the more and faster hardware info the higher the rating), like: - GPS
	 * (max. strength etc.) - Leds (how many + which colors etc.) - Screen
	 * resolution + screen DPI (density) + screen refresh rate - Megapixel
	 * camera - Accelerometer sensor - Gravity sensor - Gyroscope sensor - Light
	 * sensor - Linear acceleration sensor - Pressure sensor - Magnetic field
	 * sensor - Temperature sensor - Wifi + Bluetooth - Network type - Phone
	 * type - Data connection type
	 */

	private ListView listView;

	private int progress;
	private ListViewCustomAdapter arrayAdapter;
	private static final String BOGOMIPS_PATTERN = "BogoMIPS[\\s]*:[\\s]*(\\d+\\.\\d+)[\\s]*\n";
	@SuppressWarnings("unused")
	private static final String MEMTOTAL_PATTERN = "MemTotal[\\s]*:[\\s]*(\\d+)[\\s]*kB\n";
	@SuppressWarnings("unused")
	private static final String MEMFREE_PATTERN = "MemFree[\\s]*:[\\s]*(\\d+)[\\s]*kB\n";

	private FetchHardwareInfo fetchHardwareInfoTask;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.hardware_information);
		setProgressBarVisibility(true);
		this.listView = (ListView) findViewById(R.id.ListView);
		// startCollectInformation();
		//ArrayList<String> listItems = new ArrayList<String>();
		arrayAdapter = new ListViewCustomAdapter(this);
		//arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, listItems);
		listView.setAdapter(arrayAdapter);

		fetchHardwareInfoTask=(FetchHardwareInfo)getLastNonConfigurationInstance();
	    
		if (fetchHardwareInfoTask==null)
		{
			fetchHardwareInfoTask = new FetchHardwareInfo(this);
			fetchHardwareInfoTask.execute();
		}
		else
		{
			fetchHardwareInfoTask.attach(this);		
		}
	}

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
    	//savedInstanceState.putParcelableArrayList("array", arrayAdapter);
    	super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) 
    {
    	super.onRestoreInstanceState(savedInstanceState);    
    	//
    }  
	
	
    @Override
    public Object onRetainNonConfigurationInstance()
    {
    	fetchHardwareInfoTask.detach();

    	return(fetchHardwareInfoTask);
    }
	
	
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		overridePendingTransition(0, 0);
	}

	public void setProgressPercent(String status, String item, Integer percentage) 
	{
		if(item.equals("Failed"))
		{
			arrayAdapter.add(status, item, R.drawable.unchecked);
		}
		else
		{
			arrayAdapter.add(status, item, R.drawable.checked);
		}
		setProgress(this.progress += (percentage*100));
		arrayAdapter.notifyDataSetChanged();
		
	}


	// ----------------------------------MAX CPU
	// SCALING-------------------------------------//
	public static int getCPUFrequencyMaxScaling() throws Exception {
		return readSystemFileAsInt("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
	}

	// ----------------------------------MAX
	// CPU-------------------------------------------//
	public static int getCPUFrequencyMax() throws Exception {
		return readSystemFileAsInt("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
	}

	private static int readSystemFileAsInt(final String pSystemFile)
			throws Exception {
		InputStream in = null;
		try {
			final Process process = new ProcessBuilder(new String[] {
					"/system/bin/cat", pSystemFile }).start();
			in = process.getInputStream();
			final String content = readFully(in);
			return Integer.parseInt(content);
		} catch (final Exception e) {
			throw new Exception(e);
		}
	}

	public static final String readFully(final InputStream pInputStream)
			throws IOException {
		final StringBuilder sb = new StringBuilder();
		final Scanner sc = new Scanner(pInputStream);
		while (sc.hasNextLine()) {
			sb.append(sc.nextLine());
		}
		return sb.toString();
	}

	// ----------------------------------BOGO
	// MIPS-------------------------------------------//
	public static float getCPUBogoMips() throws Exception {
		final MatchResult matchResult = matchSystemFile("/proc/cpuinfo",
				BOGOMIPS_PATTERN, 1000);

		try {
			if (matchResult.groupCount() > 0) {
				return Float.parseFloat(matchResult.group(1));
			} else {
				throw new Exception();
			}
		} catch (final NumberFormatException e) {
			throw new Exception(e);
		}
	}

	private static MatchResult matchSystemFile(final String pSystemFile,
			final String pPattern, final int pHorizon) throws Exception {
		InputStream in = null;
		try {
			final Process process = new ProcessBuilder(new String[] {
					"/system/bin/cat", pSystemFile }).start();
			in = process.getInputStream();
			final Scanner scanner = new Scanner(in);
			final boolean matchFound = scanner.findWithinHorizon(pPattern,
					pHorizon) != null;
			if (matchFound) {
				return scanner.match();
			} else {
				throw new Exception();
			}
		} catch (final IOException e) {
			throw new Exception(e);
		}
	}

	public void testingDone(double testresult) {
		BenchmarkApplication application = (BenchmarkApplication) getApplication();
		application.setHardwareBenchMarkScore((int)testresult);
        if(application.getTestStatus())
        {
			Intent intent = new Intent(HardwareInformation.this,IOStorageBenchmark.class);
			startActivity(intent);
        }
	}

}
