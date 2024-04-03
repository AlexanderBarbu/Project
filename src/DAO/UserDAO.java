import java.util.ArrayList;

public interface UserDAO{

    user find(String ID);

    void save(user User);

    void delete(user User);

    ArrayList<user> findAll();
}