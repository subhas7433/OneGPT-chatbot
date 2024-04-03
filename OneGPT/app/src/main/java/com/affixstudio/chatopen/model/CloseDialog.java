package com.affixstudio.chatopen.model;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.affixstudio.chatopen.ai.R;


public class CloseDialog {

    public void show(Activity a)
    {
        Dialog dialog=new Dialog(a);
        View v=a.getLayoutInflater().inflate(R.layout.close_dialog,null) ;

        dialog.getWindow().setBackgroundDrawable(a.getDrawable(R.drawable.custom_dialog_bg));

//        String bidderToken = BidderTokenProvider.getBidderToken(context);




        Button no=v.findViewById(R.id.no),
                yes=v.findViewById(R.id.yes);
        no.setOnClickListener(view -> {

            dialog.dismiss();

        });
        yes.setOnClickListener(view -> {

            a.finishAffinity();
            System.exit(1);

        });
        dialog.setContentView(v);
        dialog.show();
    }
    public void restartShow(Activity a)
    {
        Dialog dialog=new Dialog(a);
        View v=a.getLayoutInflater().inflate(R.layout.purchase_succesfull_dialog,null) ;
        dialog.setCancelable(false);

        dialog.getWindow().setBackgroundDrawable(a.getDrawable(R.drawable.custom_dialog_bg));

        Button yes=v.findViewById(R.id.restart);

        yes.setOnClickListener(view -> {

            a.finishAffinity();
            System.exit(1);

        });
        dialog.setContentView(v);
        dialog.show();
    }
    public void contactUs(Activity a)
    {
        Dialog dialog=new Dialog(a);
        View v=a.getLayoutInflater().inflate(R.layout.contact_us_dialog,null) ;


        dialog.getWindow().setBackgroundDrawable(a.getDrawable(R.drawable.custom_dialog_bg));

        Button whatsapp=v.findViewById(R.id.whatsapp);
        Button email=v.findViewById(R.id.email);
        TextView faq=v.findViewById(R.id.faqs);

        whatsapp.setOnClickListener(view -> {

            String stringUri="https://wa.me/91"+a.getString(R.string.whatsappNumber) ;
            a.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(stringUri))/*.setPackage("com.whatsapp")*/);

        });
        email.setOnClickListener(view -> {

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:"+a.getString(R.string.email)));
            a.startActivity(emailIntent);

        });
        faq.setOnClickListener(view -> {
            String faqUri=a.getString(R.string.faq_url) ;
            a.startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(faqUri)));
        });

        dialog.setContentView(v);
        dialog.show();
    }

    public Dialog showLoading(Activity a)
    {
        Dialog dialog=new Dialog(a);
        View v=a.getLayoutInflater().inflate(R.layout.generating_img_dialog,null) ;


        dialog.getWindow().setBackgroundDrawable(a.getDrawable(R.drawable.custom_dialog_bg));

        dialog.setCancelable(false);
        dialog.setContentView(v);
        dialog.show();
        return dialog;
    }

    public  void openDrawer(Activity a)
    {

        Intent local=new Intent();

        local.setAction(a.getString(R.string.openDrawerAction));
        a.sendBroadcast(local);

    }
}
