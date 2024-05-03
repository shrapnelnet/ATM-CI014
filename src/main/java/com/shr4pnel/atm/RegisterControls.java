package com.shr4pnel.atm;

import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Controller for the register.fxml file
 * @author <a href="https://github.com/shrapnelnet">Tyler</a>
 * @version 1.4.0
 * @since 1.0.0
 */
public class RegisterControls {
    /** An instance of Database */
    Database db = new Database();
    /** Represents the password field */
    @FXML
    MFXPasswordField registerPassword;
    /** Represents the username field */
    @FXML
    MFXTextField registerUsername;
    /** An initially invisible block of text, used to display feedback on unsuccessful registration */
    @FXML
    Text registerFeedback;
    /** Used to perform operations on root controller while it is out of focus */
    private Controls rootController;

    /**
     * Registers a new account using the values of the text fields.
     * Called within the register.fxml file
     */
    @FXML
    void doRegister() {
        String password = registerPassword.getText();
        String username = registerUsername.getText();
        boolean success = db.createAccount(username, password);
        if (success) {
            this.rootController.display_secondary.setText(
                "Registration for \"" + username + "\" successful! Please log-in.");
            Stage stage = (Stage) registerUsername.getScene().getWindow();
            stage.close();
        }
        registerFeedback.setText("Registration failed. check logs");
    }

    /**
     * Used to create a handle between the current controller and the root controller
     * @param rootController A Controls instance representing the root window's controller
     */
    public void setPrimaryController(Controls rootController) {
        this.rootController = rootController;
    }
}
