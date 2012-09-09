package com.betcha;

import java.sql.SQLException;
import java.util.logging.Logger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService {

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
		Integer ownerId = Integer.valueOf(bundle.getString("owner_id"));
		Integer userId = Integer.valueOf(bundle.getString("user_id"));
		Integer betId = Integer.valueOf(bundle.getString("bet_id"));
		String pred = bundle.getString("prediction_id");
		Integer predictionId = null;
		if(pred != null) {
			predictionId = Integer.valueOf(pred);
		}
		
		Log.e("GCMIntentService.onMessage()", "owner: " + ownerId + ", user: " + userId + ", bet: " + betId + ", prediction: " + predictionId );
		
		if(msgType.equals("invite")) {
			// TODO load bet from server and create locally then 
			// if app is active open bet screen, otherwise show a status notification in the notification bar
			
		} else if(msgType.equals("update")) {
			// TODO update prediction value, if app is active in bet screen update screen
			
		} else if(msgType.equals("chat")) {
			Integer chatMessageId = Integer.valueOf(bundle.getString("chat_message_id"));
			String chatMessageText = bundle.getString("chat_message_text");
			Log.e("GCMIntentService.onMessage()", "chat_message_id: " + chatMessageId + ", chat_message_text: " + chatMessageText );
			// TODO save and show chat message 
			// if app is active in bet screen update screen, otherwise show a status notification in the notification bar
			
		}	
	}

	@Override
	protected void onRegistered(Context ctx, String regId) {
		// TODO Called after a registration intent is received, passes the registration ID assigned by GCM to that device/application pair as parameter. 
		// Typically, you should send the regid to your server so it can use it to send messages to this device.
		Log.i("GCMIntentService.onRegistered() device id", regId);
		
		BetchaApp app = (BetchaApp) ctx;
		if(app.getMe()!=null) {
			app.getMe().setPush_notifications_device_id(regId);
			
			try {
				Log.i("GCMIntentService.onRegistered()", "updating server");
				app.getMe().update();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}	
	}

	@Override
	protected void onUnregistered(Context ctx, String regId) {
		// TODO Called after the device has been unregistered from GCM. Typically, you should send the regid to the server so it unregisters the device.
		Log.i("GCMIntentService.onUnregistered() device id", regId);
		
		BetchaApp app = (BetchaApp) ctx;
		if(app.getMe()!=null) {
			app.getMe().setPush_notifications_device_id("");
			
			try {
				app.getMe().update();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}	
	}



}
