package com.affixstudio.chatopen.GetData;

import static com.affixstudio.chatopen.ai.StartActivity.mSocket;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.affixstudio.chatopen.ai.R;
import com.affixstudio.chatopen.model.Database;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.socket.emitter.Emitter;

public class PdfToText {


    Activity a;

    public PdfToText(Activity a) {
        this.a = a;
    }


    int isReceived=0;

    Database db;
    double[] embedding={};
    int pageNumber=0;
    String parsedText = "";
    String docName="";
    PDDocument document;
    SendPDFData sendPDFData;
    public void getText(Uri path,SendPDFData sendPDFData)
    {

        this.sendPDFData=sendPDFData;
        //(_id INTEGER PRIMARY KEY autoincrement, PdfName text DEFAULT 'Not set',PageNo text DEFAULT '1',Text text DEFAULT '0',Embedding BLOB )
        String embeddingQuery="CREATE TABLE IF NOT EXISTS "+a.getString(R.string.pdfEmbedingTable)+" "+a.getString(R.string.pdfEmbedingQueryEndPart);
        db=new Database(a,a.getString(R.string.pdfEmbedingTable),embeddingQuery,1);


        docName=getOriginalFileName(a,path);
        Cursor cursor=db.getWritableDatabase().rawQuery("Select * from " + a.getString(R.string.pdfEmbedingTable)+" where PdfName='"+docName+"'", null);

        if (!Objects.isNull(cursor))
        {
            if (cursor.getCount()>0)
            {
                getPdfDataFromDB(cursor);
            }

        }







        mSocket.on("embedding_input", new Emitter.Listener() {
            @Override
            public void call(Object... args)
            {
                if (isReceived==0) // some time its getting response twice
                {
                    isReceived=1;

                    if (args[0].toString().length()>10)
                    {
                        // convert the args to string array from json object
                        String[] strEmbedding=args[0].toString().replace("[","").replace("]","").split(",");;

                        // store the string array as double array
                         embedding = new double[strEmbedding.length];

                        // Parse and convert each value from string to double
                        for (int i = 0; i < strEmbedding.length; i++) {
                            embedding[i] = Double.parseDouble(strEmbedding[i].trim());
                        }
                        Log.i("PdfToText","embedding = "+args[0]);

                        // insert to db

                    }

                }

            }
        });

        execute=Executors.newSingleThreadExecutor();
        execute.execute(new Runnable() {
            @Override
            public void run() {



                PDFBoxResourceLoader.init(a);


                try {

                    parsedText = "";
                     document = null;

                    try {
                        InputStream inputStream=a.getContentResolver().openInputStream(path);
                        document = PDDocument.load(inputStream);
                    } catch(IOException e) {
                        Log.e("PdfBox-Android-Sample", "Exception thrown while loading document to strip", e);
                    }

                    getEmbeddingForPage();
                   

                } catch (Exception e)
                {
                    System.out.println(e);
                }
            }
        });

    }
    ExecutorService execute;
    int currentPageNo=0;
    private void getEmbeddingForPage() {

        execute.execute(new Runnable() {
            @Override
            public void run() {

                try {

                    PDFTextStripper pdfStripper = new PDFTextStripper();

                    for (int i=1;i<=document.getNumberOfPages();i++)
                    {
                        currentPageNo=i;
                        pdfStripper.setStartPage(i);
                        pdfStripper.setEndPage(i);

                        parsedText = pdfStripper.getText(document);
                        insertIntoDB(new double[]{});
                    }
                    System.out.println("Embedding Storing is finished");

                    Cursor cursor=db.getWritableDatabase().rawQuery("Select * from " + a.getString(R.string.pdfEmbedingTable)+" where PdfName='"+docName+"'", null);

                    getPdfDataFromDB(cursor);

                }
                catch (IOException e)
                {
                    Log.e("PdfBox-Android-Sample", "Exception thrown while stripping text", e);
                }

                System.out.println(parsedText);


            }
        });

    }

    private void insertIntoDB(double[] embedding)
    {

        ContentValues values = new ContentValues();
        values.put("PdfName", docName);
        values.put("PageNo", currentPageNo);
        values.put("Text", parsedText);
        //values.put("Embedding", embeddingBytes);
        db.getWritableDatabase().insert(a.getString(R.string.pdfEmbedingTable),null,values);

    }

    private void getPdfDataFromDB(Cursor cursor)
    {
        ArrayList<PdfData> pdfDatas=new ArrayList<>();

        int l=1;
        while (cursor.moveToNext())
        {
            if (cursor.getString(1).equals(docName))
            {
                PdfData pdfData=new PdfData(docName,l,cursor.getString(3),new double[]{});
                pdfDatas.add(pdfData);
            }
            l++;
        }

        sendPDFData.send(pdfDatas);

        try {
            if (document != null) document.close();
            if (db != null) db.close();
            cursor.close();
        }
        catch (IOException e)
        {
            Log.e("PdfBox-Android-Sample", "Exception thrown while closing document", e);
        } // todo set condition for choosing previous file again and get the data from db and return it to chat fragment

    }

    public  String getOriginalFileName(Context context, Uri uri) {
        String displayName = null;
        String scheme = uri.getScheme();

        if (scheme != null && scheme.equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
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


    public ArrayList<PdfData> getPdfData()
    {
        String embeddingQuery="CREATE TABLE IF NOT EXISTS "+a.getString(R.string.pdfEmbedingTable)+" "+a.getString(R.string.pdfEmbedingQueryEndPart);
        db=new Database(a,a.getString(R.string.pdfEmbedingTable),embeddingQuery,1);


        ArrayList<PdfData> pdfDatas=new ArrayList<>();
        Cursor cursor=db.getInfo();

        int l=1;
        while (cursor.moveToNext())
        {

                PdfData pdfData=new PdfData(cursor.getString(1),l,cursor.getString(3),new double[]{});
                pdfDatas.add(pdfData);

            l++;
        }


        try {

            cursor.close();
        }
        catch (Exception e)
        {
            Log.e("PdfBox-Android-Sample", "Exception thrown while closing document", e);
        } // todo set condition for choosing previous file again and get the data from db and return it to chat fragment

        return pdfDatas;
    }






}
