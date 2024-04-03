package com.affixstudio.chatopen.ai;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.affixstudio.chatopen.ai.StartActivity.sp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.affixstudio.chatopen.GetData.ApiResponse;
import com.affixstudio.chatopen.GetData.GetImage;
import com.affixstudio.chatopen.GetData.ReceiveImage;
import com.affixstudio.chatopen.model.CloseDialog;
import com.affixstudio.chatopen.model.TableData;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class ArtFragment extends Fragment {

    int loop=0;
    Context c;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_art,container, false);

        EditText promptText=v.findViewById(R.id.promptText);
        c=getContext();


        CloseDialog waitDialog=new CloseDialog();

        GetImage getImage=new GetImage();

        v.findViewById(R.id.generateBtn).setOnClickListener(view -> {

            if (promptText.getText().toString().isEmpty())
            {
                Toast.makeText(getContext(), "Enter a prompt", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.i("Main","started");

            Dialog loading=waitDialog.showLoading(getActivity());
            getImage.generate(getContext(), promptText.getText().toString(), "", new ReceiveImage() {
                @Override
                public void image(Bitmap bitmap) {
                    loading.dismiss();
                    try {
                        // Save the Bitmap to a file
                        File file = new File(getActivity().getCacheDir(), "image.png");
                        FileOutputStream fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.close();
                        Uri uri = FileProvider.getUriForFile(getContext(), "com.affixstudio.app.fileprovider", file);
                        // Pass the file URI through the Intent
                        Intent intent = new Intent(getActivity(), GenerateImageActivity.class);
                        intent.putExtra("i", uri);
                        // intent.putExtra("b", bitmap);
                        startActivity(intent.putExtra("p",promptText.getText().toString()));

                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Server is at it's peak. Please try later.", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    //startActivity(new Intent(getContext(),GenerateImageActivity.class).putExtra("i",bitmap).putExtra("p",promptText.getText().toString()));
                    //imageView.setImageBitmap(bitmap);
                }
            });

            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (loading.isShowing())
                    {
                        loading.dismiss();
                        Toast.makeText(getContext(), "Server is at it's pick. Please try later.", Toast.LENGTH_SHORT).show();
                    }

                }
            },35000);
        });

        ImageView clearTxt=v.findViewById(R.id.clearTxt);
        clearTxt.setOnClickListener(view -> {
            promptText.setText("");
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


        promptText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (!editable.toString().isEmpty())
                {
                    clearTxt.setVisibility(View.VISIBLE);
                }else {
                    clearTxt.setVisibility(View.GONE);
                }
            }
        });


        RecyclerView recyclerView=v.findViewById(R.id.recycleView); // todo set all api and app update and ads
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);

        getImage.get(new ApiResponse() {
            @Override
            public void response(String response) {
                Log.i("Home","response "+response);
                try {
                    JSONArray array=new JSONArray(response);

                    ArrayList<TableData> data=new ArrayList<>();
                    loop = Math.min(array.length(), 100);
                    for (int i=0;loop>i;i++){

                        String j=array.getString(i);
                        JSONObject jo = new JSONObject(j);
                        data.add(new TableData(jo.getInt("id"),jo.getString("url"),jo.getString("prompt")));

                    }
                    if (data.size()>0)
                    {
                        v.findViewById(R.id.pd).setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(new MyAdapter(data));
                        v.findViewById(R.id.scrollToBottom).setOnClickListener(view -> {
                            recyclerView.scrollToPosition(0);
                        });

                        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);
                            }

                            @Override
                            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);
                                // shouldScroll=false;
                                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
                                int totalItemCount = recyclerView.getAdapter().getItemCount();
                                if (lastVisibleItemPosition == 0) {
                                    // RecyclerView is at the end
                                    // Do something here
                                    v.findViewById(R.id.scrollToBottom).setVisibility(View.GONE);
                                } else {
                                    // RecyclerView is not at the end
                                    // Do something else here
                                    v.findViewById(R.id.scrollToBottom).setVisibility(View.GONE);
                                }
                            }
                        });
                    }



                }catch (Exception e)
                {
                    e.printStackTrace();
                    Log.e("MainActivity","response "+e.getMessage());
                }

            }
        },getContext(),getString(R.string.Ai_Images_endpoints));

        v.findViewById(R.id.menuIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseDialog drawer=new CloseDialog();
                drawer.openDrawer(getActivity());
            }
        });

        return v;
    }
    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private ArrayList<TableData> data;
        private Picasso picasso;

        MyAdapter(ArrayList<TableData> data) {
            this.data = data;
            picasso = Picasso.get();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.art_imageview_recycle, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Random rand = new Random();
            int randomNum = rand.nextInt(loop);
            TableData data1 = data.get(randomNum);
            TextView prompt=holder.itemView.findViewById(R.id.textView);
            prompt.setText(data1.getPrompt());

            picasso.load(data1.getUrl()).into(holder.imageView);
           // holder.itemView.findViewById(R.id.artImageView).startAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.recycle_gallery));
            holder.itemView.findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void onClick(View view) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("label", data1.getPrompt());
                            clipboard.setPrimaryClip(clip);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Toast.makeText(getContext(), "Copied", Toast.LENGTH_SHORT).show();
                        }
                    }.execute();
                }
            });
            // Apply animation to each item
            Animation slideIn = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.slide_in_right);
            holder.itemView.startAnimation(slideIn);
        }

        @Override
        public int getItemCount() {
            return loop;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            MyViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.artImageView);
            }
        }

    }

}