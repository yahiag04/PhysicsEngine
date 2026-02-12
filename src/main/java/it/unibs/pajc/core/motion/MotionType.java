package it.unibs.pajc.core.motion;

public enum MotionType {
    PROJECTILE("Projectile Motion"),
    CIRCULAR("Circular Motion"),
    SIMPLE_HARMONIC("Simple Harmonic Motion"),
    PENDULUM("Pendulum Motion"),
    LINEAR("Linear Motion"),
    FREEFALL("Freefall"),
    PLAYER_CONTROLLED("Player Controlled");

    private final String displayName;

    MotionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
