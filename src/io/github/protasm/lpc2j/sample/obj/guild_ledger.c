/* ------------------------------------------------------------
 *  /obj/guild_ledger.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include "item_common.h"

void create() {
    set_item("guild ledger",
             "A heavy ledger listing apprentices, dues, and deliveries bound for the workshops.",
             4,
             ITEM_FIXED);
}

int query_entries() {
    return 28;
}
