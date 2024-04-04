import java.util.ArrayList;

import Model.Hotel;

public interface HotelDAO{

    Hotel find(String Name);

    void save(Hotel hotel);

    void delete(Hotel hotel);

    ArrayList<Hotel> findAll();

}