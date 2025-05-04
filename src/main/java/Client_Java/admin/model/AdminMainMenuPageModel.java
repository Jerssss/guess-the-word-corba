package Client_Java.admin.model;

import Server_Java.idls.AdminIDL.*;

public class AdminMainMenuPageModel {

    private final AdminService adminService;
    private final String sessionToken;
    private final int adminID;

    public AdminMainMenuPageModel(AdminService adminService, String sessionToken, int adminID) {
        this.adminService = adminService;
        this.sessionToken = sessionToken;
        this.adminID = adminID;
    }
}
