package com.betcha;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.betcha.model.cache.ModelCache;
import com.betcha.model.cache.SyncTask;

public class ConnectivityReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(ConnectivityReceiver.class.getSimpleName(), "action: " + intent.getAction());
		
		//stop receiving network state changes events
		ModelCache.disableConnectivityReciever();
		
		SyncTask.run(null);
	}

}
