package Authentication;

import DAO.UserDAO;
import DAO.UsermDAO;
import Model.User;

public class UserManager {

    private static UserDAO dao = new UsermDAO();

    public static User getUser(String id) {
        User user = dao.find(id);
        if (user == null) {
            user = new User();
            user.Name = "Default";
            user.Surname = "User";
        }        
        return user;
    }

    public static void addUser(User user) {
        if (user != null) {
            dao.save(user);
        }
    }
}
