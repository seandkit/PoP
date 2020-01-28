package com.example.pop.sqlitedb;

import com.example.pop.DBConstants;

public class SQLiteQueries {

    //TABLE FOR NFC TRANSFER
    public static String createNFCTapTableString(){
        return "CREATE TABLE nfctap ( id INTEGER PRIMARY KEY, " +
                "uuid TEXT," +
                "vendor TEXT," +
                "date TEXT)";
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //NO LONGER NEEDED SQLITE **TRANSFER ONGOING**
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String createUserTableString(){
        return "CREATE TABLE " + DBConstants.USERDATA + " ( "
                + DBConstants.ID + " INTEGER PRIMARY KEY, "
                + DBConstants.USERNAME + " TEXT,"
                + DBConstants.EMAIL + " TEXT, "
                + DBConstants.PASSWORD + " TEXT)";
    }
    public static String createReceiptTableString(){
        return "CREATE TABLE " + DBConstants.RECEIPTDATA + " ( "
                + DBConstants.ID + " INTEGER PRIMARY KEY, "
                + DBConstants.DATE + " TEXT,"
                + DBConstants.VENDORNAME + " TEXT, "
                + DBConstants.CARDTRANS + " INTEGER,"
                + DBConstants.RECEIPTTOTAL + " REAL,"
                + DBConstants.USERID + " INTEGER NOT NULL REFERENCES " + DBConstants.USERDATA + "(" + DBConstants.ID + "))";
    }
    public static String createItemTableString(){
        return "CREATE TABLE "+ DBConstants.ITEMDATA + " ("
                + DBConstants.ID + " INTEGER PRIMARY KEY, "
                + DBConstants.ITEMNAME + " TEXT)";
    }
    public static String createReceiptItemTableString(){
        return "CREATE TABLE " + DBConstants.RECEIPTITEMDATA + "( "
                + DBConstants.RECEIPTITEMPK + " INTEGER PRIMARY KEY, "
                + DBConstants.RECEIPT_ID + " INTEGER NOT NULL REFERENCES " + DBConstants.RECEIPTDATA + "('" + DBConstants.ID +"'), "
                + DBConstants.ITEM_ID + " INTEGER NOT NULL REFERENCES " + DBConstants.ITEMDATA + "('" + DBConstants.ID +"'), "
                + DBConstants.PRICE + " INTEGER, "
                + DBConstants.QUANTITY+ " INTEGER)";
    }
    public static String findAllReceiptsByUserIdString(int id){
        return"Select * FROM " + DBConstants.RECEIPTDATA
                +" WHERE " + DBConstants.USERID+ " = "+id;
    }
    public static String findReceiptByReceiptIdString(int id){
        return "Select * FROM " + DBConstants.RECEIPTDATA
                + "WHERE " + DBConstants.ID + " = " + id;
    }
    public static String checkEmailExistString(String email){
        return "Select count(*) FROM "
                + DBConstants.USERDATA + " WHERE "
                + DBConstants.EMAIL + " = '" + email + "'";
    }
    public static String checkUsernameExistString(String username){
        return "Select count(*) FROM "
                + DBConstants.USERDATA + " WHERE "
                + DBConstants.USERNAME + " = '" + username + "'";
    }
    public static String checkItemNameExistString(String itemName){
        return "Select count(*) FROM "
                + DBConstants.ITEMDATA + " WHERE "
                + DBConstants.ITEMNAME + " = '" + itemName + "'";
    }
    public static String findAccountHandlerString(String username, String password){
        return "Select "+ DBConstants.USERNAME +", "+ DBConstants.EMAIL +", " + DBConstants.PASSWORD +" FROM "
                + DBConstants.USERDATA + " WHERE "
                + DBConstants.USERNAME + " = " + "'" + username + "'"
                + " AND " + DBConstants.PASSWORD + " = '" + password + "'";
    }
}
