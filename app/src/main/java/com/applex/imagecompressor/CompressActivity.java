package com.applex.imagecompressor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CompressActivity extends AppCompatActivity implements SelectPhotoDialog.OnPhotoSelectedListener  {

    public static final int REQUEST_CODE = 1;
    ImageView img;
    private Bitmap mSelectedBitmap;
    private Uri mSelectedUri;
    private int quality;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress);

        verifyPermissions();
        img = findViewById(R.id.imageView1);

        button = findViewById(R.id.button1);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                saveImage(bmap());
            }
        });
    }

    @Override
    public void getImagePath(Uri imagePath) {
        mSelectedBitmap = null;
        mSelectedUri = imagePath;
        Bitmap bmap = bmap();
        int h = bmap.getHeight();
        int w = bmap.getWidth();
        Display display = getWindowManager().getDefaultDisplay();
        if(h > w) {
            Picasso.get()
                    .load(imagePath.toString())
                    .resize(img.getWidth(), (int) (0.75*(display.getHeight())))
                    .centerCrop()
                    .into(img);
        } else {
            Picasso.get()
                    .load(imagePath.toString())
                    .fit()
                    .centerInside()
                    .into(img);
        }
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        img.setImageBitmap(bitmap);
        mSelectedBitmap = bitmap;
        mSelectedUri = null;
    }


    public void showFragment() {
        SelectPhotoDialog photoDialog = new SelectPhotoDialog();
        photoDialog.show(getSupportFragmentManager(), "selectPhoto");
    }



    private Bitmap bmap() {
        if (mSelectedBitmap == null) {
            Bitmap bMap  = null;
            try {
                bMap = MediaStore.Images.Media.getBitmap(CompressActivity.this.getContentResolver(), mSelectedUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bMap;
        } else {
            return mSelectedBitmap;
        }
    }

    private long predictSize(Bitmap sBitmap) {
        ByteArrayOutputStream ou = new ByteArrayOutputStream();
        sBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ou);
        byte[] imageInByte = ou.toByteArray();
        long lengthbyte = imageInByte.length;
        long length = lengthbyte/1024;
        return length;
    }

    private Bitmap iterationCompress(Bitmap img) {
        long size = predictSize(img);
        while(size > 200) {
            ByteArrayOutputStream ou = new ByteArrayOutputStream();
            img.compress(Bitmap.CompressFormat.JPEG, 75, ou);
            size = predictSize(img);
        }
        Toast.makeText(getApplicationContext(), ""+size, Toast.LENGTH_LONG).show();
        return img;
    }

    private void saveImage(Bitmap finalBitmap) {
        Toast.makeText(getApplicationContext(), "STarting", Toast.LENGTH_LONG).show();
        long size = predictSize(finalBitmap);
        Toast.makeText(getApplicationContext(), ""+size, Toast.LENGTH_LONG).show();

//        finalBitmap = iterationCompress(finalBitmap);
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        File file = new File (myDir, "Image01.jpg");
        if (file.exists ())
            file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
//          FIRST LOGIC:
//            while (true) {
//                if(predictSize(finalBitmap) <= 200) {
//                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                    break;
//                }
//                else {
//                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
//                }
//            }
//          SECOND LOGIC:
//            while(predictSize(finalBitmap) > 200) {
//                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
//            }
//            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_LONG).show();
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(CompressActivity.this, "Compressed and Downloaded", Toast.LENGTH_SHORT).show();
    }

    private int getQuality() {
        long size = predictSize(bmap());
        if (size <= 200) {
            quality = 100;
        } else if (size > 200 && size <= 500) {
            quality = 75;
        } else if (size > 500 && size <= 900) {
            quality = 60;
        } else if (size > 900 && size <= 1200) {
            quality = 50;
        } else if (size > 1200 && size <= 2000) {
            quality = 40;
        } else if (size > 2000 && size <= 3000) {
            quality = 35;
        } else if (size > 3000 && size <= 4000) {
            quality = 30;
        } else if (size > 4000 && size <= 5000) {
            quality = 25;
        } else if (size > 5000 && size <= 6000) {
            quality = 20;
        } else if (size > 6000 && size <= 7000) {
            quality = 15;
        } else if (size > 7000 && size <= 8000) {
            quality = 10;
        } else if (size > 8000) {
            quality = 5;
        }
        while (size < 150) {

        }
        return quality;
    }

    private void verifyPermissions() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED){
            showFragment();
        } else {
            ActivityCompat.requestPermissions(CompressActivity.this, permissions, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        verifyPermissions();
    }
}
