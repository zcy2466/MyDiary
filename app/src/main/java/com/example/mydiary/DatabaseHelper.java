package com.example.mydiary;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME="NotePad.db";
    public static final int VERSION=1;
    public static final String TABLE_NAME="Diary";
    public static final String TABLE_NAME1="User";
    //建表语句
    public static final String CREATE_DIARY="create table Diary(" +
            "id integer primary key autoincrement," +
            "title text," +
            "time text," +
            "author text," +
            "content text," +
            "picture BLOB)";

    public static final String CREATE_USER = "create table User("+
            "Username varchar(30),"+
            "password varchar(30))";
    private Context mContext;
    public DatabaseHelper(Context context,String name, SQLiteDatabase.CursorFactory factory,int version){
        super(context,name,factory,version);
        mContext=context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DIARY);
        db.execSQL(CREATE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public DatabaseHelper(@Nullable Context context) {
        super(context,DB_NAME,null,1);
    }
    public long register(User u){
        MD5 md = new MD5();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("username ",u.getUsername());
        String s = md.getMD5(u.getPassword());
        cv.put("password",s);
        long users = db.insert("user",null,cv);
        return users;
    }

    //根据用户名查找当前登录用户信息
    public User select(String username){
        SQLiteDatabase db = getWritableDatabase();
        User SelectUser = new User();
        Cursor user = db.query("User", new String[]{"username"}, "Username=?", new String[]{username}, null, null, null);
        while(user.moveToNext()){
            @SuppressLint("Range") String uname =user.getString(user.getColumnIndex("Username"));
            SelectUser.setUsername(uname);
        }
        user.close();
        return SelectUser;
    }

    //登录方法实现
    public boolean login(String username,String password){
        SQLiteDatabase db = getWritableDatabase();
        MD5 md = new MD5();
        boolean result = false;
        Cursor users = db.query("User", null, "username like?", new String[]{username}, null, null, null);
        if(users!=null){
            while (users.moveToNext()){
                @SuppressLint("Range") String username1 = users.getString(users.getColumnIndex("Username"));
                Log.i("users", "login: "+username1);
                @SuppressLint("Range") String password1 = users.getString(users.getColumnIndex("password"));
                if(password.length()==32){
                    result = password.equals(password1);
                }else{
                    result = md.getMD5(password).equals(password1);
                }
                return result;
            }
        }
        return false;
    }
}
