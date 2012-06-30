package com.wenhuabin.netmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class BootBroadcast extends BroadcastReceiver{
	
	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			
	        	final Handler toaster = new Handler() {
	        		public void handleMessage(Message msg) {
	        			if (msg.arg1 != 0) Toast.makeText(context, msg.arg1, Toast.LENGTH_SHORT).show();
	        		}
	        	};
				// Start a new thread to apply rules saved
				new Thread() {
					@Override
					public void run() {
						if (!Api.Apply(context, false)) {
							final Message msg = new Message();
		        			msg.arg1 = 0x7f060029;
		        			toaster.sendMessage(msg);
						}
					}
				}.start();
		}
	}

}
