import java.util.ArrayList;

import Model.User;

public interface UserDAO {

    User find(String ID);

    void save(User user);

    void delete(User user);

    ArrayList<User> findAll();
}