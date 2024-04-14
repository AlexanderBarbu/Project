package Utility;
import java.time.LocalDateTime;
import java.util.*;

public class HotelFilter {

    private ArrayList<String> areas = new ArrayList<>();
    private ArrayList<DateRange> dates = new ArrayList<>();
    private PriceRange priceRange = null;
    private int stars = 0; // Default if not specified
//    private StarsRange starsRange = null;

    private int noOfPeople = 1; // Default if not specified

    public HotelFilter() {}

    public HotelFilter(String data) {
        String[] args = data.split(";");
        int priceFrom = -1;
        int priceTo = -1;
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
                case "dates":
                    String[] dates = attributeValue.split(",");
                    for (String date : dates) {
                        String[] dateParts = date.split("/");
                        addDateRange(LocalDateTime.parse(dateParts[0]), LocalDateTime.parse(dateParts[1]));
                    }
                    break;
                case "noofpeople":
                    noOfPeople = Integer.parseInt(attributeValue);
                    break;
                case "stars":
                    stars = Integer.parseInt(attributeValue);
                    break;
                case "pricefrom":
                    priceFrom = Integer.parseInt(attributeValue);
                    break;
                case "priceto":
                    priceTo = Integer.parseInt(attributeValue);
                    break;
            }
        }
    }

    public HotelFilter(ArrayList<String> areas, ArrayList<DateRange> dates, PriceRange priceRange, int stars, int noOfPeople) {
        this.areas = areas;
        this.dates = dates;
        this.priceRange = priceRange;
        this.stars = stars;
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

    public void setStars(int stars) {
        this.stars = Math.min(5, Math.max(0, stars));
    }

    public int getStars() {
        return stars;
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
        sb.append(";noofpeople=" + noOfPeople);
        sb.append(";stars=" + stars);
        if (priceRange != null) {
            sb.append(";priceFrom=" + priceRange.getFrom());
            sb.append(";priceTo=" + priceRange.getTo());
        }
        return sb.toString();
    }
}
