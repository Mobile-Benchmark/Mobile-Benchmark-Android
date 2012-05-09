package com.android.benchmark.memorytest;

import android.app.Activity;
import android.os.Bundle;

import com.android.benchmark.R;

public class MemoryBenchMark extends Activity 
{	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memory_benchmark);
    }
    
    // Read / write speed of device memory and efficiency
    @SuppressWarnings("unused")
	private void startMemoryBenchmark()
    {
    	
    }
}
