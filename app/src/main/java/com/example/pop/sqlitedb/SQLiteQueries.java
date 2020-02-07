package com.example.pop.sqlitedb;

import com.example.pop.DBConstants;
import com.example.pop.model.Receipt;

public class SQLiteQueries {

    //TABLE FOR NFC TRANSFER
    public static String createUnlinkedReceiptsTableString(){
        return "CREATE TABLE " + DBConstants.UNLINKEDRECEIPTS + " ( id INTEGER PRIMARY KEY, " +
                "date TEXT," +
                "vendor TEXT," +
                "total TEXT," +
                "userID INTEGER," +
                "uuid TEXT)";
    }

    public static String getUnlinkedReceiptsString(){
        return "SELECT * FROM " + DBConstants.UNLINKEDRECEIPTS;
    }
}
