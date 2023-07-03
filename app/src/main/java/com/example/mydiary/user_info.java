package com.example.mydiary;

import static com.example.mydiary.DatabaseHelper.TABLE_NAME;
import static com.example.mydiary.DatabaseHelper.TABLE_NAME1;
import static com.example.mydiary.MainActivity.getDbHelper;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class user_info extends AppCompatActivity implements View.OnClickListener {
    Button cancel;
    Button sure;
    TextView user_name;
    EditText password_old;
    EditText password_new;
    EditText password_sure;
    String AUTHOR;
    String psw;
    MD5 md = new MD5();
    public DatabaseHelper mySQLiteOpenHelper = getDbHelper();
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        initial();
    }

    public void initial(){
        cancel = findViewById(R.id.cancel);
        sure = findViewById(R.id.sure);
        user_name = findViewById(R.id.user_name);
        password_old = findViewById(R.id.password_old);
        password_new = findViewById(R.id.password_new);
        password_sure = findViewById(R.id.password_sure);
        cancel.setOnClickListener(this);
        sure.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        if(bundle !=null){
            String username = bundle.getString("username");
            AUTHOR = username;
        }
        user_name.setText(AUTHOR);
    }

    @SuppressLint("Range")
    @Override
    public void onClick(View view) {
        if(view == cancel){
            finish();
        }
        if(view == sure){
            Toast.makeText(this,"密码错误！",Toast.LENGTH_LONG).show();
            user_name = findViewById(R.id.user_name);
            password_old = findViewById(R.id.password_old);
            password_new = findViewById(R.id.password_new);
            password_sure = findViewById(R.id.password_sure);
            String o = password_old.getText().toString();
            String n = password_new.getText().toString();
            String s = password_sure.getText().toString();
            SQLiteDatabase db = mySQLiteOpenHelper.getWritableDatabase();
            Cursor cursor=db.query(TABLE_NAME1,null,"Username=?", new String[]{String.valueOf(AUTHOR)},null,null,null);
            if(cursor.moveToFirst()){
                psw=cursor.getString(cursor.getColumnIndex("password"));
            }
            if(!md.getMD5(o).equals(psw)){
                Toast.makeText(this,"密码错误！",Toast.LENGTH_LONG).show();
                return;
            }else if(!n.equals(s)){
                Toast.makeText(this,"两次输入的密码不一致！",Toast.LENGTH_LONG).show();
                return;
            }else{
                ContentValues values = new ContentValues();
                values.put("password", md.getMD5(n));
                db.update(TABLE_NAME1, values, "Username=?", new String[]{String.valueOf(AUTHOR)});
                Toast.makeText(this,"修改密码成功！",Toast.LENGTH_LONG).show();
                Intent intent=new Intent(this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}