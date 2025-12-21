/* ------------------------------------------------------------
 *  /obj/bronze_torch.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include "item_common.h"

void create() {
    set_item("bronze torch",
             "A sturdy bronze torch burns steadily beside the gate, throwing warm light onto the road.",
             2,
             ITEM_FIXED);
}

status is_light_source() {
    return 1;
}
