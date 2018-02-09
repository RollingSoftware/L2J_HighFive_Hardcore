import sys
import os
import file_helper as fh
import lxml.etree as ET
import xml_helper as xh

script_name, workdir, file_extension, recursive, ids_file = sys.argv

ids_to_remove = fh.parse_ids_file(ids_file)
files_to_process = fh.filter_files_by_suffix(fh.files_to_process(workdir), file_extension)

print('Ids to remove:', ids_to_remove)

for file_item in files_to_process:
    dom = ET.parse(file_item)

    npcs = dom.findall('.//npc')
    if not npcs:
        print('No npcs were found, skipping file', file_item)
        break

    items = dom.findall('.//item')

    was_empty = False
    if not items:
        was_empty = True
    results = []
    for item in items:
        if item.get('id') in ids_to_remove and item.getparent():
            print('Removing item', item.get('id'), 'from buylist')
            item.getparent().remove(item)
            results.append(item)

    new_items = dom.findall('.//item')
    if not new_items and not was_empty:
        os.remove(file_item)
    elif results:
        with open(file_item, 'wb') as f:
            f.write(ET.tostring(dom))
