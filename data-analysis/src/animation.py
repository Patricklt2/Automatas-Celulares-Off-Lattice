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

hdr = re.compile(r"^\s*t\s*=\s*(\d+)\s*$")  # detecta líneas tipo t=0, t = 2, etc.

def leer_frames(filename):
    with open(filename) as f:
        frame_data = []   # List for actual frame
        t_actual = None

        for line in f:
            line = line.strip()
            if not line:
                continue

            m = hdr.match(line)
            if m: # heading
                if frame_data:
                    yield t_actual, frame_data
                    frame_data = [] 
                t_actual = float(m.group(1))
            else:
                # id, x, y, theta
                parts = line.split()
                if len(parts) >= 4:
                    id, sx, sy, stheta = parts
                    p = Particle(float(sx), float(sy), 0.0, float(stheta), int(id))
                    frame_data.append(p)

        # last frame of the file
        if frame_data:
            yield t_actual, frame_data



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


if __name__ == "__main__":
    main()