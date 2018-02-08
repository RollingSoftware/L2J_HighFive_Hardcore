import sys
import file_helper as fh
import xml.etree.ElementTree as ET
import xml_helper as xh

script_name, workdir, file_extension, recursive, ids_file = sys.argv

ids_to_remove = fh.parse_ids_file(ids_file)
files_to_process = fh.filter_files_by_suffix(fh.files_to_process(workdir), file_extension)

print('Ids to remove:', ids_to_remove)


def removeEmptyGroup(item_element):
    parent_node = item_element.parentNode
    if parent_node is not None and parent_node.children.length == 0 and parent_node.tagName.lower() == 'group'.lower():
        print('Removing empty group',  item_element.parentNode.getValue())
        item_element.parentNode.parentNode.removeChild(item_element.parentNode)


def processes_items(item_elements):
    for item_element in item_elements:
        if item_element.getAttribute('id') in ids_to_remove:
            print('Removing item', item_element.getAttribute('id'))
            item_element.parentNode.removeChild(item_element)
            removeEmptyGroup(item_element)


for item in files_to_process:
    parser = ET.XMLParser(target=xh.CommentedTreeBuilder())
    dom = parser(item)
    drop_lists = dom.findall('dropLists')
    for drop_list in drop_lists:
        item_elements = drop_list.findall('item')
        processes_items(item_elements)

    dom.write(item)
