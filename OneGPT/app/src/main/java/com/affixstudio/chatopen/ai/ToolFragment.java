package com.affixstudio.chatopen.ai;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static androidx.core.app.ActivityCompat.recreate;

import static com.affixstudio.chatopen.ai.StartActivity.isMember;
import static com.affixstudio.chatopen.ai.StartActivity.mSocket;
import static com.affixstudio.chatopen.ai.StartActivity.opened;
import static com.affixstudio.chatopen.ai.StartActivity.shouldOpenMainActivity;
import static com.affixstudio.chatopen.ai.StartActivity.sp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.affixstudio.chatopen.model.CloseDialog;

import java.util.Locale;
import java.util.Objects;


public class ToolFragment extends Fragment {

    private static final int REQUEST_OVERLAY_PERMISSION = 121;
    private int FLOATING_WINDOW_ID=12;

    SwitchCompat floatingWindow;
    Context c;
    View v;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        c=getContext();



        v=inflater.inflate(R.layout.fragment_tool, container, false);
        Activity a=getActivity();
        v.findViewById(R.id.darkMode).setOnClickListener(view -> {
            boolean currentState = sp.getBoolean("darkMode", false);

            new AlertDialog.Builder(c).setTitle("Restart Required")
                    .setMessage("To apply the theme, the app needs to be restarted.")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            opened=0;
                            sp.edit().putBoolean("darkMode", !currentState).apply();

                            if (currentState) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            } else {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            }

                            v.post(new Runnable() {
                                @Override
                                public void run() {
                                    a.finishAffinity();
                                    System.exit(1);
                                }
                            });
                        }
                    }).show();


        });


        sp=c.getSharedPreferences(c.getPackageName(),MODE_PRIVATE);




        TextView login=v.findViewById(R.id.login);

        login.setOnClickListener(view -> {
            if (sp.getString("email","email").contains("@"))
            {
                sp.edit().putString("email","email").apply();
                Intent intent = new Intent(c, login.class).putExtra("activity", 100);
                startActivity(intent);
                ((AppCompatActivity) c).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }else
            {
                Intent intent = new Intent(c, login.class).putExtra("activity", 1);
                startActivity(intent);
                ((AppCompatActivity) c).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        v.findViewById(R.id.privacy).setOnClickListener(view -> {

            openLink(getString(R.string.privacy_policy_url));
        });
        v.findViewById(R.id.terms).setOnClickListener(view -> {

            openLink(getString(R.string.terms_of_use_url));
        });

        v.findViewById(R.id.contactUs).setOnClickListener(view -> {
            CloseDialog c=new CloseDialog();
            c.contactUs(getActivity());
        });

        v.findViewById(R.id.rate).setOnClickListener(view -> {
            openLink(getString(R.string.playstoreLink));
        });
        v.findViewById(R.id.share).setOnClickListener(view -> {
            Intent intent=new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Share using");
            intent.putExtra( Intent.EXTRA_TEXT,getString(R.string.app_name)+" "+getString(R.string.shareAppText)+"\n"+getString(R.string.playstoreLink));
            startActivity(intent);
        });

        v.findViewById(R.id.moreApps).setOnClickListener(view -> {
            openLink(getString(R.string.moreAppURL));
        });

        v.findViewById(R.id.openSubscription).setOnClickListener(view -> {
            if (sp.getString("email","email").contains("@")) {
                Intent intent = new Intent(c, PurchaseActivity.class);
                startActivity(intent);
                ((AppCompatActivity) c).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                Intent intent = new Intent(c, login.class).putExtra("activity", 1);
                startActivity(intent);
                ((AppCompatActivity) c).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });


        v.findViewById(R.id.whatsappGpt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://wa.me/91"+getString(R.string.gptNumbar))));
            }
        });

        floatingWindow=v.findViewById(R.id.floatingWindow);
        Dialog permissionDialog=new Dialog(c);

        View vi=getLayoutInflater().inflate(R.layout.overlay_permission_layout,null);
        vi.findViewById(R.id.goForOverLayPermission).setOnClickListener(view1 -> {

            permissionDialog.dismiss();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + c.getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);

        });

        permissionDialog.getWindow().setBackgroundDrawable(c.getDrawable(R.drawable.custom_dialog_bg));
        permissionDialog.setContentView(vi);


        floatingWindow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(isMember==0)
                {
                    floatingWindow.setChecked(false);
                    sp.edit().putBoolean(getString(R.string.isFloatingOn),false).apply();
                    Toast.makeText(c, "Please buy a plan to use this feature.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (b)
                {
                    if (!Settings.canDrawOverlays(c)) {
                        // If the user has not granted the overlay permission, request it from the user

                        permissionDialog.show();
                        floatingWindow.setChecked(false);
                    } else
                    {

                        // If the user has already granted the overlay permission, create the floating window
                        // startService(new Intent(SettingActivity.this, FloatingWindowService.class));
                        showFloatingNotification();
                    }


                }else
                {

                    NotificationManager manager=(NotificationManager) c.getSystemService(NOTIFICATION_SERVICE);
                    manager.cancel(FLOATING_WINDOW_ID);

                    if(Settings.canDrawOverlays(c)){
                        sp.edit().putBoolean(getString(R.string.isFloatingOn),false).apply();
                    }

                }
            }
        });
        v.findViewById(R.id.menuIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseDialog drawer=new CloseDialog();
                drawer.openDrawer(getActivity());
            }
        });

        if (sp.getBoolean(getString(R.string.isFloatingOn),false))
        {
            if (Settings.canDrawOverlays(c))
            {
                floatingWindow.setChecked(true);
            }

        }


        return v;
    }

    void openLink(String s) {
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setData( Uri.parse(s));
        startActivity(intent);
        ((AppCompatActivity) c).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }



    void showFloatingNotification()
    {

        sp.edit().putBoolean(getString(R.string.isFloatingOn),true).apply();
        Intent intent=new Intent("com.affixstudio.chatopen.START_FLOATING_SERVICE");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent=PendingIntent.getService(c,6,
                intent,0|FLAG_IMMUTABLE);// todo changed
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {


            NotificationChannel channel = new NotificationChannel(getString(R.string.app_name), "Call Blocked Notification",
                    NotificationManager.IMPORTANCE_MIN);

            NotificationManager manager= (NotificationManager) c.getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);


        }



        Uri sound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); // setting notification sound

        NotificationCompat.Builder Nbuilder=new NotificationCompat.Builder(c,
                getString(R.string.app_name));


        Nbuilder.setContentIntent(pendingIntent);
        Nbuilder.setAutoCancel(false);
        Nbuilder.setSmallIcon(R.drawable.app_logo_transparent);
        Nbuilder.setContentTitle("Floating Chat Window");
        Nbuilder.setContentText("Tap to open floating chat window");
        Nbuilder.setSound(sound);
        Nbuilder.setOngoing(true);
        Nbuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        Nbuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);


        NotificationManager manager= (NotificationManager) c.getSystemService(NOTIFICATION_SERVICE);

        manager.notify(FLOATING_WINDOW_ID,Nbuilder.build());

    }

    @Override
    public void onResume() {
        super.onResume();
        TextView email = v.findViewById(R.id.showMain);
        String emailText=sp.getString("email","guest");

        TextView login=v.findViewById(R.id.login);
        if (emailText.contains("@"))
        {
            email.setText(emailText);
            login.setText("Logout");
        }else
        {
            email.setText(emailText.toUpperCase());
            login.setText("Login");
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if (requestCode == REQUEST_OVERLAY_PERMISSION)
        {
            if (Settings.canDrawOverlays(c)) {
                // If the user has granted the overlay permission, create the floating window
                floatingWindow.setChecked(true);
                //startService(new Intent(SettingActivity.this, FloatingWindowService.class));
            } else {
                // If the user has not granted the overlay permission, show an error message
                Toast.makeText(c, "Overlay permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }


    }
}