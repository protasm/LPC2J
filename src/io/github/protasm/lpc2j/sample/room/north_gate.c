/* ------------------------------------------------------------
 *  /room/north_gate.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include <paths.h>
#include "room_common.h"

void create() {
    room_type = ROOM_OUTDOORS;
    light_level = LIGHT_BRIGHT;

    short_desc = "North Gate";
    long_desc =
"Stone walls rise to either side of the northern gatehouse.\n"
"Merchants shuffle past, and a bronze torch keeps the archway warm.\n";

    exits = ([
        "south": R_SOUTH_SQUARE,
        "east": R_MARKET_STREET
    ]);

    items = ([
        OBJ_BRONZE_TORCH: "Mounted to the gatehouse arch and kept alight by the guards.",
        "gate": "A reinforced timber gate stands open to welcome traders."
    ]);

    npcs = ({ });
}
