import java.util.ArrayList;



public class HotelmDAO implements HotelDAO{
    
    protected static ArrayList<Hotel> hotels = new ArrayList<>();

    @Override
    public Hotel find(String Name){

        for(Hotel current : hotels){
            if(current.Name == Name){
                return current;
            }
        }

        return null;
    }

    @Override
    public void save(Hotel hotel){
        hotels.add(hotel);
    }

    @Override
    public void delete(Hotel hotel){
        hotels.remove(hotel);
    }
    
    @Override
    public ArrayList<user> findAll(){
        return hotels;
    }

}
