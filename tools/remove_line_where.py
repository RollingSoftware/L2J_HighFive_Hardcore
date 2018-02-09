import sys
import os

script_name, files_path, files_extension, new_files_path, remove_where = sys.argv

all_files = [one_file for one_file in os.listdir(files_path) if os.path.isfile(os.path.join(files_path, one_file)) and one_file.endswith(files_extension)]

for current_file in all_files:
    print("Processing " + current_file)

    new_lines = []
    with open(os.path.join(files_path, current_file), 'r') as current_file_handle:
        for line in current_file_handle:
            if remove_where not in line:
                new_lines.append(line)

    with open(os.path.join(new_files_path, current_file), 'w') as new_file_handle:
        new_file_handle.writelines(new_lines)
