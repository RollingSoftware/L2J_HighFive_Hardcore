import os
from typing import List


def list_folder(workdir: str) -> List[str]:
    return [os.path.join(workdir, dir_item)
            for dir_item in os.listdir(workdir)]


def plain_list_folder(workdir: str) -> List[str]:
    return [item for item in list_folder(workdir) if os.path.isfile(item)]


def recursive_list_folder(workdir: str) -> List[str]:
    folder_items = list_folder(workdir)
    files = []
    for item in folder_items:
        if os.path.isdir(item):
            files.extend(recursive_list_folder(item))
        elif os.path.isfile(item):
            files.append(item)
    return files


def files_to_process(workdir, recursive=True):
    if recursive:
        return recursive_list_folder(workdir)
    else:
        return plain_list_folder(workdir)


def filter_files_by_suffix(files_to_process, file_extension):
    return [suffixed_item for suffixed_item in files_to_process
            if suffixed_item.endswith(file_extension)]


def parse_ids_file(file_path):
    with open(file_path) as id_file_handle:
        return [line.strip() for line in ','.join(id_file_handle.readlines()).split(',')]
