import numpy as np
import os
import matplotlib.pyplot as plt


# Leer datos del archivo
data_dir = '../data_src'
data_files = [f for f in os.listdir(data_dir) if os.path.isfile(os.path.join(data_dir, f))]

for file_name in data_files:

    with open('../data_src/' + file_name, 'r') as f:
        data = [float(line.strip()) for line in f if line.strip()]


    # Calcular media y desvío estándar de los últimos 300 datos
    ultimos_datos = data[-300:] if len(data) >= 300 else data
    media = np.mean(ultimos_datos)
    desvio = np.std(ultimos_datos)
    x_range = list(range(len(data)))

    plt.plot(x_range, data, label='Línea continua')
    plt.scatter(x_range, data, color='red', s=10, label='Puntos')
    plt.errorbar(x_range, data, yerr=desvio, fmt='none', ecolor='gray', alpha=0.5, label='Desvío estándar')
    plt.xlabel('Índice')
    plt.ylabel('Valor')
    plt.title(f'Gráfico de datos\nMedia: {media:.2f}, Desvío estándar: {desvio:.2f}')
    plt.legend()
    plt.savefig('../results/plo-v-time-' + file_name + '.png')
    #plt.show()
    plt.clf()