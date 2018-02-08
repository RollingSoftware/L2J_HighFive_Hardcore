import sys
import os
from typing import List
from xml.dom import minidom

script_name, workdir, file_extension, recursive, contains_text = sys.argv


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


files_to_process = []
if recursive:
    files_to_process = recursive_list_folder(workdir)
else:
    files_to_process = plain_list_folder(workdir)


files_to_process = [prefixed_item for prefixed_item in files_to_process
                    if prefixed_item.endswith(file_extension)]

item_ids = []
for item in files_to_process:
    dom = minidom.parse(item)
    item_elements = dom.getElementsByTagName('item')
    for element in item_elements:
        if contains_text in element.getAttribute('name'):
            print('Item', element.getAttribute('id'),
                  '[', element.getAttribute('name'), ']')
            item_ids.append(element.getAttribute('id'))

with open('find_item_by_name_result.txt', 'w') as result:
    result.write(', '.join(item_ids))
