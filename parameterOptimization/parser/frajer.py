import re
import csv

def makeCsv(input_file, output_file):
    pattern = re.compile(r'IT: (\d+).*?Alpha\(\d+\): ([\-\d\.]+)')
    with open(output_file, 'w', newline='') as csvfile:
        with open(input_file, 'r') as read_file:
            csvwriter = csv.writer(csvfile)
            csvwriter.writerow(['ITER', 'ALPHA'])
            for line in read_file.readlines():
                matches = pattern.findall(line)
                for match in matches:
                    print(f'{match[0]},{match[1]}')
                    csvwriter.writerow([match[0],match[1]])

makeCsv('input.txt', 'output.csv')
print("Data written to output.csv")