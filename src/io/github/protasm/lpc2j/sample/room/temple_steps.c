/* ------------------------------------------------------------
 *  /room/temple_steps.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include <paths.h>
#include "room_common.h"

void create() {
    room_type = ROOM_OUTDOORS;
    light_level = LIGHT_BRIGHT;

    short_desc = "Temple Steps";
    long_desc =
"Wide stone steps climb toward a modest shrine.\n"
"Braziers smoke with incense, and pilgrims pause to tie prayer ribbons.\n";

    exits = ([
        "west": R_MARKET_STREET,
        "east": R_SCHOLARS_GARDEN,
        "south": R_GUILD_HALL
    ]);

    items = ([
        OBJ_TEMPLE_SCROLL: "A blessing left by a grateful traveler.",
        "braziers": "Twin braziers burn fragrant resin that drifts down the steps."
    ]);

    npcs = ({ });
}
