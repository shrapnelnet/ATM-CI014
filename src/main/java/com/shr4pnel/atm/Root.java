package com.shr4pnel.atm;

import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Root extends Application {
    @Override
    public void start(Stage stage) throws FileNotFoundException {
        Log.trace("Root::start: Initializing");
        // https://github.com/palexdev/MaterialFX?tab=readme-ov-file#theming-system
        // this builder is in charge of merging the default "modena" stylesheet
        // with the materialfx root stylesheet
        // it looks awful without this!
        UserAgentBuilder.builder()
                .themes(JavaFXThemes.MODENA)
                .themes(MaterialFXStylesheets.forAssemble(false))
                .setDeploy(true)
                .setResolveAssets(true)
                .build()
                .setGlobal();

        FXMLLoader loader = new FXMLLoader();
        stage.setTitle("Shr4pnelATM");
        stage.setResizable(false);
        // todo standardize location
        loader.setLocation(getClass().getResource("/com/shr4pnel/atm/interface.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException err) {
            Log.error("interface.fxml not found");
            throw new FileNotFoundException();
        }
    }
}