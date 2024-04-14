package Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes.Name;

public class Room {
    
    public String Name; //KEY

    public int Rating = 0;
    public int NoReviews = 0;
    public boolean IsFull;

    protected List<Reservation> reservations = new ArrayList<>(); // all reservations of this room

    private int _capacity;
    private final String _areaID;
    private String _imgPath;
    private int price;
    private String _managerID;

    public Room(String Name, int Cap, String Aid, String path, int price) {

        this.Name       = Name;
        this._capacity  = Cap;
        this._areaID    = Aid;
        this._imgPath   = path;
        this.price = price;
        IsFull = false;
    }

//----------------GETTERS------------------------------------

    public int capacity() { return _capacity;  }
    public String areaID()   { return _areaID;    }
    public String managerID(){ return _managerID; }
    public List<Reservation> getreservations() { return reservations;}

//----------------SETTERS------------------------------------ 

    public void setCapacity(int cap) { _capacity = cap;}

    public void set_managerID(String ID){ _managerID = ID; }
//-----------------------------------------------------------

    public boolean checkAvailability(LocalDateTime start, LocalDateTime end){
        int startday = start.getDayOfMonth();
        int startmonth = start.getMonthValue();
        int endday = end.getDayOfMonth();
        int endmonth = end.getMonthValue();

        if (reservations.isEmpty()){
            return true;
        }

        for (Reservation r : reservations) {

            int reservationstartmonth = r.StartDate.getMonthValue();
            int reservationstartday = r.StartDate.getDayOfMonth();
            int reservationendmonth = r.EndDate.getMonthValue();
            int reservationendday = r.EndDate.getMonthValue();

            if (reservationstartmonth == startmonth && reservationstartday >= startday
                    && reservationstartday < endday) {
                return false;
            }
        }
        return true;
    }

    public void addReservation(Reservation r){

        if(reservations.contains(r)){
            return;
        }else{
            reservations.add(r);
        }
        
    }

    public int getPrice() {
        return price;
    }
}// Room
