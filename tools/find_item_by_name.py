import sys
import file_helper as fh
from xml.dom import minidom

script_name, workdir, file_extension, recursive, contains_text = sys.argv

files_to_process = fh.filter_files_by_suffix(fh.files_to_process(workdir), file_extension)

item_ids = []
for item in files_to_process:
    dom = minidom.parse(item)
    item_elements = dom.getElementsByTagName('item')
    for element in item_elements:
        if contains_text.lower() in element.getAttribute('name').lower():
            print('Item', element.getAttribute('id'),
                  '[', element.getAttribute('name'), ']')
            item_ids.append(element.getAttribute('id'))

with open('find_item_by_name_result.txt', 'w') as result:
    result.write(', '.join(item_ids))
