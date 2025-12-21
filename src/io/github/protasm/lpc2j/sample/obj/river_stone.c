/* ------------------------------------------------------------
 *  /obj/river_stone.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include "item_common.h"

void create() {
    set_item("river stone",
             "A smooth stone warmed by the sun, polished by years of flowing water.",
             2,
             ITEM_TAKEABLE);
}

string query_finish() {
    return "mirror-smooth";
}
