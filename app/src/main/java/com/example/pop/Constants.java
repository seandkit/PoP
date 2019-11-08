package com.example.pop;

public final class Constants {
    public static final int DATABASE_VERSION = 1;
    public static final int DATABASE_VERSION2 = 2;

    public static final String DATABASE_NAME = "PoP.db";

    //Tables
    public static final String USERDATA = "userdata";
    public static final String RECEIPTDATA = "receiptdata";
    public static final String ITEMDATA = "itemdata";
    public static final String RECEIPTITEMDATA = "receiptitemdata";

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
    public static final String ITEMPRICE = "itemprice";

    //receiptitemdata columns
    public static final String RECEIPTITEMPK = "receiptitempk";


}
