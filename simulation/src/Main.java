/*
* Main class to run in order to generate the simulation
*
* */
public class Main {
    public static void main(String[] args) {
        Simulation sim = new Simulation(5000, 1, 10, 5, 1, 10, "particle_test6969.txt");
        
        /*
        * Tested with:
        *
        * N=10, L = 5, ts = 1, maxiter = 10, r=1 , nu = 0.1   --> low noise
        * N = 5000, L= 5, ts = 1, maxiter = 10, r = 1, nu = 10   --> high noise
        *
        * */
        sim.runSimulation();
    }
}