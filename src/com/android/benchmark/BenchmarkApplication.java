package com.android.benchmark;

import android.app.Application;


public class BenchmarkApplication extends Application
{
	/*
	 * This class saves all benchmark data which will be collected during the tests
	 */
	

	private int cpuBenchmarkScore = 0;
	private int ioInternBenchmarkScore = 0;
	private int ioExternBenchmarkScore = 0;
	private int speedBenchmarkScore = 0;
	private int hardwareBenchMarkScore = 0;
	private int submittedScore = 0;
	private String benchmarkKey;
	private Boolean testStatus = false;
	
    public BenchmarkApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public String getBenchmarkKey()
    {
    	return benchmarkKey;
    }
    
    
    public void setBenchmarkKey(String key)
    {
    	benchmarkKey = key;
    }
    public int getSubmittedScore()
    {
    	return submittedScore;
    }
    
    public void setSubmittedScore(int submittedScore)
    {
    	this.submittedScore = submittedScore;
    }
    
	public Boolean getTestStatus() {
		return testStatus;
	}
	
	public void setTestStatus(Boolean testStatus) {
		this.testStatus = testStatus;
	}
	
	public void setCPUBenchmarkScore(int cpuBenchmarkScore)
	{
		this.cpuBenchmarkScore = cpuBenchmarkScore;
	}
	
	public int getCPUBenchmarkScore()
	{
		return cpuBenchmarkScore;
	}
	
	public void setIOExternBenchmarkScore(int ioExternBenchmarkScore)
	{
		this.ioExternBenchmarkScore = ioExternBenchmarkScore;
	}
	
	public int getIOExternBenchmarkScore()
	{
		return ioExternBenchmarkScore;
	}
	
	public void setIOInternBenchmarkScore(int ioInternBenchmarkScore)
	{
		this.ioInternBenchmarkScore = ioInternBenchmarkScore;
	}
	
	public int getIOInternBenchmarkScore()
	{
		return ioInternBenchmarkScore;
	}
	
	public void setSpeedBenchmarkScore(int speedBenchmarkScore)
	{
		this.speedBenchmarkScore = speedBenchmarkScore;
	}
	
	public int getSpeedBenchmarkScore()
	{
		return speedBenchmarkScore;
	}
	
	public void setHardwareBenchMarkScore(int hardwareBenchMarkScore)
	{
		this.hardwareBenchMarkScore = hardwareBenchMarkScore;
	}
	
	public int getHardwareBenchMarkScore()
	{
		return hardwareBenchMarkScore;
	}
	
	public boolean getGlobalTestStatus()
	{
		return !(hardwareBenchMarkScore == 0 || speedBenchmarkScore == 0 || ioExternBenchmarkScore == 0 || ioInternBenchmarkScore == 0 ||cpuBenchmarkScore == 0);
	}
	
	public int getTotalBenchMarkScore()
	{
		return (hardwareBenchMarkScore + speedBenchmarkScore + ioExternBenchmarkScore + ioExternBenchmarkScore + cpuBenchmarkScore);
	}

	
}