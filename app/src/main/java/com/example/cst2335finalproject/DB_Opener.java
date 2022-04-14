package com.example.cst2335finalproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Code based on lab 5 Databases */
public class DB_Opener extends SQLiteOpenHelper {

    protected final static String DATABASE_NAME = "NasaImageDB";
    protected final static int VERSION_NUM = 1;
    public final static String TABLE_NAME = "NASA_IMAGES";

    public final static String COL_FILENAME= "FILENAME";
    public final static String COL_TITLE= "TITLE";
    public final static String COL_FILEPATH= "FILEPATH";
    public final static String COL_PUBLISHED_DATE= "PUBLISHED_DATE";
    public final static String COL_EXPLANATION= "EXPLANATION";
    public final static String COL_ID = "_id";

    DB_Opener(Context ctx) {
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_FILENAME + " text,"
                + COL_TITLE + " text,"
                + COL_FILEPATH + " text,"
                + COL_PUBLISHED_DATE + " text,"
                + COL_EXPLANATION  + " text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1)
    {   //Drop the old table:
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);

        //Create the new table:
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {   //Drop the old table:
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);

        //Create the new table:
        onCreate(db);
    }
}