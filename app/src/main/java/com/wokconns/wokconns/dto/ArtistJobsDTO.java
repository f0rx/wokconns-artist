package com.wokconns.wokconns.dto;

import java.io.Serializable;

/**
 * Created by VARUN on 01/01/19.
 */
public class ArtistJobsDTO implements Serializable {
    String job_id = "";
    String user_id = "";
    String title = "";
    String description = "";
    String address = "";
    String avtar = "";
    String job_date = "";
    String time = "";
    String job_timestamp = "";
    String currency_symbol = "";
    String cat_name = "";
    String price = "";
    String username = "";
    String userImage = "";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJob_id() {
        return job_id;
    }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvtar() {
        return avtar;
    }

    public void setAvtar(String avtar) {
        this.avtar = avtar;
    }

    public String getJob_date() {
        return job_date;
    }

    public void setJob_date(String job_date) {
        this.job_date = job_date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getJob_timestamp() {
        return job_timestamp;
    }

    public void setJob_timestamp(String job_timestamp) {
        this.job_timestamp = job_timestamp;
    }

    public String getCurrency_symbol() {
        return currency_symbol;
    }

    public void setCurrency_symbol(String currency_symbol) {
        this.currency_symbol = currency_symbol;
    }

    public String getCat_name() {
        return cat_name;
    }

    public void setCat_name(String cat_name) {
        this.cat_name = cat_name;
    }
}