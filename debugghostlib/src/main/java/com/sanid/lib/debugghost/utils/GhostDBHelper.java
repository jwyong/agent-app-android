package com.sanid.lib.debugghost.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

public class GhostDBHelper extends SQLiteOpenHelper {

    public static final String QUERY_ALL_TABLES = "SELECT name FROM sqlite_master WHERE type='table';";
    public static final String QUERY_TABLE = "SELECT * FROM ##TABLE##";

    private final Context mContext;
    private final String mDbName;

    Cursor cursor;

    public GhostDBHelper(Context context, String dbName, int dbVersion) {
        super(context, dbName, null, dbVersion);

        mDbName = dbName;
        mContext = context;
    }

    private static String getErrorHTML(String textStrong, String message) {
        StringBuilder sbErr = new StringBuilder("<div class=\"alert alert-danger\" role=\"alert\">");
        if (textStrong != null) {
            sbErr.append("<strong>" + textStrong + "</strong>");
        }
        if (message != null) {
            sbErr.append(message);
        }
        sbErr.append("</div>");
        return sbErr.toString();
    }

    public static byte[] blobFromCursor(Cursor c, String field) {
        if (c.isNull(c.getColumnIndex(field))) {
            return null;
        }


        return c.getBlob(c.getColumnIndex(field));
    }

    public static String stringFromCursor(Cursor c, String field) {
        if (c.isNull(c.getColumnIndex(field))) {
            return null;
        }


        return c.getString(c.getColumnIndex(field));
    }

    public static long longFromCursor(Cursor c, String field) {
        if (c.isNull(c.getColumnIndex(field))) {
            return -1l;
        }
        return c.getLong(c.getColumnIndex(field));
    }

    public static int intFromCursor(Cursor c, String field) {
        if (c.isNull(c.getColumnIndex(field))) {
            return -1;
        }
        return c.getInt(c.getColumnIndex(field));
    }

    public static double doubleFromCursor(Cursor c, String field) {
        if (c.isNull(c.getColumnIndex(field))) {
            return -1;
        }
        return c.getDouble(c.getColumnIndex(field));
    }

    public static float floatFromCursor(Cursor c, String field) {
        if (c.isNull(c.getColumnIndex(field))) {
            return -1;
        }
        return c.getFloat(c.getColumnIndex(field));
    }

    public static boolean boolFromCursor(Cursor c, String field) {
        if (c.isNull(c.getColumnIndex(field))) {
            return false;
        }
        int val = c.getInt(c.getColumnIndex(field));
        return val > 0;
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
    }

    // Method is called during an upgrade of the database,
    // if you increase the database version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

    }

    public Cursor sqlQuery(String query) {
        SQLiteDatabase dbRead = getReadableDatabase();

        Cursor c = dbRead.rawQuery(query, null);
        return c;
    }

    public String getHTMLTables() {
        Cursor c = sqlQuery(QUERY_ALL_TABLES);
        StringBuilder sb = new StringBuilder();

        sb.append("<ul>");
        while (c.moveToNext()) {
            String tName = stringFromCursor(c, "name");
            sb.append("<li>");
            sb.append("<a href=\"/db/" + tName + "\">");
            sb.append(tName);
            sb.append("</a>");
            sb.append("</li>");
        }
        sb.append("</ul>");
        c.close();

        return sb.toString();
    }

    public String getHTMLTable(String tableName) {
        StringBuilder sb = new StringBuilder();
        String out;
        Cursor c = null;
        boolean isBLob = false;
        try {
            c = sqlQuery(QUERY_TABLE.replace("##TABLE##", tableName));

            sb.append("<table class=\"table table-bordered\">");
            sb.append("<thead>");
            sb.append("<tr>");
            for (int i = 0; i < c.getColumnCount(); i++) {
                sb.append("<th>");
                sb.append(c.getColumnName(i));
                sb.append("</th>");

            }
            sb.append("</tr>");
            sb.append("</thead>");

            while (c.moveToNext()) {
                sb.append("<tr>");


                for (int i = 0; i < c.getColumnCount(); i++) {
                    sb.append("<td>");
                    Cursor cursor = sqlQuery(String.format("PRAGMA table_info(%s)", tableName));

                    cursor.moveToPosition(i);
                    isBLob = cursor.getString(2).equals("BLOB");
                    cursor.close();

                    if (isBLob) {
                        if (blobFromCursor(c, c.getColumnName(i)) != null) {
                            sb.append(String.format("<img src=\"data:image/png;base64,%s\" />", Base64.encodeToString(blobFromCursor(c, c.getColumnName(i)), Base64.DEFAULT)));
                        }
                    } else {
                        sb.append(stringFromCursor(c, c.getColumnName(i)));
                    }

                }
                sb.append("</td>");
            }
            sb.append("</tr>");
            sb.append("</table>");

            out = sb.toString();
        } catch (SQLiteException e) {
            out = getErrorHTML("Exception: ", e.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
        }

        return out;
    }

    public String getDbName() {
        return mDbName;
    }

    public String getPathToDbFile() {
        return mContext.getDatabasePath(mDbName).getPath();
    }
}
