package org.skylight1;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class ConvertService extends Service {
	public static final int MSG_ID_TO_UPPER_CASE = 1000;
	public static final int MSG_ID_TO_UPPER_CASE_RESPONSE = 1001;
	public static final int MSG_ID_START_PLAYING_ALL = 1;
	public static final int MSG_ID_START_PLAYING_ALL_RESPONSE = 2;
	public static final int MSG_ID_START_PLAYING = 3;
	public static final int MSG_ID_START_PLAYING_RESPONSE = 4;
	public static final int MSG_ID_STOP_PLAYING_ALL = 5;
	public static final int MSG_ID_STOP_PLAYING_ALL_RESPONSE = 6;
	public static final int MSG_ID_STOP_PLAYING = 7;
	public static final int MSG_ID_STOP_PLAYING_RESPONSE = 8;
	public static final int MSG_ID_QUERY_DEVICE = 9;
	public static final int MSG_ID_QUERY_DEVICE_RESPONSE = 10;
	public static final int MSG_ID_QUERY_DEVICE_ALL = 11;
	public static final int MSG_ID_QUERY_DEVICE_ALL_RESPONSE = 12;
	public static final int MSG_ID_QUERY_STATUS = 13;
	public static final int MSG_ID_QUERY_STATUS_RESPONSE = 14;


	//public static final int MSG_ID_STOP_PLAYING = 6;
	//public static final int MSG_ID_STOP_PLAYING = 7;
	//public static final int MSG_ID_STOP_PLAYING = 8;
	//public static final int MSG_ID_STOP_PLAYING = 9;
	//public static final int MSG_ID_STOP_PLAYING = 10;

	private Messenger msg = new Messenger(new ConvertHandler());

	@Override
	public IBinder onBind(Intent arg0) {
		return msg.getBinder();
	}

	class ConvertHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// This is the action
			int msgType = msg.what;

			switch (msgType) {
			case MSG_ID_TO_UPPER_CASE: {
				try {
					// Incoming data
					String data = msg.getData().getString("data");
					Message resp = Message.obtain(null, MSG_ID_TO_UPPER_CASE_RESPONSE);
					Bundle bResp = new Bundle();
					bResp.putString("respData", data.toUpperCase());
					resp.setData(bResp);

					msg.replyTo.send(resp);
				} catch (RemoteException e) {

					e.printStackTrace();
				}
				break;
			}
			default:
				super.handleMessage(msg);
			}
		}
	}
}
