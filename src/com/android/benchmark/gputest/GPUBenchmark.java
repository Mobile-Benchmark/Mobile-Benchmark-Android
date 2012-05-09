package com.android.benchmark.gputest;

import android.app.Activity;
import android.os.Bundle;

import com.android.benchmark.R;

public class GPUBenchmark extends Activity
{	
	/*
	 * Get the lowest FPS and the biggest change in FPS (in x time)
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpu_benchmark);
    }
    
    // 2D Benchmark (bitmaps, lines, font, text, GUI elements)
    @SuppressWarnings("unused")
	private void start2DBenchmark()
    {
    	
    }
    
    // 3D Benchmark (rendering)
    @SuppressWarnings("unused")
	private void start3DBenchmark()
    {
    	
    }
}
