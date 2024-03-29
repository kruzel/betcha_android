package com.betcha;

import java.sql.SQLException;
import java.util.List;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.betcha.activity.BetDetailsActivity;
import com.betcha.model.Bet;
import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	private static final int HELLO_ID = 1;
	private static final String GCM_SENDER_ID = "1053196289883";
	
	public GCMIntentService() {
        super(GCM_SENDER_ID);
	}

	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Called when the device tries to register or unregister, but GCM returned an error. 
		// Typically, there is nothing to be done other than evaluating the error (returned by errorId) and trying to fix the problem.
		Log.e("GCMIntentService.onError() error ", arg1);
	}

	@Override
	protected void onMessage(Context arg0, Intent intent) {
		//  Called when your server sends a message to GCM, and GCM delivers it to the device. 
		// If the message has a payload, its contents are available as extras in the intent.
		Bundle bundle = intent.getExtras();
		String msgType = bundle.getString("type");
		String ownerId = bundle.getString("owner_id");
		String userId = bundle.getString("user_id");
		String betId = bundle.getString("bet_id");
		String predictionId = bundle.getString("prediction_id");
				
		Log.i("GCMIntentService.onMessage()", "type:" + msgType + ", owner: " + ownerId + ", user: " + userId + ", bet: " + betId + ", prediction: " + predictionId );
		
		Bet bet = null;
		if(msgType.equals("bet_update")) {
			try {
				bet = Bet.getModelDao().queryForId(betId);
			} catch (SQLException e) {
			}
			
			if(bet==null) {
				bet = new Bet();
				bet.setId(betId);
			}
			
			if(bet.onRestGet()==0) 
				return;
			
			ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
		    List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1); 
		    String activityName = taskInfo.get(0).topActivity.getShortClassName();
			
			if(activityName.equals(".activity.BetDetailsActivity")) {
				Intent i = new Intent("com.betcha.BetDetailsActivityReceiver");
				i.putExtra("bet_id", bet.getId());
				sendBroadcast(i);
				
			} else if(activityName.equals(".activity.BetsListActivity")) {
				Intent i = new Intent("com.betcha.BetsListFragmentReceiver");
				i.putExtra("bet_id", bet.getId());
				sendBroadcast(i);
				
			} else {
				String ns = Context.NOTIFICATION_SERVICE;
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
				
				int icon = R.drawable.ic_tab_creatbet_grey;
				CharSequence tickerText = "DropaBet invitation";
				long when = System.currentTimeMillis();
				Notification notification = new Notification(icon, tickerText, when);
				
				Context context = getApplicationContext();
				CharSequence contentTitle = "DropaBet";
				CharSequence contentText = "You have a new bet update from " + bet.getOwner().getName();
				Intent notificationIntent = new Intent(this, BetDetailsActivity.class);
				notificationIntent.putExtra("bet_id", bet.getId());
				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
				notification.defaults |= Notification.DEFAULT_SOUND;
	
				notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	
				mNotificationManager.notify(HELLO_ID, notification);
			}
			
		} 
	}

	@Override
	protected void onRegistered(Context ctx, String regId) {
		// TODO Called after a registration intent is received, passes the registration ID assigned by GCM to that device/application pair as parameter. 
		// Typically, you should send the regid to your server so it can use it to send messages to this device.
		Log.i("GCMIntentService.onRegistered() device id", regId);
		
		BetchaApp app = (BetchaApp) ctx;
		if(app.getCurUser()!=null) {
			app.getCurUser().setPush_notifications_device_id(regId);
			
			Log.i("GCMIntentService.onRegistered()", "updating server");
			app.getCurUser().update();
		}	
	}

	@Override
	protected void onUnregistered(Context ctx, String regId) {
		// TODO Called after the device has been unregistered from GCM. Typically, you should send the regid to the server so it unregisters the device.
		Log.i("GCMIntentService.onUnregistered() device id", regId);
		
		BetchaApp app = (BetchaApp) ctx;
		if(app.getCurUser()!=null) {
			app.getCurUser().setPush_notifications_device_id("");
			app.getCurUser().update();
		}	
	}

}
