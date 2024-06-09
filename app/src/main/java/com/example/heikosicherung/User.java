package com.example.heikosicherung;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String name;
    private String amount;
    private String usage;
<<<<<<< HEAD
=======

>>>>>>> 1b60332a34d3862326e7903350a89bad2e4078cd
    private String date;

    public User() {
    }

    public User(String name, String amount, String usage, String date) {
        this.name = name;
        this.amount = amount;
        this.usage = usage;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getDate() {
        return date;
    }
}
