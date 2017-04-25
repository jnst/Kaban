package com.example.jnst.kaban;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.example.jnst.kaban.util.IabHelper;
import com.example.jnst.kaban.util.IabResult;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "Kaban";

    IInAppBillingService mService;
    IabHelper mHelper;

    ServiceConnection mServiceConn = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        mService = IInAppBillingService.Stub.asInterface(service);
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
          mService = null;
      }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        // Create helper instance
        String base64EncodedPublicKey = "CONSTRUCT_YOUR_KEY_AND_PLACE_IT_HERE";
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                }
            }
        });

        // Sample
        ArrayList<String> skuList = new ArrayList<>();
        skuList.add("premiumUpgrade");
        skuList.add("gas");
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
        try {
            Bundle skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);
        } catch (android.os.RemoteException e) {
            Log.e("MainActivity", e.getMessage());
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
