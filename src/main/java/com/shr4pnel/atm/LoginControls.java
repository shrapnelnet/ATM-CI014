package com.shr4pnel.atm;

import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginControls {
    Account account;
    Database db = new Database();

    private Controls rootController;

    @FXML
    void initialize() {
        Log.trace("LoginControls:: initialize");
        db.initialize();
    }

    @FXML
    MFXTextField loginUsername;

    @FXML
    MFXPasswordField loginPassword;

    @FXML
    Text loginFeedback;

    @FXML
    private void doLogin() {
        final String username = loginUsername.getText();
        final String password = loginPassword.getText();
        if (username.isEmpty() || password.isEmpty())
            return;
        account = db.tryLogin(username, password);
        if (account == null) {
            Log.warn("LoginControls::doLogin: No such account");
            loginFeedback.setText("No such account");
            return;
        }
        Log.trace("Logged in");
        // https://stackoverflow.com/questions/10751271/accessing-fxml-controller-class
        // this is how i modify the main window from a seperate scene
        // null point exceptions galore if you don't set it before you show the scene!
        if (rootController != null) {
            rootController.display_secondary.setText("Logged in as " + username + "!");
            rootController.logout.setDisable(false);
            rootController.login.setDisable(true);
            rootController.account = account;
        }
        Stage stage = (Stage) loginUsername.getScene().getWindow();
        stage.close();
    }

    public void setPrimaryController(Controls rootController) {
        this.rootController = rootController;
    }
}
