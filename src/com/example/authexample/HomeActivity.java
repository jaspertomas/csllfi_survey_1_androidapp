package com.example.authexample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.savagelook.android.UrlJsonAsyncTask;

public class HomeActivity extends Activity {

	private static final String TASKS_URL = Constants.SERVER_URL+"api/v1/tasks.json";
	private SharedPreferences mPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

	    mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
	    //loadTasksFromAPI(TASKS_URL);
	}
	
	
	private void loadTasksFromAPI(String url) {
	    GetTasksTask getTasksTask = new GetTasksTask(HomeActivity.this);
	    getTasksTask.setMessageLoading("Loading tasks...");
	    //getTasksTask.execute(url);
	    getTasksTask.execute(url + "?auth_token=" + mPreferences.getString("AuthToken", ""));
	}
	
	private class GetTasksTask extends UrlJsonAsyncTask {
	    public GetTasksTask(Context context) {
	        super(context);
	    }

	    @Override
        protected void onPostExecute(JSONObject json) {
            try {
            	//display json string
        		Toast.makeText(getApplicationContext(), json.toString(), Toast.LENGTH_LONG).show();

        		if(!json.getBoolean("success"))
            	{
//                	Toast.makeText(context, json.getString("info"),Toast.LENGTH_LONG).show();
//                	Toast.makeText(context, "Server not found",Toast.LENGTH_LONG).show();
            		return;
            	}
            	
                JSONArray jsonTasks = json.getJSONObject("data").getJSONArray("tasks");
                int length = jsonTasks.length();
                List<String> tasksTitles = new ArrayList<String>(length);

                for (int i = 0; i < length; i++) {
                    tasksTitles.add(jsonTasks.getJSONObject(i).getString("title"));
                }

                ListView tasksListView = (ListView) findViewById (R.id.tasks_list_view);
                if (tasksListView != null) {
                    tasksListView.setAdapter(new ArrayAdapter<String>(HomeActivity.this,
                      android.R.layout.simple_list_item_1, tasksTitles));
                }
            } catch (Exception e) {
            	Toast.makeText(context, e.getMessage(),Toast.LENGTH_LONG).show();
	        } finally {
	            super.onPostExecute(json);
	        }
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_logout:
	        	logout();
	            return true;
	        case R.id.menu_refresh:
	            loadTasksFromAPI(TASKS_URL);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void onResume() {
	    super.onResume();

	    if (mPreferences.contains("AuthToken")) {
	        loadTasksFromAPI(TASKS_URL);
	    } else {
	        Intent intent = new Intent(HomeActivity.this, WelcomeActivity.class);
	        startActivityForResult(intent, 0);
	    }
	}


//	public void logout(View button) {
//        LogoutTask logoutTask = new LogoutTask(LogoutActivity.this);
//        logoutTask.setMessageLoading("Logging in...");
//        logoutTask.execute(LOGIN_API_ENDPOINT_URL);
//	}
	private final static String LOGOUT_API_ENDPOINT_URL = Constants.SERVER_URL+"api/v1/sessions";

	public void logout()
	{

        LogoutTask logoutTask = new LogoutTask(HomeActivity.this);
        logoutTask.setMessageLoading("Logging out...");
        logoutTask.execute(LOGOUT_API_ENDPOINT_URL);

	}	
	
	private class LogoutTask extends UrlJsonAsyncTask {
	    public LogoutTask(Context context) {
	        super(context);
	    }

	    @Override
	    protected JSONObject doInBackground(String... urls) {
	        DefaultHttpClient client = new DefaultHttpClient();
	        String authtoken=mPreferences.getString("AuthToken", "");
	        HttpDelete post = new HttpDelete(urls[0]+"?auth_token="+authtoken+"");

	        String response = null;
	        JSONObject json = new JSONObject();

	        try {
	            try {
	                // setup the returned values beforehand, 
	                // in case something goes wrong
	                json.put("success", false);
	                json.put("info", "Server not found");
	                
	                // add the user email and password to
	                // the params
//	                userObj.put("email", mUserEmail);
//	                userObj.put("password", mUserPassword);
//	                holder.put("user", userObj);
//	                StringEntity se = new StringEntity(holder.toString());
//	                post.setEntity(se);

	                // setup the request headers
	                post.setHeader("Accept", "application/json");
	                post.setHeader("Content-Type", "application/json");

//                	Toast.makeText(context, post.toString(),Toast.LENGTH_LONG).show();
	                Log.e("ClientProtocol Post", post.getURI().toString());
	                
	                ResponseHandler<String> responseHandler = new BasicResponseHandler();
	                response = client.execute(post, responseHandler);
	                json = new JSONObject(response);


	            } catch (HttpResponseException e) {

	                Log.e("ClientProtocol JSON", json.toString());
	                
	                e.printStackTrace();
	                Log.e("ClientProtocol", "" + e);
	                json.put("info", "HttpResponseException");
	            } catch (IOException e) {
	                e.printStackTrace();
	                Log.e("IO", "" + e);
	            }
	        } catch (JSONException e) {
	            e.printStackTrace();
	            Log.e("JSON", "" + e);
	        }

	        return json;
	    }

	    @Override
	    protected void onPostExecute(JSONObject json) {
	        try {
	        	
            	//display json string
        		Toast.makeText(getApplicationContext(), json.toString(), Toast.LENGTH_LONG).show();        		
        		
	            if (json.getBoolean("success")) {
	                // everything is ok
	        		mPreferences.edit().remove("AuthToken").commit();

	                // launch the HomeActivity and close this one
	                Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
	                startActivityForResult(intent, 0);
	                //finish();
	            }
	            Toast.makeText(context, json.getString("info"), Toast.LENGTH_LONG).show();
	        } catch (Exception e) {
	            // something went wrong: show a Toast
	            // with the exception message
	            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
	        } finally {
	            super.onPostExecute(json);
	        }
	    }
	}

}

