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

    was_empty = False
    items = dom.findall('.//item')
    if not items:
        was_empty = True
    results = []
    for item in items:
        ingridient_ids = xh.extract_ids(item.findall('.//ingredient'))
        production_ids = xh.extract_ids(item.findall('.//production'))
        all_ids = []
        all_ids.extend(ingridient_ids)
        all_ids.extend(production_ids)
        for id_to_remove in ids_to_remove:
            if id_to_remove in all_ids:
                if item.getparent():
                    print('Removing', id_to_remove, 'from', item.getparent())
                    item.getparent().remove(item)
                    results.append(item)

    new_items = dom.findall('.//item')
    if not new_items and not was_empty:
        os.remove(file_item)
    elif results:
        with open(file_item, 'wb') as f:
            f.write(ET.tostring(dom))
