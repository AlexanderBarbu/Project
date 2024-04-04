import java.util.ArrayList;

import Model.User;

public class UsermDAO implements UserDAO{
    protected static ArrayList<User> users = new ArrayList<>();

    @Override
    public User find(String ID) {

        for(User current : users) {
            if(current.UID.equals(ID)) {
                return current;
            }
        }

        return null;
    }

    @Override
    public void save(User user) {
        users.add(user);
    }

    @Override
    public void delete(User User) {
        users.remove(User);
    }
    
    @Override
    public ArrayList<User> findAll() {
        return users;
    }
}
