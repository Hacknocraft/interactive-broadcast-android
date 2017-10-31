package com.fancred.android.ibsample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.fancred.android.ibsample.events.EventAdapter;
import com.tokbox.android.IB.CelebrityHostActivity;
import com.tokbox.android.IB.FanActivity;
import com.tokbox.android.IB.config.IBConfig;
import com.tokbox.android.IB.events.EventProperties;
import com.tokbox.android.IB.events.EventRole;
import com.tokbox.android.IB.events.EventStatus;
import com.tokbox.android.IB.events.EventUtils;
import com.tokbox.android.IB.model.InstanceApp;
import com.tokbox.android.IB.ws.WebServiceCoordinator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Main Activity
 */
public class EventListActivity extends AppCompatActivity implements WebServiceCoordinator.Listener {

    private static final String LOG_TAG = EventListActivity.class.getSimpleName();
    private WebServiceCoordinator mWebServiceCoordinator;
    private ProgressDialog mProgress;
    private Handler mHandler = new Handler();
    private ArrayList<JSONObject> mEventList = new ArrayList<>();
    private EventAdapter mEventAdapter;
    private GridView mListActivities;
    private TextView mEventListTitle;
    private JSONArray mArrEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(IBConfig.ADMIN_ID == null || IBConfig.ADMIN_ID.equals("")) {
            goToMainActivity();
        }
        setContentView(R.layout.event_list_activity);

        mWebServiceCoordinator = new WebServiceCoordinator(this, this);

        //Set fonts
        mEventListTitle = (TextView) findViewById(R.id.event_list_title);
        Typeface font = EventUtils.getFont(this);
        mEventListTitle.setTypeface(font);

        //start the progress bar
        startLoadingAnimation();

        //
        getEventsByAdmin();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(LOG_TAG, "Back from the event");
        goToMainActivity();
    }

    private void goToMainActivity() {
        Intent localIntent;
        localIntent = new Intent(EventListActivity.this, MainActivity.class);
        startActivity(localIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {

        super.onResume();

        if(mListActivities != null) {
            mListActivities.setAdapter(null);
            //start the progress bar
            startLoadingAnimation();
            getEventsByAdmin();
        }

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void getEventsByAdmin() {
        mWebServiceCoordinator.getEventsByAdmin();
    }

    private void startLoadingAnimation() {
        mProgress = new ProgressDialog(this);
        mProgress.setTitle("Loading");
        mProgress.setMessage("Wait while loading...");
        mProgress.setCancelable(false);
        mProgress.show();
    }

    private void stopLoadingAnimation() {
        if(mProgress != null && mProgress.isShowing()){
            mProgress.dismiss();
        }
    }

    private void showEventList() {
        Log.i(LOG_TAG, "starting event list app");

        mListActivities = (GridView) findViewById(R.id.gridView);
        mEventList.clear();
        try {
            for (int i=0; i<mArrEvents.length(); i++) {
//                if(!mArrEvents.getJSONObject(i).getString(EventProperties.STATUS).equals(EventStatus.CLOSED)) {
                    mEventList.add(mArrEvents.getJSONObject(i));
//                }
            }
        } catch(JSONException ex) {
            Log.e(LOG_TAG, ex.getMessage());
        }

        mEventAdapter = new EventAdapter(this, R.layout.event_item, mEventList);

        mListActivities.setAdapter(mEventAdapter);
    }

    private void showEvent() {
        //Passing the apiData to AudioVideoActivity
        Intent localIntent;
        if(IBConfig.USER_TYPE == EventRole.FAN) {
            localIntent = new Intent(EventListActivity.this, FanActivity.class);
        } else {
            localIntent = new Intent(EventListActivity.this, CelebrityHostActivity.class);
        }

        Bundle localBundle = new Bundle();
        localBundle.putString("event_index", "0");
        localIntent.putExtras(localBundle);
        startActivityForResult(localIntent, 0);
    }

    public void showEvent(int event_index) {
        //Passing the apiData to AudioVideoActivity
        Intent localIntent;
        if(IBConfig.USER_TYPE == EventRole.FAN) {
            localIntent = new Intent(EventListActivity.this, FanActivity.class);
        } else {
            localIntent = new Intent(EventListActivity.this, CelebrityHostActivity.class);
        }
        Bundle localBundle = new Bundle();
        localBundle.putString("event_index", Integer.toString(event_index));
        localIntent.putExtras(localBundle);
        startActivity(localIntent);
        finish();
    }

    /**
     * Web Service Coordinator delegate methods
     */
    @Override
    public void onDataReady(JSONObject instanceAppData) {
        //Set instanceApp Data
        InstanceApp.getInstance().setData(instanceAppData);

        mArrEvents = new JSONArray();
        try {
            if(instanceAppData.has("events")) {
                mArrEvents = instanceAppData.getJSONArray("events");
            }

            //Check the count of events.
            if(mArrEvents.length() > 1) {
                showEventList();
            } else {
                if(mArrEvents.length() == 1) {
                    showEvent();
                } else {
                    Toast.makeText(getApplicationContext(),"No events were found", Toast.LENGTH_LONG).show();
                }
            }

        } catch(JSONException e) {
            Log.e(LOG_TAG, "parsing instanceAppData error", e);
        } finally {
            stopLoadingAnimation();
        }

    }

    @Override
    public void onWebServiceCoordinatorError(Exception error) {
        Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
        Toast.makeText(getApplicationContext(),"Unable to connect to the server. Trying again in 5 seconds..", Toast.LENGTH_LONG).show();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getEventsByAdmin();
            }
        }, 5000);
    }

}