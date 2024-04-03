public class mDAOinit {
    
    private UsermDAO UserDAO;
    private HotelmDAO HotelDAO;

    public UsermDAO getUserDAO(){
        return new UsermDAO();
    }

    public HotelmDAO getHotelmDAO(){
        return new HotelmDAO();
    }

    public UsermDAO getInitUserDAO(){
        UserDAO = new UsermDAO();
        // INIT!!!!!!!! NOT READY
        return UserDAO;
    }
}
