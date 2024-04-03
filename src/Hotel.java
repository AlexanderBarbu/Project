import java.util.HashMap;
import java.util.Map;

public class Hotel {

    public String Name;

    public int Rating;
    public int NoReviews;
    public boolean IsFull;

    private int _noRooms;
    private int _areaID;
    private int _managerID;

    private Map<String, Room> _rooms = new HashMap<>();

    public Hotel(String Name, int R, int NR, int rooms, int Aid, int MID) {
        this.Name       = Name;
        this.Rating     = R;
        this.NoReviews  = NR;
        this._noRooms   = rooms;
        this._areaID    = Aid;
        this._managerID = MID;
        IsFull = false;
    }

}//Hotel
