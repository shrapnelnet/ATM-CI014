package com.shr4pnel.atm;

import static javafx.application.Application.launch;


// Launching should be done from separate class
// this is how JavaFX recommends this should be done
/**
 * The entrypoint to the JavaFX ATM program.
 * Launches the Root class.
 * @author <a href="https://github.com/shrapnelnet">Tyler</a>
 * @version v1.4.0
 * @since v1.0.0
 */
public class Main {
    /**
     * Launches class "Root", using opening point "start()"
     * @param args Variable length command length arguments. Unused.
     * @see javafx.application.Application#launch(Class, String...)
     * @since v1.0.0
     */
    public static void main(String[] args) {
        launch(Root.class);
    }
}
