package com.example.vtg_stech20;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.graphics.Matrix;
import android.Manifest;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import static android.os.Environment.getExternalStoragePublicDirectory;

import org.tensorflow.lite.Interpreter;


public class MainActivity extends AppCompatActivity {
    Button button;
    //ImageView imageView;
    String pathToFile;
    Interpreter tflite;
    float[][][][] changedim;
    Button predict, path;

    File picFile;
    ImageView img;
    float[][] outputval;
    HashMap<Integer, String> labels = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        labels.put(0, "Achyutaraya temple");
        labels.put(1, "Aghorashewara temple");
        labels.put(2, "Baharampur");
        labels.put(3, "Bahubali Gomateswara temple");
        labels.put(4, "Chandikeshwara temple");
        labels.put(5, "Channakeshava temple");
        labels.put(6, "Chennakeshavasvami temple");
        labels.put(7, "Chennakeshavasvami temple");
        labels.put(8, "Chitradurga fort");
        labels.put(9, "Hazara Rama temple");
        labels.put(10, "Hoysaleswara temple");
        labels.put(11, "Jor Bangla temple");
        labels.put(12, "King's Palace Basement");
        labels.put(13, "Krishna temple");
        labels.put(14, "Lakshmi Devi temple");
        labels.put(15, "Lakshmi Narsimha temple");
        labels.put(16, "Lalji temple");
        labels.put(17, "Madan Mohan temple");
        labels.put(18, "Mahanavmi Dibba");
        labels.put(19, "Rasmancha");
        labels.put(20, "Sasivekalu Ganesha temple");
        labels.put(21, "Shivappa Nayaka palace");
        labels.put(22, "Shyamarai temple");
        labels.put(23, "Surabheshwara Kona");
        labels.put(24, "Uma Maheshwara temple");
        labels.put(25, "Viroopaksha temple");
        labels.put(26, "Vishnu temple");
        labels.put(27, "Vittala temple");

        button = findViewById(R.id.click);
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picFile = dispatchPictureTakerAction();
                img = findViewById(R.id.imgToDisp);
                Bitmap bitmapImg = BitmapFactory.decodeFile(pathToFile);
                img.setImageBitmap(bitmapImg);
            }
        });


        predict = findViewById(R.id.run);
        predict.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                try {
                    tflite = new Interpreter(loadModelFile("converted_model.tflite"));
                    String res = doInference(picFile);

                    Intent intent = new Intent(MainActivity.this, DescriptionActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("result", res);
                    intent.putExtras(bundle);
                    startActivity(intent);

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String doInference(File photo) throws IOException {
        Bitmap bitmapImg = BitmapFactory.decodeFile(pathToFile);

        //bitmapImg = Bitmap.createScaledBitmap(bitmapImg, 150, 150, false);
        bitmapImg = getResizedBitmap(bitmapImg, 150, 150);
        changedim = new float[1][150][150][3];
        outputval = new float[1][28];

        int m = 0;
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 150; j++) {
                for (int k = 0; k < 150; k++) {
                    int p = bitmapImg.getPixel(j, k);
                    int R = (p >> 16) & 0xff;
                    int G = (p >> 8) & 0xff;
                    int B = p & 0xff;
                    changedim[i][j][k][0] = R;
                    changedim[i][j][k][1] = G;
                    changedim[i][j][k][2] = B;
                }
            }
        }

        tflite.run(changedim, outputval);

        for (int i = 0; i < 28; i++) {
            Log.println(7, "outputval", i + " " + outputval[0][i]);
        }

        path = findViewById(R.id.path);
        String out = "";

        float[] op = outputval[0];
        int ind = 0;

        float max = op[0];

        for (int i = 1; i < op.length; i++) {
            if (max < op[i]) {
                max = op[i];
                ind = i;
            }
        }

        for (float f : op) {
            out += Float.toString(f) + ",";
        }

        Log.println(7, "label", ind + " " + labels.get(ind));
        //path.setText(""+pathToFile);
        return (labels.get(ind));
    }


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    private ByteBuffer loadModelFile(String filename) throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd(filename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /*  @Override
      protected void onActivityResult(int requestCode, int resultCode, Intent data) {
          super.onActivityResult(requestCode, resultCode, data);
          if(requestCode == RESULT_OK){
              if(requestCode == 1){
                  Bitmap bitmap = BitmapFactory.decodeFile(pathToFile);
                  imageView.setImageBitmap(bitmap);
              }
          }
      }*/
    private File dispatchPictureTakerAction() {
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        if (takePic.resolveActivity(getPackageManager()) != null) {
            photoFile = createPhotoFile();
            if (photoFile != null) {
                pathToFile = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(MainActivity.this, "com.example.vtg_stech20.fileprovider", photoFile);
                takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePic, 1);
            }
        }
        return photoFile;
    }

    private File createPhotoFile() {
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile("clickedImage", ".jpg", storageDir);
        } catch (IOException e) {
            Log.d("mylog", "Excep: " + e.toString());
        }
        return image;
    }

}
