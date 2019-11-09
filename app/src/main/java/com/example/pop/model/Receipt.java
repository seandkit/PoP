package com.example.pop.model;

public class Receipt {

    private int id;
    private String date;
    private String vendorName;
    private int cardTrans;
    private double receiptTotal;
    private int userId;

    public Receipt(){

    }

    public Receipt(String date, String vendorName, int cardTrans, double receiptTotal, int userId){
        this.date = date;
        this.vendorName = vendorName;
        this.cardTrans = cardTrans;
        this.receiptTotal = receiptTotal;
        this.userId = userId;
    }

    public Receipt(int id, String date, String vendorName, int cardTrans, double receiptTotal, int userId){
        this.id = id;
        this.date = date;
        this.vendorName = vendorName;
        this.cardTrans = cardTrans;
        this.receiptTotal = receiptTotal;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getVendorName() {
        return vendorName;
    }

    public int isCardTrans() {
        return cardTrans;
    }

    public double getReceiptTotal() {
        return receiptTotal;
    }

    public int getUserId() {
        return userId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public void setCardTrans(int cardTrans) {
        this.cardTrans = cardTrans;
    }

    public void setReceiptTotal(double receiptTotal) {
        this.receiptTotal = receiptTotal;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
