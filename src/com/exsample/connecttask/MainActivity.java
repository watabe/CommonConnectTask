package com.exsample.connecttask;

import java.util.Collections;

import com.exsample.connecttask.R;
import com.exsample.connecttask.R.id;
import com.exsample.connecttask.http.CommonConnectRequest;
import com.exsample.connecttask.http.CommonConnectResponse;
import com.exsample.connecttask.http.CommonConnectTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener
{
	private CommonConnectTask connectTask;
	private Handler connectHandler;

	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		connectTask = new CommonConnectTask();
		connectTask.execute((Void[])null);

		connectHandler = new OnConnectHandler();

		Button buttonConnect = (Button)findViewById(id.buttonConnect);
		buttonConnect.setOnClickListener(this);

		progressDialog = new ProgressDialog(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);

		return(true);
	}

	@Override
	public void onClick(View v)
	{
		TextView editURI = (TextView)findViewById(id.editURI);
		String uri = editURI.getText().toString();

		v.setEnabled(false);

		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Connecting...");
		progressDialog.setCancelable(false);
		progressDialog.show();

		CommonConnectRequest request = new CommonConnectRequest(
				uri,
				CommonConnectRequest.Method.GET,
				Collections.<String, String>emptyMap(), // TODO This is dummy params.s
				connectHandler);
		connectTask.request(request);
	}

	// FIXME I want to solve the leak.
	class OnConnectHandler extends Handler
	{
		@Override
		public void handleMessage(Message message)
		{
			switch(message.arg1)
			{
				case	CommonConnectTask.ON_FINISHED:
				{
					CommonConnectResponse response = (CommonConnectResponse)message.obj;

					MainActivity mainActivity = MainActivity.this;
					TextView editResponse = (TextView)mainActivity.findViewById(id.editResponse);
					editResponse.setText(response.responseData);

					Toast.makeText(mainActivity, "ResponseCode:" + response.responseCode, Toast.LENGTH_SHORT).show();

					progressDialog.dismiss();

					Button buttonConnect = (Button)mainActivity.findViewById(id.buttonConnect);
					buttonConnect.setEnabled(true);
					break;
				}
			}
		}
	}
}
