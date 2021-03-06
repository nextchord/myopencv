package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ru.bartwell.exfilepicker.data.ExFilePickerResult;

public class PictureActivity extends AppCompatActivity {

    private Button buttonCamera;
    private Button buttonGallery;
    private Button buttonCancel;
    private String standard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        Intent standardIntent = getIntent();
        standard = standardIntent.getStringExtra("standard");

        buttonCamera = findViewById(R.id.button_camera);
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //android 6.0 ???????????????activity?????????????????????
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //??????WRITE_EXTERNAL_STORAGE??????
                    ActivityCompat.requestPermissions(PictureActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            1);
                }
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //??????WRITE_EXTERNAL_STORAGE??????
                    ActivityCompat.requestPermissions(PictureActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
                checkPermissionAndCamera();
            }
        });

        buttonGallery = findViewById(R.id.button_gallery);
        buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //android 6.0 ???????????????activity?????????????????????
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //??????WRITE_EXTERNAL_STORAGE??????
                    ActivityCompat.requestPermissions(PictureActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            1);
                }
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //??????WRITE_EXTERNAL_STORAGE??????
                    ActivityCompat.requestPermissions(PictureActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
                openAlbum();
            }
        });

        buttonCancel = findViewById(R.id.button_cancel_1);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //OpenCV?????????????????????????????????????????????
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                    Log.d("opencv","????????????");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.d("opencv","????????????");
                    break;
            }

        }
    };

    @Override
    protected void onStart() {
        if (!OpenCVLoader.initDebug()) {
            Logger.d("Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Logger.d("OpenCV library found inside package. Using it!"+this.getClass().getName());
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        super.onStart();
    }

    // ?????????????????????requestCode
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;
    //???????????????????????????uri
    private Uri mCameraUri;
    // ????????????????????????????????????Android 10????????????????????????????????????
    //????????????static????????????????????????????????????????????????null???,?????????????????????
    private static String mCameraImagePath;
    // ?????????Android 10????????????
    private boolean isAndroidQ = Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;

    private static final int CAMERA_REQUEST_CODE = 200;
    /**
     * ????????????????????????
     * ?????????????????????????????????
     */
    private void checkPermissionAndCamera() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.CAMERA);
        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
            //????????????????????????
            openCamera();
        } else {
            //??????????????????????????????
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CAMERA_REQUEST_CODE);
        }
    }

    /**
     * ??????????????????
     */
    private void openCamera() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // ?????????????????????
        if (captureIntent.resolveActivity(getPackageManager()) != null) {
            System.out.println("haveit");
            File photoFile = null;
            Uri photoUri = null;

            if (isAndroidQ) {
                // ??????android 10
                photoUri = createImageUri();
            } else {
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (photoFile != null) {
                    mCameraImagePath = photoFile.getAbsolutePath();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //??????Android 7.0?????????????????????FileProvider????????????content?????????Uri
                        photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                    } else {
                        photoUri = Uri.fromFile(photoFile);
                    }
                }
            }

            mCameraUri = photoUri;
            if (photoUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(captureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    /**
     * ??????????????????uri,?????????????????????????????? Android 10????????????????????????
     */
    private Uri createImageUri() {
        String status = Environment.getExternalStorageState();
        // ???????????????SD???,????????????SD?????????,?????????SD????????????????????????
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        } else {
            return getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new ContentValues());
        }
    }

    /**
     * ???????????????????????????
     */
    private File createImageFile() throws IOException {
        String imageName = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        }
        else{
            imageName = "test";
        }
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageName);
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        return tempFile;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = new Intent(PictureActivity.this,ResultActivity.class);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //??????????????????????????????imageview
//                if (isAndroidQ) {
//                    // Android 10 ????????????uri??????
//                    ivPhoto.setImageURI(mCameraUri);
//                } else {
//                    // ????????????????????????
//                    ivPhoto.setImageBitmap(BitmapFactory.decodeFile(mCameraImagePath));
//                }
                //?????????????????????????????????????????????Activity
//                intent.putExtra("mPicPath",mCameraImagePath);

                String originPath = "";
                if(isAndroidQ){
                    originPath = getPathFromUri(getApplicationContext(), mCameraUri);
                }
                else{
                    originPath = mCameraImagePath;
                }

                String[] dataStr = originPath.split("/");
                String fileTruePath = "/sdcard";
                for(int i=4;i<dataStr.length;i++){
                    fileTruePath = fileTruePath+"/"+dataStr[i];
                }
                //System.out.println(fileTruePath);
                OpenCVUtil openCVUtil = new OpenCVUtil();
                Map<Integer, Integer> ans;
                ans = openCVUtil.getAns(fileTruePath, PictureActivity.this);
                if(ans == null){
                    Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<Result> list = new ArrayList<>();
                int score = 0;


                for(int i = 1; i <= 27; i++){
                    Integer answerNumber = ans.get(i);
                    char std = standard.charAt(i-1);
                    if(answerNumber == null){
                        list.add(new Result(i,"NULL", std+""));
                        continue;
                    }
                    String answerR = "";

                    if(answerNumber == 1){
                        answerR = "A";
                    }
                    else if(answerNumber == 2){
                        answerR = "B";
                    }
                    else if(answerNumber == 3){
                        answerR = "C";
                    }
                    else if(answerNumber == 4){
                        answerR = "D";
                    }
                    else {
                        answerR = "NULL";
                    }
                    if(Character.isLetter(std)){
                        std = Character.toUpperCase(std);
                    }
                    Result item = new Result(i, answerR, std+"");
                    if(item.isRight().equals("??????")){
                        score++;
                    }
                    list.add(item);
                }

                intent.putExtra("ansList", (Serializable)list);
                intent.putExtra("score", score);
                startActivity(intent);
            }
        }
        if (requestCode == CHOOSE_PHOTO){
            if (requestCode != RESULT_OK) {
                String path;

                if(data == null)
                    return;
                //????????????????????????
                if (Build.VERSION.SDK_INT >= 19) {
                    //4.4???????????????????????????????????????????????????
                    path = handleImageOnKitKat(data);
                }else {
                    //4.4??????????????????????????????????????????
                    path = handleImageBeforeKitKat(data);
                }

                String[] dataStr = path.split("/");
                String fileTruePath = "/sdcard";
                for(int i=4;i<dataStr.length;i++){
                    fileTruePath = fileTruePath+"/"+dataStr[i];
                }
                //System.out.println(fileTruePath);
                OpenCVUtil openCVUtil = new OpenCVUtil();
                Map<Integer, Integer> ans;
                ans = openCVUtil.getAns(fileTruePath, PictureActivity.this);
                if(ans == null){
                    Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<Result> list = new ArrayList<>();
                int score = 0;


                for(int i = 1; i <= 27; i++){
                    Integer answerNumber = ans.get(i);
                    char std = standard.charAt(i-1);
                    if(answerNumber == null){
                        list.add(new Result(i,"NULL", std+""));
                        continue;
                    }
                    String answerR = "";

                    if(answerNumber == 1){
                        answerR = "A";
                    }
                    else if(answerNumber == 2){
                        answerR = "B";
                    }
                    else if(answerNumber == 3){
                        answerR = "C";
                    }
                    else if(answerNumber == 4){
                        answerR = "D";
                    }
                    else {
                        answerR = "NULL";
                    }
                    if(Character.isLetter(std)){
                        std = Character.toUpperCase(std);
                    }
                    Result item = new Result(i, answerR, std+"");
                    if(item.isRight().equals("??????")){
                        score++;
                    }
                    list.add(item);
                }

                intent.putExtra("ansList", (Serializable)list);
                intent.putExtra("score", score);
                startActivity(intent);
            }
            else{

            }
        }

    }

    //??????
    private static final int CHOOSE_PHOTO = 100;
    //????????????????????????
    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Intent intent = new Intent(MainActivity.this,PuzzleMain.class);
//        if (requestCode == CHOOSE_PHOTO){
//            if (requestCode != RESULT_OK) {
//                String path;
//                //????????????????????????
//                if (Build.VERSION.SDK_INT >= 19) {
//                    //4.4???????????????????????????????????????????????????
//                    path = handleImageOnKitKat(data);
//                }else {
//                    //4.4??????????????????????????????????????????
//                    path = handleImageBeforeKitKat(data);
//                }
//                intent.putExtra("mPicPath", path);
//                startActivity(intent);
//            }
//        }
//    }

    private String handleImageOnKitKat(Intent data) {

        String imagePath = null;
        Uri uri = data.getData();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(this,uri)) {
                //??????document?????????Uri????????????document id??????
                String docId = DocumentsContract.getDocumentId(uri);
                if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    String id = docId.split(":")[1];//????????????????????????id
                    String selection = MediaStore.Images.Media._ID+"="+id;
                    imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
                }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://" +
                            "downloads//public_downloads"),Long.valueOf(docId));
                    imagePath = getImagePath(contentUri,null);
                }
            }else if("content".equalsIgnoreCase(uri.getScheme())){
                //???????????????content?????????uri?????????????????????????????????
                imagePath = getImagePath(uri,null);
            }else if("file".equalsIgnoreCase(uri.getScheme())){
                //????????????file?????????uri????????????????????????????????????
                imagePath = uri.getPath();
            }
        }
        return imagePath;
    }

    private String handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        return imagePath;
    }

    private String getImagePath(Uri externalContentUri, String selection) {
        String path = null;
        //??????Uri???selection??????????????????????????????
        Cursor cursor = getContentResolver().query(externalContentUri,
                null,selection,null,null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

            }
            cursor.close();
        }
        return path;
    }

    //Uri?????????????????????
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getPathFromUri(Context context, Uri uri) {
        String path = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            //?????????document?????????Uri?????????document id????????????????????????Uri.decode(docId)????????????
            String docId = DocumentsContract.getDocumentId(uri);
            //primary:Azbtrace.txt
            //video:A1283522
            String[] splits = docId.split(":");
            String type = null, id = null;
            if(splits.length == 2) {
                type = splits[0];
                id = splits[1];
            }
            switch (uri.getAuthority()) {
                case "com.android.externalstorage.documents":
                    if("primary".equals(type)) {
                        path = Environment.getExternalStorageDirectory() + File.separator + id;
                    }
                    break;
                case "com.android.providers.downloads.documents":
                    if("raw".equals(type)) {
                        path = id;
                    } else {
                        Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                        path = getMediaPathFromUri(context, contentUri, null, null);
                    }
                    break;
                case "com.android.providers.media.documents":
                    Uri externalUri = null;
                    switch (type) {
                        case "image":
                            externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "video":
                            externalUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "audio":
                            externalUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                            break;
                    }
                    if(externalUri != null) {
                        String selection = "_id=?";
                        String[] selectionArgs = new String[]{ id };
                        path = getMediaPathFromUri(context, externalUri, selection, selectionArgs);
                    }
                    break;
            }
        } else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
            path = getMediaPathFromUri(context, uri, null, null);
        } else if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
            //?????????file?????????Uri(uri.fromFile)?????????????????????????????????
            path = uri.getPath();
        }
        //??????????????????????????????????????????
        return path == null ? null : (new File(path).exists() ? path : null);
    }

    private static String getMediaPathFromUri(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path;
        String authroity = uri.getAuthority();
        path = uri.getPath();
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if(!path.startsWith(sdPath)) {
            int sepIndex = path.indexOf(File.separator, 1);
            if(sepIndex == -1) path = null;
            else {
                //path = sdpath + ...
                path = "sdcard" + path.substring(sepIndex);
            }
        }

        if(path == null || !new File(path).exists()) {
            ContentResolver resolver = context.getContentResolver();
            String[] projection = new String[]{ MediaStore.MediaColumns.DATA };
            Cursor cursor = resolver.query(uri, projection, selection, selectionArgs, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    try {
                        int index = cursor.getColumnIndexOrThrow(projection[0]);
                        if (index != -1) path = cursor.getString(index);
                        //Log.i(TAG, "getMediaPathFromUri query " + path);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        path = null;
                    } finally {
                        cursor.close();
                    }
                }
            }
        }
        return path;
    }
}