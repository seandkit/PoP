package com.example.pop;

public final class DBConstants {
    public static final int DATABASE_VERSION12 =12;

    //SQLITE
    public static final String DATABASE_NAME = "PoP.db";

    //PHP Web Server
    public static final String BASE_URL = "https://mysql03.comp.dkit.ie/D00198128/";
    public static final String DATA = "data";
    public static final String STRING_EMPTY = "";

    public static final String VENDOR = "vendor";
    public static final String RECEIPT_TOTAL = "receipt_total";

    //PHP userTable
    public static final String USER_ID = "user_id";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";

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

    //receiptdata columns
    public static final String RECEIPTID = "receipt_id";
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
