package Utility;

import java.util.*;

public class HotelFilter {

    private String name = ""; //
    private ArrayList<String> areas = new ArrayList<>(); //
    private int stars = 0; //
    private int noOfReviews = 0; //
    private float rating = 0.0F; //
    private int noOfRooms = 0; //


    public HotelFilter() {}
    public HotelFilter(String data) {
        String[] args = data.split(";");
        for (String arg : args) {
            String[] parts = arg.split("=");
            String attributeName = parts[0];
            String attributeValue = parts[1];
            switch (attributeName.toLowerCase()) {
                case "areas":
                    String[] areas = attributeValue.split(",");
                    for (String area : areas) {
                        addArea(area);
                    }
                    break;
                case "name":
                    this.name = attributeValue;
                    break;
                case "stars":
                    this.stars = Integer.parseInt(attributeValue);
                    break;
                case "rating":
                    rating = Float.parseFloat(attributeValue);
                    break;
                case "noofreviews":
                    this.noOfReviews = Integer.parseInt(attributeValue);
                    break;
                case "noofrooms":
                    this.noOfRooms = Integer.parseInt(attributeValue);
                    break;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("areas=");
        for (String area : areas) {
            sb.append(area + ",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(";noofrooms=" + noOfRooms);
        sb.append(";rating=" + rating);
        if (!name.isEmpty()) {
            sb.append(";name=" + this.name);
        }
        sb.append(";stars=" + this.stars);
        sb.append(";noofreviews=" + noOfReviews);
        return sb.toString();
    }
    
    public void addArea(String area) {
        areas.add(area);
    }

    public ArrayList<String> getAreas() {
        return areas;
    }

    public int getStars() {
        return stars;
    }

    public int getNumberOfReviews() {
        return this.noOfReviews;
    }

    public int getNumberOfRooms()  {
        return this.noOfRooms;
    }

    public String getName() {
        return this.name;
    }

    public float getRating() {
        return this.rating;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumberOfRooms(int rooms) {
        this.noOfRooms = rooms;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setNumberOfReviews(int noOfReviews) {
        this.noOfReviews = noOfReviews;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }
    
}
