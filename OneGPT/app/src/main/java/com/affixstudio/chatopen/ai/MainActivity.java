package com.affixstudio.chatopen.ai;

import static com.affixstudio.chatopen.GetData.NetworkChangeReceiver.isOnline;

import static com.affixstudio.chatopen.ai.StartActivity.isMember;
import static com.affixstudio.chatopen.ai.StartActivity.mSocket;
import static com.affixstudio.chatopen.ai.StartActivity.shouldOpenMainActivity;
import static com.affixstudio.chatopen.ai.StartActivity.totalCredit;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.affixstudio.chatopen.GetData.ApiCall;
import com.affixstudio.chatopen.GetData.ApiResponse;
import com.affixstudio.chatopen.GetData.AssistantData;
import com.affixstudio.chatopen.GetData.NetworkChangeReceiver;
import com.affixstudio.chatopen.model.CloseDialog;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;


public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener, ApiResponse {



    SharedPreferences sp;


    public static int isPlanStockOut=0;

    private Fragment chatFragment;

  //  private Fragment artFragment;
    private Fragment toolFragment;
    private Fragment activeFragment;
    LinearLayout openSubscription,openActiveSubscription;

    TextView credit,premiumCredit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // when socket is get reconnected it's starting main activity again, and getting duplicate main activity
        //  to prevent multiple main activity

        shouldOpenMainActivity=0;
        //startActivity(new Intent(this, AIToolsChatActivity.class));

        Log.i("Main","called from "+getIntent().getStringExtra("calledFrom"));



        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);

        chatFragment = new ChatFragment();
       // artFragment = new ArtFragment();
        toolFragment = new ToolFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, toolFragment, "4").hide(toolFragment).commit();
        //getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, artFragment, "3").hide(artFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, chatFragment, "1").commit();
        activeFragment = chatFragment;

        sp=getSharedPreferences(getPackageName(),MODE_PRIVATE);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        DrawerLayout drawer = findViewById(R.id.drawer);
      //  drawer.closeDrawer(GravityCompat.START);

        IntentFilter drawerIntent=new IntentFilter(getString(R.string.openDrawerAction));

        openDrawer=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                drawer.open();
            }
        };
        registerReceiver(openDrawer,drawerIntent);

        IntentFilter subIntent=new IntentFilter(getString(R.string.subIntentAction));

        subRefresh=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                setSubLayout();

            }
        };
        registerReceiver(subRefresh,subIntent);

      //  AppLovinSdk.getInstance( this ).showMediationDebugger();

        // Inflate the header view and find the component with its ID
        View headerView = navigationView.getHeaderView(0);
        openSubscription = headerView.findViewById(R.id.openSubscription);
        openActiveSubscription = headerView.findViewById(R.id.openActiveSubscription);

        credit=headerView.findViewById(R.id.credit);
        premiumCredit=headerView.findViewById(R.id.premiumCredit);

        LinearLayout chat=headerView.findViewById(R.id.chat);

        LinearLayout tool=headerView.findViewById(R.id.tool);



        chat.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {
                setAllbackWhite(headerView,0);
                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(chatFragment).commit();
                activeFragment = chatFragment;


           //     chat.setBackgroundColor(getColor(R.color.colorPrimary));
                drawer.close();


            }
        });

        tool.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {
                setAllbackWhite(headerView,2);
                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(toolFragment).commit();
                activeFragment = toolFragment;

           //     chat.setBackgroundColor(getColor(R.color.colorPrimary));


                drawer.close();
            }
        });
       headerView.findViewById(R.id.purchase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sp.getString("email","email").contains("@"))
                {
                    startActivityWithAnimation(new Intent(MainActivity.this,PurchaseActivity.class));
                } else {
                    startActivityWithAnimation(new Intent(MainActivity.this,login.class).putExtra("activity",1));
                }
            }
        });
        headerView.findViewById(R.id.aiTool).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityWithAnimation(new Intent(MainActivity.this,AIToolsActivity.class));
            }
        });

        chat.performClick();
        //TextView subText=headerView.findViewById(R.id.subText);


        // Set a click listener for the 'openSubscription' component
        openSubscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               openLink(getString(R.string.freelanceLink));

                //  startActivity(new Intent(MainActivity.this,PurchaseActivity.class));
                // Handle the click event here
                // For example, open a new activity, show a toast, etc.
            }
        });
        openActiveSubscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sp.getString("email","email").contains("@"))
                {
                    startActivityWithAnimation(new Intent(MainActivity.this,PurchaseActivity.class));
                }else
                {
                    startActivityWithAnimation(new Intent(MainActivity.this,login.class).putExtra("activity",1));
                }
               // startActivity(new Intent(MainActivity.this,PurchaseActivity.class));
            }
        });




        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        receiver=new NetworkChangeReceiver();
        registerReceiver(receiver,filter);
        setSubLayout();
        getAssitantData();

        mSocket.on("user_data", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                Log.i("MainActivity","user_Data = "+args[0]);
                String r= args[0].toString();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // Code to run on the main thread
                        responseUserData(r);
                    }
                });


            }
        });
        mSocket.on("user_data_fetched", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                Log.i("MainActivity","user_Data = "+args[0]);
                String r= args[0].toString();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // Code to run on the main thread

                        responseUserData(r);
                    }
                });


            }
        });

    }

    private void setSubLayout() {
        if (isMember==1)
        {
            //subText.setText("Subscriber");
         //   openSubscription.setVisibility(View.GONE);
          //  openActiveSubscription.setVisibility(View.VISIBLE);
        }
    }

    private void setAllbackWhite(View h,int viewIndex) {

        int[][] ids={{R.id.chat,R.id.art,R.id.tool},  {R.id.chatIMG,R.id.artIMG,R.id.toolIMG},{R.id.chatTxt,R.id.artTxt,R.id.toolTxt}};


        for (int i=0;i<ids.length;i++)
        {
            for (int j=0;j<ids[i].length;j++)
            {
                if (i==0)
                {
                    LinearLayout layout=h.findViewById(ids[i][j]);

                    if (viewIndex==j)
                    {
                        layout.setBackgroundColor(getColor(R.color.colorPrimary));
                    }else
                    {
                        layout.setBackgroundColor(getColor(R.color.card_white));
                    }

                }else if (i==1)
                {
                    ImageView img=h.findViewById(ids[i][j]);


                    if (viewIndex==j)
                    {
                        img.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN);
                    }else {
                        img.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray), PorterDuff.Mode.SRC_IN);
                    }
                }else if (i==2)
                {
                    TextView textView=h.findViewById(ids[i][j]);
                    if (viewIndex==j) {
                        textView.setTextColor(getColor(R.color.white));
                    }else {
                        textView.setTextColor(getColor(R.color.dark_gray));
                    }

                }




            }
        }

    }

    private void changeDrawableLeft(TextView chat) {

        Drawable[] drawables = chat.getCompoundDrawables(); // get the current drawables
        Drawable leftDrawable = drawables[0]; // get the drawable on the left

        if(leftDrawable != null){
            // Create a new PorterDuffColorFilter with your desired color and mode
        //    PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(Color.valueOf(R.color.dark_gray), PorterDuff.Mode.SRC_ATOP);

            // Apply the color filter to the drawable
        //    leftDrawable.setColorFilter(colorFilter);
        }

    }
    void openLink(String s) {
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setData( Uri.parse(s));
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    BroadcastReceiver openDrawer;
    BroadcastReceiver subRefresh;
    private void getAssitantData() {

        String url=getString(R.string.getAssistantDataEndpoints);
        Log.i(this.toString(),"url "+url);
        ApiCall get=new ApiCall(this,url,this); // todo erase the default user id

        get.get();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!wasUpdating)
        {
            if (isOnline(this) )
            {

              //  getData();
                getUpdate();

            }else {
                startActivityWithAnimation(new Intent(this,NoInternetActivity.class));
            }
        }

    }
    private AppUpdateManager appUpdateManager;
    InstallStateUpdatedListener listener;
    private  final int MY_REQUEST_CODE = 121;
    private boolean wasUpdating=false;
    void getUpdate()
    {
        /* immedate = 1
         * flexible=0 */


        int  updateType=1;

        Log.i("inAppUpdate","first");
        appUpdateManager = AppUpdateManagerFactory.create(this); //
        listener = state -> {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                // After the update is downloaded, show a notification
                // and request user confirmation to restart the app.
                Toast.makeText(this, "App updated", Toast.LENGTH_LONG).show();
                appUpdateManager.completeUpdate();
            }

        };
        appUpdateManager.registerListener(listener);

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();


        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {


            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) // update available

            {
                try {

                    wasUpdating=true;
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, MY_REQUEST_CODE);


                } catch (IntentSender.SendIntentException e) {
                    Log.e("inAppUpdate",e.getMessage());
                }

            }

            Log.i("inAppUpdate","last");
        });


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE)
        {
            if ( resultCode != RESULT_OK  ) // when update type immediate and failed
            {


                new android.app.AlertDialog.Builder(this)
                        .setIcon(R.drawable.crisis_alert_24)
                        .setTitle("Update failed")
                        .setMessage("Please close the application and try again.")
                        .setCancelable(true)

                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finishAffinity();
                                System.exit(0);
                            }
                        })
                        .show();

            }
        }
    }



    BroadcastReceiver receiver;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
            unregisterReceiver(openDrawer);
            unregisterReceiver(subRefresh);
            if (mSocket.isActive() )
            {
                mSocket.close();
            }
        }catch (Exception e)
        {

            Log.e("e",e.getMessage());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.drawer);
        drawer.closeDrawer(GravityCompat.START);

        FragmentTransaction transaction;

        switch (item.getItemId()) {
            case R.id.navigation_chat:
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right);
                transaction.replace(R.id.chatLV, chatFragment);
                transaction.commit();
                activeFragment = chatFragment;
                return true;

            case R.id.navigation_art:
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right);
               /// transaction.replace(R.id.artLV, artFragment);
               // transaction.commit();
                //activeFragment = artFragment;
                return true;

            case R.id.navigation_tool:
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right);
                transaction.replace(R.id.main, toolFragment);
                transaction.commit();
                activeFragment = toolFragment;
                return true;
        }


        return false;
    }

    @Override
    public void onBackPressed() {
        CloseDialog close=new CloseDialog();
        close.show(this);
    }
    public void startActivity(Intent intent, boolean isForward) {
        super.startActivity(intent);
        if (isForward) {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
    public void startActivityWithAnimation(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    static ArrayList<AssistantData> toolsList=new ArrayList<>();

    @Override
    public void response(String response) {
        if (response.isBlank())
        {
            return;
        }

        try {
            JSONArray jsonArray=new JSONArray(response);
            for (int i=0;i<jsonArray.length();i++)
            {
                String jb=jsonArray.getString(i);
                JSONObject jo = new JSONObject(jb);

                AssistantData assistantData=new AssistantData(
                        jo.getString("tool_name"),
                        jo.getString("tool_description"),
                        jo.getString("tool_prompt"),
                        jo.getString("tool_icon")
                );

                toolsList.add(assistantData);


            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }



    }


    void responseUserData(String response)
    {
        //      processResponse(response);
        if (response.isBlank())
        {
            return;
        }

        if (response.length()>20)
        {
            try {

                JSONObject jo;
                JSONArray jsonArray=new JSONArray();
                String jb="";
                try { // node js server returning json object not array
                    jsonArray=new JSONArray(response);
                    jb=jsonArray.getString(0);
                    jo = new JSONObject(jb);
                }catch (Exception e)
                {
                    jo = new JSONObject(response);
                }





                totalCredit=jo.getInt("haveReward");// ads reward credit
                isMember=jo.getInt("isMember");


                if (isMember==1)
                {
                    credit.setText(R.string.no_limit);
                }else {
                    credit.setText(totalCredit+" "+getString(R.string.credit));
                }


                premiumCredit.setText(jo.getInt("premiumCredits")+" "+getString(R.string.premium_credit));


            }
            catch (Exception e)
            {

                e.printStackTrace();
                Log.e("MainActivity","error "+e.getMessage());
            }
        }





    }

}