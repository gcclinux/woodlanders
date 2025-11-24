package wagemaker.uk.launcher;

import wagemaker.uk.launcher.ui.LauncherApplication;

/**
 * Main entry point that launches the JavaFX application.
 * This wrapper helps avoid module system issues when running as a fat JAR.
 */
public class Main {
    public static void main(String[] args) {
        LauncherApplication.launch(LauncherApplication.class, args);
    }
}
