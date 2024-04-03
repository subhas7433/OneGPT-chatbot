package com.affixstudio.chatopen.Dialog;

import static com.affixstudio.chatopen.ai.StartActivity.modelLists;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.affixstudio.chatopen.ai.R;
import com.affixstudio.chatopen.model.ModelList;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AiModelDialog {

    Activity a;
    GetModelData getModelData;
    int selectionIndex=0;
    boolean isFromHomeScreen;

    public AiModelDialog(Activity a,GetModelData getModelData,int selectionIndex,boolean isFromHomeScreen) {
        this.a = a;
        this.getModelData = getModelData;
        this.selectionIndex = selectionIndex;
        this.isFromHomeScreen = isFromHomeScreen;

    }
    BottomSheetDialog aiModelDialog;





    public BottomSheetDialog prepare()
    {
        aiModelDialog=new BottomSheetDialog(a, R.style.CustomBottomSheetDialogTheme);
        View v=a.getLayoutInflater().inflate(R.layout.ai_model_bottom_dialog,null);
        RecyclerView modelRecycle=v.findViewById(R.id.modelRecycle);
        modelRecycle.setAdapter(getAdapter());

        aiModelDialog.setContentView(v);
        return aiModelDialog;
    }



    private RecyclerView.Adapter getAdapter() {
        return new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(View.inflate(parent.getContext(),R.layout.ai_model_recycle,null)) {
                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position)
            {
                int i=h.getAdapterPosition();


                TextView modelName=h.itemView.findViewById(R.id.aiModelTitle);
                TextView aiDes=h.itemView.findViewById(R.id.aiDes);
                CircleImageView aiImage=h.itemView.findViewById(R.id.aiImage);
                TextView modelSelected=h.itemView.findViewById(R.id.modelSelected);
                CircularProgressBar progress1=h.itemView.findViewById(R.id.progress1);
                CircularProgressBar progress2=h.itemView.findViewById(R.id.progress2);
                CircularProgressBar progress3=h.itemView.findViewById(R.id.progress3);
                LinearLayout aiModelLayout=h.itemView.findViewById(R.id.aiModelLayout);


                if (!isFromHomeScreen && modelLists.get(i).getId()==12)// if calling from ai tools chat screen , hide the plug in model
                {
                    aiModelLayout.setVisibility(View.GONE);
                    return;
                }


                modelName.setText(modelLists.get(i).getModel_name());
                aiDes.setText(modelLists.get(i).getDescription());

                String[] progress=modelLists.get(i).getSpecGraph().split(",");


                progress1.setProgress(Float.parseFloat(progress[0]));
                progress2.setProgress(Float.parseFloat(progress[1]));
                progress3.setProgress(Float.parseFloat(progress[2]));

                Picasso.get().load(modelLists.get(i).getIconUrl()).into(aiImage);

                if (selectionIndex==modelLists.get(i).getId())
                {
                    aiModelLayout.setBackgroundResource(R.drawable.custom_outline_bg);
                   // modelSelected.setVisibility(View.VISIBLE);
                }else {
                    aiModelLayout.setBackgroundResource(R.color.gray_bg);
                   // modelSelected.setVisibility(View.GONE);
                }

                if (modelLists.get(i).getIsPremium()==1)
                {
                    h.itemView.findViewById(R.id.premium).setVisibility(View.VISIBLE);
                }
                else
                {
                    h.itemView.findViewById(R.id.premium).setVisibility(View.GONE);
                }


                aiModelLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getModelData.modelData(modelLists.get(i).getModel_name(),i);
                        aiModelLayout.setBackgroundResource(R.drawable.custom_outline_bg);
                        modelSelected.setVisibility(View.VISIBLE);
                        aiModelDialog.dismiss();
                    }
                });


            }

            @Override
            public int getItemCount() {
                return modelLists.size();
            }
        };
    }


}
