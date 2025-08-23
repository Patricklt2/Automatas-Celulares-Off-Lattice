import math
from typing import Dict, List, Tuple, Any, Optional

# --- CONFIGURACIÓN ---
DEFAULT_SPEED = 0.03   # rapidez por defecto si no viene en el archivo (ajústala si usas otra)

def read_blocks(file_path: str) -> Tuple[Dict[str, Any], List[List[Dict[str, float]]]]:
    """
    Lee el archivo y devuelve:
      - params: diccionario con parámetros globales (nu, N, velocity, density, etc.)
      - blocks: lista de bloques; cada bloque es lista de dicts con 'id', 'x', 'y', 'theta'
    """
    params: Dict[str, Any] = {}
    blocks: List[List[Dict[str, float]]] = []
    current: List[Dict[str, float]] = []

    with open(file_path, 'r') as f:
        for raw in f:
            line = raw.strip()
            if not line:
                continue

            # Inicio de bloque temporal
            if line.startswith("t -"):
                if current:
                    blocks.append(current)
                    current = []
                continue

            # Parámetros globales tipo: nu: 0.45
            if ":" in line and ";" not in line and not line[0].isdigit():
                k, v = line.split(":", 1)
                k = k.strip()
                v = v.strip()
                try:
                    params[k] = float(v)
                except ValueError:
                    params[k] = v
                continue

            # Filas de partículas: id;x;y;theta
            if ";" in line:
                parts = [p.strip() for p in line.split(";")]
                if len(parts) >= 4:
                    try:
                        pid = int(parts[0])
                        x = float(parts[1])
                        y = float(parts[2])
                        theta = float(parts[3])
                        current.append({"id": pid, "x": x, "y": y, "theta": theta})
                    except Exception:
                        # si alguna fila está mal formateada, la ignoramos
                        pass

    if current:
        blocks.append(current)

    return params, blocks


def polarization_of_block(particles: List[Dict[str, float]], speed: float) -> float:
    """
    Polarización del bloque:
      |sum v_i| / (N * v)
    con v_i = v * (cos(theta_i), sin(theta_i)), y v constante.
    """
    N = len(particles)
    if N == 0 or speed == 0.0:
        return 0.0

    sum_vx = 0.0
    sum_vy = 0.0
    for p in particles:
        th = p["theta"]
        sum_vx += speed * math.cos(th)
        sum_vy += speed * math.sin(th)

    magnitude = math.hypot(sum_vx, sum_vy)
    return magnitude / (N * speed)


def compute_and_write_polarizations(file_path: str,
                                    default_speed: float = DEFAULT_SPEED,
                                    out_dir: str = "../data_src") -> str:
    params, blocks = read_blocks(file_path)

    speed: float = float(params.get("velocity", default_speed))

    nu_val: Optional[float] = None
    if "nu" in params:
        try:
            nu_val = float(params["nu"])
        except Exception:
            pass
    nu_tag = f"{nu_val:.6g}" if isinstance(nu_val, float) else "unknown"
    out_path = f"{out_dir}/output_10000_nu_{nu_tag}.txt"

    with open(out_path, "w") as f:
        for particles in blocks:
            pol = polarization_of_block(particles, speed)
            f.write(f"{pol}\n")

    return out_path


if __name__ == "__main__":
    file_path = "../data_src/output_10000.txt"
    out = compute_and_write_polarizations(file_path)
    print(f"Archivo generado: {out}")
