package Model;

import java.util.Random;

import Utility.DateRange;
import Utility.Logger;

import java.time.LocalDateTime;

public class Reservation {

    public String ReservationID;

    public User user = null; // the user that MADE the reservation

    public DateRange dateRange = null;
    public Hotel hotel = null;
    public Room room = null;

    public Reservation(User user, Hotel hotel, Room room, DateRange range) {
        Random random = new Random();
        this.user = user;
        this.hotel = hotel;
        this.room = room;
        this.ReservationID = Integer.toString(Math.abs(random.nextInt()));
        this.dateRange = range;
    }

    @Override
    public String toString() {
        return this.user.Name + ";"  
            + this.ReservationID + ";"
            + this.hotel.Name + ";"
            + this.room.Name + ";"
            + this.dateRange.getFrom() + ";"
            + this.dateRange.getTo() + ";";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object == null) {
            return false;
        } else if (getClass() != object.getClass()) {
            return false;
        }

        Reservation other = (Reservation)object;
        
        if (other.ReservationID.equals(this.ReservationID)) {
            return true;
        }

        return other.user.equals(this.user) 
            && other.room.equals(this.room)
            && other.dateRange.equals(this.dateRange);
    }
}//Reservation
