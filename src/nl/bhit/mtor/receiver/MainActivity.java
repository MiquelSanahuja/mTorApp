package nl.bhit.mtor.receiver;

import jim.h.common.android.zxinglib.integrator.IntentIntegrator;
import jim.h.common.android.zxinglib.integrator.IntentResult;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {
	
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private static final String SENDER_ID = "859874726228";
	
	public final static String EXTRA_MESSAGE = "nl.bhit.mtor.receiver.MainActivity.MESSAGE";
	
	private Handler handler = new Handler();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerGCM();
    }
    
	private void registerGCM() {
		GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
          GCMRegistrar.register(this, SENDER_ID);
          Log.v(TAG, "registered with sender id: "+ SENDER_ID);
        } else {
            Log.v(TAG, "Already registered with id"+regId);
        }
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
    	EditText editText = (EditText) findViewById(R.id.edit_message);
    	String message = editText.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
    
    public void scanQRCode(View view) {
        IntentIntegrator.initiateScan(MainActivity.this, R.layout.capture, R.id.viewfinder_view, R.id.preview_view, true);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IntentIntegrator.REQUEST_CODE:
                IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (scanResult == null) {
                    return;
                }
                final String result = scanResult.getContents();
                if (result != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                        	new RestAsyncCall().execute(getResApiURL(), getLoginUsername(), result);
                        }
                    });
                }
                break;
            default:
        }
    }
    
    private String getResApiURL() {
    	//TODO Load from preferences
    	//return "http://10.0.2.2:8080/services/api/";/*localhost*/
    	return "http://tomcat.bhit.nl/services/api/";
    }
    
    private String getLoginUsername() {
    	//TODO Get from authenticate login
    	return "admin";
    }
    
    private class RestAsyncCall extends AsyncTask<String, Void, Void> {
    	
    	@Override
    	protected Void doInBackground(String... params) {
	    	HttpClient httpClient = new DefaultHttpClient();
	    	HttpContext localContext = new BasicHttpContext();
	    	
	    	if (params == null || params.length < 3) {
	    		return null;
	    	}
	    	String restURL = params[0];
	    	String username = params[1];
	    	String qrToken = params[2];
	    	if (restURL == null || restURL.trim().length() == 0 ||
	    		username == null || username.trim().length() == 0  ||
	    		qrToken == null || qrToken.trim().length() == 0) {
	    		return null;
	    	}
	    	
	    	//TODO Implement a secure call.
	    	HttpGet httpGet = new HttpGet(restURL + "users/qr/" + username + "/" + qrToken);
	    	try {
		    	httpClient.execute(httpGet, localContext);
	    	} catch (Exception e) {
	    		Log.e(TAG, e.getLocalizedMessage());
	    		//TODO Implement nice message to user.
	    		Toast.makeText(getApplicationContext(), "LOGIN KO!!", Toast.LENGTH_LONG).show();
	    	}
	    	return null;
    	}
		
	}
    
}
