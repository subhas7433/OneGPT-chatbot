package com.affixstudio.chatopen.ai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GenerateImageActivity extends AppCompatActivity {
    private  final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    Bitmap bitmap;
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_image);
        ImageView imageView=findViewById(R.id.artImageView);
        TextView promptView=findViewById(R.id.textView2);






        imageUri = getIntent().getParcelableExtra("i");
        bitmap = getBitmapFromUri(imageUri);
        if (imageUri != null) {
            imageView.setImageURI(imageUri);
        }
        promptView.setText(getIntent().getStringExtra("p"));

        findViewById(R.id.crop).setOnClickListener(view -> {

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share Image"));

        });
        findViewById(R.id.downloadBtn).setOnClickListener(view -> {
            // Inside the onCreate() method of your activity or fragment
            // saveImage();
            if ((ContextCompat.checkSelfPermission(GenerateImageActivity.this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED )&& (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q))
            {
                ActivityCompat.requestPermissions(GenerateImageActivity.this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                saveImage(); // Method that saves the image to the gallery
            }


        });

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });
    }
    private void back() {
        File file=new File(imageUri.getPath());
        if (file.exists())
        {
            file.delete();
        }
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void saveImage() {

        // Inside onResponse() method of the ImageRequest callback
        ContentResolver resolver = getContentResolver();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "image_" + timeStamp + ".png";

// Save the Bitmap to a file
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            OutputStream outputStream = resolver.openOutputStream(uri);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image to gallery", Toast.LENGTH_SHORT).show();
        }

    }


    public Bitmap getBitmapFromUri(Uri uri) {
        try {
            Bitmap bitmap = null;
            InputStream input = getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(input, null, options);
            input.close();
            return bitmap;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    // Inside the onRequestPermissionsResult() method of your activity or fragment
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImage(); // Method that saves the image to the gallery
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {

    }
    public void startActivityWithAnimation(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}