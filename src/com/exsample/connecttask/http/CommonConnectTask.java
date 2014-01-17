package com.exsample.connecttask.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CommonConnectTask extends AsyncTask<Void, Integer, Void>
{
	// TODO I want to use enum.
	public static final int ON_PROGRESS = 1;
	public static final int ON_CANCELED = 2;
	public static final int ON_FINISHED = 3;

	private volatile ArrayList<CommonConnectRequest> requestQueue = new ArrayList<CommonConnectRequest>();
	private volatile CommonConnectRequest request;
	private volatile boolean alive = true;

	public boolean request(CommonConnectRequest request)
	{
		synchronized(this)
		{
			boolean result = requestQueue.add(request);

			this.notifyAll();

			return(result);
		}
	}

	protected CommonConnectRequest takeRequest()
	{
		synchronized(this)
		{
			CommonConnectRequest request = null;
	
			if(!requestQueue.isEmpty())
			{
				request = requestQueue.remove(0);
			}
	
			return(request);
		}
	}

	public void destory()
	{
		Log.i("CommonConnectTask", "destroy");
		alive = false;
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		while(alive)
		{
			request = takeRequest();
			if(request != null)
			{
				CommonConnectResponse response = connectHttp(request);

				Handler handler = request.handler;
				if(handler != null)
				{
					Message message = Message.obtain();
					message.arg1 = ON_FINISHED; // TODO Im sorry...
					message.obj = response;

					handler.sendMessage(message);
				}
			}

			synchronized(this)
			{
				try
				{
					Log.i("CommonConnectTask", "wait");
					wait(1000*10); // TODO get up in 10sec
				}
				catch(InterruptedException ignore) {}
				Log.i("CommonConnectTask", "awake");
			}
		}

		return null;
	}

	@Override
	protected void onCancelled()
	{
		Log.i("CommonConnectTask","onCancelled");
		// TODO cancel is not use AsyncTask's onCancelled.
	}

	@Override
	protected void onProgressUpdate(Integer... values)
	{
		Log.i("CommonConnectTask","onProgressUpdate");
		// TODO progress-update is not use AsyncTask's onProgressUpdate.
	}

	private CommonConnectResponse connectHttp(CommonConnectRequest request)
	{
		HttpClient client = new DefaultHttpClient();
		HttpParams tmpHttpParams = client.getParams();
		HttpConnectionParams.setConnectionTimeout(tmpHttpParams, 30*1000);
		HttpConnectionParams.setSoTimeout(tmpHttpParams, 30*1000);

		HttpResponse httpResponse = null;
		HttpUriRequest httpUriRequest = null;

		ArrayList<NameValuePair> httpParams = new ArrayList<NameValuePair>();

		for(Map.Entry<String, String> entry: request.params.entrySet())
		{
			httpParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		try
		{
			switch(request.method)
			{
				case	GET :
				{
					String query = URLEncodedUtils.format(httpParams, "UTF-8");
					httpUriRequest = new HttpGet(request.uri + "?" + query);

					break;
				}

				case	POST :
				{
					HttpPost httpPostRequest = new HttpPost(request.uri);

					httpPostRequest.setEntity(new UrlEncodedFormEntity(httpParams, "utf-8"));
					httpUriRequest = httpPostRequest;
					break;
				}

				default:
					throw new UnsupportedOperationException(request.method + " is unknown.");
//					break;
			}

			Log.i("CommonConnectTask","http connect!");
			httpResponse = client.execute(httpUriRequest);
			Log.i("CommonConnectTask","http connected");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			// TODO I have no idea.
			return(new CommonConnectResponse(999, e.getMessage()));
		}

		CommonConnectResponse resultResponse = CommonConnectResponse.empty;
		if(httpResponse != null)
		{
			StatusLine statusLine = httpResponse.getStatusLine();
			if(statusLine != null)
			{
				int responseCode = statusLine.getStatusCode();
				String responseData = "";

				HttpEntity entity = httpResponse.getEntity();
				if(entity != null)
				{
					try
					{
						responseData = EntityUtils.toString(entity);
					}
					catch(IOException ignore) {}
				}

				resultResponse = new CommonConnectResponse(responseCode, responseData);
			}
		}

		return(resultResponse);
	}
}
