/* ------------------------------------------------------------
 *  /obj/market_crate.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include "item_common.h"

void create() {
    set_item("market crate",
             "A crate filled with dried fruits and cloth bolts, stamped with the seal of the northern traders.",
             6,
             ITEM_FIXED);
}

int query_inventory_value() {
    return 15;
}
