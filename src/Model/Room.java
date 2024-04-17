package Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Utility.DateRange;

public class Room {
    
    public String Name; //KEY

    public int Rating = 0;
    public int NoReviews = 0;

    private List<DateRange> availableDates = new ArrayList<>();
    protected List<Reservation> reservations = new ArrayList<>(); // all reservations of this room

    private String hotelName = "";
    private int _capacity;
    private String _imgPath;
    private float price;

    public Room(String Name, int Cap, String path, int price) {
        this.Name       = Name;
        this._capacity  = Cap;
        this._imgPath   = path;
        this.price = price;
    }

    public Room(RoomJSON roomJson) {
        this.Name = roomJson.roomName;
        this.hotelName = roomJson.hotelName;
        this._capacity = roomJson.capacity;
        this._imgPath = roomJson.image;
        this.price = roomJson.price;
        this.NoReviews = roomJson.noOfReviews;
    }

    public Room(String data) {
        String[] params = data.split(";");
        for (String entry : params) {
            String[] parts = entry.split("=");
            String attributeName = parts[0];
            String attributeValue = parts[1];
            switch (attributeName.toLowerCase()) {
                case "roomname":
                    this.Name = attributeValue;
                    break;
                case "hotelname":
                    this.hotelName = attributeValue;
                    break;
                case "rating":
                    this.Rating = Integer.parseInt(attributeValue);
                    break;
                case "noofreviews":
                    this.NoReviews = Integer.parseInt(attributeValue);
                    break;
                case "image":
                    this._imgPath = attributeValue;
                    break;
                case "price":
                    this.price = Float.parseFloat(attributeValue);
                    break;
                case "capacity":
                    this._capacity = Integer.parseInt(attributeValue);
                    break;
            }
        }
    }

//----------------GETTERS------------------------------------

    public int capacity() { return _capacity;  }
    public List<Reservation> getreservations() { return reservations;}

//----------------SETTERS------------------------------------ 

    public void setCapacity(int cap) { _capacity = cap;}
//-----------------------------------------------------------

    public boolean addAvailableReservationDates(DateRange range) {
        for (DateRange available : this.availableDates) {
            if (available.intersects(range)) {
                return false;
            }
        }
        availableDates.add(range);
        return true;
    }

    public boolean checkAvailability(LocalDateTime start, LocalDateTime end){
        int startday = start.getDayOfMonth();
        int startmonth = start.getMonthValue();
        int endday = end.getDayOfMonth();
        int endmonth = end.getMonthValue();

        if (reservations.isEmpty()){
            return true;
        }

        for (Reservation r : reservations) {

            int reservationstartmonth = r.dateRange.getFrom().getMonthValue();
            int reservationstartday = r.dateRange.getFrom().getDayOfMonth();
            int reservationendmonth = r.dateRange.getTo().getMonthValue();
            int reservationendday = r.dateRange.getTo().getMonthValue();

            if (reservationstartmonth == startmonth && reservationstartday >= startday
                    && reservationstartday < endday) {
                return false;
            }
        }
        return true;
    }

    public boolean addReservation(Reservation r){
        if (!reservations.contains(r)) {
            for (DateRange dr : availableDates) {
                if (r.dateRange.equals(dr)) {
                    reservations.add(r);
                    return true;
                }
            }
        }
        return false;
    }

    public float getPrice() {
        return price;
    }

    public String getHotelName() {
        return hotelName;
    }

    public boolean isReservationPossible(Reservation reservation) {
        if (this.reservations.contains(reservation)) {
            return false;
        }
        for (Reservation res : this.reservations) {
            if (res.dateRange.intersects(reservation.dateRange)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else if (object == null) {
            return false;
        } else if (object.getClass() != getClass()) {
            return false;
        }
        Room other = (Room)object;
        return other.hotelName == this.hotelName && other.Name == this.Name;
    }

    @Override
    public String toString() {
        HashMap<String, String> members = new HashMap<>();
        members.put("hotelName", hotelName);
        members.put("roomName", Name);
        members.put("image", _imgPath);
        members.put("capacity", Integer.toString(this._capacity));
        members.put("price", Float.toString(price));
        members.put("rating", Integer.toString(Rating));
        members.put("noOfReviews", Integer.toString(NoReviews));
        
        StringBuilder builder = new StringBuilder();
        for (String key : members.keySet()) {
            builder.append(key + "=" + members.get(key) + ";");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}// Room
