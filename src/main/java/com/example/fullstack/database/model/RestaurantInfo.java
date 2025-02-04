package com.example.fullstack.database.model;


import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class RestaurantInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private String name;
    private String address;
    private String phone;
    private String email;
    private String city;
    private String state;
    private String zip;
    private int rating;
    private String openingHours;
    private String website;
    private String menuUrl;

    public RestaurantInfo() {
    }

    public RestaurantInfo(int id, String name, String address, String phone, String email, String city, String state, String zip, int rating, String openingHours, String website, String menuUrl) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.rating = rating;
        this.openingHours = openingHours;
        this.website = website;
        this.menuUrl = menuUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getMenuUrl() {
        return menuUrl;
    }

    public void setMenuUrl(String menuUrl) {
        this.menuUrl = menuUrl;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestaurantInfo that = (RestaurantInfo) o;
        return id == that.id && rating == that.rating && Objects.equals(name, that.name) && Objects.equals(address, that.address) && Objects.equals(phone, that.phone) && Objects.equals(email, that.email) && Objects.equals(city, that.city) && Objects.equals(state, that.state) && Objects.equals(zip, that.zip) && Objects.equals(openingHours, that.openingHours) && Objects.equals(website, that.website) && Objects.equals(menuUrl, that.menuUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, address, phone, email, city, state, zip, rating, openingHours, website, menuUrl);
    }

    @Override
    public String toString() {
        return "RestaurantInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zip='" + zip + '\'' +
                ", rating=" + rating +
                ", openingHours='" + openingHours + '\'' +
                ", website='" + website + '\'' +
                ", menuUrl='" + menuUrl + '\'' +
                '}';
    }
}
