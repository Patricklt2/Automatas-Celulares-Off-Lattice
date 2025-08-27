import os
import sys
import numpy as np
import matplotlib.pyplot as plt

# --- Configuración ---
DATA_DIR = '../data_src'
RESULTS_FILE = '../results/avg-polarization'
PLOT_PATH = '../results/avg-polarization-plot.png'
DEFAULT_L = 20.0  


def compute_stats_from_file(path, skip_first_lines=0):
    """Lee archivo con una columna de Va, calcula media, std, stderr desde skip_first_lines en adelante."""
    try:
        vals = np.genfromtxt(path, dtype=float)
    except Exception:
        return None

    if vals is None:
        return None
    if np.ndim(vals) > 1:
        vals = vals[:, 0]
    vals = np.asarray(vals, dtype=float)
    if vals.size == 0:
        return None

    if skip_first_lines > 0:
        if skip_first_lines >= vals.size:
            return None
        vals = vals[skip_first_lines:]

    n = vals.size
    if n == 0:
        return None

    mean = float(np.mean(vals))
    std = float(np.std(vals, ddof=1)) if n > 1 else float('nan')
    stderr = float(std / np.sqrt(n)) if n > 1 else float('nan')
    return n, mean, std, stderr


def density_from_NL(N, L):
    return float(N) / (float(L) ** 2)


def extract_N_from_name(file_name):
    """
    Obtiene N del nombre del archivo tomando el valor después del 2do guion bajo.
    Ejemplo: algo_otro_100.txt -> N=100
    """
    base = os.path.splitext(os.path.basename(file_name))[0]
    parts = base.split('_')
    if len(parts) < 3:
        return None
    try:
        return int(parts[2])
    except ValueError:
        return None


def main(skip_j=0, L=DEFAULT_L):
    """
    skip_j: cuántas líneas iniciales ignorar (índice j) para calcular el promedio.
    L: tamaño fijo del lado del cuadrado.
    """
    os.makedirs(os.path.dirname(RESULTS_FILE), exist_ok=True)

    files = [f for f in os.listdir(DATA_DIR) if os.path.isfile(os.path.join(DATA_DIR, f))]
    files.sort()

    with open(RESULTS_FILE, 'w', encoding='utf-8') as out:
        out.write("density;N;L;mean;std;stderr\n")
        for fname in files:
            full = os.path.join(DATA_DIR, fname)
            stats = compute_stats_from_file(full, skip_first_lines=skip_j)
            if stats is None:
                continue
            n_samples, mean, std, stderr = stats

            N = extract_N_from_name(fname)
            if N is None:
                continue
            density = density_from_NL(N, L)

            out.write(f"{density};{N};{L};{mean};{std};{stderr}\n")

    plot_avg_polarization()


def plot_avg_polarization():
    """Grafica Average Polarization vs densidad con barras de error std."""
    xs, means, stds = [], [], []

    if not os.path.exists(RESULTS_FILE):
        print("No hay archivo de resultados para graficar.")
        return

    with open(RESULTS_FILE, 'r', encoding='utf-8') as f:
        f.readline()  # header
        for line in f:
            if not line.strip():
                continue
            parts = line.strip().split(';')
            if len(parts) < 6:
                continue
            density = float(parts[0])
            mean = float(parts[3])
            std = float(parts[4]) if parts[4] != 'nan' else np.nan
            xs.append(density)
            means.append(mean)
            stds.append(std)

    if not xs:
        print("No hay datos para graficar.")
        return

    order = np.argsort(xs)
    x = np.array(xs)[order]
    y = np.array(means)[order]
    yerr = np.array(stds)[order]

    plt.figure()
    if np.isfinite(yerr).any():
        plt.errorbar(x, y, yerr=yerr, fmt='o-', capsize=3)
    else:
        plt.plot(x, y, marker='o')

    plt.xlabel('Densidad (ρ = N / L²)')
    plt.ylabel('V_a')
    plt.grid(True)
    plt.savefig(PLOT_PATH, dpi=150, bbox_inches='tight')
    # plt.show()


if __name__ == "__main__":
    """
    Usos:
      python script.py                -> L=DEFAULT_L, skip_j leído por stdin
      python script.py 25             -> L=DEFAULT_L, skip_j leído por stdin, pero L fijo 25
    """
    # Leer j desde stdin (entrada estándar)
    try:
        j_input = input("Ingrese el índice j (número de líneas a saltar para el promedio): ").strip()
        skip_j = int(j_input) if j_input else 0
    except Exception:
        skip_j = 0

    L_arg = DEFAULT_L
    if len(sys.argv) > 1:
        L_arg = float(sys.argv[1])  # opcional: pasar L fijo como primer argumento

    main(skip_j=skip_j, L=L_arg)
