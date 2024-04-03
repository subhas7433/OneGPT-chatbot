package com.affixstudio.chatopen.ai;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.affixstudio.chatopen.GetData.NetworkChangeReceiver.isOnline;
import static com.affixstudio.chatopen.ai.StartActivity.expertAIData;
import static com.affixstudio.chatopen.ai.StartActivity.isMember;
import static com.affixstudio.chatopen.ai.StartActivity.mSocket;
import static com.affixstudio.chatopen.ai.StartActivity.modelLists;
import static com.affixstudio.chatopen.ai.StartActivity.planName;
import static com.affixstudio.chatopen.ai.StartActivity.pluginDataLists;
import static com.affixstudio.chatopen.ai.StartActivity.totalCredit;
import static com.affixstudio.chatopen.ai.login.getUserID;
import static com.affixstudio.chatopen.model.ChatMessage.getCurrentTime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ReplacementSpan;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.affixstudio.chatopen.Dialog.AiModelDialog;
import com.affixstudio.chatopen.Dialog.GetModelData;
import com.affixstudio.chatopen.GetData.ApiResponse;
import com.affixstudio.chatopen.GetData.CosineSimilarity;
import com.affixstudio.chatopen.GetData.HttpRequest;
import com.affixstudio.chatopen.GetData.NetworkChangeReceiver;
import com.affixstudio.chatopen.GetData.PdfData;
import com.affixstudio.chatopen.GetData.PdfToText;
import com.affixstudio.chatopen.GetData.RapidApiCall;
import com.affixstudio.chatopen.GetData.SendPDFData;

import com.affixstudio.chatopen.model.ChatMessage;
import com.affixstudio.chatopen.model.CloseDialog;
import com.affixstudio.chatopen.model.Database;
import com.affixstudio.chatopen.model.ModelList;
import com.affixstudio.chatopen.model.PluginData;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.listeners.OnDismissListener;
import com.stfalcon.imageviewer.loader.ImageLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.socket.client.Ack;
import io.socket.emitter.Emitter;


public class ChatFragment extends Fragment implements ApiResponse,TextToSpeech.OnInitListener {
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    private static final int PERMISSIONS_REQUEST_STORAGE = 2;
    private static final int PERMISSIONS_REQUEST_CAMERA = 3;
    private static final int REQUEST_IMAGE_CAPTURE = 5;
    private final int REQUEST_IMAGE_PICK = 6;
    private final int PICK_PDF_REQUEST = 1001;
    private  final int REQUEST_IMAGE_PICK_TO_UPLOAD = 998;
    TextView showAIVersion,textLimit;



   // TextView credit;
    EditText messageBox;


    RecyclerView chatRecycle;






    SharedPreferences sp;
    TextToSpeech tts;
    public static final int databaseVersion=3;

    ArrayList<ChatMessage> chat=new ArrayList<>();

    Database db;
    boolean shouldItbeAnimate=false;
    private boolean notReceivedYet=false;
    public static int isPlanStockOut=0;
    View v;
    Context c;
    private boolean shouldScroll=true;
    private int currentConID=0;
    private int lastConID=0;


    ImageView sendQuestions;



    int currentAiExpertIndex=0;

    // CallGptJs gptJs;
    private String s="";

    private int modelID=1; // for now 12 means plugin is first
    String waitTextString="";
    private StringBuilder functionResponseBuilder=new StringBuilder();
    String functionName="";
    private JSONArray context=new JSONArray();
    private int waitTextIndex=0;// the index of wait message.helps to remove the it aquritly


    LinearLayout pluginsLA,docSendLayout;
    ImageView deleteDoc;


    public static int premiumCredit=0; // credit to use premium models like gpt 4 , claude 2
    private RecyclerView autoCompleteRecycle;
    private ArrayList<PdfData> pdfData=new ArrayList<>();

    Activity a;
    Database pdfSummaries;
    private String pdfSummary=""; // to send as system prompt and remove from message array to not send
    private String enabledPlugIns="2,3,"; // because plugins are not visible , make web access default
    private int pdfCredit=0;
    private int isTemMessage=0; // if server return 1 as temp parameter means it has send an error message

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)

    {


        v=inflater.inflate(R.layout.fragment_chat, container, false);


        // Inflate the layout for this fragment
        c=getContext();
        a=getActivity();
        sp=c.getSharedPreferences(c.getPackageName(),MODE_PRIVATE);

        //(_id INTEGER PRIMARY KEY autoincrement, PdfName text DEFAULT 'Not set',Summary text DEFAULT '1' )
        String PDFSummariesQuery="CREATE TABLE IF NOT EXISTS "+getString(R.string.PDFSummaries)+" "+getString(R.string.PDFSummariesQueryEndPart);
        pdfSummaries=new Database(c,getString(R.string.PDFSummaries),PDFSummariesQuery,1);








        mSocket.on("server_message", new Emitter.Listener() {
            @Override
            public void call(Object... args)
            {
                if (!shouldStopResponse) // should not receive any response, because user stopped the response
                {
                    return;
                }
                i(args.length+" is args size message received and  args[0] = "+args[0]);
                notReceivedYet=false;

                int tepm= (int) args[0]; // is the response a temporary reply? 1= true
                isTemMessage=tepm;

                if (tepm==1 ) // received a optional reply like getting data from web
                {

                 //   Toast.makeText(app, ""+args[1], Toast.LENGTH_SHORT).show();
                    i("temp message "+args[1]);
                }else if (tepm==2) // received an error so change to stop response sign
                {
                    shouldStopResponse=false;
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendQuestions.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.send_icon));
                            sendQuestions.setBackgroundColor(ContextCompat.getColor(c, R.color.colorPrimary));
                            i("temp error message "+args[1]);
                        }
                    });

                }


                String response;
                try {
                    response= (String) args[1];
                }catch (Exception e)
                {
                    int ar=(int) args[1];
                    response= ar+"";
                }

                i("response "+response+" || is temporary ="+tepm);

                try {
                        if (response.contains("[DONE]"))
                        {
                            Handler mHandler=new Handler(Looper.getMainLooper());
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run()
                                {


                                    sendQuestions.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.send_icon));
                                    sendQuestions.setBackgroundColor(ContextCompat.getColor(c, R.color.colorPrimary));

                                    if (chat.get(chat.size() - 1).getSentByUser()==0  && !chat.get(chat.size() - 1).getText().equals(getString(R.string.waitAsecond))) // do not save the last message if sent by user because already saved
                                    {
                                        i("currentConID "+currentConID+"| lastConID "+lastConID);
                                        i("full message : "+chat.get(chat.size() - 1).getText());
                                        i("enabledPlugIns while saving chat in db : "+enabledPlugIns);
                                        db.insertData(0,chat.get(chat.size() - 1).getText(),getCurrentTime(),currentConID,modelID,currentAiExpertIndex,enabledPlugIns);

                                        if (chat.get(chat.size() - 1).getIsTem()==0 && !Objects.isNull(toolLayoutInChat))
                                        {
                                            toolLayoutInChat.setVisibility(View.VISIBLE);
                                        }

                                        shouldStopResponse=false;
                                    }


                                }},1000);
                            return;
                        }
                    String finalResponse = response;
                    a.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!waitingdeleted)
                                {
                                    waitingdeleted=true;
                                    deleteWaitASecondMessage();

                                }

                                v.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        quickResponse(finalResponse,s);
                                    }
                                });

                            }
                        });

                    }
                catch (Exception e)
                {
                    e.printStackTrace();
                }


            }
        });
        mSocket.on("user_data", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                i("user_Data = "+args[0]);
                String r= args[0].toString();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // Code to run on the main thread
                        response(r);
                    }
                });


            }
        });
        mSocket.on("user_data_fetched", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                i("user_Data = "+args[0]);
                String r= args[0].toString();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        // Code to run on the main thread

                        if(!Objects.isNull(c))
                        {
                            response(r);
                        }

                    }
                });


            }
        });

        mSocket.on("get_pdfdata_relevent", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                JSONArray globalMessageWithContext= (JSONArray) args[0];
                String funName= (String) args[1];


                // convert the args to string array from json object
                String[] strEmbedding=args[2].toString().replace("[","").replace("]","").split(",");;

                // store the string array as double array
                double[] questionEmbedding = new double[strEmbedding.length];

                // Parse and convert each value from string to double
                for (int i = 0; i < strEmbedding.length; i++) {
                    questionEmbedding[i] = Double.parseDouble(strEmbedding[i].trim());
                }


                CosineSimilarity similarity=new CosineSimilarity();
                String resultPage=similarity.run(questionEmbedding,pdfData);

                JSONObject fun = new JSONObject();
                try {
                    fun.put("role", "function");
                    fun.put("name", funName);

                    fun.put("content", resultPage);

                    globalMessageWithContext.put(fun);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


                mSocket.emit("client_message",modelID,enabledPlugIns,pdfData.get(0).getPdfName(),globalMessageWithContext.toString(),sp.getString("email",""),getUserID(c));





            }
        });
        mSocket.on("get_pdfdata_by_page", new Emitter.Listener() {
            @Override
            public void call (Object... args)
            {
                JSONArray globalMessageWithContext= (JSONArray) args[0];
                String funName= (String) args[1];



                String str= args[2].toString();
                String[] strPageNumbers=null;
                StringBuilder pages=new StringBuilder();
                        if(str.contains(","))
                        {
                            strPageNumbers =args[2].toString().replace("[","").replace("]","").split(",");
                            for (String s:strPageNumbers)
                            {
                                int i=Integer.parseInt(s) - 1;
                                pages.append("Page Number ").append(i+1).append("\n\n").append(pdfData.get(i).getPdfText()).append("\n\n");

                            }
                        }else {
                            str=str.replace("[","").replace("]","");

                                int i=Integer.parseInt(str) -1;
                                pages.append("Page Number ").append(i+1).append("\n\n").append(pdfData.get(i).getPdfText()).append("\n\n");


                        }








                JSONObject fun = new JSONObject();
                try {
                    fun.put("role", "function");
                    fun.put("name", funName);

                    fun.put("content", pages);

                    globalMessageWithContext.put(fun);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


                mSocket.emit("client_message",modelID,enabledPlugIns,pdfData.get(0).getPdfName(),globalMessageWithContext.toString(),sp.getString("email",""),getUserID(c));




            }
        });


        mSocket.on("send_pdfdata_summery", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                int isTemporary= (int) args[0]; // 1= the server have sent an error
                String sum = (String) args[1];

                i("send_pdfdata_summery called with Summary = "+sum);


             try {
                    if (sum.contains("[DONE]"))
                    {
                        Handler mHandler=new Handler(Looper.getMainLooper());
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                int i=chat.size()-1;

                                ContentValues values=new ContentValues();
                                values.put("PdfName",pdfData.get(0).getPdfName());
                                values.put("Summary",chat.get(i).getText());

                                pdfSummaries.getWritableDatabase().insert(getString(R.string.PDFSummaries),null,values);
                                pdfSummary=chat.get(i).getText();

                            }},1000);
                        return;
                    }
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


                            v.post(new Runnable() {
                                @Override
                                public void run() {

                                    quickResponse(sum,s); // todo was dealing with pdf summary in stream mode and no balance response
                                }
                            });

                        }
                    });

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }




            }
        });
        mSocket.on("send_full_pdf_data", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                int shouldSendFullData= (int) args[0]; // 0= send full pdf data 1= send full data if summary is not available

                StringBuilder data=new StringBuilder();

                i("send_full_pdf_data called with value shouldSendFullData = "+shouldSendFullData);

                if (args.length>1)
                {
                    i("send_full_pdf_data called for = "+args[1]+" times");

                }


                if (shouldSendFullData==0)
                {

                    data=getFullPdfPages();


                }
                else
                {

                    //check for summary is it available
                   Cursor cursor= pdfSummaries.getWritableDatabase().rawQuery("Select * from "+getString(R.string.PDFSummaries)+" Where PdfName='"+pdfData.get(0).getPdfName()+"'",null);

                   if (cursor.getCount()>0) // summary is available
                   {
                       data.append("Summary :- \n").append(cursor.getString(2));
                   }else
                   {
                       // summary is not available

                       // send full data for summary
                       shouldSendFullData=0;
                        data=getFullPdfPages();
                   }



                }
                i("sending receive_pdf_data emited with value shouldSendFullData = "+shouldSendFullData+" data ="+data.toString());

                mSocket.emit("receive_pdf_data",shouldSendFullData,data.toString());

            }
        });




        TextView beta=v.findViewById(R.id.beta);

        sendQuestions=v.findViewById(R.id.sendQuestion);
        // ImageView setting=v.findViewById(R.id.setting);
        ImageView mic=v.findViewById(R.id.mic);

        messageBox=v.findViewById(R.id.messageBox);


      //  credit=v.findViewById(R.id.credit);
        showAIVersion=v.findViewById(R.id.showAIVersion);
        textLimit=v.findViewById(R.id.textLimit);


        pluginsLA = v.findViewById(R.id.pluginsLA);
        imageUploaded=v.findViewById(R.id.imageUploaded);
        deleteDoc=v.findViewById(R.id.deleteDoc);

        docSendLayout=v.findViewById(R.id.docSendLayout);
        imageUploading=v.findViewById(R.id.imageUploading);
        uploadingImageName=v.findViewById(R.id.showDocName);

        chatRecycle=v.findViewById(R.id.chatRecycle);
        autoCompleteRecycle=v.findViewById(R.id.autoCompleteRecycle);
        RecyclerView.ItemAnimator animator = chatRecycle.getItemAnimator();



        deleteDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shouldEnableSendButton(true);
                if (!currentImageUrl.isEmpty())
                {
                    HttpRequest request=new HttpRequest(new ApiResponse() {
                        @Override
                        public void response(String response) {

                        }
                    });
                    request.deleteUploadedImage(currentImageUrl.substring(currentImageUrl.lastIndexOf("/")+1));
                }

                docSendLayout.setVisibility(View.GONE);
            }
        });




        if (animator instanceof SimpleItemAnimator)
        {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        sp=c.getSharedPreferences(c.getPackageName(),MODE_PRIVATE);

        RecyclerView suggestionRecycle = v.findViewById(R.id.suggestionRecycle);
        suggestionRecycle.setLayoutManager(new LinearLayoutManager(c));
        MyRecyclerViewAdapter suggestionAdapter = new MyRecyclerViewAdapter(new ArrayList<>());
        suggestionRecycle.setAdapter(suggestionAdapter);

//        MyCustomAdapter adapter = new MyCustomAdapter(c, android.R.layout.simple_dropdown_item_1line);
//        messageBox.setAdapter(adapter);
        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int before, int count) {


                if (i==0)
                {
                    showLimit(i);
                } else
                {
                    showLimit(i+1);

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mSocket.on("receive_search_result", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                String result=(String) args[0];


                try {

                    // Parse the JSON data
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject asObject = jsonObject.getJSONObject("AS");
                    JSONArray resultsArray = asObject.getJSONArray("Results");
                    JSONObject resultsObject = resultsArray.getJSONObject(0);
                    JSONArray suggestsArray = resultsObject.getJSONArray("Suggests");

                    // Extract the suggestion sentences and add them to an adapter
                    List<String> suggestions = new ArrayList<>();
                    for (int i = 0; i < suggestsArray.length(); i++) {
                        JSONObject suggestObject = suggestsArray.getJSONObject(i);
                        String suggestion = suggestObject.getString("Txt");
                        suggestions.add(suggestion);
                    }
                    //adapter.updateData(suggestions);

                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //adapter.notifyDataSetChanged();

                            // Show suggestion list in a PopupWindow above the AutoCompleteTextView
                            if (suggestions.size() > 0) {


                                suggestionAdapter.suggestions = suggestions;
                                suggestionAdapter.notifyDataSetChanged();

                            }
                        }
                    });

                }catch (Exception e)
                {
                    e.printStackTrace();
                }


            }
        });



        v.findViewById(R.id.clearMsgBox).setOnClickListener(view -> {
            messageBox.setText("");
        });

        String query="CREATE TABLE IF NOT EXISTS "+sp.getString(getString(R.string.userIdtxt),"history")+ " "+getString(R.string.tableCreationSql);
        db=new Database(c,sp.getString(getString(R.string.userIdtxt),"history"),query,databaseVersion);



        setAllArrayData(1);

        sendQuestions.setOnClickListener( view -> {

            String message = messageBox.getText().toString();
            sendButtonOnClick(message);

        });


        mic.setOnClickListener(view -> {


            Intent intent
                    = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
            }
            catch (Exception e)
            {

                Toast.makeText(c, " " + e.getMessage(),
                                Toast.LENGTH_SHORT)
                        .show();
            }


        });














        tts = new TextToSpeech(c,this, "com.google.android.tts");








        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        receiver=new NetworkChangeReceiver();
        c.registerReceiver(receiver,filter);




        v.findViewById(R.id.aiModelSpinner).setOnClickListener(view -> {
            if (!(chat.size()>1))
            {
                AiModelDialog modelDialog=new AiModelDialog(a, new GetModelData() {
                    @Override
                    public void modelData(String modelName, int index)
                    {
                        modelID=modelLists.get(index).getId();

                        i( modelName+" ModelName on model change and model id = "+modelID);


                        showCredit();
                        textLimit.setText(messageBox.getText().toString().length()+"/"+modelLists.get(index).getInputLimit());
                        showAIVersion.setText(modelName);
                        createNewQuery();


                    }
                },modelID,true);// todo copy and paste
                BottomSheetDialog aiModelDialog=modelDialog.prepare();// todo copy and paste
                aiModelDialog.show();
            }

        });

        v.findViewById(R.id.history).setOnClickListener(view -> {
            setHistoryDialog();
        });

        v.findViewById(R.id.newQuery).setOnClickListener(view -> {
            createNewQuery();

        });

        v.findViewById(R.id.pickImage).setOnClickListener(view -> {
            showImgPickDialog();
        });





        pluginsLA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPluginDialog();
            }
        });


        showTopintroDialog();


        PdfToText pdfToText=new PdfToText(a);
        pdfData=pdfToText.getPdfData();

        i("pdfData size at starting "+pdfData.size());


        return v;
    }

    private boolean hasCredit() {
        return (isMember==1 || premiumCredit >0 || totalCredit>0 );
    }

    private void sendButtonOnClick(String message) {
        if (shouldStopResponse)
        {
            deleteLastWaitASecondMessage();
            sendQuestions.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.send_icon));
            sendQuestions.setBackgroundColor(ContextCompat.getColor(c, R.color.colorPrimary));
            shouldStopResponse=false;
            mSocket.emit("client_message_stop",1);
            return;
        }

        shouldStopResponse=true; // user have sent a query
        sendQuestions.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.stop_squre));
        sendQuestions.setBackgroundColor(ContextCompat.getColor(c, R.color.inactive_mid_black));

        hideKeyboard();

        shouldScroll = true;


        if (notReceivedYet) // still processing the old request

        {
            undoStopResponseVar();
            showSnackBar("One request at a time.", 1);
            return;
        }


        if (message.isEmpty()) {

            undoStopResponseVar();
            showSnackBar("Write something..", 1);

        } else if (message.length() > getModelById(modelID).getInputLimit()) {
            undoStopResponseVar();
            showSnackBar("You can only input " + getModelById(modelID).getInputLimit() + " characters for this model", 1);
        } else if (getModelById(modelID).getIsPremium()==1 && premiumCredit < 1)
        {

            showCreditDialog();
        }
        else if ( getModelById(modelID).getIsPremium()==0)
        {

            if (totalCredit > 0 || isMember==1)
            {


                send(message);


            }else if (isMember==0 && premiumCredit>0 )
            {

                // does user permitted to use premium credit for free models
                if (sp.getBoolean(getString(R.string.shouldUsePremiumCredit),false))
                {
                    send(message);
                }else {
                    new AlertDialog.Builder(c)
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // don't ask for next time if continuing same chat
                                    sp.edit().putBoolean(getString(R.string.shouldUsePremiumCredit),true).apply();
                                    send(message);
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    // undo all parameters
                                    undoStopResponseVar();
                                }
                            }).setMessage("You don't have normal credit but have "+premiumCredit+" premium credit. Do you want to use them for this query?")
                            .setTitle("Use Premium Credit")
                            .show();
                }

            }else {
                showCreditDialog();
            }


        }
        else {
            send(message);


        }//*/

    }
    private void undoStopResponseVar() {
        shouldStopResponse=false;
        sendQuestions.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.send_icon));
        sendQuestions.setBackgroundColor(ContextCompat.getColor(c, R.color.colorPrimary));
        shouldScroll = false;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void deleteLastWaitASecondMessage() {
        if (chat.size()>0)
        {
            int i=chat.size()-1;
            if (chat.get(i).getIsTem()==1 && chat.get(i).getSentByUser()==0)
            {
                chat.remove(i);
            }
            chatAdapter.notifyDataSetChanged();
        }
    }

    private StringBuilder getFullPdfPages() {
        StringBuilder data=new StringBuilder();

        int i=0;
        for (PdfData pdf:pdfData)
        {
            i++;
            if (i>5)
            {
                break;
            }
            if (i<3)
            {
                continue;
            }
            data.append("Page Number ").append(pdf.getPageNo()).append("\n\n").append(pdf.getPdfText()).append("\n\n");

        }
        return data;
    }

    RecyclerView.Adapter plugInAdapter;
    private void showPluginDialog() {

        BottomSheetDialog pluginDialog=new BottomSheetDialog(c,R.style.CustomBottomSheetDialogTheme);
        View v=getLayoutInflater().inflate(R.layout.plugin_dialog_layout,null);
        RecyclerView plugInRecycle=v.findViewById(R.id.pluginRecycle);
        plugInAdapter=plugInAdapter();
        plugInRecycle.setAdapter(plugInAdapter);
        pluginDialog.setContentView(v);
        pluginDialog.show();



    }


    private RecyclerView.Adapter plugInAdapter()
    {
        return new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                return new RecyclerView.ViewHolder(View.inflate(parent.getContext(),R.layout.plugins_recycle,null))
                {
                    @Override
                    public String toString()
                    {
                        return super.toString();
                    }
                };
            }


            ArrayList<LinearLayout> plugLAList=new ArrayList<>(); // to change color when pdf is on or off
            ArrayList<SwitchCompat> plugSwitchList=new ArrayList<>(); // to change turn off/on when pdf is on or off




            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {

                int i = h.getAdapterPosition();
                PluginData plugin = pluginDataLists.get(i);

                SwitchCompat pluginTurnOn = h.itemView.findViewById(R.id.pluginTurnOn);
                TextView name = h.itemView.findViewById(R.id.pluginName);
                TextView plugInDes = h.itemView.findViewById(R.id.plugInDes);
                ImageView pluginImage = h.itemView.findViewById(R.id.pluginImage);

                LinearLayout pluginsLayout = h.itemView.findViewById(R.id.pluginsLayout);

                name.setText(plugin.getName());
                plugInDes.setText(plugin.getDes());
                Picasso.get().load(plugin.getIcon()).into(pluginImage);


                String s = sp.getString(h.itemView.getContext().getString(R.string.pluginIndex), "");
                String otherEnabledPlugins = sp.getString(h.itemView.getContext().getString(R.string.otherEnabledPlugins), "");

                // Check if PDF plugin is turned on
                boolean isPdfTurnedOn = s.contains("1,");

                // Set the switch state based on plugin status
                if (isPdfTurnedOn) {
                    // If PDF is turned on, disable other plugins
                    pluginTurnOn.setChecked(plugin.getId() == 1);
                    pluginTurnOn.setEnabled(plugin.getId() == 1);
                    pluginsLayout.setBackgroundColor(ContextCompat.getColor(h.itemView.getContext(), R.color.pluginDisable));
                } else {
                    // If PDF is turned off, show the status of other plugins
                    boolean isPluginEnabled = otherEnabledPlugins.contains(plugin.getId() + ",");
                    pluginTurnOn.setChecked(isPluginEnabled);
                    pluginTurnOn.setEnabled(true); // Enable the switch for all other plugins
                    pluginsLayout.setBackgroundColor(isPluginEnabled ? ContextCompat.getColor(h.itemView.getContext(), R.color.pluginEnable) : ContextCompat.getColor(h.itemView.getContext(), R.color.pluginDisable));
                }

                pluginTurnOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        String s=sp.getString(getString(R.string.pluginIndex),"");
                        if (s.contains("1,") && plugin.getId()!=1 && b) // if plugin is on then other should be turn off
                        {
                            pluginTurnOn.setChecked(false);
                            return;
                        }
                        if (b && !s.contains(plugin.getId()+",") )
                        {


                            if (plugin.getId()==1) // if pdf is turned on
                            {

                                // add the only id of the pdf plugin into the list
                                sp.edit().putString(getString(R.string.pluginIndex),plugin.getId()+",").apply();
                                enabledPlugIns=plugin.getId()+",";


                                showEableByBackground(false); // change other's background color to show disable
                                turnSwichesOff(false); // turnOff other's switches

                            }
                            else
                            {

                                // add the id of the plugin into the list
                                sp.edit().putString(getString(R.string.pluginIndex),s+plugin.getId()+",").apply();
                                enabledPlugIns=enabledPlugIns+plugin.getId()+",";

                                String spin= sp.getString(getString(R.string.otherEnabledPlugins),"");

                                if (!spin.contains(plugin.getId()+","))// prevent duplicate entry
                                {
                                    sp.edit().putString(getString(R.string.otherEnabledPlugins),spin+plugin.getId()+",")
                                            .apply();
                                }


                            }

                        }
                        else if (!b && s.contains(plugin.getId()+","))
                        {
                            // remove the id of the plugin from the list
                            sp.edit().putString(getString(R.string.pluginIndex),s.replace(plugin.getId()+",","")).apply();
                            enabledPlugIns=enabledPlugIns.replace(plugin.getId()+",","");

                            if (plugin.getId()!=1) // when not pdf remove from otherEnabledPlugins to not turn it on when pdf turns off
                            {
                                sp.edit().putString(getString(R.string.otherEnabledPlugins),s.replace(plugin.getId()+",","")).apply();
                            }


                            showEableByBackground(true); // change other's background color to show disable
                            turnSwichesOff(true); // turnOff other's switches

                        }
                        plugInAdapter.notifyDataSetChanged();
                    }
                });


                if (i==0) // clear all the array on refresh
                {
                    plugLAList.clear();
                    plugSwitchList.clear();
                    tmp.clear();
                }


                if (!plugLAList.contains(pluginsLayout) && plugin.getId()!=1) // should not already added to list and not pdf
                {
                    plugLAList.add(pluginsLayout);
                }
                if (!plugSwitchList.contains(pluginTurnOn) && plugin.getId()!=1) // should not already added to list and not pdf
                {
                    plugSwitchList.add(pluginTurnOn);
                }
                if (!tmp.contains(plugin.getId()) && plugin.getId()!=1) // to indicate which id switch was on
                {
                    tmp.add(plugin.getId());
                }



            }
            void showEableByBackground(boolean shouldOn)
            {
                for (LinearLayout la:plugLAList)
                {

                    if (shouldOn)
                    {
                        la.setBackgroundColor(ContextCompat.getColor(c,R.color.pluginEnable));
                    }else {
                        la.setBackgroundColor(ContextCompat.getColor(c,R.color.pluginDisable));
                    }
                }



            }
            ArrayList<Integer> tmp=new ArrayList<>();
            void turnSwichesOff(boolean shouldOn)
            {


                i(tmp.size()+" is tmp size and plugSwitchList size is "+plugSwitchList.size()+"\notherEnabledPlugins="+sp.getString(getString(R.string.otherEnabledPlugins), ""));
                for (int i=0;i<plugSwitchList.size();i++)
                {

                    plugSwitchList.get(i).setChecked(shouldOn && sp.getString(getString(R.string.otherEnabledPlugins), "").contains(tmp.get(i) + ","));

                }


            }

            @Override
            public int getItemCount() {
                return pluginDataLists.size();
            }
        };







    }

    private void showAutoComplete() {
        String text=messageBox.getText().toString();
        if (text.length()<40)
        {
            RapidApiCall apiCall=new RapidApiCall();
            apiCall.autoSuggestion(text.replace(" ", "%20"), new ApiResponse() {
                @Override
                public void response(String response) {

                }
            },c);
            //autoCompleteRecycle.setAdapter();
        }else {
            autoCompleteRecycle.removeAllViews();
        }



    }

    private void changeTextLimit() {

        int limit=getModelById(modelID).getInputLimit();
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(limit); // Limit according to AI characters
        messageBox.setFilters(filters);;
        String s=messageBox.getText().toString();
        if (s.length()>limit)
        {
            messageBox.setText(s.substring(0,limit-1));
        }

        showLimit(messageBox.getText().length());
    }

    void showLimit(int textLength)
    {
        textLimit.setText(textLength+"/"+getModelById(modelID).getInputLimit());

    }

    private void createNewQuery()
    {

        currentImageUrl="";
        if (shouldStopResponse) // if was getting response, stop it then create new chat
        {
            deleteLastWaitASecondMessage();
            sendQuestions.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.send_icon));
            sendQuestions.setBackgroundColor(ContextCompat.getColor(c, R.color.colorPrimary));
            shouldStopResponse=false;
            mSocket.emit("client_message_stop",1);

        }
        v.findViewById(R.id.chooseModelDialogEnabled).setVisibility(View.VISIBLE);
        pdfData.clear();

        // ask user again in new chat if they want to use premium credit for free models
        sp.edit().putBoolean(getString(R.string.shouldUsePremiumCredit),false).apply();



        lastConID+=1;
        currentConID=lastConID;

        showChatHistory(new ArrayList<>());
        showAIVersion.setText( getModelById(modelID).getModel_name()); // todo copy and paste



        i("currentConID "+currentConID+"| lastConID "+lastConID+" model id= "+modelID);

    }

    private ModelList getModelById(int modelID) {
        for (ModelList model : modelLists) {
            if (model.getId() == modelID) {
                return model; // Found the matching object
            }
        }
       return modelLists.get(0);
    }

    boolean waitingdeleted=false;
    private void notifiDataChanged()
    {

        int i=0;
        for (ChatMessage c:chat)
        {
            if (c.getText().equals(getString(R.string.waitAsecond)))
            {
                waitTextIndex=i;
            }
            i++;
        }

        a.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatAdapter.notifyItemChanged(waitTextIndex);
            }
        });

    }



    private void callGpt(String context, int versionIndex) {
        functionResponseBuilder = new StringBuilder();
        functionName="";

    }




    boolean shouldStopResponse=false;



    ArrayList<String> autoQuestions=new ArrayList<>();
    HttpRequest httpRequestForAutoStop;



    private List<String> extractDoubleQuotedText(String response)
    {

        List<String> resultList = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"(.*?)\"");
        Matcher matcher = pattern.matcher(response);
        while (matcher.find()) {
            resultList.add(matcher.group(1));
        }
        return resultList;
    }
    private String removeNumbersAndPeriods(String text) {
        return text.replaceAll("[0-9]+\\.", "").trim();
    }
    BottomSheetDialog historyDialog;
    private void setHistoryDialog()
    {

        historyDialog=new BottomSheetDialog(c,R.style.CustomBottomSheetDialogTheme);
        View v=a.getLayoutInflater().inflate(R.layout.history_bottom_dialog,null);

        TextView noHistory=v.findViewById(R.id.noHistory);
        v.findViewById(R.id.newQuery).setOnClickListener(view -> {

            createNewQuery();
            historyDialog.dismiss();

        });
        setAllArrayData(0);
        RecyclerView chatRow=v.findViewById(R.id.chatRecycle);
        chatRow.setAdapter(HistoryAdapter());

        if (hList.size()<1)
        {

            noHistory.setVisibility(View.VISIBLE);
        }
        else
        {
            noHistory.setVisibility(View.GONE);
        }


        historyDialog.setContentView(v);
        historyDialog.show();

    }



    BottomSheetDialog  imgPickDialog;
    void showImgPickDialog()
    {
        imgPickDialog=new BottomSheetDialog(c,R.style.CustomBottomSheetDialogTheme);
        View v=a.getLayoutInflater().inflate(R.layout.pickup_document_bottom_dialog,null);


        v.findViewById(R.id.cameraLA).setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(c,android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(a,
                        new String[]{android.Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_CAMERA);
            } else {
                dispatchTakePictureIntent();
            }
        });

        v.findViewById(R.id.galaryLA).setOnClickListener(view -> {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pickImageFromGallery();
            }else if (ContextCompat.checkSelfPermission(c, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)

            {
                ActivityCompat.requestPermissions(a,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_STORAGE);
            } else {
                pickImageFromGallery();
            }
        });
        v.findViewById(R.id.pdf).setOnClickListener(view -> {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pickImageFromGallery();
            }else if (ContextCompat.checkSelfPermission(c, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)

            {
                ActivityCompat.requestPermissions(a,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_STORAGE);
            } else {
                pickPdf();
            }
        });

        if (modelID==12)
        {
            v.findViewById(R.id.upload).setVisibility(View.VISIBLE);
        }else {
            v.findViewById(R.id.upload).setVisibility(View.GONE);
        }
        v.findViewById(R.id.upload).setOnClickListener(view -> {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pickImageToUpload();
            }else if (ContextCompat.checkSelfPermission(c, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)

            {
                ActivityCompat.requestPermissions(a,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_STORAGE);
            } else {
                pickImageToUpload();
            }
        });





        imgPickDialog.setContentView(v);
        imgPickDialog.show();
    }

    void pickImageToUpload()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(intent, REQUEST_IMAGE_PICK_TO_UPLOAD);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(c, "No gallery app available", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri imageUri;
    private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(c.getPackageManager()) != null) {
            File imageFile = new File(c.getFilesDir(), "example_image.jpg");
            imageUri = FileProvider.getUriForFile(c, c.getString(R.string.authorityName), imageFile);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(c, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }
    void pickPdf()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf"); // Filter for PDF files
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Start the file picker activity
        startActivityForResult(Intent.createChooser(intent, "Select a PDF file"), PICK_PDF_REQUEST);
    }
    void showWebWarning()
    {
        BottomSheetDialog  webWarning=new BottomSheetDialog(c,R.style.CustomBottomSheetDialogTheme);
        View v=a.getLayoutInflater().inflate(R.layout.web_access_rule,null);

        webWarning.setCancelable(false);
        v.findViewById(R.id.goForOverLayPermission).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webWarning.dismiss();
            }
        });
        webWarning.setContentView(v);
        webWarning.show();

    }
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(c, "No gallery app available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                dispatchTakePictureIntent();
            }
            else
            {
                Toast.makeText(c, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(c, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }




    ArrayList<HistoryList> hList=new ArrayList<>();

    RecyclerView.Adapter HistoryAdapter()
    {
        Log.i("main","hlist size "+hList.size());

        return new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(View.inflate(parent.getContext(),R.layout.history_recycle,null)) {
                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {

                int i=(hList.size()-1)-h.getAdapterPosition();


                TextView date=h.itemView.findViewById(R.id.date);

                TextView title = h.itemView.findViewById(R.id.title);
                TextView version = h.itemView.findViewById(R.id.version);
                String topic = hList.get(i).topic;

                String pattern = "\\[I:(https?://[^\\]]+)\\]";

                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(topic);

                if (m.find()) {
                    String url = m.group(1);
                    String imageName = getImageNameFromURL(url);
                    topic.replace(url,"Asked about "+imageName);
                }else {
                    String urlRegex = "(https?://[\\w-./]+)";

                    Pattern pa = Pattern.compile(urlRegex);
                    Matcher matcher = pa.matcher(topic);

                    if (matcher.find()) {
                        topic = topic.replace(matcher.group(), "Asked about " + matcher.group());
                    }
                }



                // Capitalize the first character
                String capitalizedTopic = topic.substring(0, 1).toUpperCase() + topic.substring(1);

                title.setText(capitalizedTopic);


                //  date.setText(hList.get(i).date);
                setTimeAgo(date,hList.get(i).date);
                h.itemView.findViewById(R.id.historyRowLA).setOnClickListener( view -> {


                    setConverstation(hList.get(i).id);

                    showCredit();
                    historyDialog.dismiss();

                });


                version.setText(getModelById(hList.get(i).versionIndex).getModel_name());
                h.itemView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        h.itemView.findViewById(R.id.historyRowLA).setVisibility(View.GONE);
                        Toast.makeText(c, "Deleted", Toast.LENGTH_SHORT).show();

                        db.getReadableDatabase().execSQL("DELETE FROM history WHERE ConversationID="+hList.get(i).id);
                        if (currentConID==hList.get(i).id)
                        {
                            createNewQuery();
                        }
                    }
                });





            }

            @Override
            public int getItemCount() {
                return hList.size();
            }
        };
    }

    private void setConverstation(int id)
    {

        Cursor cursor=db.getInfo();
        ArrayList<ChatMessage> mChat=new ArrayList<>();

        while (cursor.moveToNext())
        {
            if (id==cursor.getInt(4)) // conversation id
            {
                ChatMessage cMsg=new ChatMessage(
                        //cursor.getString(1),(cursor.getInt(2)==1),cursor.getString(3),cursor.getInt(4)
                        cursor.getString(1),cursor.getInt(2),cursor.getString(3),cursor.getInt(4),cursor.getInt(6),cursor.getInt(7),cursor.getString(6),0
                );
                if (cursor.getString(6).length()==1)
                {
                    modelID=Integer.parseInt(cursor.getString(6));
                    changeTextLimit();
                }
               // enabledPlugIns=cursor.getString(6);
                mChat.add(cMsg);
                currentConID=id;

            }

        }

        showChatHistory(mChat);


        v.findViewById(R.id.chooseModelDialogEnabled).setVisibility(View.GONE);
        showAIVersion.setVisibility(View.VISIBLE);
        showAIVersion.setText( getModelById(modelID).getModel_name());
        cursor.close();


    }












    TextView creditS;
    private void showCreditDialog()
    {

        sendQuestions.setImageDrawable(ContextCompat.getDrawable(c,R.drawable.send_icon));
        sendQuestions.setBackgroundColor(ContextCompat.getColor(c, R.color.colorPrimary));
        shouldStopResponse=false;

        Dialog dialog=new Dialog(c);

        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(c,R.drawable.custom_dialog_bg));

        View v= getLayoutInflater().inflate(R.layout.gain_credit_dialog,null);

        creditS=v.findViewById(R.id.credits);
        Button addCredit=v.findViewById(R.id.addCredit);
        // Java

        MaterialTextView openPurchase=v.findViewById(R.id.buy);
        openPurchase.setOnClickListener(view -> {
            if (sp.getString("email","email").contains("@"))
            {
                startActivity(new Intent(c,PurchaseActivity.class));
            }else
            {
                startActivity(new Intent(c,login.class).putExtra("activity",1));
            }
        });

        if (isMember==1)
        {
            creditS.setText("Unlimited");

        }else {
            creditS.setText(""+totalCredit);
        }



        dialog.setContentView(v);

        dialog.show();

    }

    private void getData() {
        i("called on resume fetch_user_data");
        mSocket.emit("fetch_user_data",sp.getString("email","email"),getUserID(c));

    }











    ChatAdapter chatAdapter;

    void setAllArrayData(int indi)
    {



        String query="CREATE TABLE IF NOT EXISTS "+sp.getString(getString(R.string.userIdtxt),"history")+ " "+getString(R.string.tableCreationSql);

        Database  db1=new Database(c,sp.getString(getString(R.string.userIdtxt),"history"),query,databaseVersion);

        Cursor info=db1.getInfo();
        if (hList.size()>0)
        {
            hList.clear();
        }

        Log.i("main","count "+info.getColumnCount());
        ArrayList<ChatMessage> cMtmp=new ArrayList<>();

        ArrayList<Integer> id=new ArrayList<>();

        while (info.moveToNext())
        {

            ChatMessage cMsg=new ChatMessage(
                    info.getString(1),info.getInt(2),info.getString(3),info.getInt(4),info.getInt(6),info.getInt(7),
                    info.getString(5),0);
            cMtmp.add(cMsg);
            //chat.add(cMsg);


            if (!id.contains(cMsg.getConversationID())  && cMsg.getAiExpertIndex()==currentAiExpertIndex)
            {

                HistoryList historyList= new HistoryList(cMsg.getText(),cMsg.getConversationID(),cMsg.getTime(),cMsg.getModelID());
                hList.add(historyList); // creating history of conversation
                currentConID=cMsg.getConversationID(); // getting recent conversation
                id.add(cMsg.getConversationID());
            }
            if ( info.getInt(4)> lastConID)
            {
                lastConID=info.getInt(4);    //increase conversation id
            }


        }

        if (indi ==1)
        {
            showChatHistory(cMtmp);
        }

        db1.close();
        info.close();

    }


    private void showChatHistory (ArrayList<ChatMessage> cMtmp)
    {


        if (chat.size()>0)
        {
            chat.clear();
        }

        for (ChatMessage c:cMtmp)
        {
            if (c.getConversationID()==currentConID  && c.getAiExpertIndex()==currentAiExpertIndex)
            {
                chat.add(c);
                modelID=c.getModelID();
                changeTextLimit();
               // enabledPlugIns=c.getEnabledPlugins();
            }

        }

        i("enabledPlugIns on starting "+enabledPlugIns);

        if (chat.size()==0)
        {

            showAIVersion.setText( getModelById(modelID).getModel_name());
            chat.add(new ChatMessage(getModelById(modelID).getWelcomeMsg(),0,getCurrentTime(),currentConID,modelID,currentAiExpertIndex, enabledPlugIns,0));

        }else {
           // pluginsLA.setVisibility(View.GONE); // hide changing plugins when user started a chat
            v.findViewById(R.id.chooseModelDialogEnabled).setVisibility(View.GONE);


            showAIVersion.setVisibility(View.VISIBLE);
            showAIVersion.setText( getModelById(modelID).getModel_name());
        }
        chatAdapter=new ChatAdapter(chat);
        chatRecycle.setAdapter(chatAdapter);
        chatRecycle.scrollToPosition(chat.size() - 1);

        v.findViewById(R.id.scrollToBottom).setOnClickListener ( view -> {
            chatRecycle.smoothScrollToPosition(chat.size() - 1);
        } );

        chatRecycle.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                int totalItemCount = recyclerView.getAdapter().getItemCount();
                boolean isRecyclerViewScrollable = recyclerView.getLayoutManager().getChildCount() < totalItemCount;
                if (!isRecyclerViewScrollable)
                {
                    v.findViewById(R.id.scrollToBottom).setVisibility(View.GONE);
                    return;
                }
                if ( lastVisibleItemPosition == totalItemCount - 1 && lastVisibleItemPosition >= 0) {
                    // RecyclerView is at the end
                    // Do something here
                    v.findViewById(R.id.scrollToBottom).setVisibility(View.GONE);
                } else {
                    // RecyclerView is not at the end or not scrollable
                    // Do something else here
                    v.findViewById(R.id.scrollToBottom).setVisibility(View.VISIBLE);
                }
            }


        });
        chatRecycle.addOnItemTouchListener(new RecyclerView.OnItemTouchListener()
        {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e)
            {
                shouldScroll=false;

                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });


        v.findViewById(R.id.menuIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseDialog drawer=new CloseDialog();
                drawer.openDrawer(a);
                hideKeyboard();
            }
        });
        //info.close();

    }






    private void send (String s)
    {
        try {

            waitingdeleted=false; // helps to delete the waiting message

            // Make the choose model button gone to not let user change ai model after starting a conversation
            v.findViewById(R.id.chooseModelDialogEnabled).setVisibility(View.GONE);

            showAIVersion.setVisibility(View.VISIBLE);
            showAIVersion.setText( getModelById(modelID).getModel_name());





            JSONArray messagesArr = new JSONArray();

            int stop=0;


            if (!currentImageUrl.isEmpty())
            {
                s="Image Url = "+currentImageUrl+"\n\n"+s;
                currentImageUrl="";
                docSendLayout.setVisibility(View.GONE);
            }

            // putting the question at the end of array
            messagesArr.put(new JSONObject().put("role", "user").put("content", s));

            int numOfContext=getModelById(modelID).getContextSize(); // How many past questions should be taken


            if (autoQuestions.size()==0)
            {
                for (int i = chat.size() - 1; i >= 0; i--) // getting past response in json objects
                {



                    if (stop > numOfContext) {
                        break;
                    } else {
                        stop += 1;
                    }

                    // Create a new JSONObject for each message
                    JSONObject messageObj = new JSONObject();


                    if (!chat.get(i).getText().equals(getModelById(modelID).getWelcomeMsg()) && chat.get(i).getIsTem()==0) // skip the first message
                    {
                        if (chat.get(i).getSentByUser()>0 ) // user or functions and permanent
                        {
                            messageObj.put("role", "user");
                        }
                        else  // isTem=0 means real response. add only the real messages in the context, not the error one also
                        {

                            messageObj.put("role", "assistant");
                        }
                        messageObj.put("content", chat.get(i).getText());
                        messagesArr.put(messageObj);
                    }


                }
            }

            insertIntoChat(1,s);

            waitTextIndex=chat.size()-1; // helps to remove waiting text correctly
            waitTextString=getString(R.string.generateIng);
            insertIntoChat(0,getString(R.string.waitAsecond)); // optional reply



            messageBox.setText("");

            i("client_message versionIndex= "+ modelID+" email = "+sp.getString("email","")+" userId "+ getUserID(c));

            String pluginIndexes=enabledPlugIns;



            // code changes to remove the isMember condition , because they looks identical

            JSONArray context = new JSONArray();
            for (int i = messagesArr.length() - 1; i >= 0; i--)
            {
                context.put(messagesArr.get(i));
            }
            functionResponseBuilder = new StringBuilder();
            functionName="";
            callGpt(context.toString(),modelID);


            if (modelID==12) // if the plugin is on
            {


                String pdfName="";

                if (pluginIndexes.contains("1,") && pdfData.size()>0) // pdf is on
                {

                    pdfName=pdfData.get(0).getPdfName();

                    i(pdfName+" is pdf name ");
                }

                i(pdfName+" is pdf name, "+pluginIndexes+" is pdf index , client_message versionIndex= "+ modelID+" email = "+sp.getString("email","")+" userId "+ getUserID(c));


                mSocket.emit("client_message", modelID,pluginIndexes,pdfName ,context.toString(),sp.getString("email",""), getUserID(c));
            }
            else {

                i("plug is not on, "+pluginIndexes+" is pdf index , client_message versionIndex= "+ modelID+" email = "+sp.getString("email","")+" userId "+ getUserID(c));

                mSocket.emit("client_message", modelID,pluginIndexes, "",context.toString(),sp.getString("email",""), getUserID(c));
            }







        }
        catch (Exception e)
        {
            e.printStackTrace();

        }

    }
    String getImageNameFromURL(String url) {
        int lastSlash = url.lastIndexOf('/');
        int secondLastSlash = url.lastIndexOf('/', lastSlash - 1);

        return url.substring(secondLastSlash + 1).replace("/", "_");
    }
    private void quickResponse(String response,
                               String s) {
        if (processResponse(response))
        {
            if (response.contains("[DONE]"))
            {
                return;
            }






                            // Update the UI with the received chat message
                            int lastIndex = chat.size() - 1;

                            String s1=chat.get(lastIndex).getText();

                            if (s1.equals(getString(R.string.waitAsecond)) || s1.equals(getString(R.string.analyzing)))
                            {
                                chat.remove(lastIndex);
                            }



                            lastIndex = chat.size() - 1;
                            // Check if the last chat message has the same sender as the new message
                            if (lastIndex >= 0 && chat.get(lastIndex).sentByUser == 0)
                            {
                                // Append the new message content to the existing message

                                ChatMessage lastMessage = chat.get(lastIndex);




                                String updatedContent = lastMessage.getText()  + response;

                                if (updatedContent.equals(getString(R.string.waitAsecond)))
                                {
                                    return;
                                }

                                if (lastMessage.getIsTem()==1) //the message is temporary reply from server
                                {
                                    chat.get(lastIndex).setText(response); // replacing the old tem message with new message
                                    chat.get(lastIndex).setIsTem(isTemMessage); // setting the is tem to current is tem

                                }else {
                                    chat.get(lastIndex).setText(updatedContent);
                                }




                            }
                            else {
                                // Create a new ChatMessage object and add it to the chat list
                                ChatMessage newMessage = new ChatMessage(response, 0, getCurrentTime(), currentConID,modelID, currentAiExpertIndex, enabledPlugIns,isTemMessage);
                                chat.add(newMessage);
                            }





                            int finalLastIndex = lastIndex;
                            a.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    shouldScroll=true;
                                    // Notify the adapter that the dataset has changed
                                    chatAdapter.notifyItemChanged(finalLastIndex);

                                    // Scroll the RecyclerView to the bottom
                                 //   chatRecycle.scrollToPosition(chat.size() - 1);
                                }
                            });

        }
    }







    private void insertIntoChat(int i, String s)
    {

        i(i+" "+s);
        boolean isByUser = (i>0);
        boolean isToldToWait=s.equals(getString(R.string.waitAsecond)); // when showing typing




        StringBuilder links=new StringBuilder("");

        if (links.toString().isEmpty())
        {
            links.append("0");
        }


        if (!isToldToWait)
        {
            db.insertData(i,s,getCurrentTime(),currentConID, modelID,currentAiExpertIndex,enabledPlugIns);
        }

        shouldItbeAnimate=true;
        if (isToldToWait && isByUser) // when  user and told to wait todo not hiding typing anni first time in auto
        {
            i("isToldToWait && !isByUser");
            chat.add(new ChatMessage(s,i,getCurrentTime(),currentConID,modelID,currentAiExpertIndex, enabledPlugIns,1));
            chatAdapter.notifyItemInserted(chat.size() - 1);
            //chatRecycle.scrollToPosition(chat.size() - 1);
        }
        if (isToldToWait && !isByUser) // when not user and told to wait todo not hiding typing anni first time in auto
        {
            i("isToldToWait && !isByUser");
            chat.add(new ChatMessage(s,i,getCurrentTime(),currentConID,modelID,currentAiExpertIndex, enabledPlugIns,1));
            chatAdapter.notifyItemInserted(chat.size() - 1);
            //chatRecycle.scrollToPosition(chat.size() - 1);
        }else if (!isToldToWait && !isByUser)// when not user and not told to wait
        {
            i("!isToldToWait && !isByUser");
            chat.remove(chat.size()-1);
            chat.add(new ChatMessage(s,i,getCurrentTime(),currentConID,modelID,currentAiExpertIndex, enabledPlugIns,0));

            chatAdapter.notifyDataSetChanged();

        }else if (!isToldToWait  )// when not user and not told to wait
        {
            i("!isToldToWait && isByUser");



            chat.add(new ChatMessage(s,i,getCurrentTime(),currentConID,modelID,currentAiExpertIndex, enabledPlugIns,0));
           // chatAdapter.notifyDataSetChanged();
            chatAdapter.notifyDataSetChanged();



        }

        chatRecycle.smoothScrollToPosition(chat.size() - 1);
    }

    private void i(String anElse) {
        Log.i("Chat",anElse);
    }


    @Override
    public void onResume() {
        super.onResume();
        c=getContext();

        if (!isOnline(c))
        {
            startActivity(new Intent(c,NoInternetActivity.class));
        }
        else {

            getData();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("chat","Request code "+requestCode);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT)
        {
            if (resultCode == RESULT_OK && data != null)
            {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                messageBox.setText(messageBox.getText()+" "+
                        Objects.requireNonNull(result).get(0));
            }
        }
        else if
        (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            if (imageUri != null) {
                Uri destinationUri = Uri.fromFile(new File(c.getCacheDir(), "cropped"));
                UCrop uCrop = UCrop.of(imageUri, destinationUri);

                uCrop.start(c,this);

            } else {
                Log.i("chat","Capture image uri is null");
                Toast.makeText(c, "Image URI is null", Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK)
        {


            if (data != null && data.getData() != null) {
                if (data.getData() != null) {
                    Uri destinationUri = Uri.fromFile(new File(c.getCacheDir(), "cropped"));
                    UCrop uCrop = UCrop.of(data.getData(), destinationUri);

                    uCrop.start(c,this);

                } else {
                    Log.i("chat","Capture image uri is null");
                    Toast.makeText(c, "Image URI is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(c, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == REQUEST_IMAGE_PICK_TO_UPLOAD && resultCode == RESULT_OK)
        {

            imgPickDialog.dismiss();
            if (data != null && data.getData() != null)
            {
                sendToServer(data.getData());
            }
            else
            {
                Toast.makeText(c, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
        else  if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK)
        {
            if (data != null) {
                Uri selectedPdfUri = data.getData();
                pdfSendWarningDialog(selectedPdfUri);

                // Now you can work with the selected PDF file using its Uri
                // For example, you can display its path or perform further processing.
            }
        }
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            try {
                final Uri resultUri = UCrop.getOutput(data);
                Bitmap bitmap = null;
                Log.i("chat","UCrop.REQUEST_CROP");
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(c.getContentResolver(), resultUri);
                    recognizeText(bitmap);
                    File file=new File(resultUri.getPath());
                    if (file.exists())
                    {
                        file.delete();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }catch (Exception e)
            {
                Log.i("chat","UCrop.getOutput error");
            }

        }


    }
    LottieAnimationView imageUploaded;
    ProgressBar imageUploading;
    TextView uploadingImageName;
    String currentImageUrl=""; // will add to the users message when user send the query
    private void sendToServer(Uri data) {

        docSendLayout.setVisibility(View.VISIBLE);
        imageUploaded.setVisibility(View.GONE);
        imageUploading.setVisibility(View.VISIBLE);


        shouldEnableSendButton(false);



        HttpRequest httpRequest=new HttpRequest(new ApiResponse() {
            @Override
            public void response(String response)
            {
                a.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        shouldEnableSendButton(true);

                        imageUploaded.setVisibility(View.VISIBLE);
                        imageUploading.setVisibility(View.GONE);
                    }
                });

                currentImageUrl=response;

            }
        });
        httpRequest.sendImage(data, c, new ApiResponse() {
            @Override
            public void response(String name) {
                uploadingImageName.setText(name);
            }
        });


    }

    private void shouldEnableSendButton(boolean shouldEnable) {
        sendQuestions.setClickable(shouldEnable);
        if(shouldEnable)
        {
            sendQuestions.setBackgroundColor(ContextCompat.getColor(c,R.color.colorPrimary));
        }else {
            sendQuestions.setBackgroundColor(ContextCompat.getColor(c,R.color.gray));
        }

    }

    BottomSheetDialog pdfWarningDialog;
    private void pdfSendWarningDialog(Uri uri)
    {
        pdfWarningDialog=new BottomSheetDialog(c);
        View view=getLayoutInflater().inflate(R.layout.pdf_send_warning_dialog,null);

        TextView pdfName=view.findViewById(R.id.pdfName);
        TextView warningText=view.findViewById(R.id.warningText);

        Button no=view.findViewById(R.id.no);
        Button yes=view.findViewById(R.id.yes);





    try {
            InputStream inputStream=a.getContentResolver().openInputStream(uri);
            PDDocument document = PDDocument.load(inputStream);

            String name=getOriginalFileName(c,uri);

            pdfName.setText(name+"("+document.getNumberOfPages()+" pages)");
            warningText.setText("You have "+pdfCredit+" PDF pages credit, after you proceeds "+(pdfCredit-document.getNumberOfPages())+" PDF pages credit will be left.This can't be undo. Do you want to proceed with this PDF? ");

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pdfWarningDialog.dismiss();
                }
            });
            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    pdfWarningDialog.dismiss();
                    insertIntoChat(2,name); //


                    PdfToText pdfToText=new PdfToText(a);
                    pdfToText.getText(uri, new SendPDFData() {
                        @Override
                        public void send(ArrayList<PdfData> data) {
                            pdfData=data;
                            Cursor cursor= pdfSummaries.getReadableDatabase().rawQuery("Select * from " + getString(R.string.PDFSummaries)+" where PdfName='"+pdfData.get(0).getPdfName()+"'", null);

                            if (cursor.getCount()==0)  // if not already save
                            {

                                a.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        insertIntoChat(0,getString(R.string.waitAsecond)); //
                                    }
                                });

                                mSocket.emit("get_summary", getFullPdfPages().toString(), pdfData.size(), sp.getString("email",""), getUserID(c));
                            }
                            cursor.close();

                        }
                    });

                }
            });

    } catch (IOException e) {
            throw new RuntimeException(e);
        }




    pdfWarningDialog.setContentView(view);
    pdfWarningDialog.show();






    }

    public  String getOriginalFileName(Context context, Uri uri)
    {
        String displayName = null;
        String scheme = uri.getScheme();

        if (scheme != null && scheme.equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null))
            {
                if (cursor != null && cursor.moveToFirst())
                {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        displayName = cursor.getString(nameIndex);
                    }
                }
            }
        } else if (scheme != null && scheme.equals("file")) {
            // For file URIs, the display name can be obtained directly from the URI
            displayName = uri.getLastPathSegment();
        }

        Log.i("PdfToText","Original Name "+displayName);
        return displayName;
    }


    private void recognizeText(Bitmap bitmap) {
        if (imgPickDialog!=null)
        {
            imgPickDialog.dismiss();
        }
        // Initialize TextRecognizer
        TextRecognizer textRecognizer = new TextRecognizer.Builder(c).build();
        if (!textRecognizer.isOperational()) {
            // Handle error
            Log.e(this.toString(),"error not operational");
            return;
        }

        // Pass Bitmap to TextRecognizer and obtain SparseArray of TextBlock objects
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<TextBlock> textBlocks = textRecognizer.detect(frame);

        // Extract text from TextBlock objects and combine into a single string
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.valueAt(i);
            stringBuilder.append(textBlock.getValue());
            stringBuilder.append("\n"); // Add newline character after each text block
        }
        String result = stringBuilder.toString();

        messageBox.setText(result);
        // Display the result in the TextView
        Log.i(this.toString(), "result "+result);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void response(String response)
    {
  //      processResponse(response);
        if (response.isBlank())
        {
            return;
        }




        notReceivedYet=false;
       // Log.i(this.toString(),"response "+response);
        if (response.equals("100"))
        {
            deleteWaitASecondMessage();
            showCreditDialog();
          //  showSnackBar("You have reached your daily limit.",1);
        }
        else if (response.equals("Error:affix"))
        {
            deleteWaitASecondMessage();

            showSnackBar("Something went wrong. Please try again",1);
        }else if(response.contains("{\"affcode\":100,"))
        {
            showCreditDialog();
          //  showSnackBar("You have reached your daily limit.",1);
            try {
                JSONArray jsonArray=new JSONArray(response);

                String jb=jsonArray.getString(0);
                JSONObject jo = new JSONObject(jb);


                totalCredit=jo.getInt("haveReward");

                isMember=jo.getInt("isMember");
                planName = jo.getString("subscriptionId");
            } catch (Exception e)
            {
                e.printStackTrace();
            }



        }
        else if (response.equals("200") || response.equals("404") ) // device id and email is not matching with database
        {
            deleteWaitASecondMessage();
            new AlertDialog.Builder(c)
                    .setTitle("User Not Found")
                    .setCancelable(false)
                    .setMessage("Something went wrong. Please login again.")
                    .setNegativeButton("Login", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            startActivity(new Intent(c,login.class).putExtra("activity",1));
                        }
                    })
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            a.finishAffinity();
                            System.exit(1);
                        }
                    }).show();
        }
        else if (response.length()>20)
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
                planName = jo.getString("subscriptionId");

                isPlanStockOut=jo.getInt("isPlanStockOut");
                premiumCredit=jo.getInt("premiumCredits");
                pdfCredit=jo.getInt("pdf_pages");

                Log.i("Main","!Objects.isNull(creditS) && isMember!=1 "+(!Objects.isNull(creditS) && isMember!=1));

                showCredit();



                if(!Objects.isNull(creditS) && isMember!=1)
                {
                    creditS.setText(""+totalCredit);

                } else if (!Objects.isNull(creditS) && isMember==1)
                {
                    creditS.setText("Subscriber");

                }




                if (jsonArray.length()>1 && !shouldStopResponse)
                {
                    jb=jsonArray.getString(1);
                    jo = new JSONObject(jb);

                    String reply=jo.getString("Reply");
                    //   if ()
                    //reply=reply.replaceFirst("^\\n\\n","");
                    reply=reply.replaceFirst("^(\\\\n)+","");
                    insertIntoChat(0,reply);

                }










            }
            catch (Exception e)
            {
                deleteWaitASecondMessage();

                showSnackBar("Something went wrong. Please try again",1);
                e.printStackTrace();
                Log.e("MainActivity","error "+e.getMessage());
            }
        }

        Intent local=new Intent();

        if (!Objects.isNull(a))
        {
            local.setAction(getString(R.string.subIntentAction));
            a.sendBroadcast(local);
        }



    }

    private void showCredit()  // setting credit accordingly model
    {

        if (getModelById(modelID).getIsPremium()==1)
        {
         //   credit.setText(premiumCredit+" "+getString(R.string.premium_credit));
         //   credit.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.premium_clr));
        }
        else
        {
          //  credit.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.green));
            if (isMember==1)
            {
            //    credit.setText(R.string.no_limit);
            }else {
            //    credit.setText(totalCredit+" "+getString(R.string.credit));
            }
        }
    }


    boolean processResponse(String response)
    {
        if (response.length()==0)
        {
            return false;
        }
        Intent local=new Intent();

        local.setAction(getString(R.string.subIntentAction));

        if (!Objects.isNull(a))
        {
            a.sendBroadcast(local);
        }





        notReceivedYet=false;
        //Log.i(this.toString(),"response "+response);
        if ( response.equals("100") && isTemMessage==2 )
        {
            deleteWaitASecondMessage();
            showCreditDialog();
           // showSnackBar("You have reached your daily limit.",1);
        }else if (response.equals("Error:affix"))
        {
            deleteWaitASecondMessage();

            showSnackBar("Something went wrong. Please try again",1);
        }
        else if(response.contains("{\"affcode\":100,"))
        {
            showCreditDialog();
          //  showSnackBar("You have reached your daily limit.",1);
            try {
                JSONArray jsonArray=new JSONArray(response);

                String jb=jsonArray.getString(0);
                JSONObject jo = new JSONObject(jb);


                totalCredit=jo.getInt("haveReward");

                isMember=jo.getInt("isMember");
                planName = jo.getString("subscriptionId");
            } catch (Exception e) {
                e.printStackTrace();
            }



        }
        else if ( (response.equals("200") || response.equals("404")) && isTemMessage==2 ) // device id and email is not matching with database
        {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deleteWaitASecondMessage();
                }
            });

            new AlertDialog.Builder(c)
                    .setTitle("User Not Found")
                    .setCancelable(false)
                    .setMessage("Something went wrong. Please login again.")
                    .setNegativeButton("Login", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            startActivity(new Intent(c,login.class).putExtra("activity",1));
                        }
                    })
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            a.finishAffinity();
                            System.exit(1);
                        }
                    }).show();
        }else {
            return  true;
        }



        return false;
    }


    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include more cases
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void deleteWaitASecondMessage() {

        if (chat.size()==0)
        {
            return;
        }
        Iterator<ChatMessage> iterator = chat.iterator();

        while(iterator.hasNext()) {
            ChatMessage c = iterator.next();
            if (c.getText().equals(this.c.getString(R.string.waitAsecond))) {
                iterator.remove();
            }
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // Code to run on the main thread
                chatAdapter.notifyDataSetChanged();
            }
        });




    }

    @Override
    public void onInit(int i) {

        if (i == TextToSpeech.SUCCESS) {
            Locale locale = Locale.getDefault(); // or specify a specific language
            Voice voice = new Voice("en-us-x-sfg#male_1-local", locale, Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, Collections.emptySet());


            tts.setVoice(voice);
        }

    }




    LinearLayout toolLayoutInChat;// contains speak and time view, will be use to make the last reply speak visible on receive "done"
    public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

        private final List<ChatMessage> chatMessageList;
        private ImageView currentSpeakingIcon;// is the icon in recycle view which is playing now

        public ChatAdapter(List<ChatMessage> chatMessageList) {
            this.chatMessageList = chatMessageList;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout=R.layout.chat_recycle;

            View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return new ChatAdapter.ChatViewHolder(view);
        }
        int position=0;
        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder h, int p) {
            position=h.getAdapterPosition();


            ChatMessage message = chatMessageList.get(position);
            ModelList modelData=getModelById(modelID);



            h.toolLayout.setVisibility(View.GONE);
            if (message.getSentByUser()>2)
            {
                h.aiMsgView.setVisibility(View.GONE);
                h.userMsgView.setVisibility(View.GONE);
                return;
            }

            Picasso.get().load(modelData.getIconUrl()).into(h.aiImage);
            Picasso.get().load(modelData.getIconUrl()).into(h.aiTypingLogo);
            if (message.getSentByUser()>0)
            {

                h.aiMsgView.setVisibility(View.GONE);


                if (message.getSentByUser()==2)//user sent a pdf
                {
                    h.docSendLayout.setVisibility(View.VISIBLE);
                    h.showDocName.setText(message.getText());

                    // was showing pdf user send
                }
                else
                {
                    h.userMsgView.setVisibility(View.VISIBLE);
                    if (message.getText().equals(getString(R.string.waitAsecond)))
                    {
                        h.typingAnniAuto.setVisibility(View.VISIBLE);

                        h.userChat.setVisibility(View.GONE);
                        return;
                    }
                    else
                    {
                        h.userChat.setVisibility(View.VISIBLE);
                        h.userChat.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {

                                copyText(message.getText());
                                return false;
                            }
                        });
                        h.typingAnniAuto.setVisibility(View.GONE);
                    }

                    // if user attached an image, its included in message don't show the url but the image layout in the recycler view
                    h.messageTextbyUser.setText(getMessageWithoutImageUrl(message.getText(),h));
                    // h.userMegTime.setText(message.getTime());
                    h.messageTextbyUser.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            copyText(message.getText());
                            return false;
                        }
                    });

                    setTimeAgo(h.userMegTime,message.getTime());


                }









                h.copyQuestions.setOnClickListener(view -> {
                    copyText(message.getText());
                });

            }
            else
            {

                h.aiMsgView.setVisibility(View.VISIBLE);
                h.userMsgView.setVisibility(View.GONE);

                if (message.getText().equals(getString(R.string.waitAsecond)))
                {
                    h.typingAnni.setVisibility(View.VISIBLE);
                    h.aiShowText.setVisibility(View.GONE);
                    h.waitText.setText(waitTextString);

                    return;
                } else if (message.getText().equals(modelData.getWelcomeMsg() )  && chat.size()==1)  //todo copy and paste
                {
                    h.messageTextbyAi.setText("");
                    String mText;

                    mText=message.getText();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (index < mText.length()) {
                                // Add the next set of characters
                                int endIndex = index + charactersAtOnce;
                                if (endIndex > mText.length()) {
                                    endIndex = mText.length(); // Ensure endIndex doesn't exceed the string's length
                                }
                                String substring = mText.substring(index, endIndex);
                                h.messageTextbyAi.append(substring);
                                index += charactersAtOnce;

                                // If there's more to type, post the Runnable again after a delay
                                if (index < mText.length()) {
                                    handler.postDelayed(this, delay);
                                }
//                                if (index%30==0)
//                                {
//                                    chatRecycle.smoothScrollToPosition(chat.size() - 1);
//                                }
                                i((mText.length()-1) + "=(mText.length()-1) in chat Adapter endIndex="+endIndex);
                                if (endIndex>=(mText.length()-1))
                                {
                                    messageBox.requestFocus();
                                    i("showing keyboard");
                                    InputMethodManager imm = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.showSoftInput(messageBox, InputMethodManager.SHOW_IMPLICIT);


                                }
                            }
                        }
                    }, delay);

//                    if (message.getIsTem()==0)
//                    {
//                        h.toolLayout.setVisibility(View.VISIBLE);
//                    }else {
//                        h.toolLayout.setVisibility(View.GONE);
//                    }

                    return;
                }
                else if (message.getText().equals(getString(R.string.analyzing)))
                {
                    h.waitingLayout.setVisibility(View.VISIBLE);
                    String mText;
                    final int[] mIndex = {0};
                    long mDelay = 20; // in milliseconds

                    mText=message.getText();


                    Handler mHandler = new Handler();
                    Runnable characterAdder = null;
                    long finalMDelay = mDelay;
                    characterAdder = new Runnable()
                    {
                        @Override
                        public void run() {
                            CharSequence w=mText.subSequence(0, mIndex[0]++);
                            h.waitText.setText(w);

                            if (shouldScroll )
                            {
                                chatRecycle.smoothScrollToPosition(chat.size() - 1);
                            }
                            //chatRecycle.scrollToPosition(chat.size()-1);
                            if (mIndex[0] <= mText.length())
                            {
                                mHandler.postDelayed(this, finalMDelay);
                            }else {
                                h.itemView.findViewById(R.id.anni).setVisibility(View.VISIBLE);
                            }
                        }
                    };
                    mHandler.removeCallbacks(characterAdder);
                    mHandler.postDelayed(characterAdder, mDelay);
                    h.aiShowText.setVisibility(View.GONE);

                }
                else
                {
                    h.aiShowText.setVisibility(View.VISIBLE);
                    h.typingAnni.setVisibility(View.GONE);





                }

                h.aiName.setText(expertAIData.get(currentAiExpertIndex).getName());

                h.readReplyImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (isSpeaking==0)
                        {
                            currentSpeakingIcon=h.readReplyImg;
                            speak(h.readReplyImg,message.getText());
                            showSnackBar("Playing",0);

                        }else {
                            tts.stop();
                            currentSpeakingIcon.setImageDrawable(ContextCompat.
                                    getDrawable(c, R.drawable.speaker_icon));
                            isSpeaking=0;
                        }
                    }
                });

                setTimeAgo(h.aiMsgTime,message.getTime());



                h.inShort.setVisibility(View.GONE);




                //h.messageTextbyAi.setText(message.getText());
                setTextAndMakeClickable(message.getText(),h.messageTextbyAi,h);

//                if (shouldScroll)
//                {
//                    chatRecycle.smoothScrollToPosition(chat.size() - 1);
//                }

                h.aiShowText.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view)
                    {
                        // showExpertDialog();
                        showTools(message.getText());
                        return true;
                    }
                });


                if (shouldStopResponse && chat.size()-1==position) // means getting response and the chat is the last one
                {
                    // get the last layouts tools to show when receive done from server


                    if (message.getIsTem()==0)
                    {
                        toolLayoutInChat=h.toolLayout;
                    }else {
                        toolLayoutInChat=null;
                    }
                    return;
                }
                if (message.getIsTem()==0)
                {
                    h.toolLayout.setVisibility(View.VISIBLE);
                }else {
                    h.toolLayout.setVisibility(View.GONE);
                }





            }


        }

        String getMessageWithoutImageUrl(String text,ChatViewHolder h)
        {


            i("getMessageWithoutImageUrl");
            //todo image is not showing also url is visible to the user

            // Use a regular expression to match the url between "Image Url = " and "\n"
            String pattern = "Image Url = (https?://.+)\\n\\n";;

            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(text);

            if (m.find()) {
                i("the message include image");
                String url = m.group(1);
                // show the image
                h.itemView.findViewById(R.id.imageSendLayout).setVisibility(View.VISIBLE);

                i("uploaded image url before loading = "+url+"..");
                ImageView image=h.itemView.findViewById(R.id.image);
                ContentLoadingProgressBar loading=h.itemView.findViewById(R.id.userImageLoading);

                loading.setVisibility(View.VISIBLE);

                Picasso picasso = new Picasso.Builder(c)
                        .memoryCache(new LruCache(1024 * 1024 * 3 ))
                        .build();

                // loading from cache
                picasso.load(url)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .resize(300, 300) // resize to 400x400 pixels
                        .centerCrop()
                        .into(image, new Callback() {
                            @Override
                            public void onSuccess() {
                                // Image loaded from cache
                                loading.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(Exception e) {
                                // Try again online if cache failed
                                picasso.load(url).resize(300, 300) // resize to 400x400 pixels
                                        .centerCrop()
                                        .into(image, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                // Image loaded from network
                                                loading.setVisibility(View.VISIBLE);
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                // if can't load from url , show a default image
                                                loading.setVisibility(View.VISIBLE);
                                                picasso.load(R.drawable.image_not_loaded)
                                                        .into(image);
                                            }
                                        });
                            }
                        });


                h.itemView.findViewById(R.id.imageSendLayout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        List<String> imageList = new ArrayList<>();
                        imageList.add(url);

                        i("url for loading image in full screen = "+imageList.get(0));
                        Window window = a.getWindow();


                        new StfalconImageViewer.Builder<>(c, imageList, new ImageLoader<String>() {
                            @Override
                            public void loadImage(ImageView imageView, String image) {
                                window.setStatusBarColor(ContextCompat.getColor(c, android.R.color.black));
                                picasso.load(url)
                                        .networkPolicy(NetworkPolicy.OFFLINE)

                                        .into(imageView, new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                // Image loaded from cache
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                // Try again online if cache failed
                                                picasso.load(url)
                                                        .into(imageView, new Callback() {
                                                            @Override
                                                            public void onSuccess() {
                                                                // Image loaded from network
                                                            }

                                                            @Override
                                                            public void onError(Exception e) {
                                                                // if can't load from url , show a default image
                                                                picasso.load(R.drawable.image_not_loaded)
                                                                        .into(imageView);
                                                            }
                                                        });
                                            }
                                        });
                            }
                        }).
                                withBackgroundColor(ContextCompat.getColor(c, R.color.black))
                                .withImageMarginPixels(20)
                                .withTransitionFrom( image).withHiddenStatusBar(true)
                                .withDismissListener(new OnDismissListener() {
                                    @Override
                                    public void onDismiss() {
                                        window.setStatusBarColor(ContextCompat.getColor(c, R.color.white));
                                    }
                                })
                                .show();
                    }
                });

                //get the original message
                text=text.replaceFirst(pattern, "");




            }

            return text;


        }
        private void setTextAndMakeClickable(String inputString, TextView textView, ChatViewHolder h) {

            inputString=getAiMessageWithoutImageUrl(inputString,h);

            // Regular expression to match the link text and URL
            Pattern pattern = Pattern.compile("\\[(.*?)\\]\\((.*?)\\)");

            // Create a SpannableString object
            SpannableString spannableString = new SpannableString(inputString);

            // Find all matches of the regular expression in the input string
            Matcher matcher = pattern.matcher(inputString);
            while (matcher.find()) {
                // Get the link text and URL from the match
                String linkText = matcher.group(1);
                String linkUrl = matcher.group(2);

                // Get the start and end indices of the match in the input string
                int start = matcher.start();
                int end = matcher.end();

                // Create a ClickableSpan object to handle link clicks
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        // Handle link click event
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl));
                        startActivity(intent);
                    }
                };

                // Set the ClickableSpan object to the SpannableString object
                spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Remove the square brackets from the link text
                linkText = linkText.replace("[", "").replace("]", "");

                // Create a ReplacementSpan object to replace the match with the link text
                String finalLinkText = linkText;
                ReplacementSpan replacementSpan = new ReplacementSpan() {
                    @Override
                    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
                        return (int) paint.measureText(finalLinkText);
                    }

                    @Override
                    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
                        //paint.setColor(Color.GREEN);
                        paint.setUnderlineText(true);
                        canvas.drawText(finalLinkText, x, y, paint);
                    }
                };

                // Set the ReplacementSpan object to the SpannableString object
                spannableString.setSpan(replacementSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
               // spannableString.removeSpan(new StyleSpan(Typeface.BOLD)); // Remove the bold style from the link text
            }

            // Set the SpannableString object to the TextView
            textView.setText(spannableString);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            String finalInputString = inputString;
            textView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showTools(finalInputString);
                    return false;
                }
            });

        }

        int alreadyShowedGeneratedImageIndex=0; // when receiving the response in stream , helps to prevent loading again again the image
        private String getAiMessageWithoutImageUrl(String text, ChatViewHolder h) {


            i("getMessageWithoutImageUrl");
            //todo image is not showing also url is visible to the user

            // Use a regular expression to match the url between "Image Url = " and "\n"
            String pattern = "\\[I:(https?://[^\\]]+)\\]";

            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(text);

            if (m.find()) {
                i("the message include image");
                String url = m.group(1);

                // load the image if it not loaded already
                if (alreadyShowedGeneratedImageIndex!=position)
                {
                    // show the image
                    h.itemView.findViewById(R.id.generatedImageLayout).setVisibility(View.VISIBLE);
                    h.itemView.findViewById(R.id.generatedImageLayout).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });

                    i("uploaded image url before loading = "+url+"..");
                    ImageView image=h.itemView.findViewById(R.id.generatedImage);


                    ContentLoadingProgressBar loading=h.itemView.findViewById(R.id.AIImageLoading);
                    loading.setVisibility(View.VISIBLE);




                    String imageName=getImageNameFromURL(url);
                    File imageFile = new File(c.getFilesDir(), imageName);

                    if(imageFile.exists())
                    {
                        i("generated image file exist");
                        // Load from internal storage
                        Picasso.get().load(imageFile)
                                .resize(300, 300) // resize to 400x400 pixels
                                .centerCrop()
                                .into(image, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        loading.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        loading.setVisibility(View.GONE);
                                    }
                                });

                    }
                    else {

                        // Download from internet, save and load
                        Picasso.get()
                                .load(url)
                                .into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        i("Bitmap loading from server success");
                                        try {
                                            FileOutputStream out = new FileOutputStream(imageFile);
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                                            out.flush();
                                            out.close();
                                            v.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    i("Loaded image");
                                                    chatAdapter.notifyItemChanged(chat.size()-1);
                                                    alreadyShowedGeneratedImageIndex=0;
                                                }
                                            },3000);





                                        } catch (Exception e) {
                                            // Handle error
                                        }
                                    }

                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                        i("Bitmap loading from server failed");
                                        loading.setVisibility(View.GONE);
                                        // Handle error
                                        Picasso.get().load(url)
                                                .resize(300, 300) // resize to 400x400 pixels
                                                .centerCrop()
                                                .into(image, new Callback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        loading.setVisibility(View.GONE);
                                                    }

                                                    @Override
                                                    public void onError(Exception e) {
                                                        loading.setVisibility(View.GONE);
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }
                                });

                    }

                    image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            List<String> imageList = new ArrayList<>();
                            if (imageFile.exists())
                            {
                                imageList.add(imageFile.getPath());
                            }else
                            {
                                imageList.add(url);
                            }

                            i("url for loading image in full screen = "+imageList.get(0));
                            Window window = a.getWindow();


                            new StfalconImageViewer.Builder<>(c, imageList, new ImageLoader<String>() {
                                @Override
                                public void loadImage(ImageView imageView, String image) {
                                    window.setStatusBarColor(ContextCompat.getColor(c, android.R.color.black));
                                    if (image.startsWith("http://") || image.startsWith("https://")) {
                                        // Load from URL
                                        Picasso.get().load(image).into(imageView);
                                    } else {
                                        // Load from file
                                        Picasso.get().load(new File(image)).into(imageView);
                                    }
                                }
                            }).
                                    withBackgroundColor(ContextCompat.getColor(c, R.color.black))
                                    .withImageMarginPixels(20)
                                    .withTransitionFrom( image).withHiddenStatusBar(true)
                                    .withDismissListener(new OnDismissListener() {
                                        @Override
                                        public void onDismiss() {
                                            window.setStatusBarColor(ContextCompat.getColor(c, R.color.white));
                                        }
                                    })
                                    .show();
                        }
                    });

                    h.itemView.findViewById(R.id.downloadGeneratedImage).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(imageFile.exists()){
                                // Copy from internal storage to gallery
                                try {
                                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                                    saveToGallery(bitmap, imageName);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // Download from server and save to gallery
                                Picasso.get().load(url).into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        saveToGallery(bitmap, imageName);
                                    }

                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                        Toast.makeText(c, "Failed to save image", Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }
                                });
                            }
                        }
                    });


                    alreadyShowedGeneratedImageIndex=position;


                }




                //get the original message
                text=text.replaceFirst(pattern, "");





            }

            return text;
        }



        // Method to save a bitmap to the gallery
        private void saveToGallery(Bitmap bitmap, String imageName) {
            ProgressDialog pd=new ProgressDialog(c);
            pd.setCancelable(false);
            pd.setMessage("Downloading..");
            pd.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    OutputStream fos;
                    try {
                        ContentResolver resolver = c.getContentResolver();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageName);
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                        fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        Objects.requireNonNull(fos);
                        pd.dismiss();
                        a.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(c, "Image saved to gallery", Toast.LENGTH_LONG).show();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        pd.dismiss();
                        a.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(c, "Failed to download the image", Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                }
            }).start();

        }

        int index = 0;
        long delay = 70; // delay in milliseconds between characters
        int charactersAtOnce = 7;
        private void showTools(String message) {

            final String[] me = {message};
            BottomSheetDialog tool=new BottomSheetDialog(c,R.style.CustomBottomSheetDialogTheme);
            View v=getLayoutInflater().inflate(R.layout.show_tools_bottom_dialog,null);


            v.findViewById(R.id.replyCopy).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    copyText(me[0]);
                    tool.dismiss();
                }
            });
            v.findViewById(R.id.replyShare).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String pattern = "\\[I:(https?://[^\\]]+)\\]";

                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(me[0]);

                    if (m.find())
                    {
                        String url=m.group(1);
                        String imageName=getImageNameFromURL(url);
                        File imageFile = new File(c.getFilesDir(), imageName);
                        if (imageFile.exists())
                        {
                            Uri imageUri = FileProvider.getUriForFile(c, getString(R.string.authorityName) , imageFile);

                            int startIndex = me[0].indexOf("[");
                            int endIndex = me[0].indexOf("]");

                            if (startIndex != -1 && endIndex != -1) {
                                String replacement = me[0].substring(startIndex, endIndex + 1);
                                me[0] = me[0].replace(replacement, "");
                            }
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_TEXT, me[0]);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                            shareIntent.setType("image/*");
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(shareIntent, "Share to.."));
                        }else {
                            shareText(me[0]);
                        }



                    }else {
                        shareText(me[0]);


                    }
                    tool.dismiss();
                }
            });


            tool.setContentView(v);
            tool.show();


        }

        private void shareText(String message) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Share using");
            intent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(intent);
        }

        int isSpeaking=0;
        private void speak(ImageView readReplyImg, String message) {
            showSnackBar("Playing",0);


            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String s) {
                    Log.i("MainActivity"," started speaking");
                    isSpeaking=1;
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            readReplyImg.setImageDrawable(ContextCompat.
                                    getDrawable(c, R.drawable.listening_icon));
                        }
                    });

                }

                @Override
                public void onDone(String s) {
                    Log.i("MainActivity"," started onDone");
                    isSpeaking=0;
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            readReplyImg.setImageDrawable(ContextCompat.
                                    getDrawable(c, R.drawable.speaker_icon));
                        }
                    });

                }

                @Override
                public void onError(String s) {
                    isSpeaking=0;
                    showSnackBar("Something went wrong.",0);
                }
            });
            tts.speak(message,TextToSpeech.QUEUE_FLUSH,null,"utterance_id");
        }
        @Override
        public int getItemCount() {
            return chatMessageList.size();
        }

        void copyText(String text)
        {

            ClipboardManager clipboardManager=(ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);

            ClipData clipData= ClipData.newPlainText("Text",text);
            clipboardManager.setPrimaryClip(clipData);

            showSnackBar("Copied",0);

        }

        public  class ChatViewHolder extends RecyclerView.ViewHolder {

            public TextView messageTextbyAi,messageTextbyUser,userMegTime,aiMsgTime,seeMore,senderName;
            public TextView inShort;

            LinearLayout userMsgView,aiMsgView,docSendLayout,aiShowText,userChat,waitingLayout,toolLayout;

            TextView copyQuestions,aiName,waitText,copyReply,shareReply,showDocName;
            ImageView readReplyImg,waitMessage,aiImage,aiTypingLogo;
            TextSwitcher mTextSwitcher;

            LinearLayout typingAnni;
            LottieAnimationView typingAnniAuto;

            public ChatViewHolder(View itemView)
            {

                super(itemView);
                messageTextbyAi = itemView.findViewById(R.id.replyText);
                senderName = itemView.findViewById(R.id.senderName);
                messageTextbyUser = itemView.findViewById(R.id.questionText);
                userMegTime = itemView.findViewById(R.id.questionTime);
                aiMsgTime = itemView.findViewById(R.id.timeOfReplay);
                inShort = itemView.findViewById(R.id.shortIt);

                mTextSwitcher = itemView.findViewById(R.id.textSwitcher);

                seeMore = itemView.findViewById(R.id.seeMore); // for non members


                copyReply = itemView.findViewById(R.id.replyCopyTxt);
                copyQuestions = itemView.findViewById(R.id.questionCopy);
                shareReply = itemView.findViewById(R.id.replyShareTxt);
                readReplyImg = itemView.findViewById(R.id.speak);
                toolLayout = itemView.findViewById(R.id.toolLayout);
                aiImage = itemView.findViewById(R.id.aiImage);


                userMsgView= itemView.findViewById(R.id.userMsgView);
                userChat= itemView.findViewById(R.id.userChat);
                aiMsgView= itemView.findViewById(R.id.aiAnsView);
                docSendLayout= itemView.findViewById(R.id.docSendLayout);
                aiName= itemView.findViewById(R.id.aiName);
                waitText= itemView.findViewById(R.id.waitText);

                typingAnni= itemView.findViewById(R.id.typingAnni);
                typingAnniAuto= itemView.findViewById(R.id.typingAnniAuto);
                aiShowText= itemView.findViewById(R.id.aiShowText);
                showDocName= itemView.findViewById(R.id.showDocName);
                waitingLayout= itemView.findViewById(R.id.waitingLayout);
                aiTypingLogo= itemView.findViewById(R.id.aiImageLogo);



            }

        }

    }




    void showExpertDialog()
    {
        BottomSheetDialog expertDia=new BottomSheetDialog(c,R.style.CustomBottomSheetDialogTheme);

        View view=getLayoutInflater().inflate(R.layout.expert_bottom_dialog,null,false);

        RecyclerView expertRecycle=view.findViewById(R.id.expertRecycle);

        expertRecycle.setAdapter(ExpertAdapter(expertDia));

        expertDia.setContentView(view);
        expertDia.show();

    }

    private RecyclerView.Adapter ExpertAdapter(BottomSheetDialog expertDia) {

        Picasso picasso=Picasso.get();
        return new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(getLayoutInflater().inflate(R.layout.experts_recycle,null))
                {
                    @Override
                    public String toString()
                    {
                        return super.toString();
                    }
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position)
            {
                int i=h.getAdapterPosition();
                TextView name=h.itemView.findViewById(R.id.name);
                TextView des=h.itemView.findViewById(R.id.des);
                LinearLayout layout=h.itemView.findViewById(R.id.expertLayout);
                ImageView isSelected=h.itemView.findViewById(R.id.selectedImg);
                ImageView expertImg=h.itemView.findViewById(R.id.expertImg);


                picasso.load(expertAIData.get(i).getIcon()).into(expertImg);

                name.setText(expertAIData.get(i).getName());
                des.setText(expertAIData.get(i).getDes());

                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (isMember<1)
                        {
                            expertDia.dismiss();
                            showCreditDialog();
                            return;
                        }
                        expertDia.dismiss();
                        currentAiExpertIndex=i;
                        //topAiName.setText(expertAIData.get(i).getName());
                        setAllArrayData(1);


                        //Picasso.get().load(expertAIData.get(currentAI).getIcon()).into(changeAiImg);
                    }
                });

                if (currentAiExpertIndex==i)
                {
                    layout.setBackgroundColor(a.getColor(R.color.colorPrimary));
                    name.setTextColor(a.getColor(R.color.white));
                }
                else
                {
                    name.setTextColor(a.getColor(R.color.colorPrimary));
                    layout.setBackgroundColor(a.getColor(R.color.primary_light));

                }





            }

            @Override
            public int getItemCount() {
                return expertAIData.size();
            }
        };
    }


    void showTopintroDialog()
    {
        PackageInfo packageInfo;
        int versionCode;
        try {
            packageInfo = c.getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }


       if (sp.getInt("appVersion",0)==versionCode)
       {
           return;
       }

       sp.edit().putInt("appVersion",versionCode).apply();
       BottomSheetDialog introDialog=new BottomSheetDialog(getContext(),R.style.CustomBottomSheetDialogTheme);
       View view=getLayoutInflater().inflate(R.layout.features_bottom_dialog,null,false);
       view.findViewById(R.id.getStarted).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               introDialog.dismiss();
           }
       });


       introDialog.setContentView(view);
       introDialog.show();

    }


    void showSnackBar(String s,int i)
    {
        if (Objects.isNull(c))
        {
            return;
        }
        int color=R.color.snakbar_bg;
        if (i==1)
        {
            color=R.color.red;
        }

        Snackbar snackbar= Snackbar.make(v.findViewById(R.id.chatLV), s, Snackbar.LENGTH_SHORT)
                .setTextColor(ContextCompat.getColor(c, R.color.white))
                .setBackgroundTint(ContextCompat.getColor(c, color));

        snackbar.setAnchorView(v.findViewById(R.id.messageView)).show();



    }
    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) a.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = a.getCurrentFocus();
        if (view == null) {
            view = new View(getContext());
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    BroadcastReceiver receiver;
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            c.unregisterReceiver(receiver);
        }catch (Exception e)
        {

            Log.e("e",e.getMessage());
        }
    }
    public void setTimeAgo(TextView timeAgo, String time)
    {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a d/M/yy");

        Date d1 = null;
        Date d2 = null;
        try {
            d1 = sdf.parse(time);
            d2 = sdf.parse(sdf.format(new Date()));
        } catch (ParseException e) {
            Log.e("time", e.getMessage());
        }

        long diff = 0;
        if (d2 != null) {
            diff = d2.getTime() - d1.getTime();
        }

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (days < 9 && days > 6) {
            timeAgo.setText("A week ago");
        } else if (days >= 9) {
            timeAgo.setText(time);
        } else if (days <= 6 && days > 0) {
            timeAgo.setText("" + days + " day(s) ago");
        } else if (hours > 0) {
            long min = hours * 60;

            if (minutes > min)
            {
                long MinDiff = minutes - min;
                timeAgo.setText("" + hours + " h " + MinDiff + "min ago");
            } else {
                timeAgo.setText("" + hours + " h ago");
            }
        } else if (minutes > 0) {
            long sec = minutes * 60;

            if (seconds > sec) {
                long secDiff = seconds - sec;
                timeAgo.setText("" + minutes + " min " + secDiff + "sec ago");
            } else {
                timeAgo.setText("" + minutes + " min ago");
            }
        } else if (seconds > 0) {
            timeAgo.setText("Few moments ago");
        }

        Log.i("timeago", "" + hours + " " + minutes);
    }

    public static void startWithAnimation(Activity startingActivity) {
        Intent intent = new Intent(startingActivity, AIToolsActivity.class);
        startingActivity.startActivity(intent);
        startingActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    class HistoryList{

        String topic;
        int id;
        int versionIndex;
        String date;

        public HistoryList(String topic, int id, String date,int versionIndex) {
            this.topic = topic;
            this.id = id;
            this.versionIndex = versionIndex;
            this.date = date;
        }
    }

    public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        private List<String> suggestions;

        public MyRecyclerViewAdapter(List<String> suggestions) {
            this.suggestions = suggestions;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.auto_complete_recycle, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            int i= holder.getAdapterPosition();

            String suggestion = suggestions.get(i);
            holder.suggestionText.setText(suggestion);
            holder.paste_auto_suggestion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    messageBox.setText(suggestion);
                }
            });
            holder.suggestionLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    messageBox.setText("");
                    sendButtonOnClick(suggestion);
                }
            });
        }

        @Override
        public int getItemCount() {
            return suggestions.size();
        }

        public  class ViewHolder extends RecyclerView.ViewHolder {

            public TextView suggestionText;
            ImageView paste_auto_suggestion;
            LinearLayout suggestionLayout;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                suggestionText = itemView.findViewById(R.id.suggestionText);
                paste_auto_suggestion = itemView.findViewById(R.id.paste_auto_suggestion);
                suggestionLayout = itemView.findViewById(R.id.suggestionLayout);
            }
        }
    }
}