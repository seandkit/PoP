package com.example.pop.sqlitedb;

import com.example.pop.DBConstants;

public class SQLiteQueries {

    //TABLE FOR NFC TRANSFER
    public static String createUnlinkedReceiptsTableString(){
        return "CREATE TABLE unlinkedReceipts ( id INTEGER PRIMARY KEY, " +
                "uuid TEXT," +
                "vendor TEXT," +
                "date TEXT," +
                "total TEXT)";
    }

    public static String getUnlinkedReceiptsString(){
        return "SELECT * unlinkedReceipts";
    }
}
