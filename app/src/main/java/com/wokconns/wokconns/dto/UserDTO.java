package com.wokconns.wokconns.dto;

import com.wokconns.wokconns.interfacess.Consts;

import java.io.Serializable;

/**
 * Created by VARUN on 01/01/19.
 */
public class UserDTO implements Serializable {

    String user_id = "";
    String name = "";
    String email_id = "";
    String password = "";
    String image = "";
    String address = "";
    String office_address = "";
    String live_lat = "";
    String live_long = "";
    String role = "";
    String status = "";
    String created_at = "";
    String mobile = "";
    String referral_code = "";
    String user_referral_code = "";
    String gender = "";
    String city = "";
    String country = "";
    String updated_at = "";
    String device_type = "";
    String device_id = "";
    String device_token = "";
    String latitude = "";
    String longitude = "";
    String i_card = "";
    String country_code = "";
    String mobile_no = "";
    String bank_name = "";
    String account_no = "";
    String ifsc_code = "";
    String account_holder_name = "";
    int is_profile = 0;
    int approval_status = 0;

    public UserDTO() {}

    public UserDTO(String user_id, String name, String email_id, String password, String image, String address, String office_address, String live_lat, String live_long, String role, String status, String created_at, String mobile, String referral_code, String user_referral_code, String gender, String city, String country, String updated_at, String device_type, String device_id, String device_token, String latitude, String longitude, String i_card, String country_code, String mobile_no, String bank_name, String account_no, String ifsc_code, String account_holder_name, int is_profile, int approval_status) {
        this.user_id = user_id;
        this.name = name;
        this.email_id = email_id;
        this.password = password;
        this.image = image;
        this.address = address;
        this.office_address = office_address;
        this.live_lat = live_lat;
        this.live_long = live_long;
        this.role = role;
        this.status = status;
        this.created_at = created_at;
        this.mobile = mobile;
        this.referral_code = referral_code;
        this.user_referral_code = user_referral_code;
        this.gender = gender;
        this.city = city;
        this.country = country;
        this.updated_at = updated_at;
        this.device_type = device_type;
        this.device_id = device_id;
        this.device_token = device_token;
        this.latitude = latitude;
        this.longitude = longitude;
        this.i_card = i_card;
        this.country_code = country_code;
        this.mobile_no = mobile_no;
        this.bank_name = bank_name;
        this.account_no = account_no;
        this.ifsc_code = ifsc_code;
        this.account_holder_name = account_holder_name;
        this.is_profile = is_profile;
        this.approval_status = approval_status;
    }

    public int getIs_profile() {
        return is_profile;
    }

    public void setIs_profile(int is_profile) {
        this.is_profile = is_profile;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail_id() {
        return email_id;
    }

    public void setEmail_id(String email_id) {
        this.email_id = email_id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOffice_address() {
        return office_address;
    }

    public void setOffice_address(String office_address) {
        this.office_address = office_address;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getApproval_status() {
        return approval_status;
    }

    public void setApproval_status(int approval_status) {
        this.approval_status = approval_status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getReferral_code() {
        return referral_code;
    }

    public void setReferral_code(String referral_code) {
        this.referral_code = referral_code;
    }

    public String getUser_referral_code() {
        return user_referral_code;
    }

    public void setUser_referral_code(String user_referral_code) {
        this.user_referral_code = user_referral_code;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getDevice_type() {
        return device_type;
    }

    public void setDevice_type(String device_type) {
        this.device_type = device_type;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getLive_lat() {
        return live_lat;
    }

    public void setLive_lat(String live_lat) {
        this.live_lat = live_lat;
    }

    public String getLive_long() {
        return live_long;
    }

    public void setLive_long(String live_long) {
        this.live_long = live_long;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getI_card() {
        return i_card;
    }

    public void setI_card(String i_card) {
        this.i_card = i_card;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getMobile_no() {
        return mobile_no;
    }

    public void setMobile_no(String mobile_no) {
        this.mobile_no = mobile_no;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    public String getAccount_no() {
        return account_no;
    }

    public void setAccount_no(String account_no) {
        this.account_no = account_no;
    }

    public String getIfsc_code() {
        return ifsc_code;
    }

    public void setIfsc_code(String ifsc_code) {
        this.ifsc_code = ifsc_code;
    }

    public String getAccount_holder_name() {
        return account_holder_name;
    }

    public void setAccount_holder_name(String account_holder_name) {
        this.account_holder_name = account_holder_name;
    }
}
