package com.affixstudio.chatopen.model;

import static com.affixstudio.chatopen.ai.ChatFragment.databaseVersion;
import static com.affixstudio.chatopen.ai.StartActivity.isMember;
import static com.affixstudio.chatopen.ai.StartActivity.planName;
import static com.affixstudio.chatopen.ai.StartActivity.sp;
import static com.affixstudio.chatopen.ai.StartActivity.totalCredit;
import static com.affixstudio.chatopen.ai.login.getUserID;
import static com.affixstudio.chatopen.model.ChatMessage.getCurrentTime;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.affixstudio.chatopen.GetData.ApiCall;
import com.affixstudio.chatopen.GetData.ApiResponse;
import com.affixstudio.chatopen.GetData.HttpRequest;
import com.affixstudio.chatopen.ai.MainActivity;
import com.affixstudio.chatopen.ai.R;
import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

public class FloatingWindowService extends Service implements ApiResponse {

    private WindowManager windowManager;
    private View floatingView;

    TextView replyText,questionText,creditLeft,replyCopy,replyShare;
    ImageView send,setting;
    TextView paste,clear,newQuery;
    EditText messageBox;
    private String replyString="reply";
    Database db;

    LinearLayout showMessageAI;
    LottieAnimationView typingAnni;
    TranslateAnimation anim=new TranslateAnimation(0,0,-40,0);


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    private void showFloatingWindow()
    {
        sp=getSharedPreferences(getPackageName(),MODE_PRIVATE);
        String query="CREATE TABLE IF NOT EXISTS "+sp.getString(getString(R.string.userIdtxt),"history")+ " "+getString(R.string.tableCreationSql);
        db=new Database(this,sp.getString(getString(R.string.userIdtxt),"history"),query,databaseVersion);


        // Create a WindowManager instance
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Inflate the floating window layout file
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_window, null);

        // Set up the layout parameters for the floating window
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,

                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );


        // Set the position of the floating window
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.x = 0;
        params.y =  50;
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        // Add the floating window to the screen
        windowManager.addView(floatingView, params);



        // Declaration

        ConstraintLayout EnterTextView=floatingView.findViewById(R.id.typeQuery);
        ConstraintLayout showResult=floatingView.findViewById(R.id.showResult);


        // initialization
        newQuery = floatingView.findViewById(R.id.newQuery);
        ImageView close=floatingView.findViewById(R.id.close);

        replyCopy=floatingView.findViewById(R.id.replyCopy);
        paste=floatingView.findViewById(R.id.paste);
        clear=floatingView.findViewById(R.id.clear);

        send=floatingView.findViewById(R.id.send);//
        replyShare=floatingView.findViewById(R.id.replyShare);
        setting=floatingView.findViewById(R.id.setting);

        replyText=floatingView.findViewById(R.id.replyText);//
        questionText=floatingView.findViewById(R.id.questionText);//
        creditLeft=floatingView.findViewById(R.id.creditLeft);//

        messageBox=floatingView.findViewById(R.id.messageBox);

        showMessageAI=floatingView.findViewById(R.id.showMessageAI);
        typingAnni=floatingView.findViewById(R.id.typingAnni);




        // set up click listener


        replyCopy.setOnClickListener(view -> {
            ClipboardManager clipboardManager=(ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            ClipData clipData= ClipData.newPlainText("Text",replyString);
            clipboardManager.setPrimaryClip(clipData);

            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
        });
        replyShare.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);

            intent.setType("text/plain");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Share using");
            intent.putExtra(Intent.EXTRA_TEXT, replyString);
            startActivity(intent);
        });

        clear.setOnClickListener(view -> {
            messageBox.setText("");
        });
        setting.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            stopSelf();
        });

        paste.setOnClickListener(view -> {
            // Get the clipboard manager
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

           // Check if the clipboard contains text
            if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                // Get the text from the clipboard
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                String textFromClipboard = item.getText().toString();

                messageBox.setText(messageBox.getText().toString()+" "+textFromClipboard);
                // Do something with the text

            } else {
                // Clipboard doesn't contain text
                Toast.makeText(this, "No thing to paste", Toast.LENGTH_SHORT).show();
            }

        });


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });
        newQuery.setOnClickListener(view -> {
            EnterTextView.setVisibility(View.VISIBLE);
            messageBox.setText("");
            messageBox.requestFocus();
            showResult.setVisibility(View.GONE);

        });

        TextView noBalance=floatingView.findViewById(R.id.noBalance);
        send.setOnClickListener(view -> {

            String message=messageBox.getText().toString();
            if (message.length()==0)
            {

                Toast.makeText(this, "Write something..", Toast.LENGTH_SHORT).show();
               // showSnackBar(,1);

            }
            else {
                if (isMember==0)
                {
//                    noBalance.setVisibility(View.VISIBLE);
                    noBalance.animate()
                            .translationY(0)
                            .setDuration(700)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    noBalance.setVisibility(View.VISIBLE);
                                }
                            });
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            noBalance.setVisibility(View.GONE);
                            noBalance.animate()
                                    .translationY(200)
                                    .setDuration(1000)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            noBalance.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    },3000);

                  //  Toast.makeText(this, "You don't have any Credits.", Toast.LENGTH_SHORT).show();
                } else
                {

                    EnterTextView.setVisibility(View.GONE);
                    //messageBox.setText("");
                    showResult.setVisibility(View.VISIBLE);
                    sendFun(message);
//            ApiCall get=new ApiCall(this,"https://affixstudio.co.in/php/getAppData.php?"+RadioSelected + getString(R.string.userIdtxt)+ "=" +
//                    getUserID(this)+"&"+getString(R.string.messagetxt)+"="+s,MainActivity.this);
//
//            get.get();

                }
            }

        });
        anim.setFillAfter(true);
        noBalance.setAnimation(anim);


        messageBox.requestFocus();
        // getting data from server
        String url=getString(R.string.singUpOrLoginUrlTEST)+"userID=" +getUserID(this)+"&email="+sp.getString("email","email");
        Log.i(this.toString(),"url "+url);
        ApiCall get=new ApiCall(this,url,this); // todo erase the default user id

        get.get();

//        floatingView.findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//                stopSelf();
//
//            }
//        });
    }

    private void sendFun(String message) {

        try {


        //chatRecycle.scrollToPosition(chatMessageList.size() - 1);
      //  insertIntoChat(0,getString(R.string.waitAsecond)); // optional reply

       // replyText.setText(getString(R.string.waitAsecond));
        questionText.setText(message);

       typingAnni.setVisibility(View.VISIBLE);
       showMessageAI.setVisibility(View.GONE);

        messageBox.setText("");

        boolean isToldToWait=message.equals(getString(R.string.waitAsecond)); // when showing typing





        if (!isToldToWait)
        {
            db.insertData(1,message,getCurrentTime(),-1,0,0, "0");
        }
        JSONArray messagesArr = new JSONArray();


        messagesArr.put(new JSONObject().put("role", "user").put("content","You are "+getString(R.string.app_name)+sp.getString("affixCommand",getString(R.string.defaultAiCommand)))); // normal ai command is also included in expertaidata
        messagesArr.put(new JSONObject().put("role", "assistant").put("content","Yes."));
            messagesArr.put(new JSONObject().put("role", "user").put("content", message));
        HttpRequest httpRequest=new HttpRequest(this);

        String s=getString(R.string.aiResponseEndpoints)+"aiType=2&" + getString(R.string.userIdtxt)+ "=" +
                getUserID(this)+"&"+getString(R.string.messagetxt)+"="+message+"&isShort=0&email="+sp.getString("email","")+"&isAuto=false&autoIndex=1";
        Log.i("float","url "+s);
        httpRequest.StartPost(s,messagesArr.toString());



        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void animateReplyText(String message) {
        String mText;
        final int[] mIndex = {0};
        long mDelay = 20; // in milliseconds
        mText=message;



        Handler mHandler = new Handler();
        Runnable characterAdder = null;
        characterAdder = new Runnable() {
            @Override
            public void run() {
                replyText.setText(mText.subSequence(0, mIndex[0]++));
                //chatRecycle.scrollToPosition(chat.size()-1);
                if (mIndex[0] <= mText.length()) {
                    mHandler.postDelayed(this, mDelay);
                }
            }
        };
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            windowManager.removeView(floatingView);
            floatingView = null;
        }
    }


    @Override
    public void response(String response) {
        Log.i(this.toString(),"response "+response);

        typingAnni.setVisibility(View.GONE);
        showMessageAI.setVisibility(View.VISIBLE);
        if (response.equals("100"))
        {
          //  deleteWaitASecondMessage();
            Toast toast = Toast.makeText(getApplicationContext(), "You don't have enough balance.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0); // set the gravity to top-left corner
            toast.show();
            animateReplyText("You don't have enough balance.");
           // Toast.makeText(this, "You don't have enough balance.", Toast.LENGTH_SHORT).show();

        }else if (response.equals("Error:affix"))
        {
            animateReplyText("Something went wrong. Please try again");

            //Toast.makeText(this, "Something went wrong. Please try again", Toast.LENGTH_SHORT).show();

        }else if (response.equals("200") || response.equals("404")) // device id and email is not matching with database
        {

            new AlertDialog.Builder(this)
                    .setTitle("User Not Found")
                    .setCancelable(false)
                    .setMessage("Something went wrong. Please login again.")
                    .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            stopSelf();
                        }
                    })
                    .show();
        }else if (response.length()>20)
        {
            try {





                JSONArray jsonArray=new JSONArray(response);





                String jb=jsonArray.getString(0);
                JSONObject jo = new JSONObject(jb);

                creditLeft.setVisibility(View.GONE);
                totalCredit=jo.getInt("haveReward");// ads reward credit
                isMember=jo.getInt("isMember");
                planName = jo.getString("subscriptionId");
                creditLeft.setText(totalCredit+" "+getString(R.string.currencyName));

                if(!Objects.isNull(creditLeft))
                {

                    creditLeft.setText(totalCredit+" credit left");
                }



                if (jsonArray.length()>1)
                {
                    jb=jsonArray.getString(1);
                    jo = new JSONObject(jb);

                    String reply=jo.getString("Reply");
                    reply=reply.replaceFirst("\\n\\n","");
                    //replyText.setText(reply);
                    animateReplyText(reply);
                    replyString=reply;
                    db.insertData(0,replyString,getCurrentTime(),-1,0,0, "0");


                }






            }catch (Exception e)
            {
                Log.e("MainActivity","error "+e.getMessage());
            }
        }

    }
}
