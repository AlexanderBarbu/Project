package Utility;

import java.time.LocalDateTime;
import java.util.*;

public class RoomFilter {

    private ArrayList<String> areas = new ArrayList<>();
    private ArrayList<DateRange> dates = new ArrayList<>();
    private PriceRange priceRange = null;
    private float rating = 0.0f;
    private int noOfPeople = 1;

    private String roomName = "";
    private String hotelName = "";

    public RoomFilter() {}

    public RoomFilter(String data) {
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
                    roomName = attributeValue;
                    break;
                case "hotelname":
                    hotelName = attributeValue;
                    break;
                case "dates":
                    String[] dates = attributeValue.split(",");
                    for (String date : dates) {
                        String[] dateParts = date.split("/");
                        addDateRange(LocalDateTime.parse(dateParts[0]), LocalDateTime.parse(dateParts[1]));
                    }
                    break;
                case "capacity":
                    noOfPeople = Integer.parseInt(attributeValue);
                    break;
                case "rating":
                    rating = Float.parseFloat(attributeValue);
                    break;
                case "pricefrom":
                    priceRange.setFrom(Float.parseFloat(attributeValue));
                    break;
                case "priceto":
                    priceRange.setTo(Float.parseFloat(attributeValue));
                    break;
            }
        }
    }

    public RoomFilter(ArrayList<String> areas, ArrayList<DateRange> dates, PriceRange priceRange, int noOfPeople) {
        this.areas = areas;
        this.dates = dates;
        this.priceRange = priceRange;
        this.noOfPeople = noOfPeople;
    }

    public void addArea(String area) {
        areas.add(area);
    }

    public ArrayList<String> getAreas() {
        return areas;
    }

    public void addDateRange(LocalDateTime from, LocalDateTime to) {
        dates.add(new DateRange(from, to));
    }

    public float getRating() {
        return this.rating;
    }

    public ArrayList<DateRange> getDates() {
        return dates;
    }

    public void setNumberOfPeople(int no) {
        noOfPeople = no;
    }

    public int getNumberOfPeople() {
        return noOfPeople;
    }

    public void setPriceRange(int from, int to) {
        priceRange = new PriceRange(from, to);
    }

    public PriceRange getPriceRange() {
        return priceRange;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("areas=");
        for (String area : areas) {
            sb.append(area + ",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(";dates=");
        for (DateRange dateRange : dates) {
            sb.append(dateRange.getFrom().toString() + "/" + dateRange.getTo().toString() + ",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(";capacity=" + noOfPeople);
        if (priceRange != null) {
            sb.append(";priceFrom=" + priceRange.getFrom());
            sb.append(";priceTo=" + priceRange.getTo());
        }
        sb.append(";rating=" + rating);
        if (!roomName.isEmpty()) {
            sb.append(";name=" + roomName);
        }
        if (!hotelName.isEmpty()) {
            sb.append(";hotelname=" + hotelName);
        }
        return sb.toString();
    }
}
