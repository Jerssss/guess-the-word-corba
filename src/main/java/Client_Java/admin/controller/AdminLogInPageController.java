package Client_Java.admin.controller;

import Client_Java.admin.model.AdminLogInPageModel;
import Client_Java.admin.view.AdminLogInPageView;

public class AdminLogInPageController {
    private final AdminLogInPageView view;
    private final AdminLogInPageModel model;

    public AdminLogInPageController(AdminLogInPageModel model, AdminLogInPageView view) {
        this.view = view;
        this.model = model;
    }
}
