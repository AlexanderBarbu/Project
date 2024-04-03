import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes.Name;

public class Room {
    
    public String Name; //KEY

    public int Rating;
    public int NoReviews;
    public boolean IsFull;

    protected List<Reservation> reservations = new ArrayList<>(); // all reservations of this room
    protected boolean[][] availabilityPreview = new boolean[12][31]; // the idea we talked about the other day

    private int _capacity;
    private String _areaID;
    private String _imgPath;
    private String _managerID;

    public Room(String Name, int R, int NR, int Cap, String Aid, String path, String MID) {

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
    public String areaID()   { return _areaID;    }
    public String managerID(){ return _managerID; }

//----------------SETTERS------------------------------------ 

    public void setCapacity(int cap) { _capacity = cap;}

//----------------------------------------------------------- 

    public boolean checkAvailability(LocalDateTime start, LocalDateTime end){
        int startday = start.getDayOfMonth();
        int startmonth = start.getMonthValue();
        int endday = end.getDayOfMonth();
        int endmonth = end.getMonthValue();

        for(int j = startmonth; j<= endmonth; startmonth++){
            for (int i=startday; i <= endday; i++){
                
                if(availabilityPreview[j][i]){
                    return false;
                }
                //Complexity is O(n^2 on paper but on worst case if endmonth > startmonth which is unlikely)
            }
        }//for 1

        return true;
    }
    
}// Room
