package maciek.w.runtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.Nullable;

/**
 * Created by Maciek on 04.11.2020
 */
class DataBaseHalper extends SQLiteOpenHelper {

    static String name = "users";
    static int version = 1;

    String createTableUsers = "CREATE TABLE if not exists users (\n" +
            "    id       INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    name     TEXT,\n" +
            "    surname  TEXT,\n" +
            "    email    TEXT,\n" +
            "    username TEXT,\n" +
            "    password TEXT\n" +
            ");\n";


    String test ="CREATE TABLE if not exists contacts (\n" +
            "\tcontact_id INTEGER PRIMARY KEY,\n" +
            "\tfirst_name TEXT NOT NULL,\n" +
            "\tlast_name TEXT NOT NULL,\n" +
            "\temail TEXT NOT NULL UNIQUE,\n" +
            "\tphone TEXT NOT NULL UNIQUE\n" +
            ");";

    public DataBaseHalper(@Nullable Context context) {
        super(context, name, null, version);
        getWritableDatabase().execSQL(createTableUsers);
    }

    public void createUser(ContentValues contentValues){

        getWritableDatabase().insert(name,"",contentValues);



    }

    public boolean isLoginValid(String username, String password){
        String sql = "SELECT count(*) from "+name+" where username= '"
                +username+"' and password= '"+password+"'";

        SQLiteStatement statement = getReadableDatabase().compileStatement(sql);
        long l = statement.simpleQueryForLong();
        statement.close();

        if(l==1){
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isUsernameFree(String username){
        String sql = "SELECT count(*) from "+name+" where username= '"
                +username+"'";
        SQLiteStatement statement = getReadableDatabase().compileStatement(sql);
        long l = statement.simpleQueryForLong();
        statement.close();

        if(l==0){
            return true;
        }
        else {
            return false;
        }

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
