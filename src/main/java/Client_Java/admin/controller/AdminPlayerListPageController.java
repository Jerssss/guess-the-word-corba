package Client_Java.admin.controller;

import Client_Java.admin.model.AdminPlayerListPageModel;
import Client_Java.admin.view.AdminPlayerListPageView;
import Shared_Files.PlayerAccount;
import javafx.collections.transformation.FilteredList;

public class AdminPlayerListPageController {

    private final AdminPlayerListPageModel model;
    private final AdminPlayerListPageView view;

    public AdminPlayerListPageController(AdminPlayerListPageModel model, AdminPlayerListPageView view) {
        this.model = model;
        this.view = view;

        initialize();
    }

    private void initialize() {
        view.initializeTableColumns();
        setupSearchFilter();
        view.playersTable.setItems(model.getPlayerList());
    }

    private void setupSearchFilter() {
        FilteredList<PlayerAccount> filteredData = new FilteredList<>(model.getPlayerList(), p -> true);

        view.searchTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal.toLowerCase();
            filteredData.setPredicate(player -> {
                if (filter == null || filter.isEmpty()) {
                    return true;
                }
                return player.getUsername().toLowerCase().contains(filter);
            });
        });

        view.playersTable.setItems(filteredData);
    }

    // Add methods for edit and delete actions here.
}
