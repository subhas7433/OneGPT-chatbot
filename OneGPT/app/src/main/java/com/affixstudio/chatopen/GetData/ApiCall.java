package com.affixstudio.chatopen.GetData;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
public class ApiCall {

    ApiResponse sendResponse;
    String url;
    Context c;

    public ApiCall(ApiResponse sendResponse, String url, Context c) {
        this.sendResponse = sendResponse;
        this.url = url;
        this.c = c;
    }



    public void get()
    {

        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

              //  pd.dismiss();
                sendResponse.response(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               // Toast.makeText(c, c.getString(R.string.someThingWorngTEXT), Toast.LENGTH_LONG).show();

                sendResponse.response("100");
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy());
        RequestQueue queue= Volley.newRequestQueue(c);
        queue.add(stringRequest);

    }
}
