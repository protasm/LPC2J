/* ------------------------------------------------------------
 *  /obj/temple_scroll.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include "item_common.h"

void create() {
    set_item("temple scroll",
             "A rolled parchment bearing a blessing for safe journeys, edges smudged with incense.",
             1,
             ITEM_TAKEABLE);
}

status is_scroll() {
    return 1;
}
