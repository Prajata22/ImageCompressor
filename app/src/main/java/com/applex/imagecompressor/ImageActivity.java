package com.applex.imagecompressor;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ImageActivity extends AppCompatActivity {

    ImageView img;
    private Bitmap mSelectedBitmap;
    private byte[] getByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        img = findViewById(R.id.imageView3);
        getByteArray = getIntent().getByteArrayExtra("BitmapByteArray");
        mSelectedBitmap = BitmapFactory.decodeByteArray(getByteArray, 0, getByteArray.length);
        img.setImageBitmap(mSelectedBitmap);

        img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showFragment();
                return true;
            }
        });
    }
    public void showFragment() {
        Bundle b = new Bundle();
        b.putByteArray("image",getByteArray);
        DownloadPhotoDialog photoDialog = new DownloadPhotoDialog();
        photoDialog.show(getSupportFragmentManager(), "downloadPhoto");
        photoDialog.setArguments(b);
    }
}
