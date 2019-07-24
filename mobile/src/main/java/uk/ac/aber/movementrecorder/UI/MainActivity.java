package uk.ac.aber.movementrecorder.UI;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import uk.ac.aber.movementrecorder.Data.SamplingService;
import uk.ac.aber.movementrecorder.R;

import static uk.ac.aber.movementrecorder.Data.SamplingService.ServiceReceiver.PHONE_SS_ACTION_RESP;

public class MainActivity extends AppCompatActivity implements PersonalDataFragment.IPersonalDataFragment{

    private TimeSeriesFragment timeSeriesFragment;
    private PersonalDataFragment personalDataFragment;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private SensorFragment sensorFragment;
    private NavigationView navigationView;
    public static final String PREFS_NAME = "uk.ac.aber.movementrecorder.preferences";

    NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            changeFragment(item.getItemId());
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);
        drawer = findViewById(R.id.dlNavigation);
        toggle = new ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //timeSeriesFragment = new TimeSeriesFragment();
        //timeSeriesFragment.setContext(this);

        //sensorPickerFragment = new SensorPickerFragment();

        //personalDataFragment = new PersonalDataFragment();
        changeFragment(R.id.navSampling);

        // Turn on bluetooth
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBtAdapter.isEnabled()) {
            mBtAdapter.enable();
        }

    }

    @Override
    protected void onResume() {

        super.onResume();

//        if (!isServiceRunning(MessageService.class)) {
//            this.startService(new Intent(this, MessageService.class));
//        }

        // Let the service know that the app is in the foreground
        if (isServiceRunning(SamplingService.class)) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(PHONE_SS_ACTION_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("id", "message");
            broadcastIntent.putExtra("message", "running");
            this.sendBroadcast(broadcastIntent);
        }
    }

    @Override
    protected void onStop() {

        if (!isServiceRunning(SamplingService.class)) {
            // Destroy messagelistener
//            Intent broadcastIntent = new Intent();
//            broadcastIntent.setAction(TS_RESP);
//            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
//            broadcastIntent.putExtra("id", "message");
//            broadcastIntent.putExtra("message", "destroy");
//            this.sendBroadcast(broadcastIntent);
        }
        else {
            // Let the service know that the app is no longer in the foreground
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(PHONE_SS_ACTION_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("id", "message");
            broadcastIntent.putExtra("message", "closing");
            this.sendBroadcast(broadcastIntent);
        }

        super.onStop();
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    public void dataAdded(String id, String yob, String height, String weight) {

    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void changeFragment(int fragID) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment;
        switch (fragID) {
            case R.id.navSampling:
                fragment = new TimeSeriesFragment();
                break;
            case R.id.navParticipantData:
                fragment = new PersonalDataFragment();
                break;
            case R.id.navSensorSettings:
                fragment = new SensorFragment();
                break;
            case R.id.navAdvancedSettings:
                fragment = new AdvancedSettingsFragment();
                break;
            default:
                drawer.closeDrawer(GravityCompat.START);
                return;
        }

        transaction.replace(R.id.fragmentFrame, fragment);
        //transaction.addToBackStack(null);
        transaction.commit();

        drawer.closeDrawer(GravityCompat.START);
    }
}

