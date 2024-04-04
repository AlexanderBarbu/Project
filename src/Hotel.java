import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class Hotel {

    public String Name;

    public int Rating;
    public int NoReviews;
    public boolean IsFull;

    private int _noRooms;
    public String _areaID;
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

    // When passing hotel data through messages, the string will
    // be something like 'name=Hotel;rating=3;areaID=342' etc
    // so we want to be able to initialize a hotel object using this string.
    // This is basically the inverse of .toString()
    public Hotel(String data) {
        String[] params = data.split(";");
        for (String entry : params) {
            String[] parts = entry.split("=");
            String attributeName = parts[0];
            String attributeValue = parts[1];
            switch (attributeName.toLowerCase()) {
                case "name":
                    this.Name = attributeValue;
                    break;
                case "rating":
                    this.Rating = Integer.parseInt(attributeValue);
                    break;
                case "noreviews":
                    this.NoReviews = Integer.parseInt(attributeValue);
                    break;
                case "full":
                    this.IsFull = (attributeValue.toLowerCase() == "true");
                    break;
                case "area":
                    this._areaID = attributeValue;
                    break;
            }
        }
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

    public Room getAvailableRoom(int capacity , LocalDateTime startdate, LocalDateTime enddate){

        for(Room room : _rooms.values()){
            if(room.capacity() >= capacity){
                continue;
            }
            if(room.checkAvailability(startdate, enddate)){
                IsFull = false;
                return room;
            }
        }

        IsFull = true;
        return null;
    }
//-----------------------------------------------------------
    
    public void addRoom(Room room){
        _rooms.put(room.Name, room);
    }

    @Override
    public String toString() {
        HashMap<String, String> members = new HashMap<>();
        members.put("name",      this.Name);
        members.put("area",      this._areaID);
        members.put("noreviews", Integer.toString(this.NoReviews));
        members.put("rating",    Integer.toString(this.Rating));
        members.put("full",      this.IsFull ? "true" : "false");

        StringBuilder sb = new StringBuilder();
        for (String key : members.keySet()) {
            sb.append(key + "=" + members.get(key) + ";");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

}//Hotel
