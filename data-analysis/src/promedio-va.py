
import sys
import os
import matplotlib.pyplot as plt

def main(time = 0):

    data_dir = '../data_src'
    data_files = [f for f in os.listdir(data_dir) if os.path.isfile(os.path.join(data_dir, f))]

    for file_name in data_files:
        with open('../data_src/' + file_name, 'r') as file:
            counter = 0
            avg_polarization = 0
            avg_counter = 0
            for line in file:
                if counter >= time:
                    #print(line.strip())
                    avg_polarization += float(line.strip())
                    avg_counter += 1
                counter += 1
            if avg_counter > 0:
                with open('../results/avg-polarization', 'a') as avg_file:
                    avg_file.write(f"{file_name.split('_')[2].replace('.txt', '')};{avg_polarization / avg_counter}\n")

    plot_avg_polarization()
    


def plot_avg_polarization():

    nu_points = []
    avg_polarizations = []

    with open('../results/avg-polarization', 'r') as avg_file:
        for line in avg_file:
            nu, avg_polarization = line.strip().split(';')
            nu_points.append(float(nu))
            avg_polarizations.append(float(avg_polarization))

    nu_points, avg_polarizations = zip(*sorted(zip(nu_points, avg_polarizations)))

    plt.plot(nu_points, avg_polarizations, marker='o')
    plt.xlabel('Nu')
    plt.ylabel('Average Polarization')
    plt.title('Average Polarization vs Nu')
    plt.grid()
    plt.savefig('../results/avg-polarization-plot.png')
    plt.show()

if __name__ == "__main__":
    if len(sys.argv) > 1:
        time_arg = int(sys.argv[1])
        main(time_arg)
    else:
        main() # default value