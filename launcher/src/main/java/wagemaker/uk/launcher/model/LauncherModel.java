package wagemaker.uk.launcher.model;

/**
 * Snapshot of the UI state presented to the player.
 */
public record LauncherModel(
        LauncherState state,
        String primaryActionLabel,
        boolean primaryActionEnabled,
        String message,
        String localVersion,
        String remoteVersion
) {
    public static LauncherModel checking() {
        return new LauncherModel(LauncherState.CHECKING, "Checking…", false,
                "Contacting Woodlanders release service…", "n/a", "n/a");
    }

    public LauncherModel withMessage(String newMessage) {
        return new LauncherModel(state, primaryActionLabel, primaryActionEnabled, newMessage, localVersion, remoteVersion);
    }
}
