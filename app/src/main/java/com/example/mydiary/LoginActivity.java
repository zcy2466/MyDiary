package com.example.mydiary;

import static com.example.mydiary.DatabaseHelper.DB_NAME;
import static com.example.mydiary.DatabaseHelper.VERSION;
import static com.example.mydiary.MainActivity.getDbHelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    Button Load;
    Button register;
    EditText user;
    EditText password;
    SharedPreferences sp;
    CheckBox remember;

    MD5 md = new MD5();
    DatabaseHelper mySQLiteOpenHelper = getDbHelper();
    String AUTHORNAME;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sp = this.getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        mySQLiteOpenHelper=new DatabaseHelper(LoginActivity.this,DB_NAME,null,VERSION);
        mySQLiteOpenHelper.getWritableDatabase();
        initial();
    }
    public void initial(){
        Load = (Button) findViewById(R.id.Load);
        remember = (CheckBox) findViewById(R.id.remember);
        user = (EditText) findViewById(R.id.user);
        password = (EditText) findViewById(R.id.password);
        register = (Button) findViewById(R.id.register);

        if(sp.getBoolean("rememberBoolean",false)){
            user.setText(sp.getString("user",null));
            password.setText(sp.getString("password",null));
            remember.setChecked(true);
        }
        Load.setOnClickListener(this);
        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        user = (EditText) findViewById(R.id.user);
        password = (EditText) findViewById(R.id.password);

        if(view == Load){
            String name = user.getText().toString();
            String mime = password.getText().toString();
            boolean CheckBoxLogin = remember.isChecked();
            if(name.trim().equals("")){
                Toast.makeText(this,"请输入用户名！",Toast.LENGTH_LONG).show();
                return;
            }else if (mime.trim().equals("")) {
                Toast.makeText(this,"请输入密码！",Toast.LENGTH_LONG).show();
                return;
            }
            boolean login = mySQLiteOpenHelper.login(name, mime);
            if(login){
                String hs = md.getMD5(mime);
                if(CheckBoxLogin){
                    Editor editor = sp.edit();
                    editor.putString("user", name);
                    editor.putString("password", hs);
                    editor.putBoolean("rememberBoolean", true);
                    editor.commit();
                }else{
                    Editor editor = sp.edit();
                    editor.putString("user", null);
                    editor.putString("password", null);
                    editor.putBoolean("rememberBoolean", false);
                    editor.commit();
                }
                Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show();
                User select = mySQLiteOpenHelper.select(name);
                Intent home = new Intent(LoginActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("username", select.getUsername());
                home.putExtras(bundle);
                startActivity(home);
            }else{
                Toast.makeText(this,"用户名或密码错误！",Toast.LENGTH_LONG).show();
            }

        }

        if(view == register){
            Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

