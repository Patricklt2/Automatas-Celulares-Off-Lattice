import argparse
import math
import os
import sys
from typing import List, Tuple, Optional

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation, PillowWriter, FFMpegWriter
import re

# ---------- Classes -----------
class Particle:
    def __init__(self, x, y, velocity, theta, id):
        self.x = x
        self.y = y
        self.velocity = velocity
        self.theta = theta
        self.id = id

    def setVelocity(self, velocity):
        self.velocity = velocity

    def __repr__(self):
        return f"Particle(id={self.id}, x={self.x:.3f}, y={self.y:.3f}, theta={self.theta:.3f})"


# ---------- Utilities ----------

hdr = re.compile(r"^\s*t\s*:\s*\d+")
hdr_N = re.compile(r"^\s*N\s*:\s*\d+")
hdr_L = re.compile(r"^\s*L\s*:\s*\d+")
hdr_polarization = re.compile(r"^\s*polarization\s*:\s*([\d.,]+)\s*$")
hdr_density = re.compile(r"^\s*density\s*:\s*([\d.,]+)\s*$")

def leer_frames(filename):
    with open(filename) as f:
        frame_data = []   # List for actual frame
        t_actual = None
        for line in f:
            line = line.strip()
            if not line or hdr_polarization.match(line) or hdr_density.match(line) or hdr_N.match(line) or hdr_L.match(line):
                continue

            m = hdr.match(line)
            if m: # heading
                if frame_data:
                    yield (frame_data)
                    frame_data = [] 
                t_actual = float(m.group(0).split(':')[1].strip())
            else:
                # id, x, y, theta
                parts = line.split(';')
                if len(parts) >= 4:
                    id, sx, sy, stheta = parts
                    #Lets fix the decimal comma issue in the simulation output
                    p = Particle(float(sx.replace(',', '.')), float(sy.replace(',', '.')), 0.03, float(stheta.replace(',', '.')), int(id))
                    frame_data.append(p)

        # last frame of the file
        if frame_data:
            yield (frame_data)

def leer_header(filename):
    N = None
    L = None
    with open(filename) as f:
        for line in f:
            line = line.strip()
            m = hdr_L.match(line)
            if m:
                L = int(m.group(0).split(':')[1].strip())
            m = hdr_N.match(line)
            if m:
                N = int(m.group(0).split(':')[1].strip())
            if N is not None and L is not None:
                return [N, L]
    return [N, L]


"""
Un mini ejemplo de que es con lo que testie que funcione.
def main():
    gen = leer_frames("particle_test11.txt")  # Replace with your actual data file

    for t_actual, frame_data in gen:
        print(f"Time: {t_actual}, Particles: {frame_data}")
"""

"""
def main():
    p = argparse.ArgumentParser()
    p.add_argument("--input", "-i", required=True, help="Path to fig2a-2.txt")
    # p.add_argument("--fps", type=int, default=20)
    # p.add_argument("--marker_size", type=int, default=10)
    # p.add_argument("--arrows", action="store_true", help="Draw heading arrows if 'theta' column exists")
    # p.add_argument("--xlim", nargs=2, type=float, help="Override x-axis limits: xmin xmax")
    # p.add_argument("--ylim", nargs=2, type=float, help="Override y-axis limits: ymin ymax")
    # p.add_argument("--show", action="store_true", help="Show the animation window instead of saving")
    # p.add_argument("--save", choices=["gif", "mp4"], help="Save animation to this format")
    # p.add_argument("--out", default="animation.gif", help="Output file path when using --save")
    args = p.parse_args()

    particles = 400
    L = 10
    frames = 100
    fig, ax = plt.subplots()
    ax.set_xlim(0, L)
    ax.set_ylim(0, L)
    scat = ax.scatter([], [])

    df = leer_frames(args.input)

    fig, anim = make_animation(
        df,
        fps=args.fps,
        marker_size=args.marker_size,
        show_arrows=args.arrows,
        xlim=args.xlim,
        ylim=args.ylim,
    )

    if args.show and args.save:
        print("Choose either --show or --save, not both.", file=sys.stderr)
        sys.exit(2)

    if args.show or not args.save:
        plt.show()
    else:
        save_animation(anim, args.out, fps=args.fps, format_=args.save)
        print(f"Saved animation to {args.out}")
"""

def main():
    gen = leer_frames("hola.txt")
    arr = leer_header("hola.txt")
    N = arr[0]
    L = arr[1]

    fig, ax = plt.subplots()
    ax.set_xlim(0, L)
    ax.set_ylim(0, L)

    first_frame = next(gen)

    xy = np.array([[p.x, p.y] for p in first_frame])
    dx = np.array([np.cos(p.theta) for p in first_frame])
    dy = np.array([np.sin(p.theta) for p in first_frame])
    angles = np.array([p.theta for p in first_frame])

    quiv = ax.quiver(
        xy[:, 0], xy[:, 1],
        dx, dy,
        angles,
        angles='xy',
        scale_units='xy',
        scale=None,
        cmap='hsv',
        width=0.005
    )
    fig.colorbar(quiv, ax=ax, label="Orientation (theta)")

    def init_animation():
        return quiv,

    def update_animation(particles):
        xy = np.array([[p.x, p.y] for p in particles])
        dx = np.array([np.cos(p.theta) for p in particles])
        dy = np.array([np.sin(p.theta) for p in particles])
        angles = np.array([p.theta for p in particles])

        quiv.set_offsets(xy)
        quiv.set_UVC(dx, dy, angles)
        return quiv,

    ani = FuncAnimation(
        fig, update_animation, frames=gen,
        init_func=init_animation, blit=False, interval=10
    )

    #ani.save("particles.gif", writer=PillowWriter(fps=30))
    
    # Para guardar la animación como GIF:
    ani.save("particles.gif", writer="pillow", fps=30)

    # Para guardar la animación como MP4 (requiere ffmpeg instalado):
    # ani.save("particles.mp4", writer=FFMpegWriter(fps=30))
    plt.show()

if __name__ == "__main__":
    main()