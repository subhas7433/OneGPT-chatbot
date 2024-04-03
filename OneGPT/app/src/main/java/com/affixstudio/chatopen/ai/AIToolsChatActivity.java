package com.affixstudio.chatopen.ai;

import static com.affixstudio.chatopen.ai.ChatFragment.premiumCredit;
import static com.affixstudio.chatopen.ai.MainActivity.toolsList;
import static com.affixstudio.chatopen.ai.StartActivity.isMember;
import static com.affixstudio.chatopen.ai.StartActivity.mSocket;
import static com.affixstudio.chatopen.ai.StartActivity.modelLists;
import static com.affixstudio.chatopen.ai.StartActivity.planName;
import static com.affixstudio.chatopen.ai.StartActivity.sp;
import static com.affixstudio.chatopen.ai.StartActivity.totalCredit;
import static com.affixstudio.chatopen.ai.login.getUserID;
import static com.affixstudio.chatopen.model.ChatMessage.getCurrentTime;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.affixstudio.chatopen.Dialog.AiModelDialog;
import com.affixstudio.chatopen.Dialog.GetModelData;
import com.affixstudio.chatopen.GetData.AssistantData;
import com.affixstudio.chatopen.model.Database;
import com.affixstudio.chatopen.model.ModelList;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class AIToolsChatActivity extends AppCompatActivity {



    private static final int PERMISSIONS_REQUEST_CAMERA = 23;
    private static final int REQUEST_IMAGE_PICK = 33;
    private static final int PERMISSIONS_REQUEST_STORAGE = 44;
    private static final int REQUEST_IMAGE_CAPTURE = 55;
    ActivityResultLauncher<Intent> mSpeechActivityResultLauncher;

    EditText queryBox;
    Context c;
    private AiModelDialog modelDialog;
    private int modelID=1;
    TextView credit;
    private Socket mSocket;
    private boolean notReceivedYet=false;
    private int currentHistoryId=0;
    {
        try {
            mSocket = IO.socket(getString(R.string.tools_socket_endpoint));
        } catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }
    String s=""; // query by the user to save in db and other uses
    AssistantData data;
    int toolIndex=0;
    Database db;
    TextView resultText;

  //  ArrayList<ModelList> toolsModel; // the plugin model is removed from tool model list


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_tools_chat);


        c=AIToolsChatActivity.this;
        Intent i = getIntent();
        toolIndex=i.getIntExtra("i",0);
        data = toolsList.get(toolIndex);

        TextView toolNameHead=findViewById(R.id.toolNameHead);
        TextView wordCount=findViewById(R.id.wordCount);
        resultText=findViewById(R.id.resultText);
        TextView showAIVersion=findViewById(R.id.showAIVersion);
        credit=findViewById(R.id.credit);

        queryBox=findViewById(R.id.queryBox);

        Button generateBTN=findViewById(R.id.generateBTN);

        ImageView toolsHistory=findViewById(R.id.toolsHistory);
        ImageView mic=findViewById(R.id.mic);
        ImageView pickImage=findViewById(R.id.pickImage);
        ImageView viewFull=findViewById(R.id.viewFull);
        ImageView copy=findViewById(R.id.copy);
        ImageView share=findViewById(R.id.share);
        ImageView aiToolLogo=findViewById(R.id.aiToolLogo);

        Picasso.get().load(data.getTool_icon()).into(aiToolLogo);

        LottieAnimationView chatLoadingLottie=findViewById(R.id.chatLoadingLottie);


        //setModelList();

        modelID=sp.getInt(getString(R.string.ai_task_model_selected_sp),1); // get the previous selected model

        String query="CREATE TABLE IF NOT EXISTS "+getString(R.string.tool_history)+ " "+getString(R.string.ToolHistorytableCreationSql);


        db=new Database(this,getString(R.string.tool_history),query,1);

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i("Start","Socket.IO Connected!");
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

        mSocket.connect();

        final boolean[] isFull = {false};
        viewFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFull[0])
                {
                    findViewById(R.id.queryLayout).setVisibility(View.VISIBLE);
                    isFull[0] =false;
                }
                else
                {
                    findViewById(R.id.queryLayout).setVisibility(View.GONE);
                    isFull[0] =true;
                }
            }
        });
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s=resultText.getText().toString();
                if (s.isEmpty())
                {
                    showSnackBar("No result to copy",1);
                    return;
                }
                ClipboardManager clipboardManager=(ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);

                ClipData clipData= ClipData.newPlainText("Text",s);
                clipboardManager.setPrimaryClip(clipData);

                showSnackBar("Copied",0);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s= resultText.getText().toString();
                if (s.isEmpty())
                {
                    showSnackBar("No result to share",1);
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_SEND);

                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Share using");
                intent.putExtra(Intent.EXTRA_TEXT, s);
                startActivity(intent);
            }
        });



        toolNameHead.setText(data.getTool_name());

        queryBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Update the word count text view
                wordCount.setText((i+1)+"/"+getModelById(modelID).getInputLimit());

                if (queryBox.getText().toString().isEmpty() ) // if the query is empty
                {
                    generateBTN.setBackgroundColor(ContextCompat.getColor(AIToolsChatActivity.this,R.color.inactive_mid_black));

                }else {
                    generateBTN.setBackgroundColor(ContextCompat.getColor(AIToolsChatActivity.this,R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Speech to text
        mSpeechActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        if (intent != null) {
                            ArrayList<String> resultArray = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                            if (resultArray != null && !resultArray.isEmpty()) {
                                String spokenText = resultArray.get(0);
                                queryBox.setText(queryBox.getText().toString()+" "+spokenText); // add the spoken text to the query box

                            }
                        }
                    }
                });


        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSpeechRecognition();
            }
        });

        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImgPickDialog();
            }
        });






       findViewById(R.id.aiModelSpinner).setOnClickListener(view -> { // show the ai models dialog


           AiModelDialog modelDialog=new AiModelDialog(this, new GetModelData() {
               @Override
               public void modelData(String modelName, int index)
               {

                   modelID=modelLists.get(index).getId(); // get the selected model id
                   sp.edit().putInt(getString(R.string.ai_task_model_selected_sp),modelID).apply(); // save the selected model id in shared preferences for future use
                   showCredit(); // update the credit accordingly
                   wordCount.setText(queryBox.getText().toString().length()+"/"+modelLists.get(index).getInputLimit()); // update the word count text view
                   showAIVersion.setText(modelName); // show the selected model name
                   queryBox.setText(queryBox.getText().toString()); // update the query box with the previous query



               }
           },modelID,false);

           BottomSheetDialog aiModelDialog=modelDialog.prepare();
           aiModelDialog.show();

        });
       generateBTN.setOnClickListener(view -> {
            s=queryBox.getText().toString();
           if (!s.isEmpty())
           {
               hideKeyboard();
               if (getModelById(modelID).getIsPremium()==1)
               {
                   if (premiumCredit>0)
                   {
                       chatLoadingLottie.setVisibility(View.VISIBLE);
                       resultText.setText("");
                       mSocket.emit("client_message",modelID,getMessage(s),sp.getString("email","email"),getUserID(c));

                   }else {
                       i("called showCreditDialog in premium generate");
                       showCreditDialog();
                   }
               }
               else
               {


                   if (totalCredit > 0 || isMember==1)
                   {

                       resultText.setText("");
                       chatLoadingLottie.setVisibility(View.VISIBLE);
                       mSocket.emit("client_message",modelID,getMessage(s),sp.getString("email","email"),getUserID(c));



                   } else if (totalCredit < 1 && isMember==0 && premiumCredit>0 )
                   {
                       new AlertDialog.Builder(c)
                               .setCancelable(true)
                               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialogInterface, int i) {
                                       resultText.setText("");
                                       chatLoadingLottie.setVisibility(View.VISIBLE);
                                       mSocket.emit("client_message",modelID,getMessage(s),sp.getString("email","email"),getUserID(c));

                                   }
                               }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialogInterface, int i) {

                                   }
                               }).setMessage("You don't have normal credit but have "+premiumCredit+" premium credit. Do you want to use them for this query?")
                               .setTitle("Use Premium Credit")
                               .show();
                   }else {
                       showCreditDialog();
                   }



               }


           }
           else if (s.length() > getModelById(modelID).getInputLimit())
           {
               showSnackBar("You can only input "+getModelById(modelID).getInputLimit()+" characters for this model",1);
           }

        });

        mSocket.on("server_message", new Emitter.Listener() {
            @Override
            public void call(Object... args)
            {
                i("message received "+args[0]);
                notReceivedYet=false;

                String response= (String) args[0];

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chatLoadingLottie.setVisibility(View.GONE);
                        resultText.setText(response);
                    }
                });

                if (args.length==1)
                {
                    db.insertDataToolHistory(s,getCurrentTime(),response,modelID,toolIndex);

                    getAllHistoryData(); // refresh the history
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



        showCredit();

        getAllHistoryData();
        toolsHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setHistoryDialog();
            }
        });
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        showCredit();
        ModelList model=getModelById(modelID);
        wordCount.setText(queryBox.getText().toString().length()+"/"+model.getInputLimit());
        showAIVersion.setText(model.getModel_name());// show the previous selected model name
    }




    private ModelList getModelById(int modelID) {
        for (ModelList model : modelLists) {
            if (model.getId() == modelID)
            {
                return model; // Found the matching object
            }
        }
        return modelLists.get(0);
    }
    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    BottomSheetDialog historyDialog;
    private void setHistoryDialog()
    {

        historyDialog=new BottomSheetDialog(c,R.style.CustomBottomSheetDialogTheme);
        View v=getLayoutInflater().inflate(R.layout.history_bottom_dialog,null);

        TextView noHistory=v.findViewById(R.id.noHistory);
        v.findViewById(R.id.newQuery).setVisibility(View.GONE);

        RecyclerView chatRow=v.findViewById(R.id.chatRecycle);
        chatRow.setAdapter(ToolHistoryAdapter());

        if (toolHistoryData.size()<1)
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

    private RecyclerView.Adapter ToolHistoryAdapter() {

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
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position)
            {
                int i=(toolHistoryData.size()-1)-h.getAdapterPosition();


               // TextView date=h.itemView.findViewById(R.id.date);

                TextView title = h.itemView.findViewById(R.id.title);
                TextView version = h.itemView.findViewById(R.id.version);
                String topic = toolHistoryData.get(i).Query;

                // Capitalize the first character
                String capitalizedTopic = topic.substring(0, 1).toUpperCase() + topic.substring(1);

                title.setText(capitalizedTopic);


                //  date.setText(hList.get(i).date);

                h.itemView.findViewById(R.id.historyRowLA).setOnClickListener( view -> {

                    setLayout(i);

                    showCredit();
                    currentHistoryId=toolHistoryData.get(i).Id;
                    historyDialog.dismiss();

                });


                version.setText(modelLists.get(toolHistoryData.get(i).aiIndex).getModel_name());
                h.itemView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        h.itemView.findViewById(R.id.historyRowLA).setVisibility(View.GONE);
                        Toast.makeText(c, "Deleted", Toast.LENGTH_SHORT).show();

                        toolHistoryData.remove(i);
                        db.getReadableDatabase().execSQL("DELETE FROM "+getString(R.string.tool_history)+" WHERE _id="+toolHistoryData.get(i).Id);
                        if (currentHistoryId==toolHistoryData.get(i).Id)
                        {
                            makeFieldEmpty();
                        }
                    }
                });

            }

            @Override
            public int getItemCount() {
                return toolHistoryData.size();
            }
        };
    }

    private void setLayout(int i) {
        queryBox.setText(toolHistoryData.get(i).Query);
        resultText.setText(toolHistoryData.get(i).Result);
    }

    private void makeFieldEmpty() {
        queryBox.setText("");
        resultText.setText("");

    }

    ArrayList<ToolHistoryData> toolHistoryData=new ArrayList<>();
    private void getAllHistoryData() {

        Cursor cursor=db.getInfo();
        toolHistoryData.clear();
        while (cursor.moveToNext())
        {

            if ( cursor.getInt(5) ==toolIndex)
            {
                ToolHistoryData historyData= new ToolHistoryData(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getInt(4),
                        cursor.getInt(5)
                );
                toolHistoryData.add(historyData);
            }


        }


    }

    private Object getMessage(String s) {
        JSONArray messagesArr = new JSONArray();
        try {

            messagesArr.put(new JSONObject().put("role", "user").put("content", data.getTool_prompt()));
            messagesArr.put(new JSONObject().put("role", "assistant").put("content","Yes."));
            messagesArr.put(new JSONObject().put("role", "user").put("content", s));

            i("Message object = "+messagesArr);

        } catch (JSONException e) {
            showSnackBar("Something went wrong, try again. If this problem persist please contact us.",1);
            throw new RuntimeException(e);
        }
        return messagesArr.toString();
    }

    private void showCredit()  // setting credit accordingly model
    {
        if (getModelById(modelID).getIsPremium()==1)
        {
            credit.setText(premiumCredit+" "+getString(R.string.premium_credit));
            credit.setBackgroundColor(ContextCompat.getColor(this,R.color.premium_clr));
        }
        else
        {
            credit.setBackgroundColor(ContextCompat.getColor(this,R.color.green));
            if (isMember==1)
            {
                credit.setText(R.string.no_limit);
            }else {
                credit.setText(totalCredit+" "+getString(R.string.credit));
            }
        }
    }
    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

        try {
            mSpeechActivityResultLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    BottomSheetDialog imgPickDialog;
    void showImgPickDialog()
    {
        imgPickDialog=new BottomSheetDialog(this,R.style.CustomBottomSheetDialogTheme);
        View v=getLayoutInflater().inflate(R.layout.pickup_document_bottom_dialog,null);

        v.findViewById(R.id.cameraLA).setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(AIToolsChatActivity.this,android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AIToolsChatActivity.this,
                        new String[]{android.Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_CAMERA);
            } else {
                dispatchTakePictureIntent();
            }
        });

        v.findViewById(R.id.galaryLA).setOnClickListener(view -> {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pickImageFromGallery();
            }else if (ContextCompat.checkSelfPermission(AIToolsChatActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)

            {
                ActivityCompat.requestPermissions(AIToolsChatActivity.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_STORAGE);
            } else {
                pickImageFromGallery();
            }
        });




        imgPickDialog.setContentView(v);
        imgPickDialog.show();
    }
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(AIToolsChatActivity.this, "No gallery app available", Toast.LENGTH_SHORT).show();
        }
    }
    private Uri imageUri;
    private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(AIToolsChatActivity.this.getPackageManager()) != null) {
            File imageFile = new File(AIToolsChatActivity.this.getFilesDir(), "example_image.jpg");
            imageUri = FileProvider.getUriForFile(AIToolsChatActivity.this, getString(R.string.authorityName), imageFile);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(AIToolsChatActivity.this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(AIToolsChatActivity.this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(AIToolsChatActivity.this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("chat","Request code "+requestCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (imageUri != null) {
                Uri destinationUri = Uri.fromFile(new File(c.getCacheDir(), "cropped"));
                UCrop uCrop = UCrop.of(imageUri, destinationUri);

                uCrop.start(AIToolsChatActivity.this);

            } else {
                Log.i("chat","Capture image uri is null");
                Toast.makeText(c, "Image URI is null", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK)
        {
            if (data != null && data.getData() != null) {
                if (data.getData() != null) {
                    Uri destinationUri = Uri.fromFile(new File(c.getCacheDir(), "cropped"));
                    UCrop uCrop = UCrop.of(data.getData(), destinationUri);

                    uCrop.start(AIToolsChatActivity.this);

                } else {
                    Log.i("chat","Capture image uri is null");
                    Toast.makeText(c, "Image URI is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(c, "Failed to load image", Toast.LENGTH_SHORT).show();
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

        queryBox.setText(result);
        // Display the result in the TextView
        Log.i(this.toString(), "result "+result);
    }

    void i(String s)
    {
        Log.i("AITools",s);
    }
    @SuppressLint("SetTextI18n")

    public void response(String response)
    {
        //      processResponse(response);
        if (response.isBlank())
        {
            return;
        }

        Intent local=new Intent();

        local.setAction(getString(R.string.subIntentAction));
        sendBroadcast(local);

        notReceivedYet=false;
        Log.i(this.toString(),"response "+response);
        if (response.equals("100"))
        {

            showCreditDialog();
            //  showSnackBar("You have reached your daily limit.",1);
        }else if (response.equals("Error:affix"))
        {
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
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        else if (response.equals("200") || response.equals("404") ) // device id and email is not matching with database
        {

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
                            finishAffinity();
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

                premiumCredit=jo.getInt("premiumCredits");


                showCredit();


            }catch (Exception e)
            {


                showSnackBar("Something went wrong. Please try again",1);
                e.printStackTrace();
                Log.e("MainActivity","error "+e.getMessage());
            }
        }





    }
    void showSnackBar(String s,int i)
    {
        int color=R.color.snakbar_bg;
        if (i==1)
        {
            color=R.color.red;
        }

        Snackbar snackbar= Snackbar.make(findViewById(R.id.toolLayout), s, Snackbar.LENGTH_SHORT)
                .setTextColor(ContextCompat.getColor(c, R.color.white))
                .setBackgroundTint(ContextCompat.getColor(c, color));

        snackbar.show();



    }
    private void showCreditDialog()
    {
        Dialog dialog=new Dialog(c);

        dialog.getWindow().setBackgroundDrawable(c.getDrawable(R.drawable.custom_dialog_bg));

        View v= getLayoutInflater().inflate(R.layout.gain_credit_dialog,null);

        TextView creditS=v.findViewById(R.id.credits);
        Button addCredit=v.findViewById(R.id.addCredit);
        // Java
        TextView textView = v.findViewById(R.id.reachText);
        SpannableString spannableString = new SpannableString("You have reached your daily limit.");

        // Apply color to a specific word
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary));
        spannableString.setSpan(foregroundColorSpan, 22, 34, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply bold style to a specific word
        StyleSpan boldStyleSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldStyleSpan, 22, 34, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply background color to a specific word
//        BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(getResources().getColor(R.color.colorPrimary));
//        spannableString.setSpan(backgroundColorSpan, 22, 34, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);


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
    class ToolHistoryData
    {

        int Id;
        String Query;
        String Result;
        String Time;
        int aiIndex;
        int toolIndex;


        public ToolHistoryData(int id, String query, String result, String time, int aiIndex, int toolIndex) {
            Id = id;
            Query = query;
            Result = result;
            Time = time;
            this.aiIndex = aiIndex;
            this.toolIndex = toolIndex;
        }

        public int getId() {
            return Id;
        }

        public String getQuery() {
            return Query;
        }

        public String getResult() {
            return Result;
        }

        public String getTime() {
            return Time;
        }

        public int getAiIndex() {
            return aiIndex;
        }

        public int getToolIndex() {
            return toolIndex;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {

            if (mSocket.isActive())
            {
                mSocket.close();
            }
        }catch (Exception e)
        {

            Log.e("e",e.getMessage());
        }

    }
}