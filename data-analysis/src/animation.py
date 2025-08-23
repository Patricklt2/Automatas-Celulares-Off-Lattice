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

hdr = re.compile(r"^\s*t\s*-\s*\d+")
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

def main():
    parser = argparse.ArgumentParser(description="Arguments for animation script")

    parser.add_argument("filename", help="The file to process")
    parser.add_argument("-s", "--save", action="store_true", help="Enable saving the animation to a file", default=False)

    args = parser.parse_args()

    gen = leer_frames(args.filename)
    arr = leer_header(args.filename)
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

    plt.show()

    if args.save:
        ani.save("particles.gif", writer=PillowWriter(fps=30))

if __name__ == "__main__":
    main()