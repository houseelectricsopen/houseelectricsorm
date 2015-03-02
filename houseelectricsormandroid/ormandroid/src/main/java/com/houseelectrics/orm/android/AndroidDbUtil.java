package com.houseelectrics.orm.android;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;

/**
 * Created by roberttodd on 15/01/2015.
 */
public class AndroidDbUtil
{
    public SqliteDatabaseAndroidService createDb(Activity activity, String dbAbsolutePath, boolean clean)
    {
        if (clean) activity.deleteDatabase(dbAbsolutePath);
        localSqliteHelper = new LocalSqliteHelper(activity, dbAbsolutePath);
        SQLiteDatabase database = localSqliteHelper.getWritableDatabase();
        SqliteDatabaseAndroidService da = new SqliteDatabaseAndroidService();
        da.setDatabase(database);
        return da;
    }

    LocalSqliteHelper localSqliteHelper;

    public void close()
    {
        if (localSqliteHelper!=null) localSqliteHelper.close();
    }

    public static class LocalSqliteHelper extends SQLiteOpenHelper
    {

        public LocalSqliteHelper(Context context, String dbname) {
            super(context, dbname, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase)
        {
            //sqLiteDatabase.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i1, int i2)
        {
            throw new RuntimeException("migration from " + i1 + " to " + i2 + " not supported ");
        }
    }

    public String getOrCreateExternalStorageDbPath(String dir, String dbName)
    {
        return (new File(getOrCreateExternalStorageDirectory(dir), dbName)).getAbsolutePath();
    }

    public File getOrCreateExternalStorageDirectory(String dir)
    {
        //if (fLocalDataDir != null) return fLocalDataDir;
        File fLocalDataDir;
        File fExtStorage = Environment.getExternalStorageDirectory();
        String strTemp = fExtStorage.getPath() + "/" + dir;
        fLocalDataDir = new File(strTemp);
        if (!fLocalDataDir.exists())
        {
            boolean succeeded = fLocalDataDir.mkdirs();
            if (!succeeded)
            {
                throw new RuntimeException("failed to create directory " + strTemp);
            }
        }
        return fLocalDataDir;
    }

}
