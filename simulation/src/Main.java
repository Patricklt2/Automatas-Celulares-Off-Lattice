/*
* Main class to run in order to generate the simulation
*
* */
public class Main {
    public static void main(String[] args) {
        Simulation sim = new Simulation(300, 1, 100, 25, 1, 0.1, "particle_test694206942069420.txt");

        /*
        * Tested with:
        *
        * N=10, L = 5, ts = 1, maxiter = 10, r=1 , nu = 0.1   --> low noise
        * N = 5000, L= 5, ts = 1, maxiter = 10, r = 1, nu = 10   --> high noise
        * TODO: WITH 300 1 100 25 1 0.1 , THE VALUES OSCILATE A LOT
        * */
        sim.runSimulation();
    }
}