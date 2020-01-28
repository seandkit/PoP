package com.example.pop;

public final class DBConstants {
    public static final int DATABASE_VERSION12 =12;

    public static final String DATABASE_NAME = "PoP.db";

    //Tables
    public static final String USERDATA = "userdata";
    public static final String RECEIPTDATA = "receiptdata";
    public static final String ITEMDATA = "itemdata";
    public static final String RECEIPTITEMDATA = "receiptitemdata";

    //generic column
    public static final String ID = "id";

    //userdata columns
    public static final String USERID = "userid";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";

    //receiptdata columns
    public static final String RECEIPTID = "receiptid";
    public static final String DATE = "date";
    public static final String VENDORNAME = "vendorname";
    public static final String CARDTRANS = "cardtrans";
    public static final String RECEIPTTOTAL = "receipttotal";

    //itemdata columns
    public static final String ITEMID = "itemid";
    public static final String ITEMNAME = "itemname";

    //receiptitemdata columns
    public static final String RECEIPTITEMPK = "receiptitempk";
    public static final String PRICE = "price";
    public static final String QUANTITY = "quantity";
}
