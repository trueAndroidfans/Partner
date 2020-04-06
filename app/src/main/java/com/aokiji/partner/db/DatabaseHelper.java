package com.aokiji.partner.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.orhanobut.logger.Logger;

public class DatabaseHelper extends SQLiteOpenHelper {

    public final static String CREATE_MESSAGE = "create table Message ("
            + "id integer primary key autoincrement,"
            + "sender integer,"
            + "time text,"
            + "message text)";

    private Context mContext;

    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.mContext = context;
    }


    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MESSAGE);
        Logger.i("db create succeed");
    }


    @Override public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("drop table if exists Message");
        onCreate(db);
    }

}
