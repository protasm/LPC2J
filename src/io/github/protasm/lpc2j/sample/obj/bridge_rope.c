/* ------------------------------------------------------------
 *  /obj/bridge_rope.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include "item_common.h"

void create() {
    set_item("bridge rope",
             "A length of tarred rope coiled neatly, ready to secure the bridge railings.",
             3,
             ITEM_TAKEABLE);
}

status is_coiled() {
    return 1;
}
