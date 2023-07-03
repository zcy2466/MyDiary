package com.example.mydiary;

import static com.example.mydiary.DatabaseHelper.DB_NAME;
import static com.example.mydiary.DatabaseHelper.TABLE_NAME;
import static com.example.mydiary.DatabaseHelper.TABLE_NAME1;
import static com.example.mydiary.DatabaseHelper.VERSION;
import static com.example.mydiary.MainActivity.dbHelper;
import static com.example.mydiary.MainActivity.getDbHelper;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    EditText user_name;
    EditText password_register;
    Button sure;
    Button cancel;
    private List<User> users = new ArrayList<>();
    private SQLiteDatabase db;
    public DatabaseHelper databaseHelper = getDbHelper();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        databaseHelper=new DatabaseHelper(RegisterActivity.this,DB_NAME,null,VERSION);
        databaseHelper.getWritableDatabase();
        initial();
    }
    public void initial(){
        user_name = (EditText) findViewById(R.id.user_name);
        password_register = (EditText) findViewById(R.id.password_register);
        sure = (Button) findViewById(R.id.sure);
        cancel = (Button) findViewById(R.id.cancel);
        sure.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        user_name = (EditText) findViewById(R.id.user_name);
        password_register = (EditText) findViewById(R.id.password_register);
        if(view == sure){
            String name1 = user_name.getText().toString();
            String mime1 = password_register.getText().toString();
            if(name1.trim().equals("")){
                Toast.makeText(this,"请输入用户名！",Toast.LENGTH_LONG).show();
                return;
            }else if (mime1.trim().equals("")) {
                Toast.makeText(this,"请输入密码！",Toast.LENGTH_LONG).show();
                return;
            }
            User user = new User(name1,mime1);
            long r1 = databaseHelper.register(user);
            if(mime1.length()>=16){
                Toast.makeText(this,"密码不能超过16位",Toast.LENGTH_LONG).show();
                return;
            }
            if(r1!=-1){
                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                Toast.makeText(this,"注册成功",Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }else{
                Toast.makeText(this, "注册失败！", Toast.LENGTH_SHORT).show();
            }
        }

        if(view == cancel){
            Toast.makeText(this,"注册失败",Toast.LENGTH_LONG).show();
            Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}