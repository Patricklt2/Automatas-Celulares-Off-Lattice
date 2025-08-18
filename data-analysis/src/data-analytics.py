import numpy as np
import os
import matplotlib.pyplot as plt


# Leer datos del archivo
data_dir = '../data_src'
data_files = [f for f in os.listdir(data_dir) if os.path.isfile(os.path.join(data_dir, f))]

for file_name in data_files:

    with open('../data_src/' + file_name, 'r') as f:
        data = [float(line.strip()) for line in f if line.strip()]

    x_range = list(range(len(data)))

    plt.plot(x_range, data, label='Línea continua')
    plt.scatter(x_range, data, color='red', s=10, label='Puntos')
    plt.xlabel('Índice')
    plt.ylabel('Valor')
    plt.title('Gráfico de datos')
    plt.legend()
    plt.savefig('../results/plo-v-time-' + file_name + '.png')
    plt.show()
    plt.clf()