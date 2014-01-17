package com.exsample.connecttask.http;

import java.util.Map;

import android.os.Handler;

public class CommonConnectRequest
{
	public enum Method
	{
		GET,
		POST,
	}

	public final String uri;
	public final Method method;
	public final Map<String, String> params; // FIXME I don't want to use public, if it can do.
	public final Handler handler;

	public CommonConnectRequest(String uri, Method method, Map<String, String> params, Handler handler)
	{
		this.uri = uri;
		this.method = method;
		this.params = params;
		this.handler = handler;
	}
}
