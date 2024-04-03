
public interface HotelDAO{

    Hotel find(Name);

    void save(Hotel hotel);

    void delete(Hotel hotel);

    ArrayList<Hotel> findAll();

}