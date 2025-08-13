import java.util.ArrayList;
import java.util.List;

/*
* Models each Particle
* */
public class Particle {
    // Particle velocity (should be) 0.03
    private final double velocity;

    // Particle coordinates
    private double currentX;
    private double currentY;

    // Particle Theta angle
    private double thetaAngle;

    private final int id;

    // Starts with no neighbors
    private List<Particle> neighbors = new ArrayList<>();

    public Particle( double currentX,
                     double currentY,
                     double velocity,
                     double thetaAngle,
                     int id
                     ) {
        this.velocity = velocity;
        this.currentX = currentX;
        this.currentY = currentY;
        this.thetaAngle = thetaAngle;
        this.id = id;
        this.neighbors.add(this);
    }

    public void updatePosition( double newX, double newY, double newThetaAngle ) {
        this.currentX = newX;
        this.currentY = newY;
        this.thetaAngle = newThetaAngle;
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

    public int getId() {
        return id;
    }

    public List<Particle> getNeighbors() {
        return neighbors;
    }

    public void addNeighbor(Particle neighbor) {
        neighbors.add(neighbor);
    }
}