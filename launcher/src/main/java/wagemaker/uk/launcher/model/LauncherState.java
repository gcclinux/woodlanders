package wagemaker.uk.launcher.model;

/**
 * Represents the coarse UI state for the launcher primary action.
 */
public enum LauncherState {
    CHECKING(false),
    READY_TO_LAUNCH(true),
    NEEDS_UPDATE(true),
    OFFLINE_READY(true),
    BLOCKED(true),
    UPDATING(false),
    LAUNCHING(false),
    ERROR(true);

    private final boolean primaryActionEnabled;

    LauncherState(boolean primaryActionEnabled) {
        this.primaryActionEnabled = primaryActionEnabled;
    }

    public boolean isPrimaryActionEnabled() {
        return primaryActionEnabled;
    }
}
