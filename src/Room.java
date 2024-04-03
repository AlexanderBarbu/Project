import java.util.jar.Attributes.Name;

public class Room {
    
    public String Name; //KEY

    public int Rating;
    public int NoReviews;
    public boolean IsFull;

    private int _capacity;
    private int _areaID;
    private String _imgPath;
    private int _managerID;

    public Room(String Name, int R, int NR, int Cap, int Aid, String path, int MID) {

        this.Name       = Name;
        this.Rating     = R;
        this.NoReviews  = NR;
        this._capacity  = Cap;
        this._areaID    = Aid;
        this._imgPath   = path;
        this._managerID = MID;
        IsFull = false;
    }

//----------------GETTERS------------------------------------

    public int capacity() { return _capacity;  }
    public int areaID()   { return _areaID;    }
    public int managerID(){ return _managerID; }

//----------------SETTERS------------------------------------ 

    public void setCapacity(int cap) { _capacity = cap;}
    
}// Room
