package com;

public class Receipt {
    private int receipt_id;
    private String date;
    private String merchant_name;
    private boolean card_trans;

    public Receipt(int receipt_id, String date, String merchant_name, boolean card_trans){
        this.receipt_id = receipt_id;
        this.date = date;
        this.merchant_name = merchant_name;
        this.card_trans = card_trans;
    }

    public void setReceipt_id(int receipt_id){
        this.receipt_id = receipt_id;
    }

    public void setDate(String date){
        this.date = date;
    }

    public void setMerchant_name(String merchant_name){
        this.merchant_name = merchant_name;
    }

    public void setCard_trans(boolean card_trans){
        this.card_trans = card_trans;
    }

    public int getReceipt_id(){
        return receipt_id;
    }

    public String getDate(){
        return date;
    }

    public String getMerchant_name(){
        return merchant_name;
    }

    public boolean getCard_trans(){
        return card_trans;
    }
}
