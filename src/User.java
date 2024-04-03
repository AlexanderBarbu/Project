import java.util.HashMap;
import java.util.Map;

public class user {
    
    public String UID;
    
    public int Name;
    public int Surname;
    public boolean isManager;

    private String _managerID = "-1";
    private Map<String, Reservation> _reservations = new HashMap<>();
    private Map<String, Hotel> _hotels = new HashMap<>();

    public String managerID(){ return _managerID; } //Getter

    public void setManagerID(String ID){ _managerID = ID;} //Setter - ONE TIME USE ONLY 

    //------------------------MANAGER METHODS------------------------------------------


    //------------------------CUSTOMEER METHODS----------------------------------------

    public void makeReservation(){
        
    }
}
