package com.shr4pnel.atm;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.io.IOException;
import java.util.HashMap;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Controller for interface.fxml and about.fxml
 * Handles all user keypresses, and passes them to Database when necessary
 * @author <a href="https://github.com/shrapnelnet">Tyler</a>
 * @version 1.4.0
 * @since 1.0.0
 */
public class Controls {
    /** Logger instance for Controls */
    private final Logger controlsLogger = LogManager.getLogger();
    /** Checks if enter has been previously pressed */
    boolean enterInConfirmState = false;
    /** Holds the logged-in user's account */
    Account account;
    /** Used to map fx:id to string representations of actual integers, so they can be appended to the primary display */
    HashMap<String, String> idMap = new HashMap<>();
    /** A handle to the current database */
    Database db = new Database();

    /** The primary display, displays the number that will be used in the next transaction */
    @FXML
    MFXTextField display_primary;

    /** The secondary display, shows informative information on the current operation */
    @FXML
    TextArea display_secondary;

    /** Represents the radio menu group of all the available transactions */
    @FXML
    ToggleGroup transactions;

    /** The button used to log-out */
    @FXML
    MenuItem logout;

    /** The button used to log-in */
    @FXML
    MenuItem login;

    /** The button used to change password */
    @FXML
    MenuItem changePassword;

    /**
     * Constructor for Controls
     * Used to map button-presses to be used in a switch statement within handleButtonPress()
     * @see Controls#handleButtonPress(ActionEvent)
     */
    public Controls() {
        String[] idStrings = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "zero"};
        String[] idDigits = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
        int i;
        for (i = 0; i < 10; ++i) {
            idMap.put(idStrings[i], idDigits[i]);
        }
    }

    /**
     * Called automatically by JavaFX
     * Used to initialize database
     * @see Database#initialize()
     */
    @FXML
    public void initialize() {
        db.initialize();
        controlsLogger.trace("initialize: Initializing");
    }

    /**
     * Opens the browser to display the GNU GPLv3 license
     */
    @FXML
    private void showLicense() {
        // applications have to be instantiated with this override even if you don't use it
        // blame gluon for not making it static!
        Application a = new Application() {
            @Override
            public void start(Stage stage) {
            }
        };
        HostServices hostServices = a.getHostServices();
        hostServices.showDocument("https://www.gnu.org/licenses/gpl-3.0.en.html#license-text");
    }


    /**
     * Opens a new stage to display the login window
     * @throws IOException If login.fxml is not found in resources folder
     */
    @FXML
    private void showLogin() throws IOException {
        controlsLogger.trace("showLogin: Hit");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/shr4pnel/atm/login.fxml"));
        Parent login = loader.load();
        // to access controls from different stage
        LoginControls loginControls = loader.getController();
        loginControls.setPrimaryController(this);
        Stage stage = new Stage();
        stage.setTitle("Login");
        Scene scene = new Scene(login);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Opens a new stage to display the registration window
     * @throws IOException If register.fxml is not found in resources folder
     */
    @FXML
    private void showRegister() throws IOException {
        controlsLogger.trace("showRegister: Hit");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/shr4pnel/atm/register.fxml"));
        Parent login = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Register");
        RegisterControls registerControls = loader.getController();
        registerControls.setPrimaryController(this);
        Scene scene = new Scene(login);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Gets the currently selected transaction within transaction menu
     * @return String containing text value of selected RadioMenuItem transaction type
     */
    private String getTransactionType() {
        RadioMenuItem selected = (RadioMenuItem) transactions.getSelectedToggle();
        return selected.getText();
    }

    /**
     * Opens a new stage to display the about window
     * @throws IOException If about.fxml not found in resources folder
     */
    @FXML
    private void about() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/shr4pnel/atm/about.fxml"));
        Parent about = loader.load();
        Stage stage = new Stage();
        stage.setTitle("About");
        Scene scene = new Scene(about);
        stage.setScene(scene);
        stage.setX(600);
        stage.setY(400);
        controlsLogger.trace("about: Showing about window");
        stage.show();
    }

    /**
     * Deposits money into users account
     * @param transactionSum Amount of money to deposit
     */
    private void deposit(int transactionSum) {
        long initialBalance = db.balance(account.username);
        long totalBalance = initialBalance + transactionSum;
        if (totalBalance > Integer.MAX_VALUE) {
            controlsLogger.warn("deposit: Deposit would cause integer overflow");
            display_secondary.setText("Don't you think you have enough money already?");
            display_primary.setText("");
            return;
        }
        boolean success = db.deposit(account.username, transactionSum);
        if (success) {
            display_secondary.setText(
                "£" + transactionSum + " deposit for account: " + account.username + " succeeded!");
            return;
        }
        display_secondary.setText(
            "Deposit failed! check logs, and please still give me a first for the assignment!");
    }

    /**
     * Withdraws money from current users account
     * @param transactionSum Amount of money to withdraw
     */
    private void withdraw(int transactionSum) {
        int balance = db.balance(account.username);
        if (balance < transactionSum) {
            display_primary.setText("");
            display_secondary.setText("You can't make a withdrawal of £" + transactionSum + " as your balance is only £" + balance + ".");
            return;
        }
        boolean success = db.withdraw(account.username, transactionSum);
        if (success) {
            display_secondary.setText("£" + transactionSum + " withdrawal for account: " + account.username + " succeeded!");
            return;
        }
        display_secondary.setText("Withdrawal failed! check logs, and please still give me a first for the assignment!");
    }

    /**
     * Displays the current users balance in the secondary display box
     */
    private void balance() {
        display_primary.setText("");
        int balance;
        try {
            balance = db.balance(account.username);
        } catch (NullPointerException err) {
            controlsLogger.warn("balance: Attempt to read username while no user is logged in");
            return;
        }
        display_secondary.setText("Your balance is: £" + balance);
    }

    /**
     * Handles the user pressing enter on the keypad, and decides which transaction function to pass the transaction sum in the primary display to.
     */
    @FXML
    private void enter() {
        String transactionSumString = display_primary.getText();
        int transactionSum = 0;
        if (!transactionSumString.isEmpty()) {
            try {
                transactionSum = Integer.parseInt(transactionSumString);
            } catch (NumberFormatException err) {
                controlsLogger.warn("enter: Number exceeds 4 bytes, would throw NumberFormatException");
                display_secondary.setText("That's way too much money!");
                display_primary.setText("");
                return;
            }
        }
        if (transactionSum == 0 && !getTransactionType().equals("Balance")) {
            return;
        }
        if (!enterInConfirmState && !getTransactionType().equals("Balance")) {
            enterInConfirmState = true;
            display_secondary.setText("Would you like to " + getTransactionType().toLowerCase() + " £" + transactionSum + "? Press enter again to confirm.");
            return;
        }
        enterInConfirmState = false;
        switch (getTransactionType()) {
            case "Deposit":
                deposit(transactionSum);
                break;
            case "Withdraw":
                withdraw(transactionSum);
                break;
            case "Balance":
                balance();
                break;
        }
    }

    /**
     * Removes a character from the end of the primary display.
     */
    @FXML
    private void backspace() {
        if (enterInConfirmState) {
            enterInConfirmState = false;
            display_secondary.setText(
                "Transaction cancelled! enter new sum or change transaction.");
            return;
        }
        String displayContent = display_primary.getText();
        if (displayContent.isEmpty()) {
            return;
        }
        String newDisplayContent = displayContent.substring(0, displayContent.length() - 1);
        display_primary.setText(newDisplayContent);
    }

    /**
     * Logs the currently logged-in user out.
     */
    @FXML
    private void doLogout() {
        if (account == null) {
            controlsLogger.warn("doLogout: attempt to log-out without being logged in");
            return;
        }
        controlsLogger.trace("doLogout: Logging out");
        account = null;
        logout.setDisable(true);
        login.setDisable(false);
        changePassword.setDisable(true);
        display_primary.setText("");
        display_secondary.setText("Logged out!");
    }

    /**
     * Opens the changewindow.fxml scene in a new window
     * @throws IOException if changewindow.fxml is not found within the resources folder
     */
    @FXML
    private void doOpenChangePassword() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/com/shr4pnel/atm/changepassword.fxml"));
        Parent changePassword = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Change password");
        ChangePasswordControls changePasswordControls = loader.getController();
        changePasswordControls.setPrimaryController(this);
        Scene scene = new Scene(changePassword);
        stage.setScene(scene);
        controlsLogger.trace("changePassword: Showing change password window");
        stage.show();
    }


    /**
     * Parses the number pressed, and appends it to the primary display
     * @param id The fx:id of the button pressed
     */
    private void handleButtonNumberPress(String id) {
        if (account == null) {
            display_secondary.setText("Log-in to make a transaction!");
            return;
        }
        if (enterInConfirmState) {
            enterInConfirmState = false;
            display_secondary.setText(
                "Transaction cancelled! enter new sum or change transaction.");
        }
        String numberToAppend = idMap.get(id);
        display_primary.appendText(numberToAppend);
    }

    /**
     * Handles a numeric or function button being pressed on the keypad.
     * @param event Passed in automatically on keypress. Represents the button pressed. Used to get the fx:id of the button pressed
     */
    public void handleButtonPress(ActionEvent event) {
        MFXButton source = (MFXButton) event.getSource();
        String id = source.getId();
        controlsLogger.trace("handleButtonPress: KEYPRESS: {}", id);
        switch (id) {
            case "backspace":
                backspace();
                break;

            case "one":
            case "two":
            case "three":
            case "four":
            case "five":
            case "six":
            case "seven":
            case "eight":
            case "nine":
            case "zero":
                handleButtonNumberPress(id);
                break;

            case "enter":
                enter();
                break;
        }
    }
}