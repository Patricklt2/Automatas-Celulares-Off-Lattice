import numpy as np
import matplotlib.pyplot as plt


# Leer datos del archivo
with open('./polarization-v-time.txt', 'r') as f:
    data = [float(line.strip()) for line in f if line.strip()]

# Crear el array de rango para el eje x
x_range = list(range(len(data)))

plt.plot(x_range, data, label='Línea continua')
plt.scatter(x_range, data, color='red', s=10, label='Puntos')
plt.xlabel('Índice')
plt.ylabel('Valor')
plt.title('Gráfico de datos')
plt.legend()
plt.savefig('../results/plo-v-time.png')
plt.show()
