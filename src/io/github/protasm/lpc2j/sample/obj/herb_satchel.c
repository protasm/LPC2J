/* ------------------------------------------------------------
 *  /obj/herb_satchel.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include "item_common.h"

void create() {
    set_item("herb satchel",
             "A small linen satchel stuffed with fragrant herbs from the garden beds.",
             1,
             ITEM_TAKEABLE);
}

string query_scent() {
    return "mint and rosemary";
}
