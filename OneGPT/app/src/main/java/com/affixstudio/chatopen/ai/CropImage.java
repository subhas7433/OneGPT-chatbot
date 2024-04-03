package com.affixstudio.chatopen.ai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class CropImage extends AppCompatActivity {

    ImageView imageView;
    TextView cropButton;
    TextView submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);


        imageView = findViewById(R.id.imageView);
        cropButton = findViewById(R.id.crop);
        submit = findViewById(R.id.downloadBtn);

        // Set the image you want to display and crop
       // imageView.setImageResource(R.drawable.app_logo_transparent);
        Uri imageUriString =  Uri.parse(getIntent().getStringExtra("b"));; // Replace 'DataType' with the actual data type and 'Type' with the corresponding type.

        if (imageUriString != null) {
           // Uri imageUri = Uri.parse(imageUriString);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUriString);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (bitmap != null) {

                imageView.setImageBitmap(bitmap);
                // Use the bitmap for cropping
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.i("chat","Capture image uri is null in crop activity");
            Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show();
        }
    }
}