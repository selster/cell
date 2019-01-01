package com.xlstocsv.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import com.xlstocsv.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    // my_db = "test" + month + day -->>  table_12_22_2018.db
    private static String my_db = "";
   // private static final String DATABASE_NAME = "contactsManager.db";
    private static final String TABLE_CONTACTS = "contacts";
    private static final String Dummy = "dummy";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PH_NO = "phone_number";
    public static DatabaseHandler handler = null;
    private static String databaseName = "";

    private String databasePath = "";
    //    private static final String DATABASE_PATH = Environment.getExternalStorageDirectory().getPath()
//            + File.separator + "NotificationHistory/0/6/log/excel/" + DATABASE_NAME;
    private static final String DATABASE_FOLDER = Environment.getExternalStorageDirectory().getPath()
            // xls files are written to here
            // /storage/emulated/0/Notification History0/1Log/Excel
            + File.separator + "NotificationHistory/0/6/log/excel/";  // looks up excel files?
    private static final String DATABASE_FOLDER_out = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "NotificationHistory/0/6/log/excel/"; // where it will write files
    public static final String EXCEL_FILES = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "Notification History0/";

    private DatabaseHandler(Context context) {
        super(context, DATABASE_FOLDER_out + getMyDatabaseName(), null, DATABASE_VERSION);

     //   super(context, DATABASE_NAME, null, DATABASE_VERSION);

        databasePath = context.getDatabasePath(my_db).getPath();
        Log.d("Test", "Database Folder :" + DATABASE_FOLDER_out);
        Log.d("Test","Database Path :" +  databasePath);
        Log.d("Test", my_db);
    }


    private static String getMyDatabaseName() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        String time = simpleDateFormat.format(calendar.getTime());
        time = time.replaceAll(":","_");
        String date_time = year + "_" + month + "_" + day + "_" + time;
        String my_db_table = "table" + "_" + date_time + ".db";
        my_db = my_db_table;
        return my_db_table;
    }
//    private DatabaseHandler(Context context) {
//        super(context, DATABASE_PATH, null, DATABASE_VERSION);
//    }

    public static DatabaseHandler getInstance(Context context) {
        if (handler == null) {
            File databasePath = new File(DATABASE_FOLDER);
            if (!databasePath.exists())
                databasePath.mkdirs();
            handler = new DatabaseHandler(context);
        }
        return handler;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void createTable(ArrayList<String> columns) {
        SQLiteDatabase db = this.getWritableDatabase();
        StringBuilder builder = new StringBuilder();
        String deleteTable = "DROP TABLE IF EXISTS " + TABLE_CONTACTS;
        Log.e("deleteTable: ", deleteTable);
        db.execSQL(deleteTable);
        String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_CONTACTS + "(";
        builder.append("");
        builder.append(createTable);
        for (int i = 0; i < columns.size(); i++) {
            if (i < columns.size() - 1)
                builder.append(columns.get(i) + " TEXT,");
            else
                builder.append(columns.get(i) + " TEXT)");
        }
        Log.e("createTable: ", builder.toString());
        db.execSQL(builder.toString());

//        Log.d("DatabasePath", databasePath);
//        String query_Table = "select * from " + TABLE_CONTACTS;
//        Cursor cursor = db.rawQuery(query_Table)
//        db.execSQL(query_Table);
//        Log.e(query_Table);
//        return builder.toString();
    }

    public String getDatabasePath() {
        return this.getWritableDatabase().getPath();
    }
    public void addContact(ArrayList<String> columns, ArrayList<String> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_CONTACTS);
        ContentValues values = new ContentValues();

        int i = 0;
        for (int j = 0; j < list.size(); j++) {
            if (i < columns.size()) {
                values.put(columns.get(i), list.get(j));
                i = i + 1;
                if (i == columns.size()) {
                    db.insert(TABLE_CONTACTS, null, values);
                    i = 0;
                }
            }
        }

        //2nd argument is String containing nullColumnHack
        db.close();
        exportDatabase();
    }

    //    // code to get all contacts in a list view
    public Cursor getAllContacts() {
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Log.e("0: ", cursor.getString(0));
                Log.e("1: ", cursor.getString(1));
                Log.e("2: ", cursor.getString(2));
                Log.e("3: ", cursor.getString(3));
                Log.e("4: ", cursor.getString(4));
            } while (cursor.moveToNext());
        }

        // return contact list
        return cursor;
    }

    public void exportDatabase() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + MainActivity.PACKAGE_NAME + "//databases//" + my_db + "";
                String backupDBPath = my_db;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(DATABASE_FOLDER, my_db);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {

        }
    }

    public void createNewDatabase() {
        handler = null;
    }
}
