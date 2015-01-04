/**********************************************************************************************************/
// Harman HKController IPC Example
// This simplified example show how to build simple connections to the HKController Application
//
// For more information, see Android Docs http://developer.android.com/guide/components/bound-services.html
/***********************************************************************************************************/

package org.skylight1;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class HKService {
    private static final String TAG = null;
	private Messenger messenger;
    int connStatus = 0; //0:not connected, 1:connected
	private Context context;
	
	public HKService(Context ctxt) {
		context = ctxt;
	}

    //connect button event handler - Button 1
    public void sendConnect() {
        ServiceConnection sConn;

        //logString("Sending Connect Request to HKConnect App.");

        sConn = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {
                messenger = null;
                //logString("onServiceDisconnected");
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // We are connected to the service
                messenger = new Messenger(service);
                //logString("onServiceConnected: New Message object created:" + name.toString());
                connStatus = 1;
            }
        };

        //bind to service.
        context.bindService(new Intent("org.skylight1.ConvertService"), sConn, Context.BIND_AUTO_CREATE);

    }

    //Send Play Command event handler
    public void sendPlay() {

        try {
            if(connStatus == 1) {
                //logString("Sending Play Request to HKConnect.");
                String val = ""; //not yet implemented
                Message msg = Message.obtain(null, ConvertService.MSG_ID_START_PLAYING_ALL);

                msg.replyTo = new Messenger(new ResponseHandler());

                // We pass the value and set in msg object
                Bundle b = new Bundle();
                b.putString("data", val);
                msg.setData(b);

                //send message to HKConnect app
                messenger.send(msg);
            } else {
                //logString("Please tap the connect button before attempting to communicate with the HK Controller App.");
            }
        } catch (RemoteException e) {
            //logString(e.toString());
        }
    }

    //Send Pause Command event handler
    public void sendPause() {
        try {
            if(connStatus == 1) {
                //logString("Sending Pause Request to HKConnect.");
                String val = ""; //not yet implemented
                Message msg = Message.obtain(null, ConvertService.MSG_ID_STOP_PLAYING_ALL);

                msg.replyTo = new Messenger(new ResponseHandler());

                // We pass the value and set in msg object
                Bundle b = new Bundle();
                b.putString("data", val);
                msg.setData(b);

                //send message to HKConnect App
                messenger.send(msg);
            } else {
                //logString("Please tap the connect button before attempting to communicate with the HK Controller App.");
            }
        } catch (RemoteException e) {
            //logString( e.toString());
        }
    }


    //Send Query Status event handler
    public void sendQuery() {

        try {
            if(connStatus == 1) {
                //logString( "Sending Query to HKConnect.");
                String val = ""; //not yet implemented
                Message msg = Message.obtain(null, ConvertService.MSG_ID_QUERY_DEVICE);

                msg.replyTo = new Messenger(new ResponseHandler());

                // We pass the value and set in msg object
                Bundle b = new Bundle();
                b.putString("data", val);
                msg.setData(b);

                //send message to HKConnect app
                messenger.send(msg);
            } else {
                //logString( "Please tap the connect button before attempting to communicate with the HK Controller App.");
            }

        } catch (RemoteException e) {
        	Log.e(TAG, e.toString());
        }

    }

    // This class handles the Service response
    class ResponseHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            int respCode = msg.what;
            String str = msg.getData().getString("respData");

            switch (respCode) {

                default:
//                    logString("Length:" + str.length()+ "Bytes | " + "Data: " + str);
                    break;
            }
        }
    }
}
