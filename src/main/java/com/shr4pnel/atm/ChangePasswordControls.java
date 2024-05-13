package com.shr4pnel.atm;

import io.github.palexdev.materialfx.controls.MFXPasswordField;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChangePasswordControls {
    private static final Logger changePasswordControlsLogger = LogManager.getLogger();
    private static final Database db = new Database();
    /** Used to perform operations on root controller while it is out of focus */
    private Controls rootController;

    /** The field containing the original password during a password change. */
    @FXML
    MFXPasswordField oldPassword;

    /** The field containing the new password during a password change */
    @FXML
    MFXPasswordField newPassword;

    /** The field that displays feedback on the status of the password change operation */
    @FXML
    Text changePasswordFeedback;

    /**
     * Changes the currently logged-in users password if the old password matches
     * @throws IOException If changepassword.fxml cannot be found in resources
     */
    @FXML
    private void doChangePassword() throws IOException {
        String username = rootController.account.username;
        String oldPasswordString = oldPassword.getText();
        String newPasswordString = newPassword.getText();
        boolean success = db.changePassword(username, oldPasswordString, newPasswordString);
        if (success) {
            changePasswordControlsLogger.info("doChangePassword: Password change succeeded.");
            Stage stage = (Stage) oldPassword.getScene().getWindow();
            stage.close();
        }
        changePasswordControlsLogger.info("doChangePassword: Password change failed");
        changePasswordFeedback.setText("Password change failed.");
    }

    /**
     * Used to create a handle between the current controller and the root controller
     * @param rootController A Controls instance representing the root window's controller
     */
    public void setPrimaryController(Controls rootController) {
        this.rootController = rootController;
    }

}