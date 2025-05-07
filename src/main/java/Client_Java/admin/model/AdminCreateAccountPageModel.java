package Client_Java.admin.model;


import AdminIDL.AccountExistsException;
import AdminIDL.AdminService;
import AdminIDL.NotLoggedInException;

public class AdminCreateAccountPageModel {
    private final AdminService adminService;
    private final String sessionToken;
    private final int adminID;

    public AdminCreateAccountPageModel(AdminService adminService, String sessionToken, int adminID) {
        this.adminService = adminService;
        this.sessionToken = sessionToken;
        this.adminID = adminID;
    }

    public void createPlayer(String username, String password) throws AccountExistsException, NotLoggedInException {
        adminService.createPlayer(username, password, sessionToken, adminID);
    }
}