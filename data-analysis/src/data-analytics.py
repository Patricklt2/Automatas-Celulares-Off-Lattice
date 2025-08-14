import numpy as np
import matplotlib.pyplot as plt

# Load the data
# Replace 'data.txt' with your filename
data = np.loadtxt('polarization.txt', delimiter=';')
va = data[:, 0]   # first column
eta = data[:, 1]  # second column

# Create the plot
plt.figure(figsize=(6, 4))
plt.scatter(eta, va, color='black', s=20, label='Data')
plt.plot(eta, va, color='red', linewidth=2, label='Fit/Trend')  # same data as line

# Format like your image
plt.xlabel(r'$\eta$')
plt.ylabel(r'$v_a$')
plt.xlim(0, 5)
plt.ylim(0, 1.05)
plt.legend()
plt.tight_layout()

plt.show()
