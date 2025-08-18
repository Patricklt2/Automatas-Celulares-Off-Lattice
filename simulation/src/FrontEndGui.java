import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class FrontEndGui {
    private static Simulation sim;

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        SwingUtilities.invokeLater(FrontEndGui::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Simulation Runner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5,5,5,5);

        String[] simTypes = {"New Simulation Data", "Animation", "RandomNeighbour", "Polarization", "Polarization PHI", "Density"};
        JComboBox<String> simTypeBox = new JComboBox<>(simTypes);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        frame.add(simTypeBox, gbc);

        JLabel nLabel = new JLabel("N:"); 
        JTextField nField = new JTextField("1000");

        JLabel tsLabel = new JLabel("Time step:"); 
        JTextField tsField = new JTextField("1");

        JLabel maxIterLabel = new JLabel("Max iterations:"); 
        JTextField maxIterField = new JTextField("1000");

        JLabel lLabel = new JLabel("L:"); 
        JTextField lField = new JTextField("15");

        JLabel rLabel = new JLabel("Radius:"); 
        JTextField rField = new JTextField("1");

        JLabel nuLabel = new JLabel("Nu:"); 
        JTextField nuField = new JTextField("1");

        JComponent[][] newSimFields = {
                {nLabel, nField}, {tsLabel, tsField}, {maxIterLabel, maxIterField},
                {lLabel, lField}, {rLabel, rField}, {nuLabel, nuField}
        };

        JLabel fileLabel = new JLabel("Output file:"); JTextField fileField = new JTextField("output.txt");
        gbc.gridx = 0; gbc.gridy = newSimFields.length + 1; gbc.gridwidth = 1;
        frame.add(fileLabel, gbc);
        gbc.gridx = 1; frame.add(fileField, gbc);

        JLabel minNLabel = new JLabel("Min N:"); JTextField minNField = new JTextField("100");
        JLabel maxNLabel = new JLabel("Max N:"); JTextField maxNField = new JTextField("500");
        JLabel stepNLabel = new JLabel("Step N:"); JTextField stepNField = new JTextField("50");
        JComponent[][] densityFields = {
                {minNLabel, minNField}, {maxNLabel, maxNField}, {stepNLabel, stepNField}
        };

        JLabel minNuLabel = new JLabel("Min Nu:"); JTextField minNuField = new JTextField("0.1");
        JLabel maxNuLabel = new JLabel("Max Nu:"); JTextField maxNuField = new JTextField("5");
        JLabel stepNuLabel = new JLabel("Step Nu:"); JTextField stepNuField = new JTextField("0.1");
        JComponent[][] polarizationFields = {
                {minNuLabel, minNuField}, {maxNuLabel, maxNuField}, {stepNuLabel, stepNuField}
        };

        int row = 1;
        for (JComponent[] field : newSimFields) {
            gbc.gridx = 0; gbc.gridy = row; frame.add(field[0], gbc);
            gbc.gridx = 1; frame.add(field[1], gbc);
            row++;
        }

        row++;

        for (JComponent[] field : densityFields) {
            gbc.gridx = 0; gbc.gridy = row; frame.add(field[0], gbc);
            gbc.gridx = 1; frame.add(field[1], gbc);
            row++;
        }

        for (JComponent[] field : polarizationFields) {
            gbc.gridx = 0; gbc.gridy = row; frame.add(field[0], gbc);
            gbc.gridx = 1; frame.add(field[1], gbc);
            row++;
        }

        JButton runButton = new JButton("Run");
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        frame.add(runButton, gbc);

        simTypeBox.addActionListener(e -> {
            String type = (String) simTypeBox.getSelectedItem();

            boolean newSimVisible = type.equals("New Simulation Data");
            for (JComponent[] f : newSimFields) { f[0].setVisible(newSimVisible); f[1].setVisible(newSimVisible); }

            boolean fileVisible = !type.equals("New Simulation Data");
            fileLabel.setVisible(fileVisible);
            fileField.setVisible(fileVisible);

            boolean densityVisible = type.equals("Density");
            for (JComponent[] f : densityFields) { f[0].setVisible(densityVisible); f[1].setVisible(densityVisible); }

            boolean polarizationVisible = type.equals("Polarization");
            for (JComponent[] f : polarizationFields) { f[0].setVisible(polarizationVisible); f[1].setVisible(polarizationVisible); }
        });

        simTypeBox.setSelectedIndex(0);

        runButton.addActionListener((ActionEvent e) -> {
            String simType = (String) simTypeBox.getSelectedItem();
            try {
                switch (simType) {
                    case "New Simulation Data" -> {
                        int N = Integer.parseInt(nField.getText());
                        double ts = Double.parseDouble(tsField.getText());
                        int maxIter = Integer.parseInt(maxIterField.getText());
                        int L = Integer.parseInt(lField.getText());
                        double r = Double.parseDouble(rField.getText());
                        double nu = Double.parseDouble(nuField.getText());
                        generateNewSimulation(N, (int) ts, maxIter, L, r, nu);
                        JOptionPane.showMessageDialog(frame, "New Simulation Data created!");
                    }
                    case "Animation" -> runSimulationForAnimation(fileField.getText());
                    case "RandomNeighbour" -> runSimulationForAnimationRandomNeighbour(fileField.getText());
                    case "Polarization" -> {
                        double minNu = Double.parseDouble(minNuField.getText());
                        double maxNu = Double.parseDouble(maxNuField.getText());
                        double stepNu = Double.parseDouble(stepNuField.getText());
                        runSimulationForPolarization(fileField.getText(), minNu, maxNu, stepNu);
                    }
                    case "Density" -> {
                        int minN = Integer.parseInt(minNField.getText());
                        int maxN = Integer.parseInt(maxNField.getText());
                        int stepN = Integer.parseInt(stepNField.getText());
                        runSimulationForDensity(fileField.getText(), minN, maxN, stepN);
                    }
                    case "Polarization PHI" -> runSimulationForPolarizationRandomNeighbor(fileField.getText());
                }
            } catch (HeadlessException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        });

        frame.setVisible(true);
    }

    private static void runSimulationForAnimation(String file) {
        sim.resetParticlesToInitialSnapshot();
        sim.runSimulationForAnimation(file);
        JOptionPane.showMessageDialog(null, "Animation finished!");
    }

    private static void runSimulationForAnimationRandomNeighbour(String file) {
        sim.resetParticlesToInitialSnapshot();
        sim.runSimulationForAnimationRandomNeighbour(file);
        JOptionPane.showMessageDialog(null, "Random Neighbour Animation finished!");
    }

    private static void runSimulationForPolarization(String file, double minNu, double maxNu, double stepNu) {
        double auxNu = sim.getNu();
        for (double nu = minNu; nu <= maxNu; nu += stepNu) {
            String cFile = String.format("%s_nu_%.2f.txt", file.replace(".txt", ""), nu);
            sim.resetParticlesToInitialSnapshot();
            sim.runSimulationForPolarization(cFile, nu);
        }
        sim.setNu(auxNu);
        JOptionPane.showMessageDialog(null, "Polarization Animation finished!");
    }

    private static void runSimulationForPolarizationRandomNeighbor(String file) {
        String cFile = String.format("%s_phi.txt", file.replace(".txt", ""));
        sim.resetParticlesToInitialSnapshot();
        sim.runSimulationForPolarizationRandomNeighbor(cFile);
        JOptionPane.showMessageDialog(null, "Polarization Phi  finished!");
    }

    private static void runSimulationForDensity(String file, int minN, int maxN, int stepN) {
        int auxN = sim.getN();
        for (int n = minN; n <= maxN; n += stepN) {
            String cFile = String.format("%s_D_%d.txt", file.replace(".txt", ""), n);
            sim.resetVariables(n, sim.getTimeStep(), sim.getMaxIterations(), sim.getL(), sim.getRc(), sim.getNu());
            sim.runSimulationForDensity(cFile, n);
        }
        sim.setN(auxN);
        JOptionPane.showMessageDialog(null, "Density Animation finished!");
    }

    private static void generateNewSimulation(int N, int ts, int maxIter, int L, double r, double nu) {
        FrontEndGui.sim = new Simulation(N, ts, maxIter, L, r, nu);
    }
}
