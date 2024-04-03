import java.util.HashMap;
import java.util.Map;

public class Hotel {

    public String Name;

    public int Rating;
    public int NoReviews;
    public boolean IsFull;

    private int _noRooms;
    private String _areaID;
    private String _managerID;

    private Map<String, Room> _rooms = new HashMap<>();
    

    public Hotel(String Name, int R, int NR, String Aid, String MID) {
        this.Name       = Name;
        this.Rating     = R;
        this.NoReviews  = NR;
        this._areaID    = Aid;
        this._managerID = MID;
        IsFull = false;
    }

//----------------GETTERS------------------------------------

    public String areaID()   { return _areaID;      }
    public String managerID(){ return _managerID;   }
    public int noRooms()  { 
        _noRooms = _rooms.size();
        return _noRooms;
    }

    public Map<String, Room> getRooms(){
        return _rooms;
    }
//-----------------------------------------------------------
    
    public void addRoom(Room room){
        _rooms.put(room.Name, room);
    }

}//Hotel
