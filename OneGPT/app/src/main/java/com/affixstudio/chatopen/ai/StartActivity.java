package com.affixstudio.chatopen.ai;

import static com.affixstudio.chatopen.GetData.NetworkChangeReceiver.isOnline;
import static com.affixstudio.chatopen.ai.MainActivity.isPlanStockOut;
import static com.affixstudio.chatopen.ai.login.getUserID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.affixstudio.chatopen.GetData.ApiCall;
import com.affixstudio.chatopen.GetData.ApiResponse;
import com.affixstudio.chatopen.model.ExpertAIData;
import com.affixstudio.chatopen.model.ModelList;
import com.affixstudio.chatopen.model.PluginData;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.revenuecat.purchases.Offerings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class StartActivity extends AppCompatActivity implements ApiResponse {

    public static SharedPreferences sp;
    public static int isMember=0; //
    public static String planName="no"; //
    public static int totalCredit=0; //
    private  final int MY_REQUEST_CODE = 121;

    private boolean wasUpdating=false;

    private ImageView o, n, e, g, p, t;
    private TextView cursor;
    private Animation slideUpAndFadeIn, blinkAnimation;
    private int currentIndex = 0;
    private View[] views;

    public static ArrayList<ExpertAIData> expertAIData=new ArrayList<>();
    public static ArrayList<String> apiKeys=new ArrayList<>();
    public static ArrayList<String> rapidChatUrl=new ArrayList<>();
    public static ArrayList<String> aiCommand=new ArrayList<>();

    public static Offerings offerings;
    public static String rapidApIkey="";
    private boolean isGoDone=false;
    private boolean isAnimationDone=false;
    private boolean shouldStartActivity=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        sp=getSharedPreferences(getPackageName(),MODE_PRIVATE);
        boolean currentState = sp.getBoolean("darkMode", false);
        if (currentState) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }



        setContentView(R.layout.activity_start_screen);

        expertAIData.add(new ExpertAIData("ai","good","good","good"));

        o = findViewById(R.id.o);
        n = findViewById(R.id.n);
        e = findViewById(R.id.e);
        g = findViewById(R.id.g);


        views = new View[]{o, n, e, g};

        slideUpAndFadeIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_and_fade_in);
        blinkAnimation = AnimationUtils.loadAnimation(StartActivity.this, R.anim.blink);

        slideUpAndFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                currentIndex++;
                if (currentIndex < views.length) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startTypingAnimationForView(views[currentIndex]);
                        }
                    }, 100);
                } else {
                    // Here you open MainActivity
                    isAnimationDone=true;
                    if (isGoDone && shouldStartActivity) // when logo animation showed
                    {
                        isGoDone=false;
                      //  shouldStartActivity=false;
                        openActivity();
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        startTypingAnimationForView(o);

        Log.i(this.toString(),"email " +sp.getString("email","email"));
        if (getUserID(this).isEmpty())
        {
            new AlertDialog.Builder(this)
                    .setTitle("Not Available")
                    .setMessage("Currently our service is not available on this device. Use other device.")
                    .setPositiveButton("Understand", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finishAffinity();
                            System.exit(1);
                        }
                    })
                    .show();

        }






    }
    private AppUpdateManager appUpdateManager;
    InstallStateUpdatedListener listener;



    public static ArrayList<ModelList> modelLists=new ArrayList<>();
    public static ArrayList<PluginData> pluginDataLists=new ArrayList<>();


    BroadcastReceiver broadcastReceiver;
    public static Socket mSocket;


    public String portAddress ="port address";
    private void go()
    {

        Log.i("go","Called go function");

        ApiCall apiCall=new ApiCall(new ApiResponse() {
            @Override
            public void response(String response)
            {

                Log.i("MyApplication","Response from php "+response);
                if (!response.equals("100")) // 100 meaning error
                {
                    portAddress=response; //To do unRemove Comment
                }


                try {
                    mSocket = IO.socket(portAddress);

                    mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.i("Start","Socket.IO Connected!");

                            mSocket.emit("fetch_user_data",sp.getString("email","email"),getUserID(StartActivity.this));
                        }
                    });

                    mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.i("Start","Socket.IO Disconnected!");
                        }
                    });
                    mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.e("Socket.IO", "Error: " + args[0]);
                        }
                    });
                    mSocket.on("model_data_list", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {

                            i(args.length+" model_data_list = "+args[0]);
                            modelLists.clear();
                            try {

                                JSONArray ja = new JSONArray(args[0].toString());

                                for(int i=0;i<ja.length();i++)
                                {


                                    String jString= ja.getString(i);
                                    JSONObject jo=new JSONObject(jString);

                                    if (jo.getInt("isVisible")==1) // store only if it is set to visible
                                    {
                                        modelLists.add(new ModelList(jo.getInt("id"),
                                                jo.getString("model_name"),
                                                jo.getInt("isPremium"),
                                                jo.getInt("inputLimit"),
                                                jo.getInt("display_rank"),
                                                jo.getInt("isVisible"),
                                                jo.getInt("contextSize"),
                                                jo.getString("description"),
                                                jo.getString("welcomeMsg"),/*// todo copy and paste*/
                                                jo.getString("specGraph"),
                                                jo.getString("iconUrl")
                                        ));
                                        rapidApIkey=jo.getString("rapidApiKey");
                                    }



                                }

                                Comparator<ModelList> comparator = new Comparator<ModelList>() {
                                    @Override
                                    public int compare(ModelList model1, ModelList model2) {
                                        // Compare based on the Display_rank field in incremental order
                                        return Integer.compare(model1.getDisplay_rank(), model2.getDisplay_rank());
                                    }
                                };
                                // Sort the ArrayList using the custom comparator
                                Collections.sort(modelLists, comparator);
                                isGoDone=true;
                                openActivity();

                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }



                        }
                    });
                    mSocket.on("plugins_data_list", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {

                            i(args.length+" plugins_data_list = "+args[0]);
                            pluginDataLists.clear();
                            try {

                                JSONArray ja = new JSONArray(args[0].toString());

                                for(int i=0;i<ja.length();i++)
                                {


                                    String jString= ja.getString(i);
                                    JSONObject jo=new JSONObject(jString);

                                    if (jo.getInt("isVisible")==1) // store only if it is set to visible
                                    {
                                        pluginDataLists.add(new PluginData(jo.getInt("id"),
                                                jo.getString("name"),
                                                jo.getString("des"),
                                                jo.getString("icon"),
                                                jo.getInt("isVisible")
                                        ));

                                    }



                                }



                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }



                        }
                    });


                    mSocket.connect();

                    i("last of go");

                } catch (URISyntaxException e)
                {
                    e.printStackTrace();
                }
            }
        }, getString(R.string.getSocketPortEndpoint),getApplicationContext());

        apiCall.get();


    }

    public static int shouldOpenMainActivity=1; // when socket is get reconnected it's starting main activity again, and getting duplicate main activity
    //  to prevent multiple main activity
    private void openActivity() {
        if (shouldOpenMainActivity==1)
        {
            if (sp.getString("email","email").equals("email"))
            {
                startActivityWithAnimation(new Intent(StartActivity.this,login.class).putExtra("activity",0));
            }
            else
            {
                startActivityWithAnimation(new Intent(StartActivity.this,MainActivity.class));
            }
        }



    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!Objects.isNull(broadcastReceiver))
        {
            unregisterReceiver(broadcastReceiver);
        }
    }

    static int opened=0;
    @Override
    public void response(String response)
    {



        Log.i(this.toString(),"response "+response);
        if (response.equals("400") || response.equals("404"))
        {
            startActivityWithAnimation(new Intent(this,login.class).putExtra("activity",0));
        }else if (response.length()>7 )
        {

        try {


                setData(response,this);

                startActivityWithAnimation(new Intent(this,MainActivity.class).putExtra("calledFrom","StartActivity"));



            }catch (Exception e)
            {
                e.printStackTrace();
               // Toast.makeText(this, "Something went wrong please restart the app", Toast.LENGTH_SHORT).show();
            }

        }


    }


    @Override
    protected void onResume()
    {
        super.onResume();
        if (isOnline(StartActivity.this) )
        {

            if (!verifyInstallerId()) // todo make it true
            {
                if (opened == 0) {
                    Log.i("Start", "resume");
                    opened = 1;
                    go();
                }

            }
            else
            {
                new android.app.AlertDialog.Builder(this).setTitle("Invalid")
                        .setMessage("Please install the app from play store")
                        .setCancelable(false)
                        .setPositiveButton("Install", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.playstoreUrl)+getPackageName())));
                            }
                        })
                        .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finishAffinity();
                                System.exit(1);
                            }
                        }).show();
            }

        }else {
            startActivityWithAnimation(new Intent(this,NoInternetActivity.class));
        }


    }
    boolean verifyInstallerId()
    {
        // A list with valid installers package name
        List<String> validInstallers = new ArrayList<>(Arrays.asList("com.android.vending", "com.google.android.feedback"));
        // The package name of the app that has installed your app
        final String installer = getPackageManager().getInstallerPackageName(getPackageName());
        return installer != null && validInstallers.contains(installer);
    }

    public static void setData(String response, Context c)
    {
        try {
            JSONArray jsonArray = new JSONArray(response);


            String jb = jsonArray.getString(0);
            JSONObject jo = new JSONObject(jb);

            Log.i("setData","userID from server "+jo.getString("userID"));

            totalCredit = jo.getInt("haveReward");
            isMember = jo.getInt("isMember");
            planName = jo.getString("subscriptionId");
            isPlanStockOut=jo.getInt("isPlanStockOut");
            sp.edit().putString("email",jo.getString("email")).apply();

        }catch (Exception e)
        {
            e.printStackTrace();
            Toast.makeText(c, "Something went wrong please restart the app", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //opened=0;
    }
    void i(String s)
    {
        Log.i("Start",s);
    }
    public void startActivityWithAnimation(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    private void startTypingAnimationForView(View view) {
        view.setVisibility(View.VISIBLE);
        view.startAnimation(slideUpAndFadeIn);
    }
}