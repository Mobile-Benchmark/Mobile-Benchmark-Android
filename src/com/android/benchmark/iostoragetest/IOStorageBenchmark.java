package com.android.benchmark.iostoragetest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;

import com.android.benchmark.BenchmarkApplication;
import com.android.benchmark.R;
import com.android.benchmark.cputest.CPUBenchmark;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

//TODO : iops

public class IOStorageBenchmark extends Activity 
{
	private static final String TAG = "mobile";
	private static final int TOTAL_TESTS = 2; // 2 = total number of tests
	private static final double totalFileSize = 1854.48047; // 1898988 bytes
	private static final double SPEED_FACTOR = 0.65;
	
	private TextView internReadSpeedText;
	private TextView internWriteSpeedText;	
	private TextView externReadSpeedText;
	private TextView externWriteSpeedText;
	
	private ProgressDialog progressDialog;
	private StorageBenchmark storageBenchmarkTask = null;
	private String eol = System.getProperty("line.separator");
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.io_benchmark);        
        
		internReadSpeedText = (TextView) findViewById(R.id.internReadSpeedText);
		internWriteSpeedText = (TextView) findViewById(R.id.internWriteSpeedText);		
		externReadSpeedText = (TextView) findViewById(R.id.externReadSpeedText);
		externWriteSpeedText = (TextView) findViewById(R.id.externWriteSpeedText);
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("");
		progressDialog.setMessage("Loading. Please wait...");
		progressDialog.setIndeterminate(true);
		
		storageBenchmarkTask = (StorageBenchmark)getLastNonConfigurationInstance();
		
        //start Benchmark
		
		if (storageBenchmarkTask == null)
		{
			progressDialog.show();
			// Write/read speed test of intern memory and SDCard if present
			storageBenchmarkTask = new StorageBenchmark(this);
			storageBenchmarkTask.execute();
		}
		else
		{
			if(!storageBenchmarkTask.isTestDone())
			{
				progressDialog.show();
				storageBenchmarkTask.attach(this);
			}
		}
    }    
    
    @Override
    public Object onRetainNonConfigurationInstance() {
    	storageBenchmarkTask.detach();
      
      return(storageBenchmarkTask);
    }
    
    
    @Override
    protected void onPause()
    {
    	super.onPause();
    	progressDialog.dismiss();
    }
    

	protected class StorageBenchmark extends AsyncTask<Void, Void, Void>
	{
		boolean isTestDone = false;
		IOStorageBenchmark activity = null;
		
		StorageBenchmark(IOStorageBenchmark activity)
		{
			attach(activity);
		}
		
    	//Intern Storage   
    	double[][] internTestsResults = new double[TOTAL_TESTS][2];
    	double[][] externTestsResults = new double[TOTAL_TESTS][2];
    	
		protected Void doInBackground(Void... arg0) 
		{			
	    	for(int i = 0; i < TOTAL_TESTS; i++)
	    	{
		    	double[] rw = startInternStorageBenchmark();
		    	internTestsResults[i][0] = rw[0]; //read
		    	internTestsResults[i][1] = rw[1]; //write
	    	}

	    	for(int i = 0; i < TOTAL_TESTS; i++)
	    	{
		    	double[] rw = startExternStorageBenchmark();
		    	externTestsResults[i][0] = rw[0]; //read
		    	externTestsResults[i][1] = rw[1]; //write
	    	}
			return null;
		}		
		
		@Override
		protected void onPostExecute(Void result)
	    {
			activity.progressDialog.dismiss();
					
			//calculating average
	    	double internReadTotal = 0.0, internWriteTotal = 0.0, externReadTotal = 0.0, externWriteTotal = 0.0;
	    	for(int i = 0; i < TOTAL_TESTS; i++)
	    	{
	    		internReadTotal += internTestsResults[i][0]; //read
	    		internWriteTotal += internTestsResults[i][1]; //write
	    		
	    		externReadTotal += externTestsResults[i][0]; //read
	    		externWriteTotal += externTestsResults[i][1]; //write
	    	}
	    	double internReadAverage = internReadTotal / TOTAL_TESTS;
	    	double internWriteAverage = internWriteTotal / TOTAL_TESTS;
	    	
	    	double externReadAverage = externReadTotal / TOTAL_TESTS;
	    	double externWriteAverage = externWriteTotal / TOTAL_TESTS;
	    	
	    	isTestDone = true;
			DecimalFormat twoDigitFormat = new DecimalFormat("#.####");	
			
			activity.internReadSpeedText.setText(String.valueOf(twoDigitFormat.format(internReadAverage) ) + " kB/s");		   
			activity.internWriteSpeedText.setText(String.valueOf(twoDigitFormat.format(internWriteAverage) ) + " kB/s");
			
			activity.externReadSpeedText.setText(String.valueOf(twoDigitFormat.format(externReadAverage) ) + " kB/s");		   
			activity.externWriteSpeedText.setText(String.valueOf(twoDigitFormat.format(externWriteAverage) ) + " kB/s");	
	    		    	
			activity.testingDone(internReadAverage + internWriteAverage, externReadAverage +  externWriteAverage);
	    }
		
		void detach()
		{
			activity = null;
		}
		
	    void attach(IOStorageBenchmark activity)
	    {
	    	this.activity = activity;
	    }
	    
	    boolean isTestDone()
	    {
	    	return isTestDone;
	    }
	}
	
    private double[] startInternStorageBenchmark()
    {
    	double[] returnValue = new double[2];
    	long start = 0;
    	double readDuration = 0.0, writeDuration = 0.0;
		    	
		try
		{
			OutputStreamWriter writeFile = new OutputStreamWriter(openFileOutput("iotest.txt", MODE_PRIVATE));
			
			start = System.nanoTime();	
			BufferedWriter writer = new BufferedWriter(writeFile);
			writeContenToFile(writer);
			writer.close();
			writeDuration = (System.nanoTime() - start) / 1000.0; //탎
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getMessage());
		}

		StringBuffer buffer = new StringBuffer();		
		try
		{
			BufferedReader input = new BufferedReader(new InputStreamReader(openFileInput("iotest.txt")));
			String line;
			start = System.nanoTime();	
			while ((line = input.readLine()) != null)
			{
				buffer.append(line + eol);
			}
			readDuration = (System.nanoTime() - start) / 1000.0; //탎
		} 
		catch (Exception e)
		{
			Log.d(TAG, e.getMessage());
		}					
		
		//fileContent.setText(buffer.toString());
		// unnecessary files, clean up our mess
		if(!this.deleteFile("iotest.txt"))
		{
			Log.e(TAG, "File is NOT deleted");
		}		

		returnValue[0] = totalFileSize / (readDuration  / 1000000);
		returnValue[1] = totalFileSize / (writeDuration / 1000000);
		return returnValue;
    }
    
    public void testingDone(double internResult, double externResult) 
    {
    	BenchmarkApplication application = (BenchmarkApplication) getApplication();
    	
    	application.setIOInternBenchmarkScore((int) (internResult * SPEED_FACTOR));
    	application.setIOExternBenchmarkScore((int) (externResult * SPEED_FACTOR));
        if(application.getTestStatus())
        {
			Intent intent = new Intent(IOStorageBenchmark.this, CPUBenchmark.class);
			startActivity(intent);
        }
	}

	private double[] startExternStorageBenchmark()
    {    	
    	double[] returnValue = new double[2];
    	long start = 0;
    	double fileSize = 0;
    	double readDuration = 0.0, writeDuration = 0.0;
    	File sdRoot = Environment.getExternalStorageDirectory();
    	
    	//write
    	try 
    	{
    	    if (sdRoot.canWrite())
    	    {
    	        File gpxfile = new File(sdRoot, "iotest.txt");
    	        FileWriter gpxwriter = new FileWriter(gpxfile);
    	        BufferedWriter writer = new BufferedWriter(gpxwriter);
    	        start = System.nanoTime();	
    	        writeContenToFile(writer);
    	        writer.close();
    	        writeDuration = (System.nanoTime() - start) / 1000.0; //탎
    	    }
    		else
    		{
    			Log.e(TAG, "Can't write file!");
    		}
    	}
    	catch (IOException e)
    	{
    	    Log.e(TAG, "Could not write file " + e.getMessage());
    	}
    	
    	//read    	
    	File file = new File(sdRoot, "iotest.txt");
    	StringBuilder text = new StringBuilder();
    	try
    	{
    		if (sdRoot.canRead())
    	    {
	    	    BufferedReader input = new BufferedReader(new FileReader(file));
	    	    String line;
	    	    start = System.nanoTime();
	    	    while ((line = input.readLine()) != null) 
	    	    {
	    	        text.append(line + eol);
	    	    }
	    	    readDuration = (System.nanoTime() - start) / 1000.0; //탎
    	    }
    		else
    		{
    			Log.e(TAG, "Can't read file!");
    		}
    	}
    	catch (IOException e)
    	{
    		Log.d(TAG, e.getMessage());
    	}
    	
		// get file size
		try
		{
			File tempFile = new File(sdRoot,  "iotest.txt");			
			fileSize = ((double)tempFile.length() / 1024.0);  // fileSize = kB
			tempFile.delete(); //delete file
			Log.d(TAG, fileSize + " kB");
		}
		catch(Exception e)
		{
			Log.d(TAG, "File not found : " + e.getMessage());
		}
		
		returnValue[0] = fileSize / (readDuration  / 1000000);
		returnValue[1] = fileSize / (writeDuration / 1000000);
		
		return returnValue;
    }
    
    private void writeContenToFile(BufferedWriter writer) throws IOException
    {
    	for(int i = 0; i < 52; i++)
    	{
			writer.write("Lorem ipsum dolor sit amet tincidunt. Non auctor eget in est a pellentesque mauris imperdiet vestibulum tortor eget. Dapibus magnis enim suscipit nunc ut nunc leo venenatis convallis commodo libero. Tellus feugiat mauris. Urna risus ut. Justo wisi eu eu ac in ut vitae pellentesque. Velit in cras. Est taciti adipiscing mi tellus quis. Magna magna adipiscing. Cras libero velit." + eol);
			writer.write("Auctor urna magna sed varius aenean eget in blandit. Neque ridiculus repudiandae. Nulla habitant tempor faucibus nunc ligula morbi erat porttitor gravida blandit morbi. Adipiscing ante ipsum pellentesque cumque turpis. Commodo diam ac. Arcu vitae accumsan aut rhoncus donec. Eu rutrum hymenaeos. Duis pede varius in rhoncus integer interdum lorem auctor donec urna lorem est lacus vel ullamcorper libero aliquet. Assumenda in dolor. Duis pellentesque aliquet. Ullamcorper rutrum sed penatibus magna a dui lorem mauris. Ut ipsum mus tempor elit tincidunt consectetuer sed blanditiis. Porta est ad. Ad facilisis lacus. Vulputate lectus etiam. Consectetuer per potenti elit in eget. Vel eu neque varius facilisis posuere. Mi at dictum. Non purus vel. Id pellentesque ac dignissim sed pharetra lectus vel vitae." + eol);
			writer.write("Leo justo et at molestiae convallis vestibulum non dui suscipit nulla nunc molestie ut adipiscing. Semper convallis in fermentum urna a. Dignissim vestibulum quam. Suspendisse in ut. Nulla neque convallis." + eol);
			writer.write("Congue a ut. In a metus morbi libero accumsan. Ultrices sollicitudin aliquet. Tellus risus neque leo dui venenatis. Id molestie non. Tellus ac ac convallis semper tortor arcu in id malesuada porttitor aliquet. Quisque scelerisque dictum et magna aliquet morbi sodales commodo diam lacinia amet. Tellus et senectus eget sed sit. Elit imperdiet anim. Nullam nulla sit fermentum sed arcu." + eol);
			writer.write("Urna eu sodales. Arcu tempor venenatis. Facilisis feugiat sit curabitur ridiculus in nisl consectetuer nam. Lorem repellendus aptent. Laoreet nulla nulla. Nec sint odio. Posuere rutrum lacus. Nunc eum vestibulum laoreet potenti wisi quam odio vitae vestibulum elementum vestibulum. Vel hac sem velit dictum est id vivamus praesent. Consectetuer eu sollicitudin taciti vestibulum cum porta vel nec. Penatibus cubilia eu. In sem ut iaculis lectus tortor cursus elit ac vitae porta quis nullam arcu urna. Ac cras orci. Curabitur mattis dolor imperdiet lorem aliquam. Rutrum pellentesque metus quam vivamus in." + eol);
			writer.write("Nulla ligula posuere leo eget in. Lacus nec aliquam. Arcu vestibulum lectus pharetra nascetur euismod vitae dictum turpis. Justo suscipit libero. Scelerisque elit mi urna condimentum quia. Donec vitae ad vestibulum torquent tempus. Fusce erat dolor interdum amet sed massa aenean lectus. Adipiscing felis nullam quisque nam a. Enim erat labore. Tempor vehicula mauris rutrum malesuada sit lorem vitae bibendum nulla eu sapien. Ac justo tristique vitae nunc turpis. Suspendisse fusce vestibulum. Aut augue sodales. Sit montes varius ad odio malesuada vestibulum condimentum sociosqu pede nullam nullam pretium nec a. Tincidunt pretium accumsan vel adipiscing vitae. Nibh massa orci. Aliquam luctus mauris velit ac nibh. Ornare sed quis magna natoque nunc et eu et. Nulla primis arcu. Orci pellentesque rutrum." +  eol);
			writer.write("Aenean lobortis commodo. Condimentum integer non. Maecenas donec molestie vestibulum augue vitae ultrices praesent a dui pretium eget. Eget felis ac id facilisi morbi. Sed nec ut morbi nec eget. Senectus dui et. Eget pellentesque cursus quis sit elit. Phasellus velit id. Aliquet quis sed praesent elit sociis lacinia pellentesque mollis mauris litora vel lacinia enim mus massa adipiscing molestie. Massa nulla tempus nulla quis dui hendrerit lobortis duis. Id tincidunt malesuada. Quis aliquam pellentesque ullamcorper rutrum venenatis potenti proin purus. Sed sit ligula. Justo sem nulla. Torquent malesuada et. Tellus praesent turpis. In integer at lacus vitae felis. Rhoncus etiam dui metus laoreet reiciendis pretium ullam arcu veritatis ac at sit vestibulum placerat. Et nunc elit. Est semper primis quis nec facilisi et ornare sed. Pulvinar neque risus. Ullamcorper euismod orci est in praesent. Justo porttitor eget. Tempus ad cras in purus cras eu volutpat vel. Eget rutrum sit." +  eol);
			writer.write("Vel fusce in. Viverra donec felis quisque tortor in. Soluta hac leo. Justo lorem vehicula sed integer convallis sit suscipit a donec nam elit urna ligula in vel amet pellentesque ante pellentesque semper. Nullam ultrices dis. Ipsum a sit. Sed bibendum at nullam vel eros lorem erat in aliquam et tortor. Risus amet eu. Sit non vitae torquent ac in dolor congue non. Quis amet quis donec quisque massa. Wisi consequat ac et odio venenatis pede iaculis duis suspendisse libero nunc. Accumsan eget dolor. Duis cras ipsum aenean elit mollis dui nec urna. In erat velit ante per aliquam. Adipiscing velit maecenas at vel donec. Pede commodo sed amet vulputate sed pellentesque ipsum nisl. A lacus ut. Sapien sed in. Leo sed lobortis. Aliquam aenean sed et volutpat accusamus. Imperdiet integer metus. Ac leo luctus. Justo pellentesque nulla. Nec id cras. Mi turpis nec. Curabitur nulla fusce faucibus viverra eros consequat a litora at ut ante nonummy fermentum consequat mauris aenean fringilla. Arcu cras sed tristique malesuada nulla. Orci suscipit habitasse. Consequat wisi vel. Erat hymenaeos posuere ut etiam in." +  eol);
			writer.write("Dui accumsan vitae eu labore fames ac et egestas in justo mollis soluta sed tincidunt bibendum velit tellus molestie suspendisse amet. Vestibulum massa dui. Laoreet tortor varius pede et cras. Donec non lectus. Et sapien enim per cursus arcu. Feugiat inceptos dis tempus ad augue. At ultrices pellentesque. Aliquam adipiscing ligula. Libero ac justo. Pretium quis hendrerit. Molestie wisi lacus. Consectetuer dignissim lectus quisque phasellus massa. Feugiat dui urna interdum fringilla suscipit. Gravida elementum in. Amet commodo arcu facilisis arcu nulla. Nunc quam vitae. Tellus aenean fugit risus neque eros sed molestie elementum consequat orci wisi. Sociis semper sapien. Ante ultricies parturient. Enim nunc duis. Montes quis ut. Egestas in mauris. At eleifend tristique amet quam eros ac libero rutrum dolor felis maecenas morbi ipsum consequat id lectus leo. Vel parturient vestibulum amet nullam massa. Rhoncus lacus blandit aliquet rhoncus phasellus. Placerat dictum blandit id id commodo wisi tincidunt et. In nascetur platea lacus quis elit. Sodales montes sit ac ipsam varius venenatis suspendisse sollicitudin. Pede nihil morbi aliquam eget quam platea nec donec eros mauris augue. Sed facilisis ac." +  eol);
			writer.write("Blandit quisque quis. Pede vel ipsum torquent praesent varius arcu id pellentesque arcu ipsum pellentesque. Mauris libero molestie fermentum non mauris. Morbi faucibus mauris ut neque ut. Sodales erat elit. In tortor leo vulputate pulvinar lacus. Turpis euismod augue consequat vivamus facilisis qui euismod venenatis. Purus scelerisque sociosqu. Ut ipsum dictum. Ac faucibus fermentum suscipit interdum libero lacinia aliquam aenean." +  eol);
			writer.write("Volutpat odio libero. Posuere in donec. Tristique felis hendrerit. Pede conubia accumsan. Vitae ornare a nunc ultricies at. In wisi at libero et aliquam lobortis mauris quis. Ligula orci felis in congue vestibulum arcu donec etiam integer ultrices nunc taciti in integer. In consequat vestibulum nunc eu facilisi volutpat mi etiam. At sed magna rhoncus nisl euismod. Mollis purus etiam." + eol);
			writer.write("Vel libero quam. Vivamus feugiat in aliquam nunc turpis. Vestibulum ut ante. A ac orci integer maecenas vitae. Placerat sed curabitur. Sapien fringilla quis neque inceptos eu. Quis integer nibh. Pellentesque amet sed. Commodo eu mollis elementum vel ut. Et in amet suspendisse placerat massa. Suspendisse vel sit arcu mattis tellus. Risus vivamus tempor. Quam penatibus arcu lectus fusce non. Nulla proident neque. Nec velit pede eum id non phasellus libero quis. Donec proin tristique maecenas penatibus luctus massa adipiscing euismod praesent arcu adipiscing turpis et per. Proin vehicula at. Suspendisse ullamcorper metus. In est interdum quo inceptos est varius sint justo porta nunc pellentesque molestie volutpat pellentesque. Adipiscing malesuada mi dictumst lectus nullam. Arcu semper et viverra elementum deleniti. Erat cursus netus nibh aliquam ipsum porro pretium ac. Quis purus nibh ut sem pellentesque justo quam aliquam. Nulla sit suspendisse nulla vehicula praesent eu diam ullamcorper duis quis sed. Facilisis tincidunt aliquet nec mauris dictumst. Odio ac suspendisse dictumst justo neque egestas leo nonummy. Turpis molestie nunc. Sollicitudin tristique nonummy ridiculus facilisis cras phasellus sed orci. Malesuada rutrum lorem. In arcu duis non phasellus parturient. Enim nisi urna fermentum mauris ut. Amet dui lorem tortor orci est. Vel non rerum. Vehicula sapien id. Massa sollicitudin molestiae nam sit dictum." + eol);
			writer.write("Cras vitae parturient tellus leo praesent pellentesque suspendisse velit vestibulum vestibulum mus. Pretium natoque sit id neque fermentum. Aenean tempor suspendisse. Dis orci vestibulum sit platea nulla laoreet consectetuer lacus. Dolor in ducimus. Mauris ut urna. Ante sed alias sed nonummy proin id cursus ipsum. Inventore ante tincidunt. Suspendisse rutrum eu. Elit cras nam. Lectus adipiscing vestibulum. Donec rhoncus est sed vel imperdiet purus nam orci. Non amet malesuada et in eu gravida tempus malesuada. Ultrices iaculis libero. Eleifend suspendisse suscipit nulla ac quis. Vehicula in cras pulvinar ut elit condimentum eu vehicula faucibus arcu aenean. Imperdiet in tortor erat lobortis placerat. Ac urna interdum neque purus nibh. Venenatis varius sed. Vitae condimentum felis nostra ad pede praesent non primis. Quis rutrum erat. Netus lacus elit. Pede amet fusce nobis lobortis elit dignissim eros augue. Pellentesque maecenas sit quis cupiditate non. Vestibulum curabitur interdum eget vivamus enim. Nullam praesent wisi est habitant aliquet. Lacus risus a. Habitant massa lacinia a aliquam nulla ligula adipiscing pellentesque donec velit turpis. Sodales magna nibh cras sociosqu rhoncus in morbi orci. Nec facilisis aliquam." + eol);
			writer.write("Vulputate metus sed. Posuere vehicula vitae et ut mauris. At sit felis volutpat curae facilisis phasellus orci aliquam. Laoreet mattis justo. Pellentesque lobortis nunc diam lobortis tellus. Etiam justo feugiat. Sollicitudin in mauris. Quam ultrices integer. Ligula wisi et sed odio nam. Tincidunt euismod mauris lectus tincidunt eget mattis metus elit. Sed urna vel. Magna in in. Primis nullam ut. Ante purus commodo et ullamcorper eget platea in elementum sollicitudin wisi ligula. Tortor nec malesuada eleifend lectus integer rutrum pede reprehenderit. Suscipit magnis fringilla. Duis a porta. Ultrices accumsan nec eget vitae magnis. Quisquam et est tortor magna at. Turpis tempor dolor pede ut vestibulum mattis faucibus rutrum. Donec mollis sit. Vestibulum sed odio. Duis est quam felis fusce cras. Nunc luctus sollicitudin donec rutrum turpis. Sed ligula eu. In sit pellentesque. Ad mattis justo. In et volutpat lacus ac quis. Dignissim varius est. Lobortis ut ridiculus donec maecenas eros accumsan tortor sapien mollis assumenda faucibus velit erat mattis ac nisl leo amet vel cras. Sodales accumsan odio sed eros justo. Nec scelerisque massa at sapien enim. Turpis justo vitae. Nunc ac magna vivamus augue vitae. Libero tellus vestibulum. Orci aliquam vivamus. Integer amet massa mauris fringilla nec et feugiat sed nibh nisl curabitur pharetra cras velit duis elit proin fringilla leo ut. Pede et ullamcorper quis sit eget nisl ornare vitae nec phasellus lacinia ullamcorper tellus erat ac vulputate dui. Nisl eu ipsum pede varius tincidunt. Ut netus sit odio et nullam. Urna accumsan eu integer etiam sem condimentum etiam egestas. Nunc amet magna placeat nullam porttitor. Inceptos urna aliquam. Vivamus ipsum wisi luctus et metus fusce amet tellus. Sagittis egestas ac eleifend tempus arcu sed malesuada vitae ut ornare id. Volutpat ipsum vestibulum. Deserunt morbi malesuada. Cras praesent nec quis lacus enim justo feugiat blandit tincidunt dis in. Vel diam urna. At nibh massa. Ac neque tellus. Ac ut pulvinar. Faucibus maecenas eu. Nec eleifend dui mauris adipiscing faucibus integer id accumsan. Penatibus donec nisl ultrices duis donec.");
			writer.write("Lorem ipsum dolor sit amet tincidunt. Non auctor eget in est a pellentesque mauris imperdiet vestibulum tortor eget. Dapibus magnis enim suscipit nunc ut nunc leo venenatis convallis commodo libero. Tellus feugiat mauris. Urna risus ut. Justo wisi eu eu ac in ut vitae pellentesque. Velit in cras. Est taciti adipiscing mi tellus quis. Magna magna adipiscing. Cras libero velit." + eol);
			writer.write("Auctor urna magna sed varius aenean eget in blandit. Neque ridiculus repudiandae. Nulla habitant tempor faucibus nunc ligula morbi erat porttitor gravida blandit morbi. Adipiscing ante ipsum pellentesque cumque turpis. Commodo diam ac. Arcu vitae accumsan aut rhoncus donec. Eu rutrum hymenaeos. Duis pede varius in rhoncus integer interdum lorem auctor donec urna lorem est lacus vel ullamcorper libero aliquet. Assumenda in dolor. Duis pellentesque aliquet. Ullamcorper rutrum sed penatibus magna a dui lorem mauris. Ut ipsum mus tempor elit tincidunt consectetuer sed blanditiis. Porta est ad. Ad facilisis lacus. Vulputate lectus etiam. Consectetuer per potenti elit in eget. Vel eu neque varius facilisis posuere. Mi at dictum. Non purus vel. Id pellentesque ac dignissim sed pharetra lectus vel vitae." + eol);
			writer.write("Leo justo et at molestiae convallis vestibulum non dui suscipit nulla nunc molestie ut adipiscing. Semper convallis in fermentum urna a. Dignissim vestibulum quam. Suspendisse in ut. Nulla neque convallis." + eol);
			writer.write("Congue a ut. In a metus morbi libero accumsan. Ultrices sollicitudin aliquet. Tellus risus neque leo dui venenatis. Id molestie non. Tellus ac ac convallis semper tortor arcu in id malesuada porttitor aliquet. Quisque scelerisque dictum et magna aliquet morbi sodales commodo diam lacinia amet. Tellus et senectus eget sed sit. Elit imperdiet anim. Nullam nulla sit fermentum sed arcu." + eol);
			writer.write("Urna eu sodales. Arcu tempor venenatis. Facilisis feugiat sit curabitur ridiculus in nisl consectetuer nam. Lorem repellendus aptent. Laoreet nulla nulla. Nec sint odio. Posuere rutrum lacus. Nunc eum vestibulum laoreet potenti wisi quam odio vitae vestibulum elementum vestibulum. Vel hac sem velit dictum est id vivamus praesent. Consectetuer eu sollicitudin taciti vestibulum cum porta vel nec. Penatibus cubilia eu. In sem ut iaculis lectus tortor cursus elit ac vitae porta quis nullam arcu urna. Ac cras orci. Curabitur mattis dolor imperdiet lorem aliquam. Rutrum pellentesque metus quam vivamus in." + eol);
			writer.write("Nulla ligula posuere leo eget in. Lacus nec aliquam. Arcu vestibulum lectus pharetra nascetur euismod vitae dictum turpis. Justo suscipit libero. Scelerisque elit mi urna condimentum quia. Donec vitae ad vestibulum torquent tempus. Fusce erat dolor interdum amet sed massa aenean lectus. Adipiscing felis nullam quisque nam a. Enim erat labore. Tempor vehicula mauris rutrum malesuada sit lorem vitae bibendum nulla eu sapien. Ac justo tristique vitae nunc turpis. Suspendisse fusce vestibulum. Aut augue sodales. Sit montes varius ad odio malesuada vestibulum condimentum sociosqu pede nullam nullam pretium nec a. Tincidunt pretium accumsan vel adipiscing vitae. Nibh massa orci. Aliquam luctus mauris velit ac nibh. Ornare sed quis magna natoque nunc et eu et. Nulla primis arcu. Orci pellentesque rutrum." +  eol);
			writer.write("Aenean lobortis commodo. Condimentum integer non. Maecenas donec molestie vestibulum augue vitae ultrices praesent a dui pretium eget. Eget felis ac id facilisi morbi. Sed nec ut morbi nec eget. Senectus dui et. Eget pellentesque cursus quis sit elit. Phasellus velit id. Aliquet quis sed praesent elit sociis lacinia pellentesque mollis mauris litora vel lacinia enim mus massa adipiscing molestie. Massa nulla tempus nulla quis dui hendrerit lobortis duis. Id tincidunt malesuada. Quis aliquam pellentesque ullamcorper rutrum venenatis potenti proin purus. Sed sit ligula. Justo sem nulla. Torquent malesuada et. Tellus praesent turpis. In integer at lacus vitae felis. Rhoncus etiam dui metus laoreet reiciendis pretium ullam arcu veritatis ac at sit vestibulum placerat. Et nunc elit. Est semper primis quis nec facilisi et ornare sed. Pulvinar neque risus. Ullamcorper euismod orci est in praesent. Justo porttitor eget. Tempus ad cras in purus cras eu volutpat vel. Eget rutrum sit." +  eol);
			writer.write("Vel fusce in. Viverra donec felis quisque tortor in. Soluta hac leo. Justo lorem vehicula sed integer convallis sit suscipit a donec nam elit urna ligula in vel amet pellentesque ante pellentesque semper. Nullam ultrices dis. Ipsum a sit. Sed bibendum at nullam vel eros lorem erat in aliquam et tortor. Risus amet eu. Sit non vitae torquent ac in dolor congue non. Quis amet quis donec quisque massa. Wisi consequat ac et odio venenatis pede iaculis duis suspendisse libero nunc. Accumsan eget dolor. Duis cras ipsum aenean elit mollis dui nec urna. In erat velit ante per aliquam. Adipiscing velit maecenas at vel donec. Pede commodo sed amet vulputate sed pellentesque ipsum nisl. A lacus ut. Sapien sed in. Leo sed lobortis. Aliquam aenean sed et volutpat accusamus. Imperdiet integer metus. Ac leo luctus. Justo pellentesque nulla. Nec id cras. Mi turpis nec. Curabitur nulla fusce faucibus viverra eros consequat a litora at ut ante nonummy fermentum consequat mauris aenean fringilla. Arcu cras sed tristique malesuada nulla. Orci suscipit habitasse. Consequat wisi vel. Erat hymenaeos posuere ut etiam in." +  eol);
			writer.write("Dui accumsan vitae eu labore fames ac et egestas in justo mollis soluta sed tincidunt bibendum velit tellus molestie suspendisse amet. Vestibulum massa dui. Laoreet tortor varius pede et cras. Donec non lectus. Et sapien enim per cursus arcu. Feugiat inceptos dis tempus ad augue. At ultrices pellentesque. Aliquam adipiscing ligula. Libero ac justo. Pretium quis hendrerit. Molestie wisi lacus. Consectetuer dignissim lectus quisque phasellus massa. Feugiat dui urna interdum fringilla suscipit. Gravida elementum in. Amet commodo arcu facilisis arcu nulla. Nunc quam vitae. Tellus aenean fugit risus neque eros sed molestie elementum consequat orci wisi. Sociis semper sapien. Ante ultricies parturient. Enim nunc duis. Montes quis ut. Egestas in mauris. At eleifend tristique amet quam eros ac libero rutrum dolor felis maecenas morbi ipsum consequat id lectus leo. Vel parturient vestibulum amet nullam massa. Rhoncus lacus blandit aliquet rhoncus phasellus. Placerat dictum blandit id id commodo wisi tincidunt et. In nascetur platea lacus quis elit. Sodales montes sit ac ipsam varius venenatis suspendisse sollicitudin. Pede nihil morbi aliquam eget quam platea nec donec eros mauris augue. Sed facilisis ac." +  eol);
			writer.write("Blandit quisque quis. Pede vel ipsum torquent praesent varius arcu id pellentesque arcu ipsum pellentesque. Mauris libero molestie fermentum non mauris. Morbi faucibus mauris ut neque ut. Sodales erat elit. In tortor leo vulputate pulvinar lacus. Turpis euismod augue consequat vivamus facilisis qui euismod venenatis. Purus scelerisque sociosqu. Ut ipsum dictum. Ac faucibus fermentum suscipit interdum libero lacinia aliquam aenean." +  eol);
			writer.write("Volutpat odio libero. Posuere in donec. Tristique felis hendrerit. Pede conubia accumsan. Vitae ornare a nunc ultricies at. In wisi at libero et aliquam lobortis mauris quis. Ligula orci felis in congue vestibulum arcu donec etiam integer ultrices nunc taciti in integer. In consequat vestibulum nunc eu facilisi volutpat mi etiam. At sed magna rhoncus nisl euismod. Mollis purus etiam." + eol);
			writer.write("Vel libero quam. Vivamus feugiat in aliquam nunc turpis. Vestibulum ut ante. A ac orci integer maecenas vitae. Placerat sed curabitur. Sapien fringilla quis neque inceptos eu. Quis integer nibh. Pellentesque amet sed. Commodo eu mollis elementum vel ut. Et in amet suspendisse placerat massa. Suspendisse vel sit arcu mattis tellus. Risus vivamus tempor. Quam penatibus arcu lectus fusce non. Nulla proident neque. Nec velit pede eum id non phasellus libero quis. Donec proin tristique maecenas penatibus luctus massa adipiscing euismod praesent arcu adipiscing turpis et per. Proin vehicula at. Suspendisse ullamcorper metus. In est interdum quo inceptos est varius sint justo porta nunc pellentesque molestie volutpat pellentesque. Adipiscing malesuada mi dictumst lectus nullam. Arcu semper et viverra elementum deleniti. Erat cursus netus nibh aliquam ipsum porro pretium ac. Quis purus nibh ut sem pellentesque justo quam aliquam. Nulla sit suspendisse nulla vehicula praesent eu diam ullamcorper duis quis sed. Facilisis tincidunt aliquet nec mauris dictumst. Odio ac suspendisse dictumst justo neque egestas leo nonummy. Turpis molestie nunc. Sollicitudin tristique nonummy ridiculus facilisis cras phasellus sed orci. Malesuada rutrum lorem. In arcu duis non phasellus parturient. Enim nisi urna fermentum mauris ut. Amet dui lorem tortor orci est. Vel non rerum. Vehicula sapien id. Massa sollicitudin molestiae nam sit dictum." + eol);
			writer.write("Cras vitae parturient tellus leo praesent pellentesque suspendisse velit vestibulum vestibulum mus. Pretium natoque sit id neque fermentum. Aenean tempor suspendisse. Dis orci vestibulum sit platea nulla laoreet consectetuer lacus. Dolor in ducimus. Mauris ut urna. Ante sed alias sed nonummy proin id cursus ipsum. Inventore ante tincidunt. Suspendisse rutrum eu. Elit cras nam. Lectus adipiscing vestibulum. Donec rhoncus est sed vel imperdiet purus nam orci. Non amet malesuada et in eu gravida tempus malesuada. Ultrices iaculis libero. Eleifend suspendisse suscipit nulla ac quis. Vehicula in cras pulvinar ut elit condimentum eu vehicula faucibus arcu aenean. Imperdiet in tortor erat lobortis placerat. Ac urna interdum neque purus nibh. Venenatis varius sed. Vitae condimentum felis nostra ad pede praesent non primis. Quis rutrum erat. Netus lacus elit. Pede amet fusce nobis lobortis elit dignissim eros augue. Pellentesque maecenas sit quis cupiditate non. Vestibulum curabitur interdum eget vivamus enim. Nullam praesent wisi est habitant aliquet. Lacus risus a. Habitant massa lacinia a aliquam nulla ligula adipiscing pellentesque donec velit turpis. Sodales magna nibh cras sociosqu rhoncus in morbi orci. Nec facilisis aliquam." + eol);
			writer.write("Vulputate metus sed. Posuere vehicula vitae et ut mauris. At sit felis volutpat curae facilisis phasellus orci aliquam. Laoreet mattis justo. Pellentesque lobortis nunc diam lobortis tellus. Etiam justo feugiat. Sollicitudin in mauris. Quam ultrices integer. Ligula wisi et sed odio nam. Tincidunt euismod mauris lectus tincidunt eget mattis metus elit. Sed urna vel. Magna in in. Primis nullam ut. Ante purus commodo et ullamcorper eget platea in elementum sollicitudin wisi ligula. Tortor nec malesuada eleifend lectus integer rutrum pede reprehenderit. Suscipit magnis fringilla. Duis a porta. Ultrices accumsan nec eget vitae magnis. Quisquam et est tortor magna at. Turpis tempor dolor pede ut vestibulum mattis faucibus rutrum. Donec mollis sit. Vestibulum sed odio. Duis est quam felis fusce cras. Nunc luctus sollicitudin donec rutrum turpis. Sed ligula eu. In sit pellentesque. Ad mattis justo. In et volutpat lacus ac quis. Dignissim varius est. Lobortis ut ridiculus donec maecenas eros accumsan tortor sapien mollis assumenda faucibus velit erat mattis ac nisl leo amet vel cras. Sodales accumsan odio sed eros justo. Nec scelerisque massa at sapien enim. Turpis justo vitae. Nunc ac magna vivamus augue vitae. Libero tellus vestibulum. Orci aliquam vivamus. Integer amet massa mauris fringilla nec et feugiat sed nibh nisl curabitur pharetra cras velit duis elit proin fringilla leo ut. Pede et ullamcorper quis sit eget nisl ornare vitae nec phasellus lacinia ullamcorper tellus erat ac vulputate dui. Nisl eu ipsum pede varius tincidunt. Ut netus sit odio et nullam. Urna accumsan eu integer etiam sem condimentum etiam egestas. Nunc amet magna placeat nullam porttitor. Inceptos urna aliquam. Vivamus ipsum wisi luctus et metus fusce amet tellus. Sagittis egestas ac eleifend tempus arcu sed malesuada vitae ut ornare id. Volutpat ipsum vestibulum. Deserunt morbi malesuada. Cras praesent nec quis lacus enim justo feugiat blandit tincidunt dis in. Vel diam urna. At nibh massa. Ac neque tellus. Ac ut pulvinar. Faucibus maecenas eu. Nec eleifend dui mauris adipiscing faucibus integer id accumsan. Penatibus donec nisl ultrices duis donec.");
			writer.write("Lorem ipsum dolor sit amet tincidunt. Non auctor eget in est a pellentesque mauris imperdiet vestibulum tortor eget. Dapibus magnis enim suscipit nunc ut nunc leo venenatis convallis commodo libero. Tellus feugiat mauris. Urna risus ut. Justo wisi eu eu ac in ut vitae pellentesque. Velit in cras. Est taciti adipiscing mi tellus quis. Magna magna adipiscing. Cras libero velit." + eol);
			writer.write("Auctor urna magna sed varius aenean eget in blandit. Neque ridiculus repudiandae. Nulla habitant tempor faucibus nunc ligula morbi erat porttitor gravida blandit morbi. Adipiscing ante ipsum pellentesque cumque turpis. Commodo diam ac. Arcu vitae accumsan aut rhoncus donec. Eu rutrum hymenaeos. Duis pede varius in rhoncus integer interdum lorem auctor donec urna lorem est lacus vel ullamcorper libero aliquet. Assumenda in dolor. Duis pellentesque aliquet. Ullamcorper rutrum sed penatibus magna a dui lorem mauris. Ut ipsum mus tempor elit tincidunt consectetuer sed blanditiis. Porta est ad. Ad facilisis lacus. Vulputate lectus etiam. Consectetuer per potenti elit in eget. Vel eu neque varius facilisis posuere. Mi at dictum. Non purus vel. Id pellentesque ac dignissim sed pharetra lectus vel vitae." + eol);
			writer.write("Leo justo et at molestiae convallis vestibulum non dui suscipit nulla nunc molestie ut adipiscing. Semper convallis in fermentum urna a. Dignissim vestibulum quam. Suspendisse in ut. Nulla neque convallis." + eol);
			writer.write("Congue a ut. In a metus morbi libero accumsan. Ultrices sollicitudin aliquet. Tellus risus neque leo dui venenatis. Id molestie non. Tellus ac ac convallis semper tortor arcu in id malesuada porttitor aliquet. Quisque scelerisque dictum et magna aliquet morbi sodales commodo diam lacinia amet. Tellus et senectus eget sed sit. Elit imperdiet anim. Nullam nulla sit fermentum sed arcu." + eol);
			writer.write("Urna eu sodales. Arcu tempor venenatis. Facilisis feugiat sit curabitur ridiculus in nisl consectetuer nam. Lorem repellendus aptent. Laoreet nulla nulla. Nec sint odio. Posuere rutrum lacus. Nunc eum vestibulum laoreet potenti wisi quam odio vitae vestibulum elementum vestibulum. Vel hac sem velit dictum est id vivamus praesent. Consectetuer eu sollicitudin taciti vestibulum cum porta vel nec. Penatibus cubilia eu. In sem ut iaculis lectus tortor cursus elit ac vitae porta quis nullam arcu urna. Ac cras orci. Curabitur mattis dolor imperdiet lorem aliquam. Rutrum pellentesque metus quam vivamus in." + eol);
			writer.write("Nulla ligula posuere leo eget in. Lacus nec aliquam. Arcu vestibulum lectus pharetra nascetur euismod vitae dictum turpis. Justo suscipit libero. Scelerisque elit mi urna condimentum quia. Donec vitae ad vestibulum torquent tempus. Fusce erat dolor interdum amet sed massa aenean lectus. Adipiscing felis nullam quisque nam a. Enim erat labore. Tempor vehicula mauris rutrum malesuada sit lorem vitae bibendum nulla eu sapien. Ac justo tristique vitae nunc turpis. Suspendisse fusce vestibulum. Aut augue sodales. Sit montes varius ad odio malesuada vestibulum condimentum sociosqu pede nullam nullam pretium nec a. Tincidunt pretium accumsan vel adipiscing vitae. Nibh massa orci. Aliquam luctus mauris velit ac nibh. Ornare sed quis magna natoque nunc et eu et. Nulla primis arcu. Orci pellentesque rutrum." +  eol);
			writer.write("Aenean lobortis commodo. Condimentum integer non. Maecenas donec molestie vestibulum augue vitae ultrices praesent a dui pretium eget. Eget felis ac id facilisi morbi. Sed nec ut morbi nec eget. Senectus dui et. Eget pellentesque cursus quis sit elit. Phasellus velit id. Aliquet quis sed praesent elit sociis lacinia pellentesque mollis mauris litora vel lacinia enim mus massa adipiscing molestie. Massa nulla tempus nulla quis dui hendrerit lobortis duis. Id tincidunt malesuada. Quis aliquam pellentesque ullamcorper rutrum venenatis potenti proin purus. Sed sit ligula. Justo sem nulla. Torquent malesuada et. Tellus praesent turpis. In integer at lacus vitae felis. Rhoncus etiam dui metus laoreet reiciendis pretium ullam arcu veritatis ac at sit vestibulum placerat. Et nunc elit. Est semper primis quis nec facilisi et ornare sed. Pulvinar neque risus. Ullamcorper euismod orci est in praesent. Justo porttitor eget. Tempus ad cras in purus cras eu volutpat vel. Eget rutrum sit." +  eol);
			writer.write("Vel fusce in. Viverra donec felis quisque tortor in. Soluta hac leo. Justo lorem vehicula sed integer convallis sit suscipit a donec nam elit urna ligula in vel amet pellentesque ante pellentesque semper. Nullam ultrices dis. Ipsum a sit. Sed bibendum at nullam vel eros lorem erat in aliquam et tortor. Risus amet eu. Sit non vitae torquent ac in dolor congue non. Quis amet quis donec quisque massa. Wisi consequat ac et odio venenatis pede iaculis duis suspendisse libero nunc. Accumsan eget dolor. Duis cras ipsum aenean elit mollis dui nec urna. In erat velit ante per aliquam. Adipiscing velit maecenas at vel donec. Pede commodo sed amet vulputate sed pellentesque ipsum nisl. A lacus ut. Sapien sed in. Leo sed lobortis. Aliquam aenean sed et volutpat accusamus. Imperdiet integer metus. Ac leo luctus. Justo pellentesque nulla. Nec id cras. Mi turpis nec. Curabitur nulla fusce faucibus viverra eros consequat a litora at ut ante nonummy fermentum consequat mauris aenean fringilla. Arcu cras sed tristique malesuada nulla. Orci suscipit habitasse. Consequat wisi vel. Erat hymenaeos posuere ut etiam in." +  eol);
			writer.write("Dui accumsan vitae eu labore fames ac et egestas in justo mollis soluta sed tincidunt bibendum velit tellus molestie suspendisse amet. Vestibulum massa dui. Laoreet tortor varius pede et cras. Donec non lectus. Et sapien enim per cursus arcu. Feugiat inceptos dis tempus ad augue. At ultrices pellentesque. Aliquam adipiscing ligula. Libero ac justo. Pretium quis hendrerit. Molestie wisi lacus. Consectetuer dignissim lectus quisque phasellus massa. Feugiat dui urna interdum fringilla suscipit. Gravida elementum in. Amet commodo arcu facilisis arcu nulla. Nunc quam vitae. Tellus aenean fugit risus neque eros sed molestie elementum consequat orci wisi. Sociis semper sapien. Ante ultricies parturient. Enim nunc duis. Montes quis ut. Egestas in mauris. At eleifend tristique amet quam eros ac libero rutrum dolor felis maecenas morbi ipsum consequat id lectus leo. Vel parturient vestibulum amet nullam massa. Rhoncus lacus blandit aliquet rhoncus phasellus. Placerat dictum blandit id id commodo wisi tincidunt et. In nascetur platea lacus quis elit. Sodales montes sit ac ipsam varius venenatis suspendisse sollicitudin. Pede nihil morbi aliquam eget quam platea nec donec eros mauris augue. Sed facilisis ac." +  eol);
			writer.write("Blandit quisque quis. Pede vel ipsum torquent praesent varius arcu id pellentesque arcu ipsum pellentesque. Mauris libero molestie fermentum non mauris. Morbi faucibus mauris ut neque ut. Sodales erat elit. In tortor leo vulputate pulvinar lacus. Turpis euismod augue consequat vivamus facilisis qui euismod venenatis. Purus scelerisque sociosqu. Ut ipsum dictum. Ac faucibus fermentum suscipit interdum libero lacinia aliquam aenean." +  eol);
			writer.write("Volutpat odio libero. Posuere in donec. Tristique felis hendrerit. Pede conubia accumsan. Vitae ornare a nunc ultricies at. In wisi at libero et aliquam lobortis mauris quis. Ligula orci felis in congue vestibulum arcu donec etiam integer ultrices nunc taciti in integer. In consequat vestibulum nunc eu facilisi volutpat mi etiam. At sed magna rhoncus nisl euismod. Mollis purus etiam." + eol);
			writer.write("Vel libero quam. Vivamus feugiat in aliquam nunc turpis. Vestibulum ut ante. A ac orci integer maecenas vitae. Placerat sed curabitur. Sapien fringilla quis neque inceptos eu. Quis integer nibh. Pellentesque amet sed. Commodo eu mollis elementum vel ut. Et in amet suspendisse placerat massa. Suspendisse vel sit arcu mattis tellus. Risus vivamus tempor. Quam penatibus arcu lectus fusce non. Nulla proident neque. Nec velit pede eum id non phasellus libero quis. Donec proin tristique maecenas penatibus luctus massa adipiscing euismod praesent arcu adipiscing turpis et per. Proin vehicula at. Suspendisse ullamcorper metus. In est interdum quo inceptos est varius sint justo porta nunc pellentesque molestie volutpat pellentesque. Adipiscing malesuada mi dictumst lectus nullam. Arcu semper et viverra elementum deleniti. Erat cursus netus nibh aliquam ipsum porro pretium ac. Quis purus nibh ut sem pellentesque justo quam aliquam. Nulla sit suspendisse nulla vehicula praesent eu diam ullamcorper duis quis sed. Facilisis tincidunt aliquet nec mauris dictumst. Odio ac suspendisse dictumst justo neque egestas leo nonummy. Turpis molestie nunc. Sollicitudin tristique nonummy ridiculus facilisis cras phasellus sed orci. Malesuada rutrum lorem. In arcu duis non phasellus parturient. Enim nisi urna fermentum mauris ut. Amet dui lorem tortor orci est. Vel non rerum. Vehicula sapien id. Massa sollicitudin molestiae nam sit dictum." + eol);
			writer.write("Cras vitae parturient tellus leo praesent pellentesque suspendisse velit vestibulum vestibulum mus. Pretium natoque sit id neque fermentum. Aenean tempor suspendisse. Dis orci vestibulum sit platea nulla laoreet consectetuer lacus. Dolor in ducimus. Mauris ut urna. Ante sed alias sed nonummy proin id cursus ipsum. Inventore ante tincidunt. Suspendisse rutrum eu. Elit cras nam. Lectus adipiscing vestibulum. Donec rhoncus est sed vel imperdiet purus nam orci. Non amet malesuada et in eu gravida tempus malesuada. Ultrices iaculis libero. Eleifend suspendisse suscipit nulla ac quis. Vehicula in cras pulvinar ut elit condimentum eu vehicula faucibus arcu aenean. Imperdiet in tortor erat lobortis placerat. Ac urna interdum neque purus nibh. Venenatis varius sed. Vitae condimentum felis nostra ad pede praesent non primis. Quis rutrum erat. Netus lacus elit. Pede amet fusce nobis lobortis elit dignissim eros augue. Pellentesque maecenas sit quis cupiditate non. Vestibulum curabitur interdum eget vivamus enim. Nullam praesent wisi est habitant aliquet. Lacus risus a. Habitant massa lacinia a aliquam nulla ligula adipiscing pellentesque donec velit turpis. Sodales magna nibh cras sociosqu rhoncus in morbi orci. Nec facilisis aliquam." + eol);
			writer.write("Vulputate metus sed. Posuere vehicula vitae et ut mauris. At sit felis volutpat curae facilisis phasellus orci aliquam. Laoreet mattis justo. Pellentesque lobortis nunc diam lobortis tellus. Etiam justo feugiat. Sollicitudin in mauris. Quam ultrices integer. Ligula wisi et sed odio nam. Tincidunt euismod mauris lectus tincidunt eget mattis metus elit. Sed urna vel. Magna in in. Primis nullam ut. Ante purus commodo et ullamcorper eget platea in elementum sollicitudin wisi ligula. Tortor nec malesuada eleifend lectus integer rutrum pede reprehenderit. Suscipit magnis fringilla. Duis a porta. Ultrices accumsan nec eget vitae magnis. Quisquam et est tortor magna at. Turpis tempor dolor pede ut vestibulum mattis faucibus rutrum. Donec mollis sit. Vestibulum sed odio. Duis est quam felis fusce cras. Nunc luctus sollicitudin donec rutrum turpis. Sed ligula eu. In sit pellentesque. Ad mattis justo. In et volutpat lacus ac quis. Dignissim varius est. Lobortis ut ridiculus donec maecenas eros accumsan tortor sapien mollis assumenda faucibus velit erat mattis ac nisl leo amet vel cras. Sodales accumsan odio sed eros justo. Nec scelerisque massa at sapien enim. Turpis justo vitae. Nunc ac magna vivamus augue vitae. Libero tellus vestibulum. Orci aliquam vivamus. Integer amet massa mauris fringilla nec et feugiat sed nibh nisl curabitur pharetra cras velit duis elit proin fringilla leo ut. Pede et ullamcorper quis sit eget nisl ornare vitae nec phasellus lacinia ullamcorper tellus erat ac vulputate dui. Nisl eu ipsum pede varius tincidunt. Ut netus sit odio et nullam. Urna accumsan eu integer etiam sem condimentum etiam egestas. Nunc amet magna placeat nullam porttitor. Inceptos urna aliquam. Vivamus ipsum wisi luctus et metus fusce amet tellus. Sagittis egestas ac eleifend tempus arcu sed malesuada vitae ut ornare id. Volutpat ipsum vestibulum. Deserunt morbi malesuada. Cras praesent nec quis lacus enim justo feugiat blandit tincidunt dis in. Vel diam urna. At nibh massa. Ac neque tellus. Ac ut pulvinar. Faucibus maecenas eu. Nec eleifend dui mauris adipiscing faucibus integer id accumsan. Penatibus donec nisl ultrices duis donec.");
    	}
    }
}
