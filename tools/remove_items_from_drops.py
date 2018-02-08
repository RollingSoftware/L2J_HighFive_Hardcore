import sys
import file_helper as fh
from xml.dom import minidom, Node

script_name, workdir, file_extension, recursive, ids_file = sys.argv

ids_to_remove = fh.parse_ids_file(ids_file)
files_to_process = fh.filter_files_by_suffix(fh.files_to_process(workdir), file_extension)

print('Ids to remove:', ids_to_remove)

def removeEmptyGroup(item_element):
    parent_node = item_element.parentNode
    if parent_node != None and parent_node.children.length == 0 and parent_node.tagName.lower() == 'group'.lower():
        print('Removing empty group',  item_element.parentNode.getValue())
        item_element.parentNode.parentNode.removeChild(item_element.parentNode)


def processes_items(item_elements):
    for item_element in item_elements:
        if item_element.getAttribute('id') in ids_to_remove:
            comment = None
            if item_element.nextSibling not None and item_element.nextSibling.nodeType == Node.COMMENT_NODE:
                comment = item_element.nextSibling.getValue()
            print('Removing item', item_element.getAttribute('id'))
            item_element.parentNode.removeChild(item_element)
            removeEmptyGroup(item_element)


for item in files_to_process:
    dom = minidom.parse(item)
    drop_lists = dom.getElementsByTagName('dropLists')
    for drop_list in drop_lists:
        item_elements = drop_list.getElementsByTagName('item')
        processes_items(item_elements)

    dom.writexml(open(item, 'w'))
