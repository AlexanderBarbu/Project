package Model;

import java.util.Date;
import java.time.LocalDateTime;

public class Reservation {

    public String ReservationID;

    public User user; // the user that MADE the reservation

    public LocalDateTime StartDate;
    public LocalDateTime EndDate;

    public Reservation(String ID, LocalDateTime s, LocalDateTime e) {
        this.ReservationID = ID;
        this.StartDate = s;
        this.EndDate = e;
    }

}//Reservation
