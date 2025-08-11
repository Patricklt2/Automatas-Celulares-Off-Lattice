import java.math.*;

/*
* Models each Particle
*
* */
public class Particle {

    // Coordinates
    private final double currentX;
    private final double currentY;

    // Interaction radius with other particles
    private final double radius;

    // Particle velocity (should be) 0.03
    private final double velocity;

    // Angle which the particle faces
    private final double thetaAngle;

    // This would be the LxL to check for bounds
    private final double accessibleArea;


    public Particle(final double currentX,
                    final double currentY,
                    final double radius,
                    final double velocity,
                    final double thetaAngle,
                    final double accessibleArea) {
        this.currentX = currentX;
        this.currentY = currentY;
        this.radius = radius;
        this.velocity = velocity;
        this.thetaAngle = thetaAngle;
        this.accessibleArea = accessibleArea;
    }

    public void updatePosition() {
        
    }

    /*Getters*/
    public double getCurrentX() {
        return currentX;
    }

    public double getCurrentY() {
        return currentY;
    }

    public double getRadius() {
        return radius;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getThetaAngle() {
        return thetaAngle;
    }

    public double getAccessibleArea() {
        return accessibleArea;
    }
}