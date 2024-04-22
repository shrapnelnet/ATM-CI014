package com.shr4pnel.atm;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
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

import java.io.IOException;
import java.util.HashMap;

public class Controls {
    boolean enterInConfirmState = false;
    Account account;
    // to map ids to integers
    HashMap<String, String> idMap = new HashMap<>();
    Database db = new Database();

    @FXML
    MFXTextField display_primary;

    @FXML
    TextArea display_secondary;

    @FXML
    ToggleGroup transactions;

    @FXML
    MenuItem logout;

    @FXML
    MenuItem login;

    public Controls() {
        String[] idStrings = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "zero"};
        String[] idDigits = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
        int i;
        for (i = 0; i < 10; ++i)
            idMap.put(idStrings[i], idDigits[i]);
    }

    @FXML
    public void initialize() {
        db.initialize();
        Log.trace("Controls::initialize: Initializing");
    }

    // responsible for opening browser window
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


    @FXML
    private void showLogin() throws IOException {
        Log.trace("Controls::showLogin: Hit");
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

    @FXML
    private void showRegister() throws IOException {
        Log.trace("Controls::showRegister: Hit");
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


    private String getTransactionType() {
        RadioMenuItem selected = (RadioMenuItem) transactions.getSelectedToggle();
        return selected.getText();
    }

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
        Log.trace("Controls::about: Showing about window");
        stage.show();
    }

    private void deposit(int transactionSum) {
        boolean success = db.deposit(account.username, transactionSum);
        if (success) {
            this.display_secondary.setText("£" + transactionSum + " deposit for account: " + account.username + " succeeded!");
            return;
        }
        this.display_secondary.setText("Deposit failed! check logs, and please still give me a first for the assignment!");
    }

    private void withdraw(int transactionSum) {
        boolean success = db.withdraw(account.username, transactionSum);
        if (success) {
            this.display_secondary.setText("£" + transactionSum + " withdrawal for account: " + account.username + " succeeded!");
            return;
        }
        this.display_secondary.setText("Withdrawal failed! check logs, and please still give me a first for the assignment!");
    }

    @FXML
    private void enter() {
        String transactionSumString = this.display_primary.getText();
        if (transactionSumString.isEmpty()) {
            return;
        }
        int transactionSum = Integer.parseInt(transactionSumString);
        if (!enterInConfirmState) {
            enterInConfirmState = true;
            this.display_secondary.setText("Would you like to " + getTransactionType().toLowerCase() + " £" + transactionSum + "? Press enter again to confirm.");
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
        }
        if (getTransactionType().equals("Deposit")) {

        }
    }

    @FXML
    private void backspace() {
        if (enterInConfirmState) {
            enterInConfirmState = false;
            this.display_secondary.setText("Transaction cancelled! enter new sum or change transaction.");
        }
        String displayContent = display_primary.getText();
        if (displayContent.isEmpty())
            return;
        String newDisplayContent = displayContent.substring(0, displayContent.length() - 1);
        display_primary.setText(newDisplayContent);
    }

    @FXML
    private void doLogout() {
        if (account == null) {
            Log.warn("Controls::doLogout: attempt to log-out without being logged in");
            return;
        }
        Log.trace("Controls::doLogout: Logging out");
        account = null;
        this.logout.setDisable(true);
        this.login.setDisable(false);
        this.display_primary.setText("");
        this.display_secondary.setText("Logged out!");
    }

    private void handleButtonNumberPress(String id) {
        if (account == null) {
            this.display_secondary.setText("Log-in to make a transaction!");
            return;
        }
        if (enterInConfirmState) {
            enterInConfirmState = false;
            this.display_secondary.setText("Transaction cancelled! enter new sum or change transaction.");
        }
        String numberToAppend = idMap.get(id);
        this.display_primary.appendText(numberToAppend);
    }

    public void handleButtonPress(ActionEvent event) {
        MFXButton source = (MFXButton) event.getSource();
        String id = source.getId();
        Log.trace("Controls::handleButtonPress: KEYPRESS: " + id);
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