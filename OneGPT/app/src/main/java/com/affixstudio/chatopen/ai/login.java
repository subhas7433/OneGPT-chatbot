package com.affixstudio.chatopen.ai;

import static com.affixstudio.chatopen.GetData.NetworkChangeReceiver.isOnline;
import static com.affixstudio.chatopen.ai.StartActivity.setData;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


import com.affixstudio.chatopen.GetData.ApiCall;
import com.affixstudio.chatopen.GetData.ApiResponse;
import com.affixstudio.chatopen.GetData.HttpRequest;
import com.affixstudio.chatopen.GetData.NetworkChangeReceiver;
import com.affixstudio.chatopen.model.CloseDialog;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.AccountPicker;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class login extends AppCompatActivity implements ApiResponse {

    private static final int REQUEST_CODE = 1234;

    int activity=0;
    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        activity=getIntent().getIntExtra("activity",4);

        sp=getSharedPreferences(getPackageName(),MODE_PRIVATE);

        CheckBox agree=findViewById(R.id.agreeCheckbox);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (agree.isChecked())
                {
                    Intent googlePicker = AccountPicker
                            .newChooseAccountIntent(null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE},
                                    true, null, null, null, null);
                    startActivityForResult(googlePicker, REQUEST_CODE);

                    onActivityResult(1234,456,googlePicker);
                }else
                {
                    Snackbar snackbar= Snackbar.make(findViewById(R.id.mainLognin), "Please agree to the terms to proceed.", Snackbar.LENGTH_SHORT)
                            .setTextColor(ContextCompat.getColor(login.this, R.color.white))
                            .setBackgroundTint(ContextCompat.getColor(login.this, R.color.red));
                    snackbar.show();
                }



            }
        });

        if (activity==1)
        {
            findViewById(R.id.guest).setVisibility(View.GONE);
        }else {
            findViewById(R.id.guest).setVisibility(View.VISIBLE);
        }
        findViewById(R.id.guest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (agree.isChecked())
                {
                    ApiCall getUser=new ApiCall(login.this,getString(R.string.addOrGetGoustData)+"userID="
                            +getUserID(login.this),login.this);

                    pd=new ProgressDialog(login.this);
                    pd.setMessage("Fetching Data..");
                    pd.setCancelable(false);
                    pd.getWindow().setBackgroundDrawable(login.this.getDrawable(R.drawable.custom_dialog_bg));
                    pd.show();
                    email=getString(R.string.guesttxt);

                    getUser.get();

                }else
                {
                    Snackbar snackbar= Snackbar.make(findViewById(R.id.mainLognin), "Please agree to the terms to proceed.", Snackbar.LENGTH_SHORT)
                            .setTextColor(ContextCompat.getColor(login.this, R.color.white))
                            .setBackgroundTint(ContextCompat.getColor(login.this, R.color.red));
                    snackbar.show();
                }

            }
        });

        Log.i(this.toString(),"user id  "+getUserID(this));


        String agreeTxt = "By clicking on it you agree to the Privacy Policy and Terms of Use.";
        SpannableString ss = new SpannableString(agreeTxt);

        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                openUrl(getString(R.string.privacy_policy_url));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.colorAccent));
                ds.setUnderlineText(false);
            }
        };
        ClickableSpan clickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                openUrl(getString(R.string.terms_of_use_url));
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.colorAccent));
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan1, 35,49, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        ss.setSpan(clickableSpan2, 54,66, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView agreeText=findViewById(R.id.agreeText);
        agreeText.setText(ss);
        agreeText.setMovementMethod(LinkMovementMethod.getInstance());
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        receiver=new NetworkChangeReceiver();
        registerReceiver(receiver,filter);
    }

    String email;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {

            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            // email.setText(accountName);

            email=accountName;
            signUpOrLogin(accountName);
            //Toast.makeText(this, ""+accountName, Toast.LENGTH_SHORT).show();

        }
    }

    ProgressDialog pd;
    private void signUpOrLogin(String accountName) {

        pd=new ProgressDialog(this);
        pd.setMessage("Fetching Data..");
        pd.setCancelable(false);
        pd.getWindow().setBackgroundDrawable(login.this.getDrawable(R.drawable.custom_dialog_bg));
        pd.show();
        String url=getString(R.string.singUpOrLoginUrl)+"email="+accountName+"&"+getString(R.string.userIdtxt)+"="+getUserID(this);
        Log.i("login","url "+url);
        ApiCall signUp=new ApiCall(this,url,this);
        signUp.get();
//        HttpRequest request=new HttpRequest(this);
//        request.Start(url);


    }


    @Override
    public void response(String r) {
        pd.dismiss();
        String response=r.trim();
        Log.i(this.toString(),"response login "+response);
        if (response.equals("400") || response.equals("403"))
        {
            logOutFromOthersAndLogIn();
        }
        else if (response.length()>20)
        {

            sp.edit().putString("email",email).apply();
            if(email.contains("@"))
            {

                setData(response,this);
                Toast.makeText(this, "Successful", Toast.LENGTH_LONG).show();

            }
            if (getIntent().getIntExtra("activity",0)==0)
            {

                startActivityWithAnimation(new Intent(this,MainActivity.class));
                finish();
            }else
            {
                super.onBackPressed();
            }



        }

    }

    private void logOutFromOthersAndLogIn()
    {
        pd=new ProgressDialog(this);
        pd.setMessage("Updating Data..");
        pd.setCancelable(false);
        pd.getWindow().setBackgroundDrawable(login.this.getDrawable(R.drawable.custom_dialog_bg));
        pd.show();
        String url=getString(R.string.logOutFromOthersAndLoginURL)+"email="+email+"&"+getString(R.string.userIdtxt)+"="+getUserID(this);
        ApiCall signUp=new ApiCall(this,url,this);
        signUp.get();
    }


    @SuppressLint("HardwareIds")
    public static String getUserID(Context c)
    {

        String id= Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (Objects.isNull(id))
        {
            id="1";
        }

        return Build.MANUFACTURER  + Build.MODEL  + Build.PRODUCT
                +Build.DEVICE
                +Build.ID
                +Build.DISPLAY
                +Build.BOARD
                +id;

    }

    @Override
    public void onBackPressed() {

        if (getIntent().getIntExtra("activity",4)==100  ||getIntent().getIntExtra("activity",4)==0  ) // means logged out
        {
            CloseDialog closeDialog=new CloseDialog();
            closeDialog.show(this);
        }else {
            super.onBackPressed();

        }

    }
    private void openUrl(String s) {
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setData( Uri.parse(s));
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    public void startActivityWithAnimation(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isOnline(this) )
        {
            startActivityWithAnimation(new Intent(this,NoInternetActivity.class));

        }
    }

    BroadcastReceiver receiver;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);

        }catch (Exception e)
        {

            Log.e("e",e.getMessage());
        }
    }
}