package com.affixstudio.chatopen.GetData;

import static android.content.Context.CONNECTIVITY_SERVICE;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.affixstudio.chatopen.ai.NoInternetActivity;

import java.util.List;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private boolean isConnected=false;


    @Override
    public void onReceive(Context context, Intent intent) {

        isNetworkAvailable(context);

    }


    private void isNetworkAvailable(Context context)
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
                        if (!isConnected)
                        {
                           isConnected = true;


                        }

                        return;
                    }
                }
            }
        }




        if (!isAppIsInBackground(context))
        {
            context.startActivity(new Intent(context, NoInternetActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }


        isConnected = false;
    }
    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        isInBackground = false;
                    }
                }
            }
        }

        return isInBackground;
    }
    static public boolean isOnline(Context c)
    {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
