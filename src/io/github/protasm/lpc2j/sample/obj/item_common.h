/* ============================================================
 *  /obj/item_common.h
 *
 *  Local helper definitions for simple inventory items
 * ============================================================
 */

string item_name;
string item_desc;
int item_weight;
status takeable;

void set_item(string name, string desc, int weight, status can_take) {
    item_name = name;
    item_desc = desc;
    item_weight = weight;
    takeable = can_take;
}

string short() {
    return item_name;
}

void long() {
    write(item_desc);
}

int query_weight() {
    return item_weight;
}

status can_take() {
    return takeable;
}
