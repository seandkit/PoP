package com.example.pop.model;

public class Item {

    private int id;
    private String name;
    private double price;
    private int quantity;

    //
    public Item(){

    }
    //DB insertion of Item with no specified quantity
    public Item(String name, double price){
        this.name = name;
        this.price = price;
        this.quantity = 1;
    }
    //DB Insertion of Item with specified quantity
    public Item(String name, double price, int quantity){
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    //DB Pull of Item object from database
    public Item(int id, String name, double price, int quantity){
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity(){
        return quantity;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
