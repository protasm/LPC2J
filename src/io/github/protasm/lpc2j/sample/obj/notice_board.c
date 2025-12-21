/* ------------------------------------------------------------
 *  /obj/notice_board.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include "item_common.h"

void create() {
    set_item("notice board",
             "A broad board covered in public notices, caravan schedules, and requests for brave hands.",
             5,
             ITEM_FIXED);
}

string *list_notices() {
    return ({ "A patrol seeks volunteers.", "Fresh bread at the southern bakery.", "Beware of river fogs." });
}
