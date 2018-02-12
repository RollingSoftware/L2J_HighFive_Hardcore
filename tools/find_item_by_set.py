import sys
import file_helper as fh
from xml.dom import minidom

script_name, workdir, file_extension, recursive, attribute, value = sys.argv

files_to_process = fh.filter_files_by_suffix(fh.files_to_process(workdir), file_extension)

item_ids = []
for item in files_to_process:
    dom = minidom.parse(item)
    item_elements = dom.getElementsByTagName('item')
    for element in item_elements:
        sets = element.getElementsByTagName('set')
        for one_set in sets:
            if attribute == one_set.getAttribute('name') and value == one_set.getAttribute('val'):
                print('Item', one_set.getAttribute(attribute),
                      '[', element.getAttribute('name'), ']')
                item_ids.append(element.getAttribute('id'))

with open('find_item_by_set_result.txt', 'w') as result:
    result.write(', '.join(item_ids))
