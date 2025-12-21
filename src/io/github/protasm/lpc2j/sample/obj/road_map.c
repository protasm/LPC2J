/* ------------------------------------------------------------
 *  /obj/road_map.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include "item_common.h"

void create() {
    set_item("road map",
             "A hand-drawn map showing the caravan route looping around the market and down to the river.",
             1,
             ITEM_TAKEABLE);
}

string query_route() {
    return "north gate -> market -> river bridge";
}
