package Model;

import Utility.PriceRange;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Hotel {

    public String Name;

    public float Rating = 0;
    public int NoReviews = 0;
    public boolean IsFull = false;

    private int _noRooms = 0;
    public String _areaID = "";
    private String _managerID = "";
    private String _image = "";

    private Map<String, Room> _rooms = new HashMap<>();
    

    public Hotel(String Name, String Aid, String MID) {
        this.Name       = Name;
        this._areaID    = Aid;
    }

    public Hotel(HotelJSON hotelJson) {
        this.Name = hotelJson.name;
        this.NoReviews = hotelJson.noOfReviews;
        this._areaID = hotelJson.area;
        this._managerID = hotelJson.managerId;
        this.Rating = hotelJson.rating;  
        this._image = hotelJson.image;
        this._noRooms = hotelJson.noOfRooms;
    }

    // When passing hotel data through messages, the string will
    // be something like 'name=Hotel;rating=3;areaID=342' etc.
    // so we want to be able to initialize a hotel object using this string.
    // This is basically the inverse of .toString()
    public Hotel(String data) {
        String[] params = data.split(";");
        //for (String param : params) {
            //System.out.println("[Hotel] " + param);
        //}
        System.out.println("[Hotel] " + data);
        for (String entry : params) {
            String[] parts = entry.split("=");
            String attributeName = parts[0];
            String attributeValue = parts[1];
            switch (attributeName.toLowerCase()) {
                case "name":
                    this.Name = attributeValue;
                    break;
                case "rating":
                    this.Rating = Float.parseFloat(attributeValue);
                    break;
                case "noreviews":
                    this.NoReviews = Integer.parseInt(attributeValue);
                    break;
                case "full":
                    this.IsFull = (attributeValue.equalsIgnoreCase("true"));
                    break;
                case "area":
                    this._areaID = attributeValue;
                    break;
                case "managerid":
                    this._managerID = attributeValue;
                    break;
                case "noofrooms":
                    this._noRooms = Integer.parseInt(attributeValue);
                    break;
                case "image":
                    this._image = attributeValue;
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
    
    public Room getRoom(String roomName) {
        return _rooms.get(roomName);
    }

    public Room getAvailableRoom(int capacity , LocalDateTime startdate, LocalDateTime enddate, PriceRange range){

        for(Room room : _rooms.values()){
            if(room.capacity() < capacity){
                continue;
            }
            if (!(room.getPrice()<= range.getTo() && room.getPrice() >= range.getFrom())){
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
        members.put("rating",    Float.toString(this.Rating));
        members.put("full",      this.IsFull ? "true" : "false");
        members.put("managerid", this._managerID);
        members.put("image",     this._image);
        members.put("noOfRooms",   Integer.toString(this._noRooms));

        StringBuilder sb = new StringBuilder();
        for (String key : members.keySet()) {
            sb.append(key + "=" + members.get(key) + ";");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public void setMID(String ID){
        //for (Room room : _rooms.values()){
            //room.set_managerID(ID);
        //}
        _managerID = ID;
    }

    public float getRating() {
        return Rating;
    }
}//Hotel
