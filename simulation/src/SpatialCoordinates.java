public class SpatialCoordinates {
    // Coordinates
    private final double currentX;
    private final double currentY;

    // Angle which the particle faces
    private final double thetaAngle;

    // This is the side L of LxL to check for bounds
    private final double sideL;

    public SpatialCoordinates(double currentX, double currentY, double thetaAngle, double sideL) {
        this.currentX = currentX;
        this.currentY = currentY;
        this.thetaAngle = thetaAngle;
        this.sideL = sideL;
    }

    public double getCurrentX() {
        return currentX;
    }

    public double getCurrentY() {
        return currentY;
    }

    public double getThetaAngle() {
        return thetaAngle;
    }

    public double getSideL() {
        return sideL;
    }
}
