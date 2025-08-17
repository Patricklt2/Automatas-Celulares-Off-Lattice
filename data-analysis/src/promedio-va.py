
import sys

def main(time = 0):
    with open('./polarization-v-time.txt', 'r') as file:
        counter = 0
        avg_polarization = 0
        avg_counter = 0
        for line in file:
            if counter >= time:
                print(line.strip())
                avg_polarization += float(line.strip())
                avg_counter += 1
            counter += 1
        if avg_counter > 0:
            print(f"Average polarization at time {time}: {avg_polarization / avg_counter}")


if __name__ == "__main__":
    if len(sys.argv) > 1:
        time_arg = int(sys.argv[1])
        main(time_arg)
    else:
        main() # default value