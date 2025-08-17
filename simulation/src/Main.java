/*
* Main class to run in order to generate the simulation
*
* */
public class Main {
    public static void main(String[] args) {
        
        Simulation sim = new Simulation(1000, 1, 1000, 15, 1, 1);
        sim.runSimulationForAnimation("hola.txt");
        /*
        * Tested with:
        *
        * N=10, L = 5, ts = 1, maxiter = 10, r=1 , nu = 0.1   --> low noise
        * N = 5000, L= 5, ts = 1, maxiter = 10, r = 1, nu = 10   --> high noise
        * TODO: WITH 300 1 100 25 1 0.1 , THE VALUES OSCILATE A LOT even with modulo fix
        *
        * for fig2a:
        * N = 400 , step = 1 , maxIter = 100 , L = 10 , r = 1 , nu = 5     -> va -> 0
        *
        * for fig2a-2:
        * N = 400 , step = 1 , maxIter = 100 , L = 10 , r = 1 , nu = 0,1     -> va -> 1
        * */
        //sim.runSimulationForAnimation("check-polarization.txt");


        
        /*for(double nu = 5; nu > 0; nu-=0.1 ){
            sim.runSimulationForPolarization("polarization.txt", nu);
        }*/
        

        /*sim.setNu(0.1);
        sim.setL(20);
        for(int N = 0; N < 200; N += 10){
            sim.runSimulationForDensity("density.txt", N);
        }*/
    }
}