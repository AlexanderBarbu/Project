package Model;

import Utility.PriceRange;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

    public String UID;

    public int Name;
    public int Surname;
    public boolean isManager;

    private String _managerID = "-1";
    private Map<String, Reservation> _reservations = new HashMap<>();
    private Map<String, Hotel> _hotels = new HashMap<>();
    private int reservations = 0;

    public String managerID() {
        return _managerID;
    } //Getter

    public void setManagerID(String ID) {
        _managerID = ID;
    } //Setter - ONE TIME USE ONLY

    //------------------------MANAGER METHODS------------------------------------------

    public void registerHotel(Hotel h) {
        h.setMID(_managerID);
        _hotels.put(h.Name, h);
    }

    public Map<String, Reservation> getReservations(Hotel h) {
        Map<String, Reservation> hotelReservations = new HashMap<>();
        Map<String, Room> rooms = h.getRooms();

        for (Room room : rooms.values()) {
            List<Reservation> reservations = room.getreservations();

            for (Reservation res : reservations) {
                hotelReservations.put(room.Name, res);
            }
        }

        return hotelReservations;
    }

    //------------------------CUSTOMER METHODS----------------------------------------

    public boolean makeReservation(Hotel h, LocalDateTime start, LocalDateTime end, int people) {
        Room room = h.getAvailableRoom(people, start, end, PriceRange.Any());
        boolean availability = room != null;

        if (availability) {
            String RID = reservations + "R" + UID;
            room.addReservation(new Reservation(RID, start, end));
        }
        return availability;
    }
}
