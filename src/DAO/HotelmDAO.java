package DAO;

import java.util.ArrayList;


import Model.Hotel;

public class HotelmDAO implements HotelDAO {
    
    protected static ArrayList<Hotel> hotels = new ArrayList<>();

    @Override
    public Hotel find(String Name){

        for(Hotel current : hotels) {
            if(current.Name.equals(Name)) {
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
    public ArrayList<Hotel> findAll(){
        return hotels;
    }

}
