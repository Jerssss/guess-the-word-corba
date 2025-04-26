package Client_Java.util;

public class SessionChecker {
//    private final GameService gameService;
//    private volatile boolean running = true;
//
//    public SessionChecker(GameService gameService) {
//        this.gameService = gameService;
//    }
//
//    public void stop() {
//        running = false;
//    }
//
//    @Override
//    public void run() {
//        while (running) {
//            try {
//                Thread.sleep(3000); // check every 3 seconds (3000 milliseconds is within int range)
//
//                // Test if session is still valid
//                gameService.getRemainingAttempts(
//                        (int) PlayerClient_Java.getLoggedInPlayerID(),
//                        PlayerClient_Java.getSessionToken()
//                );
//
//            } catch (Exception e) {
//                running = false;
//
//                Platform.runLater(() -> {
//                    Alert alert = new Alert(AlertType.WARNING);
//                    alert.setTitle("Forced Logout");
//                    alert.setHeaderText("You have been logged out");
//                    alert.setContentText("Your session was terminated due to login from another terminal.");
//                    alert.showAndWait();
//
//                    PlayerClient_Java.setLoggedInPlayer(null);
//                    PlayerClient_Java.setSessionToken(null);
//
//                    try {
//                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
//                                getClass().getResource("/Client_Java/res/fxml/WWLoginPage.fxml")
//                        );
//                        javafx.scene.Parent root = loader.load();
//                        javafx.scene.Scene scene = new javafx.scene.Scene(root);
//                        PlayerClient_Java.getStage().setScene(scene);
//                        PlayerClient_Java.getStage().setTitle("What's the Word - Login");
//                        PlayerClient_Java.getStage().show();
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                });
//            }
//        }
//    }
}