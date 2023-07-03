package com.example.mydiary;

import static com.example.mydiary.DatabaseHelper.DB_NAME;
import static com.example.mydiary.DatabaseHelper.TABLE_NAME;
import static com.example.mydiary.DatabaseHelper.VERSION;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button Exit;
    Button add;
    Button deleteall;
    Button changepws;
    public static DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private List<String> diary = new ArrayList<>();
    public static final int TAG_INSERT=1;
    public static final int TAG_UPDATE=0;
    private static final int RESULT=0;
    private String select_item;
    private int Id;
    ListView listView;
    ArrayAdapter<String> adapter;
    private SwipeRefreshLayout swipeRefresh;
    String AUTHOR;


    public static DatabaseHelper getDbHelper(){
        return dbHelper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initial();
        dbHelper=new DatabaseHelper(MainActivity.this,DB_NAME,null,VERSION);
        dbHelper.getWritableDatabase();
        init();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent=new Intent(MainActivity.this,Diary.class);
                Id=getDiaryId(position);
                //  Log.d("MainActivity",""+id);
                intent.putExtra("ID",Id);
                intent.putExtra("TAG",TAG_UPDATE);
                startActivityForResult(intent,RESULT);
            }
        });
    }


    public void initial() {
        Exit = (Button) findViewById(R.id.Exit);
        Exit.setOnClickListener(this);
        add = (Button) findViewById(R.id.add);
        add.setOnClickListener(this);
        deleteall = (Button) findViewById(R.id.deleteall);
        deleteall.setOnClickListener(this);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        changepws = findViewById(R.id.changepsw);
        changepws.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        if(bundle !=null){
            String username = bundle.getString("username");
            AUTHOR = username;
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    //初始化数据库
    @SuppressLint("Range")
    public void init(){
        db = dbHelper.getWritableDatabase();
        diary.clear();

        //查询数据库，将title加入列表项目中
        Cursor cursor=db.query(TABLE_NAME,null,"author=?",new String[]{String.valueOf(AUTHOR)},null,null,null);
        //如果查询结果为空，就将title加入项目中
        if(cursor.moveToFirst()){
            String diary_item;
            do{
                diary_item = cursor.getString(cursor.getColumnIndex("title"));
                diary.add(diary_item);
            }while(cursor.moveToNext());
        }
        cursor.close();
        adapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,diary);
        listView=findViewById(R.id.list_item);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent  data) {
        super.onActivityResult(requestCode, resultCode, data);
        init();
    }
    //刷新列表
    private void refresh(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        init();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
        Toast.makeText(this,"刷新完成！",Toast.LENGTH_LONG).show();
    }

    @SuppressLint("Range")
    private int getDiaryId(int position){
        //获取所点击的日记的title
        int Id;
        select_item=diary.get(position);
        //获取id
        db=dbHelper.getWritableDatabase();
        Cursor cursor=db.query(TABLE_NAME,new String[]{"id"},"title=?",
                new String[]{select_item},null,null,null);
        cursor.moveToFirst();
        Id=cursor.getInt(cursor.getColumnIndex("id"));
        return Id;
    }

    @Override
    public void onClick(View view) {
        if(view == Exit){
            Intent intent=new Intent(this,LoginActivity.class);
            Toast.makeText(this,"退出登录",Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }
        if(view == add){
            Intent intent=new Intent(MainActivity.this,Diary.class);
            intent.putExtra("TAG",TAG_INSERT);
            intent.putExtra("username",AUTHOR);
            startActivityForResult(intent,RESULT);
        }
        if(view == deleteall){
            AlertDialog alertDialog2 = new AlertDialog.Builder(this)
                    .setTitle(" ")
                    .setMessage("是否删除所有日记？")
                    .setIcon(R.mipmap.ic_launcher)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            db.delete(TABLE_NAME, "author=?", new String[]{String.valueOf(AUTHOR)});
                            Toast.makeText(MainActivity.this, "清空成功", Toast.LENGTH_SHORT).show();
                            init();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加取消
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MainActivity.this, "清空取消！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }).create();
            alertDialog2.show();
        }
        if(view == changepws){
            Intent intent=new Intent(MainActivity.this,user_info.class);
            intent.putExtra("username",AUTHOR);
            startActivityForResult(intent,RESULT);
        }
    }
}
