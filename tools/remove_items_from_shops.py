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
    items = dom.findall('.//item')
    results = []
    for item in items:
        ingridient_ids = xh.extract_ids(item.findall('.//ingredient'))
        production_ids = xh.extract_ids(item.findall('.//production'))
        all_ids = []
        all_ids.extend(ingridient_ids)
        all_ids.extend(production_ids)
        for id_to_remove in ids_to_remove:
            if id_to_remove in all_ids:
                item.getparent().remove(item)
                results.append(item)

    new_items = dom.findall('.//item')
    if not new_items:
        os.remove(file_item)

    if results:
        with open(item, 'wb') as f:
            f.write(ET.tostring(dom))
