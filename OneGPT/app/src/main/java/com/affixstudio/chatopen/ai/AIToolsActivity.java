package com.affixstudio.chatopen.ai;

import static com.affixstudio.chatopen.ai.MainActivity.toolsList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.affixstudio.chatopen.GetData.AssistantData;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AIToolsActivity extends AppCompatActivity {

    RecyclerView toolRecycle;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_tools);



        toolRecycle=findViewById(R.id.toolRecycle);
        toolRecycle.setLayoutManager(new GridLayoutManager(this,2));
        toolRecycle.setAdapter(getAdapter());

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });



    }

    private RecyclerView.Adapter getAdapter()
    {

        return new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RecyclerView.ViewHolder(View.inflate(parent.getContext(),R.layout.ai_tools_list_recycle,null)) {
                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {

                int i=h.getAdapterPosition();

                AssistantData toolData=toolsList.get(i);

                TextView toolName=h.itemView.findViewById(R.id.toolName);
                TextView toolDescription=h.itemView.findViewById(R.id.toolDescription);
                ImageView toolIcon=h.itemView.findViewById(R.id.toolIcon);
                LinearLayout toolVA=h.itemView.findViewById(R.id.toolVA);


                toolName.setText(toolData.getTool_name());
                toolDescription.setText(toolData.getTool_description());

                Picasso.get().load(toolData.getTool_icon()).into(toolIcon);

                toolVA.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(AIToolsActivity.this, AIToolsChatActivity.class).putExtra("i", i);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                });

            }

            @Override
            public int getItemCount() {
                return toolsList.size();
            }
        };
    }
}