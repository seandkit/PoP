package com.example.pop.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;

public class Receipt implements Comparable<Receipt> {

    private int id;
    private String date;
    private String time;
    private String vendorName;
    private int transactionType;
    private double receiptTotal;
    private double cash;
    private String cashier;
    private String location;
    private String barcode;
    private int userId;
    private double lat;
    private double lng;
    private List<Item> items;
    private String uuid;

    public Receipt(){

    }

    public Receipt(String date, String vendorName, double receiptTotal, int userId, String uuid){
        this.date = date;
        this.vendorName = vendorName;
        this.receiptTotal = receiptTotal;
        this.userId = userId;
        this.uuid = uuid;
    }

    public Receipt(int id, String date, String vendorName,  double receiptTotal, int userId){
        this.id = id;
        this.date = date;
        this.vendorName = vendorName;
        this.receiptTotal = receiptTotal;
        this.userId = userId;
    }

    public Receipt(int id, String date, String time, String vendorName,  double receiptTotal, String barcode, int transactionType, String cashier, Double cash, String location, double lat, double lng, int userId){
        this.id = id;
        this.date = date;
        this.time = time;
        this.vendorName = vendorName;
        this.receiptTotal = receiptTotal;
        this.barcode = barcode;
        this.transactionType = transactionType;
        this.cashier = cashier;
        this.cash = cash;
        this.location = location;
        this.lat = lat;
        this.lng = lng;
        this.userId = userId;
    }

    public Receipt(int id, String date, String time, String vendorName,  double receiptTotal){
        this.id = id;
        this.date = date;
        this.time = time;
        this.vendorName = vendorName;
        this.receiptTotal = receiptTotal;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }
    public String getTime() {
        return time;
    }

    public String getVendorName() {
        return vendorName;
    }

    public int getTransactionType() {
        return transactionType;
    }

    public double getReceiptTotal() {
        return Double.valueOf(String.format("%.2f", receiptTotal));
    }

    public double getCash() {
        return Double.valueOf(String.format("%.2f", cash));
    }

    public int getUserId() {
        return userId;
    }

    public List<Item> getItems() {
        return items;
    }

    public String getUuid() {
        return uuid;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getLocation() {
        return location;
    }

    public double getLat() { return lat; }

    public double getLng() { return lng; }

    public String getCashier() {
        return cashier;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public void setTransactionType(int transactionType) {
        this.transactionType = transactionType;
    }

    public void setReceiptTotal(double receiptTotal) {
        this.receiptTotal = receiptTotal;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    public void setCashier(String cashier) {
        this.cashier = cashier;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    private double calculateTotalPrice(List<Item> items){
        double totalPrice = 0;
        for(Item i:items){
            totalPrice += i.getPrice();
        }
        return totalPrice;
    }

    @Override
    public int compareTo(Receipt receipt) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return dateFormat.parse(receipt.getDate()).compareTo(dateFormat.parse(this.getDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
