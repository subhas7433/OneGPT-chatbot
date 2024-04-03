package com.affixstudio.chatopen.ai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.affixstudio.chatopen.GetData.NetworkChangeReceiver;

public class NoInternetActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        //finish();
        super.onBackPressed();
    }
    BroadcastReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);

 //       getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        receiver=new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)

            {
                ConnectivityManager connectivity = (ConnectivityManager)
                        context.getSystemService(Context.CONNECTIVITY_SERVICE);

                if (connectivity != null)
                {

                    NetworkInfo[] info = connectivity.getAllNetworkInfo();
                    if (info != null)
                    {
                        for (NetworkInfo networkInfo : info) {

                            if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {

                                onBackPressed();

                            }
                        }
                    }
                }
            }
        };


        registerReceiver(receiver, filter);

        findViewById(R.id.turnOn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("onInternet","onDestroy");
        try {
            unregisterReceiver(receiver);
        }catch (Exception e)
        {
            Log.e("e",e.getMessage());
        }
    }
    public void startActivityWithAnimation(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}