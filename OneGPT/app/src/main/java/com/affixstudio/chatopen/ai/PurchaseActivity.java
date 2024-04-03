package com.affixstudio.chatopen.ai;

import static com.affixstudio.chatopen.GetData.NetworkChangeReceiver.isOnline;
import static com.affixstudio.chatopen.ai.MainActivity.isPlanStockOut;
import static com.affixstudio.chatopen.ai.StartActivity.isMember;
import static com.affixstudio.chatopen.ai.StartActivity.modelLists;
import static com.affixstudio.chatopen.ai.StartActivity.offerings;
import static com.affixstudio.chatopen.ai.StartActivity.planName;
import static com.affixstudio.chatopen.ai.StartActivity.sp;
import static com.affixstudio.chatopen.ai.login.getUserID;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.affixstudio.chatopen.GetData.ApiResponse;
import com.affixstudio.chatopen.GetData.HttpRequest;
import com.affixstudio.chatopen.model.CloseDialog;
import com.affixstudio.chatopen.model.ModelList;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.LogLevel;
import com.revenuecat.purchases.Offerings;
import com.revenuecat.purchases.Package;
import com.revenuecat.purchases.PurchaseParams;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesConfiguration;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.interfaces.PurchaseCallback;
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback;
import com.revenuecat.purchases.models.StoreTransaction;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PurchaseActivity extends AppCompatActivity  {

    LinearLayout pricingLayout;


    public static String currentPlanName="";

    ArrayList<String> periods=new ArrayList<>();
    private int purchaseType=0;
    TextView price;
    TextView subPeriods;
    TextView title;
    LinearLayout haveSubLayout;
    ProgressBar loadingPlans;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        periods.add("Weekly");
        periods.add("Monthly");
        periods.add("Weekly");
        periods.add("Monthly");


        price = findViewById(R.id.subPrice);
        subPeriods = findViewById(R.id.subTitle);
        title = findViewById(R.id.planCategory);
        haveSubLayout = findViewById(R.id.haveSub);

        loadingPlans = findViewById(R.id.loadingPlans);


        pricingLayout = findViewById(R.id.pricingLayout);

        findViewById(R.id.privacyUrl).setOnClickListener(view -> {

            openLink(getString(R.string.privacy_policy_url));
        });
        findViewById(R.id.terms).setOnClickListener(view -> {

            openLink(getString(R.string.terms_of_use_url));
        });


        setBenefits();


        purchaseProcessing=new ProgressDialog(PurchaseActivity.this);
        purchaseProcessing.setMessage("Processing..");
        purchaseProcessing.setCancelable(false);
        purchaseProcessing.getWindow().setBackgroundDrawable(PurchaseActivity.this.getDrawable(R.drawable.custom_dialog_bg));


        if (isMember!=1)
        {
            //   sp.edit().putString("pToken",purchaseToken).putString("productID",productID).apply();
            if (!sp.getString("pToken","").isEmpty())
            {
                purchaseProcessing.show();
                checkPurchase(sp.getString("productID",""),sp.getString("pToken",""));
            }
        }



        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });



    }

    private void setBenefits() {

        TextView freeModelNames=findViewById(R.id.freeModelNames);
        TextView premiumModelNames=findViewById(R.id.premiumModelNames);

        StringBuilder freeModel=new StringBuilder();
        StringBuilder premiumModel=new StringBuilder();

        for (ModelList model:modelLists)
        {
            if (model.getIsPremium()==0) // not premium
            {
                freeModel.append(model.getModel_name()).append("\n");
            }else {
                premiumModel.append(model.getModel_name()).append("\n");
            }
        }

        freeModelNames.setText(freeModel.toString());
        premiumModelNames.setText(premiumModel.toString());





    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }



    private void checkPurchase(String productID,String purchaseToken) {
        sp=getSharedPreferences(getPackageName(),MODE_PRIVATE);

        sp.edit().putString("pToken",purchaseToken).putString("productID",productID).apply();
        HttpRequest getData=new HttpRequest(new ApiResponse() {
            @Override
            public void response(String response) {
                i("response   "+response);
                purchaseProcessing.dismiss();

                if (response.equals("101")) // invalid
                {
                    sp.edit().putString("pToken","").putString("productID","").apply();
                    Snackbar snackbar=Snackbar.make(findViewById(R.id.main),"Purchase is rejected by Play Store", BaseTransientBottomBar.LENGTH_INDEFINITE);
                    snackbar.setAction("Understand", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                }
                            }).setTextColor(ContextCompat.getColor(PurchaseActivity.this,R.color.white))
                            .setBackgroundTint(ContextCompat.getColor(PurchaseActivity.this, R.color.red))
                            .show();



                }else if (response.equals("200"))
                {
                    sp.edit().putString("pToken","").putString("productID","").apply();
                    new AlertDialog.Builder(PurchaseActivity.this).setMessage("Restart the app for best effect.").setTitle("Successful")
                            .setCancelable(false)
                            .setIcon(R.drawable.premium_icon)
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finishAffinity();
                                    System.exit(1);
                                }
                            }).show();
                    CloseDialog closeDialog=new CloseDialog();
                    closeDialog.restartShow(PurchaseActivity.this);
                }
                else
                {
                    Snackbar snackbar=Snackbar.make(findViewById(R.id.main),"There is an error. Please check some times later or contact our support team.", BaseTransientBottomBar.LENGTH_INDEFINITE);
                    snackbar.setAction("Understand", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                }
                            }).setTextColor(ContextCompat.getColor(PurchaseActivity.this,R.color.white))
                            .setBackgroundTint(ContextCompat.getColor(PurchaseActivity.this, R.color.red))
                            .show();
                }

            }
        });
        getData.Start("check purchase endpoint"+productID+"&pToken="+purchaseToken+"&userID="+getUserID(PurchaseActivity.this)+"&email="+sp.getString("email","email"));
        // getData.StartPost("https://whatsgpt.affixstudio.co.in/php/chatAi/ChatAiCheckSub.php","pid="+productID+"&pToken="+purchaseToken+"&userID="+getUserID(PurchaseActivity.this)+"&email="+sp.getString("email","email"));

    }



    ProgressDialog purchaseProcessing;

    private void showInRecycle(List<Package> packages) {

        if (!packages.isEmpty())
        {
            loadingPlans.setVisibility(View.GONE);
            showPlan(packages);
        }


    }

    void showPlan(List<Package> products)
    {

        int[] priceTxt={R.id.pWprice,R.id.pMprice,R.id.pLprice};
        int[] priceTxtPerDay={R.id.pwkpd,R.id.pmonpd,R.id.pLifepd};

        int[] layout={R.id.pWeek,R.id.pMonth,R.id.pLife};
        int[] devied={7,30};



        for (int i=0;2>i;i++)
        {
            TextView price=findViewById(priceTxt[i]);
            TextView pricePerDay=findViewById(priceTxtPerDay[i]);
            LinearLayout planLayout=findViewById(layout[i]);

            planLayout.setVisibility(View.VISIBLE);
            int finalI = i;
            DecimalFormat df=new DecimalFormat("0.00");
            long priceInt=  (products.get(i).getProduct().getPrice().getAmountMicros()/1000000);
            planLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    purchaseType=1;
                    //billingConnector.subscribe(PurchaseActivity.this,subIDs.get(finalI));
                    purchaseProcessing.show();
                    Purchases.getSharedInstance().purchase(
                            new PurchaseParams.Builder(PurchaseActivity.this,products.get(finalI).getProduct()).build(),
                            new PurchaseCallback() {
                                @Override
                                public void onCompleted(@NonNull StoreTransaction storeTransaction, @NonNull CustomerInfo customerInfo) {

                                    Log.i("Purchase","Original json : "+storeTransaction.getOriginalJson()+" || purchase info "+storeTransaction.getSubscriptionOptionId().replace("-","_")+" "+storeTransaction.getPurchaseToken());
                                    checkPurchase(storeTransaction.getSubscriptionOptionId().replace("-","_"),storeTransaction.getPurchaseToken());
                                }

                                @Override
                                public void onError(@NonNull PurchasesError purchasesError, boolean b)
                                {
                                    purchaseProcessing.dismiss();
                                    // No purchase
                                }
                            }
                    );


                }
            });
            price.setText(products.get(i).getProduct().getPrice().getCurrencyCode()+" "+df.format(priceInt));



            if (i<2)
            {
                Double p= (double) priceInt /devied[i];
                // int p= (int) Math.round ();
                pricePerDay.setText(df.format(p)+"/day");
            }


        }


    }




    void i(String s)
    {
        Log.i("Purchase",s);
    }

    void openLink(String s)
    {

        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(s)));

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!isOnline(this))
        {
            startActivity(new Intent(this,NoInternetActivity.class));

        }else {
            if (Objects.isNull(offerings))
            {
                getProducts();
            }else {
                showProducts();
            }
        }
    }


    void getProducts()
    {
        Purchases.setLogLevel(LogLevel.DEBUG);
        Purchases.configure(new PurchasesConfiguration.Builder(this, getString(R.string.revanueCatSdkKey))
                .build());
        Purchases.getSharedInstance().getOfferings(new ReceiveOfferingsCallback() {

            @Override
            public void onReceived(@NonNull Offerings offers) {
                // Log.i("Purchase","offerings size "+offerings.toString());
                offerings=offers;
                showProducts();
            }

            @Override
            public void onError(@NonNull PurchasesError error) {
                /* Optional error handling */
            }
        });

    }

    private void showProducts() {
        if (isMember==1)
        {

            findViewById(R.id.alreadySubscribed).setVisibility(View.VISIBLE);
            findViewById(R.id.pricingLayout).setVisibility(View.GONE);

            for (Package p:offerings.getCurrent().getAvailablePackages())
            {
                i("p.getProduct().getId() "+p.getProduct().getId());
                if (p.getProduct().getId().contains(planName))
                {
                    i("id.productId "+p.getProduct().getId());


                    price.setText(p.getProduct().getPrice().getCurrencyCode()+" "+new DecimalFormat("0.00").format((p.getProduct().getPrice().getAmountMicros()/1000000)));

                    // String substring = id.title.substring(0, id.title.indexOf("("));
                    String substring = p.getProduct().getTitle();


                    if (Objects.isNull(p.getProduct().getPeriod()))
                    {
                        subPeriods.setText(" for life time");
                    }else {
                        subPeriods.setText("/"+p.getProduct().getPeriod().getUnit());
                    }

                    title.setText("Active");


                    currentPlanName= substring;
                    break;
                }

            }
        }
        else
        {
            if (isPlanStockOut == 0)
            {
                findViewById(R.id.noPlans).setVisibility(View.GONE);
                findViewById(R.id.pricingLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.alreadySubscribed).setVisibility(View.GONE);

                showInRecycle(offerings.getCurrent().getAvailablePackages());

            }else
            {

                findViewById(R.id.noPlans).setVisibility(View.VISIBLE);
                findViewById(R.id.planVa).setVisibility(View.GONE);
                findViewById(R.id.pricingLayout).setVisibility(View.GONE);
                findViewById(R.id.alreadySubscribed).setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
    public void startActivity(Intent intent, boolean isForward) {
        super.startActivity(intent);
        if (isForward) {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }


}