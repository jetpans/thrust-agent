import os
from frajer import makeCsv
from pathlib import Path

# Define the root directory
root_dir = "D:\Coding\FAKS\Mentor\\raketa-zavrsni\parameterOptimization\\stddev"  # Replace with your directory path

# Traverse the directory tree
for root, dirs, files in os.walk(root_dir):
    # Check if the current directory has no subdirectories
    if not dirs:

        leaf_folder = os.path.basename(root)
        pathParent = Path(root).parent.absolute()
        parent = os.path.basename(pathParent)
        print(parent)
        print(leaf_folder)
        makeCsv(root+"/"+"log.txt",leaf_folder+parent+".csv")