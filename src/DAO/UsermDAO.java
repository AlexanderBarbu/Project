import java.util.ArrayList;

public class UsermDAO implements UserDAO{
    protected static ArrayList<user> users = new ArrayList<>();

    @Override
    public user find(String ID){

        for(user current : users){
            if(current.UID == ID){
                return current;
            }
        }

        return null;
    }

    @Override
    public void save(user User){
        users.add(User);
    }

    @Override
    public void delete(user User){
        users.remove(User);
    }
    
    @Override
    public ArrayList<user> findAll(){
        return users;
    }
}
