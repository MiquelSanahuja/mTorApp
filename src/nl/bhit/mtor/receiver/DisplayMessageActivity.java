package nl.bhit.mtor.receiver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import nl.bhit.mtor.receiver.model.ClientMessage;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class DisplayMessageActivity extends Activity {
	ScrollView textView;
	private DateFormat sdf = SimpleDateFormat.getDateTimeInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		textView = createTextView();
		setContentView(textView);
		new RetrieveMessagesTask().execute("http://tomcat.bhit.nl/mTor/services/api/messages/2.json");

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 1);
		Intent intent = new Intent(this, DisplayMessageActivity.class);
		intent.putExtra("alarm_message", "O'Doyle Rules!");
		// In reality, you would want to have a static variable for the request
		// code instead of 192837
		PendingIntent sender = PendingIntent.getBroadcast(this, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
	}
	private ScrollView createTextView() {
		ScrollView scrollView = new ScrollView(this);
		TextView textView = new TextView(this);
		textView.setTextSize(12);
		textView.setText("temp message....");
		scrollView.addView(textView);
		return scrollView ;
	}

	List<ClientMessage> messages;
	private String getMessage(String url) {
		messages = getContacts(url);
		return "";
	}
	private TableLayout createTable(){
		TableLayout table = new TableLayout(this);

		TableRow.LayoutParams params1 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 25);
		TableRow.LayoutParams params2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 25);
		TableRow.LayoutParams params3 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 25);
		TableRow row;
		row = new TableRow(this);
		TableRow.LayoutParams params4 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, 25);
		params4.span=3;
		params4.column=1;
		row.addView(createTextView(params4, sdf.format(new Date())));
		table.addView(row);
		
		if (messages == null) {
			row = new TableRow(this);
			row.addView(createTextView(params3, "nothing found"));
			table.addView(row);
		}
		for (ClientMessage clientMessage : messages) {
			row = new TableRow(this);
			row.addView(createTextView(params1, ""+clientMessage.getProjectId()));
			row.addView(createTextView(params2, clientMessage.getStatus()));
			row.addView(createTextView(params3, clientMessage.getContent()));
			table.addView(row);
		}

		return table;
	}

	private TextView createTextView(TableRow.LayoutParams params, String text) {
		TextView textView = new TextView(this);
		textView.setLayoutParams(params);
		textView.setTextSize(12);		
		textView.setText(text);
		return textView;
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	/**
	 * Get method for the JSON Contacts
	 */
	public List<ClientMessage> getContacts(String url) {
		try {

			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAccept(Collections.singletonList(new MediaType("application", "json")));
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

			MappingJacksonHttpMessageConverter messageConverter = new MappingJacksonHttpMessageConverter();
			List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
			messageConverters.add(messageConverter);

			RestTemplate restTemplate = new RestTemplate();
			restTemplate.setMessageConverters(messageConverters);

			ResponseEntity<ClientMessage[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ClientMessage[].class);
			ClientMessage[] result = responseEntity.getBody();
			return Arrays.asList(result);
		} catch (Exception e) {
			Log.d(ACTIVITY_SERVICE, "no connection", e);
		}
		return null;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class RetrieveMessagesTask extends AsyncTask<String, Void, String> {
		/**
		 * The system calls this to perform work in a worker thread and delivers
		 * it the parameters given to AsyncTask.execute()
		 */
		protected String doInBackground(String... url) {
			return getMessage(url[0]);
		}

		/**
		 * The system calls this to perform work in the UI thread and delivers
		 * the result from doInBackground()
		 */
		protected void onPostExecute(String result) {
			textView.removeAllViews();
			textView.addView(createTable());
		}
	}
}
