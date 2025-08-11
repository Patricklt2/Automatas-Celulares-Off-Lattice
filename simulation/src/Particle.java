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

    // Spatial data for the Particle
    private final SpatialCoordinates spatialCoordinates;

    public Particle( double currentX,
                     double currentY,
                     double radius,
                     double velocity,
                     double thetaAngle,
                     double accessibleArea ) {
        this.radius = radius;
        this.velocity = velocity;
        this.spatialCoordinates = new SpatialCoordinates(currentX, currentY, thetaAngle, accessibleArea);
    }

    public void updatePosition() {

    }

    /*Getters*/
    public double getRadius() {
        return radius;
    }

    public double getVelocity() {
        return velocity;
    }

    public SpatialCoordinates getSpatialCoordinates() {
        return spatialCoordinates;
    }
}