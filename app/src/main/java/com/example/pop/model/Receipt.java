package com.example.pop.model;

import java.util.List;

public class Receipt {

    private int id;
    private String date;
    private String vendorName;
    private int cardTrans;
    private double receiptTotal;
    private int userId;
    private List<Item> items;

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

    public Receipt( String date, String vendorName, int cardTrans, int userId, List<Item> items){
        this.date = date;
        this.vendorName = vendorName;
        this.cardTrans = cardTrans;
        this.receiptTotal = calculateTotalPrice(items);
        this.userId = userId;
        this.items = items;
    }

    public Receipt(int id, String date, String vendorName, int cardTrans,  int userId, List<Item> items){
        this.id = id;
        this.date = date;
        this.vendorName = vendorName;
        this.cardTrans = cardTrans;
        this.receiptTotal = calculateTotalPrice(items);
        this.userId = userId;
        this.items = items;
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
        return Double.valueOf(String.format("%.2f", receiptTotal));
    }

    public int getUserId() {
        return userId;
    }

    public List<Item> getItems() {
        return items;
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

    public void setItems(List<Item> items) {
        this.items = items;
    }

    private double calculateTotalPrice(List<Item> items){
        double totalPrice = 0;
        for(Item i:items){
            totalPrice += i.getPrice();
        }
        return totalPrice;
    }
}
