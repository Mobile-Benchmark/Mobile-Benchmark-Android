package com.android.benchmark.fetcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class URLFetcher {
	
	private static String url = "http://mobilebenchmarkdev.appspot.com";
	private static String extension = "/rest/";
	private static String customextension = "/custom/";
	private static final String MIME_JSON = "application/json";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_ACCEPT = "Accept";
    private static final DefaultHttpClient client;
    private static int status = 0;
    
	public URLFetcher()
	{
	}
	
	static
    {
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                HttpVersion.HTTP_1_1);
        params
                .setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET,
                        HTTP.UTF_8);
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);
        params.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory
                .getSocketFactory(), 443));

        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(
                params, schemeRegistry);

        client = new DefaultHttpClient(cm, params);
    }
	
	
	
	
	
	
	private static HttpRequestInterceptor createHttpRequestInterceptor(
            final Map<String, String> sendHeaders)
    {
        return new HttpRequestInterceptor()
        {

            public void process(final HttpRequest request,
                    final HttpContext context) throws HttpException,
                    IOException
            {
                for (String key : sendHeaders.keySet())
                {
                    if (!request.containsHeader(key))
                    {
                        Log.v("TAG", " adding header: " + key + " | " + sendHeaders.get(key));
                        request.addHeader(key, sendHeaders.get(key));
                    }
                }
            }
        };
    }
	
	public static JSONObject connect(String appendUrl)
    {
		String totalURL = url + extension + appendUrl;
		Log.d("mobile", "url:" + totalURL);
				
        // Prepare a request object
        HttpGet httpget = new HttpGet(totalURL); 

        // Execute the request
        final Map<String, String> sendHeaders = new HashMap<String, String>();
        HttpResponse response;
        sendHeaders.put(HEADER_CONTENT_TYPE, MIME_JSON);
	    sendHeaders.put(HEADER_ACCEPT, MIME_JSON);
	    client.addRequestInterceptor(createHttpRequestInterceptor(sendHeaders));
	    
        JSONObject json = new JSONObject();

        try
        {
            response = client.execute(httpget);
            status = response.getStatusLine().getStatusCode();
            
            HttpEntity entity = response.getEntity();

            if (entity != null) 
            {
                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                String result= convertStreamToString(instream);

                json = new JSONObject(result);

                instream.close();
            }

        } catch (ClientProtocolException e) {
        	Log.e("mobile", e.getMessage());
        } catch (IOException e) {
        	Log.e("mobile", e.getMessage());
        } catch (JSONException e) {
        	Log.e("mobile", e.getMessage());
        }

        return json;
    }	
	
	public static JSONObject connectCustom(String appendUrl)
    {
		String totalURL = url + customextension + appendUrl;
		Log.d("mobile", "url:" + totalURL);
				
        // Prepare a request object
        HttpGet httpget = new HttpGet(totalURL); 
        // Execute the request
        final Map<String, String> sendHeaders = new HashMap<String, String>();
        HttpResponse response;
        sendHeaders.put(HEADER_CONTENT_TYPE, MIME_JSON);
	    sendHeaders.put(HEADER_ACCEPT, MIME_JSON);
	    client.addRequestInterceptor(createHttpRequestInterceptor(sendHeaders));
	    
        JSONObject json = new JSONObject();

        try {
            response = client.execute(httpget);
            status = response.getStatusLine().getStatusCode();
            
            HttpEntity entity = response.getEntity();

            if (entity != null) 
            {
                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                String result= convertStreamToString(instream);

                json = new JSONObject(result);

                instream.close();
            }

        } catch (ClientProtocolException e) {
        	Log.e("mobile", e.getMessage());
        } catch (IOException e) {
        	Log.e("mobile", e.getMessage());
        } catch (JSONException e) {
        	Log.e("mobile", e.getMessage());
        }

        return json;
    }	
	
	public static boolean isValidRequest()
	{
		boolean returnValue = false;
        if(status == HttpStatus.SC_OK)
        {
        	returnValue = true;
        }
        return returnValue;
	}
		
	public static String makeCustomRequest(String appendUrl, JSONObject json) throws Exception 
	{
		Log.d("mobile", "req: " + url + customextension + appendUrl);
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httpost = new HttpPost(url + customextension + appendUrl);
		

		StringEntity se = new StringEntity(json.toString());
		httpost.setEntity(se);
		httpost.setHeader("Accept", "application/json");
		httpost.setHeader("Content-type", "application/json");

		HttpResponse response = httpclient.execute(httpost);
		
		status = response.getStatusLine().getStatusCode();
		
        HttpEntity entity = response.getEntity();
        String result = "";
        if (entity != null) 
        {
            // A Simple JSON Response Read
            InputStream instream = entity.getContent();
            result = convertStreamToString(instream);
        }        
        
		return result;
	}
		  
	public static String convertStreamToString(InputStream is)
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
