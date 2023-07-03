package com.example.mydiary;

import static com.example.mydiary.DatabaseHelper.TABLE_NAME;
import static com.example.mydiary.MainActivity.TAG_INSERT;
import static com.example.mydiary.MainActivity.TAG_UPDATE;
import static com.example.mydiary.MainActivity.dbHelper;
import static com.example.mydiary.MainActivity.getDbHelper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Diary extends AppCompatActivity implements View.OnClickListener {

    private SQLiteDatabase db;
    EditText diary_title;
    EditText diary_author;
    TextView diary_date;
    EditText diary_content;
    Button pictureChoice;
    ImageView picture;

    private int flag;
    private int id;
    private static final int CHOICE_PHOTO=2;
    String AUTHOR = "";
    private static final int RESULT=1;

    public DatabaseHelper databaseHelper = getDbHelper();

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        initial();
        db = dbHelper.getWritableDatabase();
        //获取Intent传输的数据
        Intent intent=getIntent();
        flag=intent.getIntExtra("TAG",-1);
        switch (flag){
            case TAG_INSERT:
                break;
            case TAG_UPDATE:
                id = intent.getIntExtra("ID",-1);
                Cursor cursor=db.query(TABLE_NAME,null,"id=?",
                        new String[]{String.valueOf(id)},null,null,null);
                if(cursor.moveToFirst()){
                    @SuppressLint("Range") String select_title=cursor.getString(cursor.getColumnIndex("title"));
                    @SuppressLint("Range") String select_author=cursor.getString(cursor.getColumnIndex("author"));
                    @SuppressLint("Range") String select_content=cursor.getString(cursor.getColumnIndex("content"));
                    diary_title.setText(select_title);
                    diary_author.setText(select_author);
                    diary_content.setText(select_content);
                    if(cursor.getBlob(cursor.getColumnIndex("picture"))!=null){
                        @SuppressLint("Range") byte[] in = cursor.getBlob(cursor.getColumnIndex("picture"));
                        Bitmap bitmap= BitmapFactory.decodeByteArray(in,0,in.length);
                        picture.setImageBitmap(bitmap);
                    }

                }
                break;
            default:
        }

    }

    public void initial(){
        diary_title = findViewById(R.id.diary_title);
        diary_author= findViewById(R.id.diary_author);
        diary_date = findViewById(R.id.diary_date);
        diary_content= findViewById(R.id.diary_content);
        pictureChoice=findViewById(R.id.diary_pictureChoice);
        picture=findViewById(R.id.diary_picture);
        diary_title.setSelection(diary_title.getText().length());
        Bundle bundle = getIntent().getExtras();
        if(bundle !=null){
            String username = bundle.getString("username");
            AUTHOR = username;
        }
        diary_author.setText(AUTHOR);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        diary_date.setText(simpleDateFormat.format(date));
        diary_content.setSelection(diary_content.getText().length());

        pictureChoice.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == pictureChoice){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                openAlbum();
            }
        }
    }

    //获取打开系统相册权限打开系统相册
    private  void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOICE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent  data) {
        super.onActivityResult(requestCode, resultCode, data);
        //查询数据库，将title加入列表项目中
        Cursor cursor=db.query(TABLE_NAME,null,"author=?",new String[]{String.valueOf(AUTHOR)},null,null,null);
        switch (requestCode) {
            case CHOICE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitKat(data);
                    } else {
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    //对图片进行解析
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        String imagePath = null;
        Uri uri =data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://Images/Pictures"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }

    //图片显示前对图片进行解析
    private void handleImageBeforeKitKat(Intent data){
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        displayImage(imagePath);
    }

    //获取图片的路径
    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection){
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection,null, null);
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    //显示照片
    private void displayImage(String imagePath){
        if(imagePath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
        }else{
            Toast.makeText(this,"failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    //将menu中的actionbar添加进来
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    //设置“保存”或者“删除”按钮点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.save) {
            if (flag == TAG_INSERT) {
                ContentValues values = new ContentValues();
                values.put("title", diary_title.getText().toString());
                values.put("author", diary_author.getText().toString());
                values.put("content", diary_content.getText().toString());
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                Drawable picture_draw = picture.getDrawable();
                if(picture_draw == null){
                    values.put("picture", "");
                    db.insert(TABLE_NAME, null, values);
                    values.clear();
                    Toast.makeText(this, "Save", Toast.LENGTH_SHORT).show();
                    finish();
                    return true;
                }
                Bitmap bitmap = ((BitmapDrawable) picture_draw).getBitmap();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                values.put("picture", os.toByteArray());
                db.insert(TABLE_NAME, null, values);
                values.clear();
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                return true;
            } else if (flag == TAG_UPDATE) {
                //修改title、content和picture
                String update_title = diary_title.getText().toString();
                String update_author = diary_author.getText().toString();
                String update_content = diary_content.getText().toString();
                ContentValues values = new ContentValues();
                values.put("title", update_title);
                values.put("author", update_author);
                values.put("content", update_content);
                java.sql.Date date0 = new java.sql.Date(System.currentTimeMillis());
                SimpleDateFormat simpleDateFormat0 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                values.put("time", simpleDateFormat0.format(date0));
//                final ByteArrayOutputStream os = new ByteArrayOutputStream();
//                Drawable picture_draw = picture.getDrawable();
//                if(picture_draw == null){
                    values.put("picture", "");
                    db.update(TABLE_NAME, values, "id=?", new String[]{String.valueOf(id)});
                    Toast.makeText(this, "保存修改成功", Toast.LENGTH_SHORT).show();
                    finish();
                    return true;
//                }
//                Bitmap bitmap = ((BitmapDrawable) picture.getDrawable()).getBitmap();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
//                values.put("picture", os.toByteArray());
//                db.update(TABLE_NAME, values, "id=?", new String[]{String.valueOf(id)});
//                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
//                return true;
            }
        } else if (itemId == R.id.delete) {
            AlertDialog alertDialog2 = new AlertDialog.Builder(this)
                    .setTitle(" ")
                    .setMessage("是否确认删除该日记？")
                    .setIcon(R.mipmap.ic_launcher)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (flag == TAG_UPDATE) {
                                db.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(id)});
                            }
                            Toast.makeText(Diary.this, "删除成功", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加取消
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(Diary.this, "删除取消！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }).create();
            alertDialog2.show();

        } else if (itemId == R.id.goBack) {
            finish();
        }
        return true;
    }

}