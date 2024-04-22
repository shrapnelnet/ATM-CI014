package com.shr4pnel.atm;

import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class RegisterControls {
    private Controls rootController;
    Database db = new Database();

    @FXML
    MFXPasswordField registerPassword;

    @FXML
    MFXTextField registerUsername;

    @FXML
    Text registerFeedback;

    @FXML
    void initialize() {
        Log.trace("LoginControls:: initialize");
        db.initialize();
    }

    @FXML
    void doRegister() {
        String password = registerPassword.getText();
        String username = registerUsername.getText();
        boolean success = db.createAccount(username, password);
        if (success) {
            this.rootController.display_secondary.setText("Registration for \"" + username + "\" successful! Please log-in.");
            Stage stage = (Stage) registerUsername.getScene().getWindow();
            stage.close();
        }
        registerFeedback.setText("Registration failed. check logs");
    }

    public void setPrimaryController(Controls rootController) {
        this.rootController = rootController;
    }
}
