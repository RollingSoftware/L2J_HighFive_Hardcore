import sys
import file_helper as fh
import lxml.etree as ET
import xml_helper as xh

script_name, workdir, file_extension, recursive, ids_file = sys.argv

ids_to_remove = fh.parse_ids_file(ids_file)
files_to_process = fh.filter_files_by_suffix(fh.files_to_process(workdir), file_extension)

print('Ids to remove:', ids_to_remove)


def removeEmptyGroup(item_element):
    parent_node = item_element.getparent()
    if parent_node is not None and parent_node.children.length == 0 and parent_node.tagName.lower() == 'group'.lower():
        print('Removing empty group',  item_element.getparent().getValue())
        item_element.getparent().getparent().remove(item_element.getparent())


def processes_items(item_elements):
    removedElements = []
    for item_element in item_elements:
        if item_element.get('id') in ids_to_remove:
            next_sibling = item_element.getnext()
            comment = None
            if next_sibling is not None and next_sibling.tag is ET.Comment:
                item_element.getparent().remove(next_sibling)
                comment = next_sibling
            print('Removing item', item_element.get('id'), comment)
            item_element.getparent().remove(item_element)
            removeEmptyGroup(item_element)
            removedElements.append(item_element)
    return removedElements


for item in files_to_process:
    dom = ET.parse(item)
    drop_lists = dom.findall('.//dropLists')
    results = []
    for drop_list in drop_lists:
        item_elements = drop_list.findall('.//item')
        results.extend(processes_items(item_elements))

    if results:
        with open(item, 'wb') as f:
            f.write(ET.tostring(dom))
