import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
* Models each simulation
* 
* */

public final class Simulation {
    private List<Particle> particles;
    private List<Particle> initialSnapshot;
    private int N;
    private double timeStep;
    private int maxIterations;
    private int L;
    private double rc; // interaction radius for all particles
    private int M;
    private double nu;
    private double density;
    private final Random random;

    public Simulation(int N, double timeStep, int maxIterations, int L, double radius, double nu) {
        this.N = N;
        this.timeStep = timeStep;
        this.maxIterations = maxIterations;
        this.L = L;
        this.rc = radius;
        this.M = (int) Math.floor((double)L / rc);
        this.nu = nu;
        this.density = (double) N / (L * L);
        this.random = new Random();
        regenerateParticles();
    }


    private Simulation(int N, double timeStep, int maxIterations, int L, double rc, double nu,
                    List<Particle> particles, List<Particle> initialSnapshot, int M, double density) {
        this.N = N;
        this.timeStep = timeStep;
        this.maxIterations = maxIterations;
        this.L = L;
        this.rc = rc;
        this.nu = nu;
        this.M = M;
        this.density = density;
        this.random = new Random();
        this.particles = particles.stream().map(Particle::copy).collect(Collectors.toCollection(ArrayList::new));
        this.initialSnapshot = initialSnapshot.stream().map(Particle::copy).collect(Collectors.toCollection(ArrayList::new));
    }

    public Simulation clone() {
        return new Simulation(N, timeStep, maxIterations, L, rc, nu, particles, initialSnapshot, M, density);
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
    
    public void setN(int N){
        this.N = N;
        this.density = (double) N / (L * L);
    }

    private void writeParticleDataToFile(String fileName, int step, List<Particle> particles) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write("t - " + step + "\n");
            for (Particle particle : particles) {
                StringBuilder strBuilder = new StringBuilder();
                strBuilder.append(particle.getId()).append(";")
                        .append(particle.getCurrentX()).append(";")
                        .append(particle.getCurrentY()).append(";")
                        .append(particle.getThetaAngle()).append("\n");
                writer.write(strBuilder.toString());
            }
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
            double currentX = random.nextDouble() * L; // Random X position within L
            double currentY = random.nextDouble() * L; // Random Y position within L
            double thetaAngle = (random.nextDouble() * 2.0 * Math.PI) - Math.PI;
            Particle particle = new Particle(currentX, currentY, velocity, thetaAngle, i);
            particles.add(particle);
        }

        return particles;
    }

    public void resetParticlesToInitialSnapshot() {
        this.particles = initialSnapshot.stream()
            .map(Particle::copy)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public void regenerateParticles() {
        this.particles = generateParticles();
        this.initialSnapshot = particles.stream()
            .map(Particle::copy)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private void findNeighbors() {
        HashMap<Cell, List<Particle>> cellMap = new HashMap<>();
        double cell_size = (double) L / M;

        for (Particle particle : particles) {
            int cellX = (int) ((particle.getCurrentX() / cell_size) + M) % M;
            int cellY = (int) ((particle.getCurrentY() / cell_size) + M) % M;

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

    private void findNeighborsParallel(){
        ConcurrentHashMap<Cell, List<Particle>> cellMap = new ConcurrentHashMap<>();
        double cell_size = (double) L / M;

        particles.parallelStream().forEach(particle -> {
            int cellX = (int) ((particle.getCurrentX() / cell_size) + M) % M;
            int cellY = (int) ((particle.getCurrentY() / cell_size) + M) % M;
            Cell cell = new Cell(cellX, cellY);

            cellMap.compute(cell, (k, list) -> {
                if (list == null) list = new ArrayList<>();
                list.add(particle);
                return list;
            });
        });

        Map<Integer, List<Integer>> tempNeighbors = new ConcurrentHashMap<>();

        particles.parallelStream().forEach(p -> {
            List<Integer> neighborsForParticle = new ArrayList<>();

            int cellX = (int) ((p.getCurrentX() / cell_size) + M) % M;
            int cellY = (int) ((p.getCurrentY() / cell_size) + M) % M;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int neighborX = (cellX + dx + M) % M;
                    int neighborY = (cellY + dy + M) % M;
                    Cell neighborCell = new Cell(neighborX, neighborY);

                    List<Particle> neighborParticles = cellMap.getOrDefault(neighborCell, Collections.emptyList());
                    for (Particle neighbor : neighborParticles) {
                        if (p.getId() >= neighbor.getId()) continue;

                        double dxPos = neighbor.getCurrentX() - p.getCurrentX();
                        double dyPos = neighbor.getCurrentY() - p.getCurrentY();

                        dxPos -= Math.round(dxPos / L) * L;
                        dyPos -= Math.round(dyPos / L) * L;

                        double distance = Math.sqrt(dxPos * dxPos + dyPos * dyPos);
                        if (distance <= rc) {
                            neighborsForParticle.add(neighbor.getId());
                        }
                    }
                }
            }
            tempNeighbors.put(p.getId(), neighborsForParticle);
        });

        Map<Integer, Particle> particleById = particles.stream()
                .collect(Collectors.toMap(Particle::getId, Function.identity()));

        for (Map.Entry<Integer, List<Integer>> entry : tempNeighbors.entrySet()) {
            Particle p = particleById.get(entry.getKey());
            for (Integer neighborId : entry.getValue()) {
                Particle neighbor = particleById.get(neighborId);
                p.addNeighbor(neighbor);
                neighbor.addNeighbor(p);
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

    public void compareCellAndBrute() {
        findNeighborsParallel();
        Map<Integer, Set<Integer>> cellNeighbors = snapshotNeighbors();
        clearNeighbors();

        bruteForceMethod();
        Map<Integer, Set<Integer>> bruteNeighbors = snapshotNeighbors();
        clearNeighbors();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("differences.txt", true))) {
            for (Particle p : particles) {
                Set<Integer> cellSet = cellNeighbors.getOrDefault(p.getId(), Collections.emptySet());
                Set<Integer> bruteSet = bruteNeighbors.getOrDefault(p.getId(), Collections.emptySet());

                if (!cellSet.equals(bruteSet)) {
                    writer.write("Mismatch for particle \n" + p.getId());
                    writer.write("Cell method: \n" + cellSet);
                    writer.write("Brute force: \n" + bruteSet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<Integer, Set<Integer>> snapshotNeighbors() {
        Map<Integer, Set<Integer>> map = new HashMap<>();
        for (Particle p : particles) {
            Set<Integer> ids = p.getNeighbors().stream()
                                .map(Particle::getId)
                                .collect(Collectors.toSet());
            map.put(p.getId(), ids);
        }
        return map;
    }

    public void testMethods(){
        findNeighbors();
        printNeighbours("cellindex.txt");
        clearNeighbors();
        bruteForceMethod();
        printNeighbours("bruteforce.txt");
        clearNeighbors();
    }

    private void clearNeighbors(){
        for(Particle p : particles){
            p.clearNeighbors();
        }
    }

    private void printNeighbours(String fileName){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            for (Particle particle : particles) {
                writer.write(String.format("%d:[",
                        particle.getId()));
                List<Particle> neighbours = particle.getNeighbors();
                Collections.sort(neighbours, (p1, p2) -> Integer.compare(p2.getId(), p1.getId()));

                for(Particle neighbour : neighbours){
                    writer.write(String.format("%d ", neighbour.getId()));
                }
                writer.write("]");
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updatePositions(int iteration){
        List<Particle> updatedParticlesPositions = new ArrayList<>(N);
        for(Particle particle: particles) {
            List<Particle> neighbors = particle.getNeighbors();
            neighbors.add(particle);
            double cosSum = 0;
            double sinSum = 0;
            double newThetaAngle = particle.getThetaAngle();

            // Delta Theta is a random number chosen with a uniform probability from the
            // interval [—theta/2, theta/2].
            double noise = (random.nextDouble() - 0.5) * this.nu;
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
            if(neighbors.size() >= 1){
                double averageTheta = Math.atan2(sinSum / neighbors.size(), cosSum / neighbors.size());
                newThetaAngle = averageTheta + noise;
            }

            // particle with updated position and angle
            Particle updatedParticle = new Particle(newX, newY, particle.getVelocity(), newThetaAngle, particle.getId());
            updatedParticlesPositions.add(updatedParticle);
        }
        particles = updatedParticlesPositions;
    }

    // check
    private void updatePositionsRandomNeighbour(){
        List<Particle> updatedParticlesPositions = new ArrayList<>(N);
        for(Particle particle: particles) {
            List<Particle> neighbors = particle.getNeighbors();

            //If no neighbours are present, then we keep the old angle
            double newThetaAngle = particle.getThetaAngle();

            if(!neighbors.isEmpty()) {
                Particle randomNeighbour = neighbors.get((int) (Math.random() * neighbors.size()));
                newThetaAngle = randomNeighbour.getThetaAngle();
            }
            
            double newX = (particle.getCurrentX()
                    + particle.getVelocity() * Math.cos(particle.getThetaAngle()) * timeStep) % L;
            double newY = (particle.getCurrentY()
                    + particle.getVelocity() * Math.sin(particle.getThetaAngle()) * timeStep) % L;

            if (newX < 0)
                newX += L;
            if (newY < 0)
                newY += L;

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
        double polarizarion = (magnitude) / (N * particles.getFirst().getVelocity());
        return polarizarion;
    }

    public void runSimulationForAnimationRandomNeighbour(String filePath) {
        String path = String.format("rn_%s",filePath);

        writeDataToFile(path, String.format("L:%d\n", L));
        writeDataToFile(path, String.format("N:%d\n", N));
        writeParticleDataToFile(path, 0, particles);
        for (int i = 1; i <= maxIterations; i++){
            findNeighbors();
            updatePositionsRandomNeighbour();
            writeParticleDataToFile(path, i, particles);
        }
        writeDataToFile(path, String.format("density:%.3f\n", density));
    }

    public void runSimulationForAnimation(String filePath) {
        writeDataToFile(filePath, String.format("L:%d\n", L));
        writeDataToFile(filePath, String.format("N:%d\n", N));
        writeDataToFile(filePath, String.format("nu:%.5f\n", nu));
        writeDataToFile(filePath, String.format("density:%.5f\n", density));
        writeParticleDataToFile(filePath, 0, particles);
        for (int i = 1; i <= maxIterations; i++){
            findNeighbors();
            updatePositions(i);
            writeParticleDataToFile(filePath, i, particles);
        }
    }

    public void runSimulationForPolarization(String filePath, double nu) {
        setNu(nu);
        for(int i = 1; i <= maxIterations; i++){
            findNeighbors();
            updatePositions(i);
            writeDataToFile(filePath, String.format("%.5f\n", calculatePolarization()));
        }
    }

    public void runSimulationForPolarizationRandomNeighbor(String filePath) {
        for(int i = 1; i <= maxIterations; i++){
            findNeighbors();
            updatePositionsRandomNeighbour();
            writeDataToFile(filePath, String.format("%.5f\n", calculatePolarization()));
        }
    }
    
    // todo no entiendo lo que tengo que plottear, esto está mal
    // note: L is constant, we increase density by increasing N -> check for d between 0 and 10
    public void runSimulationForDensity(String filePath, int N){
        setN(N);
        this.particles = generateParticles();
        System.out.printf("density: %.5f", density);
        for(int i = 1; i <= maxIterations; i++){
            findNeighbors();
            updatePositions(i);
            writeDataToFile(filePath, String.format("%.5f\n", calculatePolarization()));
        }
    }

    public void resetVariables(int N, double timeStep, int maxIterations, int L, double radius, double nu) {
        this.timeStep = timeStep;
        this.maxIterations = maxIterations;
        this.L = L;
        this.rc = radius;
        this.M = (int) Math.floor((double)L / rc);
        this.nu = nu;
        this.density = (double) N / (L * L);

        if(N == this.N){
            resetParticlesToInitialSnapshot();
        } else {
            regenerateParticles();
        }
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

    public int getN() {
        return N;
    }

    public double getTimeStep() {
        return timeStep;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getL() {
        return L;
    }

    public double getRc() {
        return rc;
    }

    public int getM() {
        return M;
    }

    public double getNu() {
        return nu;
    }

    public double getDensity() {
        return density;
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public List<Particle> getInitialSnapshot() {
        return initialSnapshot;
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