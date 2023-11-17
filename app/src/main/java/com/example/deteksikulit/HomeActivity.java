package com.example.deteksikulit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.TimingLogger;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deteksikulit.ml.Model;
import com.google.android.material.snackbar.Snackbar;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import lib.folderpicker.FolderPicker;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

public class HomeActivity extends AppCompatActivity {
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1, TRAINING_PROCCES = 2, FOLDERPICKER_CODE = 3;
    Button btnDesc, add_imgs, close;
    private String userChoosenTask;
    ImageView original, greyscale, threshold, btn_close;
    double[] momentResult;
    TextView huMoments, hasil_nama, textView, textView2, textView3, executionTime;
    RadioGroup radioGroup;
    RadioButton radioButton1, radioButton2, radioButton3, radioButton4, radioButton5;
    LinearLayout linearLayout, linearLayout2, home_layout;
    GridLayout gridLayout;
    CardView train, test, panduan, info;
    private Model model;
    private Context context;
    private String reci="";
    private double max=0.0,score=0.0;
    public static int index;
    private boolean training = false;
    private Snackbar trainSnackbar;
    String writeText = null;
    private List<String> classNames;

    public static final String ROOT =
            Environment.getExternalStorageDirectory() + File.separator + "facedisease";
    public static final String FILE_DATA = "data";
    public static final String FILE_LABELS = "labels";

    private static String TAG = "MainActivity";
    static {
        if(OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCv Sukses di Install");
        }else {
            Log.d(TAG, "OpenCv Gagal di Install");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_home);
        context = this;
        gridLayout = findViewById(R.id.grid_layout);
        train = findViewById(R.id.training);
        test = findViewById(R.id.testing);
        panduan = findViewById(R.id.panduan);
        info = findViewById(R.id.about);
        btn_close = findViewById(R.id.close);
        greyscale = findViewById(R.id.image_grayscale);
        threshold = findViewById(R.id.image_threshold);
        hasil_nama = findViewById(R.id.hasil);
        btnDesc = findViewById(R.id.desc);
        huMoments = findViewById(R.id.hu);
        textView = findViewById(R.id.text1);
        textView2 = findViewById(R.id.text2);
        textView3 = findViewById(R.id.text3);
        linearLayout2 = findViewById(R.id.linear_layout2);
        home_layout = findViewById(R.id.homepage_layout);
        executionTime =findViewById(R.id.execTime);

        requestPermissions();
        init();

       LinearLayout container = findViewById(R.id.container);
        trainSnackbar = Snackbar.make(
                container, "Melatih data...", Snackbar.LENGTH_INDEFINITE);

        train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, FOLDERPICKER_CODE);
            }
        });

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageTest();
            }
        });

        panduan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HomeActivity.this, R.style.CustomAlertDialog)
                        .setCancelable(false);
                ViewGroup viewGroup = findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.panduan, viewGroup, false);
                builder.setView(dialogView);
                final android.app.AlertDialog alertDialog = builder.create();

                btn_close = dialogView.findViewById(R.id.close);
                btn_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(HomeActivity.this, R.style.CustomAlertDialog)
                        .setCancelable(false);
                ViewGroup viewGroup = findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.tentang, viewGroup, false);
                builder.setView(dialogView);
                final android.app.AlertDialog alertDialog = builder.create();

                btn_close = dialogView.findViewById(R.id.close);
                btn_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 1);
    }

    void init(){
        File dir = new File(ROOT);
        if(!dir.isDirectory()) {
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }

        AssetManager manager = getAssets();
        copyAsset(manager, FILE_DATA);
        copyAsset(manager, FILE_LABELS);
    }

    public static void copyAsset(AssetManager manager, String filename){
        InputStream in = null;
        OutputStream out = null;

        try{
            File file = new File(ROOT + File.separator + filename);
            if(!file.isFile()) {
                if (!file.exists()) {
                    file.createNewFile();
                }
            }
            in = manager.open(filename);
            out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while((read = in.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(in != null){
                try{
                    in.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            if(out != null){
                try{
                    out.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void appendText(String text, String filename) throws FileNotFoundException {
        try(FileWriter fw = new FileWriter(ROOT + File.separator + filename, true);
            PrintWriter out = new PrintWriter(new BufferedWriter(fw))){
            out.println(text);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static ArrayList<String> readLabel(String filename) throws FileNotFoundException{
        Scanner scanner = new Scanner(new File(ROOT + File.separator + filename));
        ArrayList<String> list = new ArrayList<>();
        while(scanner.hasNextLine()){
            list.add(scanner.nextLine());
        }
        scanner.close();
        return list;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Kamera"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Media"))
                        galleryIntent();
                } else {
                }
                break;
        }
    }

    private void selectImageTest() {
        final CharSequence[] items = { "Kamera", "Media",
                "Batal" };

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.CustomAlertDialog)
                .setCancelable(false);
        builder.setTitle("Pilih Gambar");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(HomeActivity.this);

                if (items[item].equals("Kamera")) {
                    userChoosenTask ="Kamera";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Media")) {
                    userChoosenTask ="Media";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Batal")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void selectImageTrain(){
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"),TRAINING_PROCCES);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // perform action when allow permission success
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE && null != data) {
                try {
                    onSelectFromGalleryResult(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if (requestCode == REQUEST_CAMERA){
                onCaptureImageResult(data);

            }else if (requestCode == FOLDERPICKER_CODE) {
                onFolderProcess(data);
            }
        }
    }

    void updateData(String file) throws Exception {
        synchronized (this) {
            Bitmap bitmap = BitmapFactory.decodeFile(file);
            Mat tmp = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
            Utils.bitmapToMat(bitmap, tmp);
            Imgproc.resize(tmp, tmp, new Size(224, 224));
            Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_RGB2GRAY);
            Imgproc.threshold(tmp, tmp, 127, 255, Imgproc.THRESH_BINARY);
            Bitmap bmthres = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(tmp, bmthres);
            executeHueMoment(bmthres);
            writeText = textView3.getText().toString() +
                    "," + String.valueOf(momentResult[0]) +
                    "," + String.valueOf(momentResult[1]) +
                    "," + String.valueOf(momentResult[2]) +
                    "," + String.valueOf(momentResult[3]) +
                    "," + String.valueOf(momentResult[4]) +
                    "," + String.valueOf(momentResult[5]) +
                    "," + String.valueOf(momentResult[6]);
//            Log.i("Humoments: ", writeText);
//            appendText(writeText, FILE_DATA);
        }
    }

    private Bitmap getBitmapFromUri(ContentResolver contentResolver, Uri uri) throws Exception {
        ParcelFileDescriptor parcelFileDescriptor =
                contentResolver.openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();

        return bitmap;
    }

    private void onCaptureImageResult(Intent data) {
        long startTime = SystemClock.uptimeMillis();
        linearLayout2.setVisibility(View.VISIBLE);
        home_layout.setVisibility(View.GONE);
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Mat tmp = new Mat(thumbnail.getWidth(), thumbnail.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(thumbnail, tmp);
        Imgproc.resize(tmp, tmp, new Size(224, 224));
        Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_RGB2GRAY);
        Bitmap bmgs = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp, bmgs);

        Imgproc.threshold(tmp, tmp, 127, 255, Imgproc.THRESH_BINARY);
        Bitmap bmthres = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp, bmthres);

        greyscale.setImageBitmap(bmgs);
        threshold.setImageBitmap(bmthres);

        executeHueMoment(bmthres);
        huMoments.setText(momentResult[0] + "\n" +
                momentResult[1] + "\n" +
                momentResult[2] + "\n" +
                momentResult[3] + "\n" +
                momentResult[4] + "\n" +
                momentResult[5] + "\n" +
                momentResult[6]);

        try {
            model = Model.newInstance(context);
            TensorImage image = TensorImage.fromBitmap(thumbnail);
            Model.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();
            model.close();
            long endTime = SystemClock.uptimeMillis() - startTime;
            String time = String.valueOf(endTime);

            for(int i=0;i<5;i++) {
                String s = probability.get(i).toString();
                String[] arr = s.split(" ");
                String a = arr[2].replace("(score=", "");
                String b = a.replace(")>", "");
                score = Float.parseFloat(b);
                Log.i("hasil", arr[1] + ": " + score);
                if (max < score) {
                    max = score;
                    reci = arr[1];
                    reci = reci.replace("\"","");


                    for(int j=0;j<Data.mainArray.length;j++){
                        System.out.println(reci + " " + Data.classNames[1] + " " + reci.equals(Data.classNames[j]));
                        if(reci.equals(Data.classNames[j])){
                            index = j;
                            break;
                        }
                    }
                    String name = Data.mainArray[index][0];
                    String desc = Data.mainArray[index][1];
                    hasil_nama.setText(name);
                    executionTime.setText("Execution time: " + time + " ms");
                    btnDesc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(HomeActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                    Intent.FLAG_ACTIVITY_NEW_TASK);

                            Mat tmp = new Mat(thumbnail.getWidth(), thumbnail.getHeight(), CvType.CV_8UC1);
                            Utils.bitmapToMat(thumbnail, tmp);
                            Imgproc.resize(tmp, tmp, new Size(224, 224));
                            Bitmap resized = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(tmp, resized);

                            i.putExtra("hasil_nama", name);
                            i.putExtra("hasil_desc", desc);
                            i.putExtra("bitmap_thumbnail", resized);
                            startActivity(i);
                        }
                    });
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void onSelectFromGalleryResult(Intent data) throws IOException {
        long startTime = SystemClock.uptimeMillis();
        linearLayout2.setVisibility(View.VISIBLE);
        home_layout.setVisibility(View.GONE);
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Mat tmp = new Mat(bm.getWidth(), bm.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bm, tmp);
        Imgproc.resize(tmp, tmp, new Size(224, 224));
        Bitmap bmori = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp, bmori);

        Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_RGB2GRAY);
        Bitmap bmgs = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp, bmgs);

        Imgproc.threshold(tmp, tmp, 127, 255, Imgproc.THRESH_BINARY);
        Bitmap bmthres = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmp, bmthres);

        greyscale.setImageBitmap(bmgs);
        threshold.setImageBitmap(bmthres);

        executeHueMoment(bmthres);

        huMoments.setText(momentResult[0] + "\n" +
                momentResult[1] + "\n" +
                momentResult[2] + "\n" +
                momentResult[3] + "\n" +
                momentResult[4] + "\n" +
                momentResult[5] + "\n" +
                momentResult[6]);

        try {
            model = Model.newInstance(context);
            TensorImage image = TensorImage.fromBitmap(bm);
            Model.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();
            model.close();
            long endTime = SystemClock.uptimeMillis() - startTime;
            String time = String.valueOf(endTime);

            for(int i=0;i<5;i++) {
                String s = probability.get(i).toString();
                String[] arr = s.split(" ");
                String a = arr[2].replace("(score=", "");
                String b = a.replace(")>", "");
                score = Float.parseFloat(b);
                Log.i("hasil", arr[1] + ": " + score);
                if (max < score) {
                    max = score;
                    reci = arr[1];
                    reci = reci.replace("\"","");

                    for(int j=0;j<Data.mainArray.length;j++){
                        System.out.println(reci + " " + Data.classNames[1] + " " + reci.equals(Data.classNames[j]));
                        if(reci.equals(Data.classNames[j])){
                            index = j;
                            break;
                        }
                    }
                    String name = Data.mainArray[index][0];
                    String desc = Data.mainArray[index][1];
                    hasil_nama.setText(name);
                    executionTime.setText("Execution time: " + time + " ms");
                    btnDesc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(HomeActivity.this, MainActivity.class);

                            Mat tmp = new Mat(bmori.getWidth(), bmori.getHeight(), CvType.CV_8UC1);
                            Utils.bitmapToMat(bmori, tmp);
                            Imgproc.resize(tmp, tmp, new Size(224, 224));
                            Bitmap resized = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(tmp, resized);

                            i.putExtra("hasil_nama", name);
                            i.putExtra("hasil_desc", desc);
                            i.putExtra("bitmap_thumbnail", resized);
                            startActivity(i);
                        }
                    });
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    // array of supported extensions (use a List if you prefer)
    static final String[] EXTENSIONS = new String[]{
            "jpg", "JPG"
    };
    static final FilenameFilter IMAGE_FILTER = new FilenameFilter() {

        @Override
        public boolean accept(final File dir, final String name) {
            for (final String ext : EXTENSIONS) {
                if (name.endsWith("." + ext)) {
                    return (true);
                }
            }
            return (false);
        }
    };

    private void onFolderProcess(Intent data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        new Thread(() -> {
            Uri uri = data.getData();
            File file = new File(uri.getPath());
            final String[] split = file.getPath().split(":");
            String folderPath = split[1];
            String path = Environment.getExternalStorageDirectory() + "/" + folderPath + "/";
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            File[] files = directory.listFiles();
            Log.d("Files", "Size: " + files.length);
            if(files.length==5) {
                long startTime = SystemClock.uptimeMillis();
                StringBuilder dialog = new StringBuilder(1000);
                dialog.append("Sedang melatih data...");
                runOnUiThread(() -> {
                    alertDialog.setMessage(dialog);
                    alertDialog.show();
                });
                training = true;
                dialog.append("\n");
                for (int i = 0; i < files.length-1; i++) {
                    String kelas = files[i].getName();
                    String filePath = path + kelas + "/";
                    File dir = new File(filePath);
                    String kelasID = String.valueOf(i);
                    textView3.setText(kelasID);
                    for (final File f : dir.listFiles(IMAGE_FILTER)) {
                        String imageName = f.getName();
                        String imageFile = filePath + imageName;
                        try {
                            updateData(imageFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            training = false;
                        }
                    }
                    dialog.append("\nKelas " + kelas + " selesai.");
                    runOnUiThread(() -> {
                        alertDialog.setMessage(dialog);
                    });
                }
                long lastTime = SystemClock.uptimeMillis() - startTime;
                dialog.append("\n\nProses pelatihan selesai.");
                runOnUiThread(() -> {
                    alertDialog.setMessage(dialog);
                    String waktu = String.valueOf(lastTime);
                    dialog.append("\nexecution time: " + waktu + " ms");
//                alertDialog.setMessage(dialog);
                });
                runOnUiThread(() -> {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alertDialog.dismiss();
                        }
                    }, 4000);
                });
            }else{
                runOnUiThread(() -> {
                    StringBuilder dialog = new StringBuilder(1000);
                    dialog.append("Folder kelas");
                    AlertDialog.Builder builder_2 = new AlertDialog.Builder(context);
                    final AlertDialog alertDialog_2 = builder_2.create();
                    alertDialog_2.setMessage(dialog);
                    alertDialog_2.show();
                });
            }
        }).start();
    }

    double centMoment(int p, int q, Bitmap bitmapFinale) {

        int bitmapWidth = bitmapFinale.getWidth();
        int bitmapHeight = bitmapFinale.getHeight();
        int[][] blackWhiteBitmap = new int[bitmapWidth][bitmapHeight];
        int moo = 0;
        for (int i = 0;i < bitmapWidth;i++) {
            for (int j = 0;j < bitmapHeight;j++) {
                int color = bitmapFinale.getPixel(i,j);
                int grayscale = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3;
                if (grayscale > 128) {
                    blackWhiteBitmap[i][j] = 1;
                    moo++;
                } else {
                    blackWhiteBitmap[i][j] = 0;
                }
            }
        }
        double m1o=0;
        double mo1=0;
        for (int i = 0;i < bitmapWidth-1;i++) {
            for (int j = 0;j < bitmapHeight-1;j++) {
                m1o=m1o+(i)*blackWhiteBitmap[i+1][j+1];
                mo1=mo1+(j)*blackWhiteBitmap[i+1][j+1];
            }
        }
        double xx=m1o/moo;
        double yy=mo1/moo;
        double mu_pq=0;
        for (int i = 0;i < bitmapWidth-1;i++) {
            double x=i-xx;
            for (int j = 0;j < bitmapHeight-1;j++) {
                double y=j-yy;
                mu_pq=mu_pq+Math.pow(x, p)*Math.pow(y, q)*blackWhiteBitmap[i+1][j+1];
            }
        }

        double gamma=0.5*(p+q)+1;
        double n_pq=mu_pq/Math.pow(moo, gamma);

        return  n_pq;
    }

    public void executeHueMoment(Bitmap bitmap) {
        momentResult = new double[7];
        double n20=centMoment(2,0,bitmap);
        double n02=centMoment(0,2,bitmap);
        momentResult[0]=n20+n02;
        double n11=centMoment(1,1,bitmap);
        momentResult[1]=Math.pow(n20-n02,2)+4*Math.pow(n11,2);
        double n30=centMoment(3,0,bitmap);
        double n12=centMoment(1,2,bitmap);
        double n21=centMoment(2,1,bitmap);
        double n03=centMoment(0,3,bitmap);
        momentResult[2]=Math.pow(n30-3*n12, 2)+Math.pow(3*n21-n03,2);
        momentResult[3]=Math.pow(n30+n12, 2)+Math.pow(n21+n03,2);
        momentResult[4]=(n30-3*n21)*(n30+n12)*(Math.pow(n30+n12, 2)-3*Math.pow(n21+n03, 2))+(3*n21-n03)*(n21+n03)*(3*Math.pow(n30+n12, 2)-Math.pow(n21+n03, 2));
        momentResult[5]=(n20-n02)*(Math.pow(n30+n12, 2)-Math.pow(n21+n03, 2))+4*n11*(n30+n12)*(n21+n03);
        momentResult[6]=(3*n21-n03)*(n30+n12)*(Math.pow(n30+n12, 2)-3*Math.pow(n21+n03, 2))-(n30+3*n12)*(n21+n03)*(3*Math.pow(n30+n12, 2)-Math.pow(n21+n03, 2));
    }
}