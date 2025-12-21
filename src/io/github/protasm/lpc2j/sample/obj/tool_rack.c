/* ------------------------------------------------------------
 *  /obj/tool_rack.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include "item_common.h"

void create() {
    set_item("tool rack",
             "A rack of hammers, chisels, and planes waiting for the next craftsperson.",
             5,
             ITEM_FIXED);
}

string *tools_available() {
    return ({ "hammer", "awl", "mallet" });
}
