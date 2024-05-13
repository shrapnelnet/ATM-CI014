package com.shr4pnel.atm;

import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import java.io.FileNotFoundException;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Bundles stylesheet, and loads FXML into stages to be rendered into a stage
 * @author <a href="https://github.com/shrapnelnet">Tyler</a>
 * @version 1.4.0
 * @since 1.0.0
 */
public class Root extends Application {
    /** Logger instance for Root.java */
    private static final Logger rootLogger = LogManager.getLogger();
    /**
     * Takes stage injected by JavaFX and renders it.
     * MaterialFX bundles all necessary stylesheets
     *
     * @param stage Passed in automatically by JavaFX. used to render FXML
     * @throws FileNotFoundException If FXML is not present in resources folder
     */
    @Override
    public void start(Stage stage) throws FileNotFoundException {
        rootLogger.trace("start: Initializing");
        // https://github.com/palexdev/MaterialFX?tab=readme-ov-file#theming-system
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
        loader.setLocation(getClass().getResource("/com/shr4pnel/atm/interface.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException err) {
            rootLogger.error("interface.fxml not found");
            throw new FileNotFoundException();
        }
    }
}