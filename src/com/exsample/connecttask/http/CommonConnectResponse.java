package com.exsample.connecttask.http;

public class CommonConnectResponse
{
	public static final CommonConnectResponse empty = new CommonConnectResponse(0, "");

	public final int responseCode;
	public final String responseData;

	public CommonConnectResponse(int responseCode, String responseData)
	{
		this.responseCode = responseCode;
		this.responseData = responseData;
	}
}
