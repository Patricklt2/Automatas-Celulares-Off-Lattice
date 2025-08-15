import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;

/*
* Models each simulation
* 
* */

public class Simulation {
    private List<Particle> particles;
    private int N;
    private double timeStep;
    private int maxIterations;
    private int L;
    private double rc; // interaction radius for all particles
    private int M;
    private double nu;
    private double density;

    public Simulation(int N, double timeStep, int maxIterations, int L, double radius, double nu) {
        resetVariables(N, timeStep, maxIterations, L, radius, nu);
    }

    public void setDensity(double density){
        this.density = density;
    }

    public void setL(int L){
        this.L = N;
    }

    public void setNu(double nu){
        this.nu = nu;
    }
    
    private void writeParticleDataToFile(String fileName, int step, List<Particle> particles) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write("t:" + step + "\n");
            for (Particle particle : particles) {
                writer.write(String.format("%d;%.6f;%.6f;%.6f",
                        particle.getId(),
                        particle.getCurrentX(),
                        particle.getCurrentY(),
                        particle.getThetaAngle()));
                writer.newLine();
            }
            writer.write("polarization:" + calculatePolarization() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeDataToFile(String fileName, String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Particle> generateParticles() {
        List<Particle> particles = new ArrayList<>(N);
        final double velocity = 0.03;

        for (int i = 0; i < N; i++) {
            double currentX = new Random().nextDouble() * L; // Random X position within L
            double currentY = new Random().nextDouble() * L; // Random Y position within L
            double thetaAngle = Math.toRadians(new Random().nextDouble() * 360); // randomize 0 to 360 degrees
            Particle particle = new Particle(currentX, currentY, velocity, thetaAngle, i);
            particles.add(particle);
        }

        return particles;
    }

    // Cell Index Method
    private void findNeighbors() {
        HashMap<Cell, List<Particle>> cellMap = new HashMap<>();

        for (Particle particle : particles) {
            int cellX = (int) (particle.getCurrentX() / rc);
            int cellY = (int) (particle.getCurrentY() / rc);

            cellX = ((cellX % M) + M) % M;
            cellY = ((cellY % M) + M) % M;

            Cell cell = new Cell(cellX, cellY);
            cellMap.computeIfAbsent(cell, k -> new ArrayList<>()).add(particle);
        }

        for (Entry<Cell, List<Particle>> entry : cellMap.entrySet()) {
            Cell cell = entry.getKey();
            List<Particle> cellParticles = entry.getValue();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int neighborX = (cell.x + dx + M) % M;
                    int neighborY = (cell.y + dy + M) % M;

                    Cell neighborCell = new Cell(neighborX, neighborY);
                    List<Particle> neighborParticles = cellMap.getOrDefault(neighborCell, Collections.emptyList());

                    for (Particle particle : cellParticles) {
                        for (Particle neighbor : neighborParticles) {
                            if (particle.getId() >= neighbor.getId()) {
                                continue;
                            }

                            double dxPos = neighbor.getCurrentX() - particle.getCurrentX();
                            double dyPos = neighbor.getCurrentY() - particle.getCurrentY();

                            dxPos -= Math.round(dxPos / L) * L;
                            dyPos -= Math.round(dyPos / L) * L;

                            double distance = Math.sqrt(dxPos * dxPos + dyPos * dyPos);

                            if (distance <= rc) {
                                particle.addNeighbor(neighbor);
                                neighbor.addNeighbor(particle);
                            }
                        }
                    }
                }
            }
        }
    }

    private void bruteForceMethod(){
        for(int i = 0; i < N; i++ ){
            for(int j = i + 1; j < N; j++){
                double dx = particles.get(i).getCurrentX() - particles.get(j).getCurrentX();
                double dy = particles.get(i).getCurrentY() - particles.get(j).getCurrentY();

                dx = dx - Math.round(dx / L) * L;
                dy = dy - Math.round(dy / L) * L;

                double distance = Math.sqrt(dx*dx + dy*dy);

                if(distance < rc){
                    particles.get(i).addNeighbor(particles.get(j));
                    particles.get(j).addNeighbor(particles.get(i));
                }
            }
        }
    }

    private void updatePositions(int iteration){
        List<Particle> updatedParticlesPositions = new ArrayList<>(N);
        for(Particle particle: particles) {
            List<Particle> neighbors = particle.getNeighbors();
            double cosSum = 0;
            double sinSum = 0;

            // Delta Theta is a random number chosen with a uniform probability from the
            // interval [—theta/2, theta/2].
            double noise = (Math.random() - 0.5) * this.nu;
            for(Particle neighbor: neighbors){
                cosSum += Math.cos(neighbor.getThetaAngle());
                sinSum += Math.sin(neighbor.getThetaAngle());
            }

            double newX = (particle.getCurrentX()
                    + particle.getVelocity() * Math.cos(particle.getThetaAngle()) * timeStep) % L;
            double newY = (particle.getCurrentY()
                    + particle.getVelocity() * Math.sin(particle.getThetaAngle()) * timeStep) % L;

            if (newX < 0)
                newX += L;
            if (newY < 0)
                newY += L;

            // average theta calculation in radians
            double averageTheta = Math.atan2(sinSum / neighbors.size(), cosSum / neighbors.size());
            double newThetaAngle = averageTheta + noise;

            // particle with updated position and angle
            Particle updatedParticle = new Particle(newX, newY, particle.getVelocity(), newThetaAngle, particle.getId());
            updatedParticlesPositions.add(updatedParticle);
        }
        particles = updatedParticlesPositions;
    }

    private double calculatePolarization() {
        // sum of velocity components for each particle
        double velocityX = 0.0;
        double velocityY = 0.0;
        for(Particle particle : particles) {
            velocityX += particle.getVelocity() * Math.cos(particle.getThetaAngle());
            velocityY += particle.getVelocity() * Math.sin(particle.getThetaAngle());   
        }
        // calculate the magnitude of the composite velocity vector
        double magnitude = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        return ((magnitude) / (N * particles.getFirst().getVelocity()));
    }

    public void runSimulationForAnimation(String filePath) {
        writeParticleDataToFile(filePath, 0, particles);
        for (int i = 1; i <= maxIterations; i++){
            bruteForceMethod();
            updatePositions(i);
            writeParticleDataToFile(filePath, i, particles);
        }
        writeDataToFile(filePath, String.format("density:%.3f\n", density));
    }

    public void runSimulationForPolarization(String filePath, double nu) {
        setNu(nu);
        for(int i = 1; i <= maxIterations; i++){
            bruteForceMethod();
            updatePositions(i);
        }
        writeDataToFile(filePath, String.format("%.5f;%.5f\n", calculatePolarization(), nu));
    }

    // todo no entiendo lo que tengo que plottear, esto está mal
    // note: L is constant, we increase density by increasing N -> check for d between 0 and 10
    public void runSimulationForDensity(String filePath, int N){
        setDensity((double) N /L);
        for(int i = 1; i <= maxIterations; i++){
            bruteForceMethod();
            updatePositions(i);
        }
        writeDataToFile(filePath, String.format("%.5f;%.5f\n", calculatePolarization(), density));
    }

    public void resetVariables(int N, double timeStep, int maxIterations, int L, double radius, double nu) {
        this.N = N;
        this.timeStep = timeStep;
        this.maxIterations = maxIterations;
        this.L = L;
        this.rc = 2 * radius;
        this.M = (int) Math.floor((double)L / rc);
        this.nu = nu;
        this.density = (double) N / (L * L);
        this.particles = generateParticles();
    }

    private final static class Cell {
        final int x, y;

        Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Cell cell))
                return false;
            return x == cell.x && y == cell.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}

/*
 * Structure of the simulation:
 * t1 
 * 1 x y theta
 * ...
 * ..
 * N x y theta
 * 
 * ...
 * tn
 * ...
 */