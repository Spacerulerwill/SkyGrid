import json
import argparse

# Set up argument parsing
parser = argparse.ArgumentParser(description="Grouper.")
parser.add_argument('input_file', type=str, help='The input JSON file with group data.')
parser.add_argument('--output_file', type=str, default='output.json', help='The output JSON file (default: output.json).')
args = parser.parse_args()

# Open and load the input JSON file
with open(args.input_file) as f:
    groups = json.load(f)

output = {}

# Process the data and calculate the output
for group in groups:
    total: int = group["total"]
    items: list[str] = group["items"]
    k = total / len(items)
    for item in items:
        output[item] = k

# Write the output to the specified file
with open(args.output_file, "w+") as f:
    json.dump(output, f, indent=4)
