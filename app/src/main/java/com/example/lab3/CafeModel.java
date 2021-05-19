package com.example.lab3;

import java.io.Serializable;

public class CafeModel implements Serializable, Comparable {

    private String id;
    private String name;
    private double lat;
    private double lng;
    private String vicinity;
    private double rating;
    private int totalRatings;
    private int priceLevel;

    private CafeModel(String id, String name, double lat, double lng, String vicinity, double rating, int totalRatings, int priceLevel) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.vicinity = vicinity;
        this.rating = rating;
        this.totalRatings = totalRatings;
        this.priceLevel = priceLevel;
    }

    public CafeModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }

    public int getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(int priceLevel) {
        this.priceLevel = priceLevel;
    }

    @Override
    public String toString() {
        return "CafeModel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", vicinity='" + vicinity + '\'' +
                ", rating=" + rating +
                ", totalRatings=" + totalRatings +
                ", priceLevel=" + priceLevel +
                '}';
    }

    @Override
    public int compareTo(Object o) {
//        if (((CafeModel) o).getRating() > this.getRating()) {
//            return (int) (((CafeModel) o).getRating() - this.getRating());
//        }
//        else {
//            return (int) (this.getRating()- ((CafeModel) o).getRating());
//        }
        return ((CafeModel) o).getRating().compareTo(this.getRating());
    }
}
