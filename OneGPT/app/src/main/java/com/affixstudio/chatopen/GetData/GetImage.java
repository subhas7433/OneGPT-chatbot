package com.affixstudio.chatopen.GetData;


import static com.affixstudio.chatopen.ai.StartActivity.apiKeys;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GetImage {

    public void generate(Context context,String prompt,String nagative,ReceiveImage send)
    {
        String url="https://image-diffusion.p.rapidapi.com/image/stable/diffusion?prompt="+prompt;
        if (!nagative.isEmpty())
        {
            url+="&negative_prompt="+nagative;
        }


        RequestQueue requestQueue = Volley.newRequestQueue(context);
    //    String url = "https://image-diffusion.p.rapidapi.com/image/stable/diffusion?prompt=jesus%20on%20the%20cross";
        ImageRequest imageRequest = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        // handle image response
                        send.image(response);
                    }
                }, 0, 0, ImageView.ScaleType.CENTER_CROP, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // handle error
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Random rand = new Random();
                int randomNum = rand.nextInt(apiKeys.size());
                Map<String, String> headers = new HashMap<>();
                headers.put("X-RapidAPI-Key", apiKeys.get(0));
                headers.put("X-RapidAPI-Host", "image-diffusion.p.rapidapi.com");
                return headers;
            }



            @Override
            public RetryPolicy getRetryPolicy() {
                return new DefaultRetryPolicy(
                        60000, // 60 seconds timeout
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            }
        };
        requestQueue.add(imageRequest);
    }

    public void get(ApiResponse sendResponse,Context c,String url)
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

                sendResponse.response("response");
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy());
        RequestQueue queue= Volley.newRequestQueue(c);
        queue.add(stringRequest);

    }


}
