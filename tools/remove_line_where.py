import sys
import os

import file_helper as fh

script_name, files_path, files_extension, remove_where = sys.argv

all_files = fh.filter_files_by_suffix(fh.files_to_process(files_path), files_extension)

for current_file in all_files:
    print("Processing " + current_file)

    new_lines = []
    with open(current_file, 'r') as current_file_handle:
        for line in current_file_handle:
            if remove_where not in line:
                new_lines.append(line)

    with open(current_file, 'w+') as new_file_handle:
        new_file_handle.writelines(new_lines)
