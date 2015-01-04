package org.skylight1;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.att.android.speech.ATTSpeechError;
import com.att.android.speech.ATTSpeechError.ErrorType;
import com.att.android.speech.ATTSpeechErrorListener;
import com.att.android.speech.ATTSpeechResult;
import com.att.android.speech.ATTSpeechResultListener;
import com.att.android.speech.ATTSpeechService;
import com.att.android.speech.ATTSpeechStateListener;

public class Alfred extends Activity 
{
    private static final String TAG = "ALFRED";
	private AudioPlayer audioPlayer = null;
    private TTSClient ttsClient = null;
    private Button speakButton = null;
    private TextView resultView = null;
    private String oauthToken = null;
	private boolean commandMode;
	public boolean isResponse;
	private HKService hkservice;
    
    /** 
     * Called when the activity is first created.  This is where we'll hook up 
     * our views in XML layout files to our application.
    **/
    @Override public void 
    onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.speech);
        
        hkservice = new HKService(this);
        //hkservice.sendConnect(); // currently works with only a hard coded track
        
        audioPlayer = new AudioPlayer(this);
        
        speakButton = (Button)findViewById(R.id.speak_button);
        speakButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startListening();
            }
        });        
        // This will show the recognized text.
        resultView = (TextView)findViewById(R.id.result);
 
// if first time:
//        alert("note", "pair or wake the bluetooth speaker");
        setupSpeechService();
        
    }
	private void startListening() {		
        stopTTS(); // don't let playback interfere with microphone
        try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//        setupSpeechService();
        ATTSpeechService speechService = getSpeechService();
        speechService.startListening();            				
	}
    /** Convenience routine to get speech service. **/
    private ATTSpeechService getSpeechService()
    {
        return ATTSpeechService.getSpeechService(this);
    }
    
    @Override 
    protected void onStart()  {
        super.onStart();
//        stopTTS();
//        startSpeechService();
        validateOAuth();
    }
    @Override
    protected void onResume() {
    	super.onResume();
    	//stopTTS(); // don't let playback interfere with microphone

//        ATTSpeechService speechService = getSpeechService();
//        speechService.startListening();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        stopTTS();
//        ATTSpeechService speechService = getSpeechService();
//        speechService.stopListening();
    }

    /**
     * When the app is authenticated with the Speech API, 
     * enable the interface and speak out a greeting.
    **/
    private void readyForSpeech() 
    {
        // First enable the speech buttons.
//        speakButton.setText(R.string.speak_button);
//        speakButton.setEnabled(true);
        // Make Text to Speech request that will speak out a greeting.
//        startTTS(getString(R.string.greeting));
        waitForTTS();
        
//        setupSpeechService();
//        ATTSpeechService speechService = getSpeechService();
////      speechService.setBearerAuthToken(oauthToken);
////      speechService.setXArgs(Collections.singletonMap("ClientScreen", "main"));        
//      speechService.startListening();

    }
	private void waitForTTS() {
		(new Thread() { 
        	public void run() {
		        while(true) {
		        	try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
		        	if(audioPlayer==null || !audioPlayer.isPlaying()) {
		        		break;
		        	}
		        }
		        Runnable task = new Runnable() {
					public void run() {
						startListening();
					}
				};				
			    new Handler(Looper.getMainLooper()).post(task);		        
        	}
        }).start();
	}
    
    /**
     * Start a TTS request to speak the argument.
    **/
    private void startTTS(String textToSpeak) {
        TTSRequest tts = TTSRequest.forService(SpeechConfig.ttsUrl(), oauthToken);
        ttsClient = new TTSClient();
        tts.postText(textToSpeak, ttsClient);
    }
    
    /**
     * Stops any Text to Speech in progress.
    **/
    private void stopTTS() {
        if (ttsClient != null)
            ttsClient.cancel = true;
        audioPlayer.stop();
    }
    
    /**
     * This callback object will get the TTS responses.
    **/
    private class TTSClient implements TTSRequest.Client 
    {
        @Override 
        public void handleResponse(byte[] audioData, Exception error) {
            if (cancel)
                return;
            if (audioData != null) {
                Log.v("Alfred", "Text to Speech returned "+audioData.length+" of audio.");
                audioPlayer.play(audioData);
            }
            else {
                // The TTS service was not able to generate the audio.
                Log.v("Alfred", "Unable to convert text to speech.", error);
                // Real applications probably shouldn't display an alert.
                alert(null, "Unable to convert text to speech.");
            }
        }
        /** Set to true to prevent playing. **/
        boolean cancel = false;
    }

    private void setupSpeechService() {
        ATTSpeechService speechService = ATTSpeechService.getSpeechService(this);
        
        // Register for the success and error callbacks.
        speechService.setSpeechResultListener(new ResultListener());
        speechService.setSpeechErrorListener(new ErrorListener());
        speechService.setSpeechStateListener(new StateListener());

        speechService.setShowUI(false);
        
        try {
            speechService.setRecognitionURL(new URI(SpeechConfig.recognitionUrl()));
        }
        catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
        
        // Specify the speech context for this app.
        speechService.setSpeechContext("QuestionAndAnswer");
        
        speechService.setMaxInitialSilence(Integer.MAX_VALUE);
        speechService.setMinRecordingTime(0);
        speechService.setMaxRecordingTime(Integer.MAX_VALUE);
        
        // Set the OAuth token that was fetched in the background.
        speechService.setBearerAuthToken(oauthToken);
        
        // Add extra arguments for speech recognition.
        // The parameter is the name of the current screen within this app.
        //Map<String,String> map = new HashMap<String,String>();
        //map.put("ClientScreen", "main");
        //map.put("VoiceName", "mike");
        //speechService.setXArgs(map);
        speechService.setXArgs(Collections.singletonMap("ClientScreen", "Alfred"));

//        speechService.startListening();  
    }
    
    /**
     * This callback object will get all the speech success notifications.
    **/
    private class ResultListener implements ATTSpeechResultListener 
    {
        public void onResult(ATTSpeechResult result) {
            ATTSpeechService speechService = ATTSpeechService.getSpeechService(Alfred.this);
            speechService.stopListening();
            // The hypothetical recognition matches are returned as a list of strings.
            List<String> textList = result.getTextStrings();
            String resultText = null;
            if (textList != null && textList.size() > 0) {
                // There may be multiple results, but this example will only use
                // the first one, which is the most likely.
                resultText = textList.get(0);
            }
            if (resultText != null && resultText.length() > 0) {
                // This is where your app will process the recognized text.
				Log.e("Alfred", "Recognized "+textList.size()+" hypotheses.");
                handleRecognition(resultText);
            }
            else {
                try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                speechService.startListening();
            }
        }
    }
    
    /** Make use of the recognition text in this app. **/
    private void handleRecognition(String resultText) {
		isResponse=true;
    	Log.e(TAG,resultText);
    	if(!commandMode) {
    		if(resultText.contains(" ")) {
    			resultText = (resultText.split(" "))[0];
    		}
	        if(resultText.equals("ok")||resultText.equals("okay")||resultText.equals("ohk")||resultText.equals("O.K.")
	        		||resultText.equals("alfred")||resultText.equals("albert")||resultText.equals("howard")
	        		||resultText.equals("after")||resultText.equals("often")||resultText.equals("office")
	        		||resultText.equals("ali")) {
	        	//hkservice.sendPause(); // currently works with only a hard coded track
	        	Intent intent = new Intent("com.android.music.musicservicecommand");
	        	intent.putExtra("command", "pause");
	        	sendBroadcast(intent);
	            startTTS("yes?");
	            commandMode = true;
	        }
            waitForTTS();
            isResponse=false;
    	} else {
    		String name = null;
    		if(resultText.contains(" ")) {
    			name = (resultText.substring(resultText.indexOf(' ')+1));
    			resultText = (resultText.split(" "))[0];
    		}
    		if(resultText.startsWith("play")) {
    			if(name==null) {
	    			commandMode = false;
	    			//kservice.sendPause(); // currently works with only a hard coded track
		        	Intent intent = new Intent("com.android.music.musicservicecommand");
		        	intent.putExtra("command", "play");
		        	sendBroadcast(intent);
    			} else {
    				playSearchArtist(name);
    			}
	            startTTS("ok");
	            waitForTTS();
	            isResponse=false;
    		} else {
    			
    		}
    	}
    	
    }
    
    private class ErrorListener implements ATTSpeechErrorListener  {
        public void onError(ATTSpeechError error) {
            ErrorType resultCode = error.getType();
            if (resultCode == ErrorType.USER_CANCELED) {

                Log.e("Alfred", "User canceled.");
            }
            else {
                String errorMessage = error.getMessage();
                Log.e("Alfred", "Recognition error #"+resultCode+": "+errorMessage, error.getException());
                ATTSpeechService speechService = ATTSpeechService.getSpeechService(Alfred.this);
                speechService.stopListening();
                speechService.startListening();
            }
        }
    }
    private class StateListener implements ATTSpeechStateListener  {
		@Override
		public void onStateChanged(SpeechState state) {
			if(state.equals(SpeechState.PROCESSING)) {
				Log.e(TAG,"processing....");
				resultView.setText("processing");
				isResponse=false;
			} else if(state.equals(SpeechState.RECORDING)) {
				Log.e(TAG,"listening....");
				resultView.setText("listening");
				isResponse=false;
			} else if(state.equals(SpeechState.ERROR)) {
				Log.e(TAG,"error....");
				resultView.setText("error");
                startListening();
			} else if(state.equals(SpeechState.IDLE)) {
				Log.e(TAG,"idle....");
				resultView.setText("idle");
				if(!isResponse) {
					Log.e(TAG,"re-startListening....");
					startListening();
				}
			} else if(state.equals(SpeechState.INITIALIZING)) {
				Log.e(TAG,"initializing....");
			}
		}
    }
    public void playSearchArtist(String artist) {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS,
                        MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE);
        intent.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, artist);
        intent.putExtra(SearchManager.QUERY, artist);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    /**
     * Start an asynchronous OAuth credential check. 
     * Disables the Speak button until the check is complete.
    **/
    private void validateOAuth() {
        SpeechAuth auth =  SpeechAuth.forService(SpeechConfig.oauthUrl(), SpeechConfig.oauthScope(), SpeechConfig.oauthKey(), SpeechConfig.oauthSecret());
        auth.fetchTo(new OAuthResponseListener());
//        speakButton.setText(R.string.speak_button_wait);
//        speakButton.setEnabled(false);
    }
    
    /**
     * Handle the result of an asynchronous OAuth check.
    **/
    private class OAuthResponseListener implements SpeechAuth.Client 
    {
        public void handleResponse(String token, Exception error) {
            if (token != null) {
                oauthToken = token;
                readyForSpeech();
            }
            else {
                alert("Speech Unavailable", "This app was rejected by the speech service.  Contact the developer for an update.");
            }
        }
    }

    private void alert(String header, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
            .setTitle(header)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
