package com.applex.imagecompressor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CompressActivity extends AppCompatActivity implements SelectPhotoDialog.OnPhotoSelectedListener  {

    public static final int REQUEST_CODE = 1;
    ImageView img;
    private Bitmap mSelectedBitmap;
    private Uri mSelectedUri;
    Button button;
    private byte[] mCompressBitmap;

    byte[] pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress);

        verifyPermissions();

        button = findViewById(R.id.button1);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bmp = bmap();
                ByteArrayOutputStream outpput = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outpput);
                pic = outpput.toByteArray();
                new ImageCompressor().execute();
            }
        });

        img = findViewById(R.id.imageView1);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bmp = bmap();
                ByteArrayOutputStream outpput = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outpput);
                pic = outpput.toByteArray();
                Intent intent2 = new Intent(CompressActivity.this, ImageActivity.class);
                intent2.putExtra("BitmapByteArray",new ImageCompressor().doInBackground());
                startActivity(intent2);
            }
        });
    }

    private class ImageCompressor extends AsyncTask<Void, Void, byte[]> {

        private static final float maxHeight = 1280.0f;
        private static final float maxWidth = 1280.0f;
        byte[] actualPic =  pic;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected byte[] doInBackground(Void... strings) {
            Bitmap scaledBitmap = null;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeByteArray(pic, 0, actualPic.length, options);

            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;

            float imgRatio = (float) actualWidth / (float) actualHeight;
            float maxRatio = maxWidth / maxHeight;

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;

                }
            }

            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];

            try {
                bmp = BitmapFactory.decodeByteArray(pic, 0, actualPic.length, options);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();

            }
            try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

            if(bmp!=null)
            {
                bmp.recycle();
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            byte[] by = out.toByteArray();
            mCompressBitmap = by;
            return by;
        }

        @Override
        protected void onPostExecute(byte[] pic) {
            Toast.makeText(getApplicationContext(), ""+(pic.length/1024), Toast.LENGTH_LONG).show();

        }

        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int heightRatio = Math.round((float) height / (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }
            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }

            return inSampleSize;
        }

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

    private byte[] returnByteArray(Bitmap bt) {
        ByteArrayOutputStream outpput = new ByteArrayOutputStream();
        bt.compress(Bitmap.CompressFormat.JPEG, 100, outpput);
        byte[] pict = outpput.toByteArray();
        return pict;
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



    @Override
    public void getImagePath(Uri imagePath) {
        mSelectedBitmap = null;
        mSelectedUri = imagePath;
        Display display = getWindowManager().getDefaultDisplay();
        if(bmap().getHeight() > bmap().getWidth()) {
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
}

