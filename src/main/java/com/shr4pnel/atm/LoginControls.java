package com.shr4pnel.atm;

import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Controller for the login.fxml file.
 * @author <a href="https://github.com/shrapnelnet">Tyler</a>
 * @version 1.4.0
 * @since 1.0.0
 */
public class LoginControls {
    /** Used to represent account state from database */
    Account account;
    /** An instance of the database */
    Database db = new Database();
    /** The text field containing the username */
    @FXML
    MFXTextField loginUsername;
    /** The text field containing the password */
    @FXML
    MFXPasswordField loginPassword;
    /** An initially invisible block of text, used to display feedback on unsuccessful logins */
    @FXML
    Text loginFeedback;
    /** Used to perform operations on root controller while it is out of focus */
    private Controls rootController;

    /**
     * Logs in, using the values in the text fields.
     * Called within the login.fxml file
     */
    @FXML
    private void doLogin() {
        final String username = loginUsername.getText();
        final String password = loginPassword.getText();
        if (username.isEmpty() || password.isEmpty()) {
            return;
        }
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

    /**
     * Used to create a handle between the current controller and the root controller
     * @param rootController A Controls instance representing the root window's controller
     */
    public void setPrimaryController(Controls rootController) {
        this.rootController = rootController;
    }
}
