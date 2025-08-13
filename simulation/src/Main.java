/*
* Main class to run in order to generate the simulation
*
* */
public class Main {
    public static void main(String[] args) {
        Simulation sim = new Simulation(300, 0.1, 10, 7, 1, 2, "particle_test11.txt");

        sim.runSimulation();
    }
}