import os
import sys
import numpy as np
import matplotlib.pyplot as plt

DATA_DIR = '../data_src'
RESULTS_FILE = '../results/avg-polarization'
PLOT_PATH = '../results/avg-polarization-plot.png'

def compute_stats_from_file(path, skip_first_lines=0):
    # Carga robusta: ignora líneas vacías/ruidos usando genfromtxt
    try:
        vals = np.genfromtxt(path, dtype=float)
    except Exception:
        return None  # archivo ilegible
    if vals.ndim > 1:
        # Si por accidente viene con más columnas, quedarnos con la primera
        vals = vals[:, 0]
    vals = np.asarray(vals, dtype=float)
    if vals.size == 0:
        return None
    vals = vals[skip_first_lines:]
    n = vals.size
    if n == 0:
        return None

    mean = float(np.mean(vals))
    std = float(np.std(vals, ddof=1)) if n > 1 else float('nan')
    stderr = float(std/np.sqrt(n)) if n > 1 else float('nan')
    return n, mean, std, stderr

def extract_nu(file_name):
    # Igual que tu lógica: tercer token separado por '_' sin .txt
    try:
        return float(file_name.split('_')[2].replace('.txt', ''))
    except Exception:
        # fallback: intentar sacar número del nombre completo
        base = os.path.splitext(file_name)[0]
        try:
            return float(base)
        except Exception:
            return float('nan')

def main(time=0):
    os.makedirs(os.path.dirname(RESULTS_FILE), exist_ok=True)

    files = [f for f in os.listdir(DATA_DIR) if os.path.isfile(os.path.join(DATA_DIR, f))]
    files.sort()

    # Escribimos cabecera y resultados
    with open(RESULTS_FILE, 'w') as out:
        out.write("nu;N;mean;std;stderr\n")
        for fname in files:
            full = os.path.join(DATA_DIR, fname)
            stats = compute_stats_from_file(full, skip_first_lines=time)
            if stats is None:
                continue
            n, mean, std, stderr = stats
            nu = extract_nu(fname)
            out.write(f"{nu};{n};{mean};{std};{stderr}\n")

    # Graficar
    plot_avg_polarization()

def plot_avg_polarization():
    nus, means, stds = [], [], []

    with open(RESULTS_FILE, 'r') as f:
        header = f.readline()
        has_header = 'std' in header or 'mean' in header
        if not has_header:
            # formato viejo: nu;avg
            for line in [header] + f.readlines():
                if not line.strip():
                    continue
                nu, avg = line.strip().split(';')
                nus.append(float(nu))
                means.append(float(avg))
                stds.append(np.nan)  # no hay std
        else:
            # formato nuevo: nu;N;mean;std;stderr
            for line in f:
                if not line.strip():
                    continue
                parts = line.strip().split(';')
                if len(parts) < 4:
                    continue
                nu = float(parts[0])
                mean = float(parts[2])
                std = float(parts[3]) if parts[3] != 'nan' else np.nan
                nus.append(nu); means.append(mean); stds.append(std)

    if not nus:
        print("No hay datos para graficar.")
        return

    # Ordenar
    order = np.argsort(nus)
    x = np.array(nus)[order]
    y = np.array(means)[order]
    yerr = np.array(stds)[order]

    plt.figure()
    if np.isfinite(yerr).any():
        plt.errorbar(x, y, yerr=yerr, fmt='o-', capsize=3)
    else:
        plt.plot(x, y, marker='o')

    plt.xlabel('Nu')
    plt.ylabel('Average Polarization')
    plt.title('Average Polarization vs Nu (std)')
    plt.grid(True)
    plt.savefig(PLOT_PATH, dpi=150, bbox_inches='tight')
    ##plt.show()

if __name__ == "__main__":
    if len(sys.argv) > 1:
        main(int(sys.argv[1]))
    else:
        main()  # default
