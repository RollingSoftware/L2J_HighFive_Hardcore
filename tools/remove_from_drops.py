import sys
import file_helper as fh
import lxml.etree as ET
import xml_helper as xh

script_name, workdir, file_extension, recursive, ids_file = sys.argv

ids_to_remove = fh.parse_ids_file(ids_file)
files_to_process = fh.filter_files_by_suffix(fh.files_to_process(workdir), file_extension)

print('Ids to remove:', ids_to_remove)


def removeEmptyDeathOrCorpse(death_node):
    if death_node is not None and (not death_node.getchildren()) and death_node.getparent() is not None and (death_node.tag.lower() == 'death' or death_node.tag.lower() == 'corpse'):
        print('Removing empty', death_node.tag)
        death_node.getparent().remove(death_node)


def removeEmptyGroup(group_node):
    if group_node is not None and (not group_node.getchildren()) and group_node.tag.lower() == 'group' and group_node.getparent() is not None:
        print('Removing empty group')
        death_node = group_node.getparent()
        death_node.remove(group_node)
        removeEmptyDeathOrCorpse(death_node)


def processes_items(item_elements):
    removedElements = []
    for item_element in item_elements:
        if item_element.get('id') in ids_to_remove:
            comment_sibling = item_element.getnext()
            if comment_sibling is not None and comment_sibling.tag is ET.Comment:
                item_element.getparent().remove(comment_sibling)
            print('Removing item', item_element.get('id'), comment_sibling)
            group_node = item_element.getparent()
            group_node.remove(item_element)
            removeEmptyGroup(group_node)
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
