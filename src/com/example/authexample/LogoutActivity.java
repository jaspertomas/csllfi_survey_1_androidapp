package com.example.authexample;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

public class LogoutActivity extends Activity {

	private final static String LOGOUT_API_ENDPOINT_URL = Constants.SERVER_URL+"api/v1/sessions.json";
	private SharedPreferences mPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_logout);

	    mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
	}

}
