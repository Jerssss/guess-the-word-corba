package Client_Java.admin.model;


import Server_Java.idls.AdminIDL.AdminService;
import Server_Java.idls.AdminIDL.NotLoggedInException;

public class AdminEditConfigurationsPageModel {
    private final AdminService adminService;
    private final String sessionToken;
    private final int adminID;

    public AdminEditConfigurationsPageModel(AdminService adminService, String sessionToken, int adminID) {
        this.adminService = adminService;
        this.sessionToken = sessionToken;
        this.adminID = adminID;
    }

    public void modifyRoundDuration(int roundTime) throws NotLoggedInException {
        adminService.modifyRoundDuration(roundTime, sessionToken, adminID);
    }

    public void modifyWaitingTime(int waitTime) throws NotLoggedInException {
        adminService.modifyWaitingTime(waitTime, sessionToken, adminID);
    }
}