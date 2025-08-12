import java.math.*;

/*
* Models each Particle
*
* */
public class Particle {
    // Interaction radius with other particles
    private final double radius;

    // Particle velocity (should be) 0.03
    private final double velocity;

    // Particle coordinates
    private double currentX;
    private double currentY;

    // Particle Theta angle
    private double thetaAngle;

    // side L of the LxL space
    private final double sideL;

    public Particle( double currentX,
                     double currentY,
                     double radius,
                     double velocity,
                     double thetaAngle,
                     double accessibleArea ) {
        this.radius = radius;
        this.velocity = velocity;
        this.currentX = currentX;
        this.currentY = currentY;
        this.thetaAngle = thetaAngle;
        this.sideL = Math.sqrt(accessibleArea); // check!!!
    }

    public void updatePosition( double newX, double newY, double newThetaAngle ) {
        this.currentX = newX;
        this.currentY = newY;
        this.thetaAngle = newThetaAngle;
    }

    /*Getters*/
    public double getRadius() {
        return radius;
    }

    public double getVelocity() {
        return velocity;
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

}