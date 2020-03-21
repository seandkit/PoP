package com.example.pop.model;

public class Folder {

    private int id;
    private String name;

    //
    public Folder(){

    }
    //DB insertion of Item with no specified quantity
    public Folder(String name){
        this.name = name;
    }
    //DB Pull of Item object from database
    public Folder(int id, String name){
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
}
