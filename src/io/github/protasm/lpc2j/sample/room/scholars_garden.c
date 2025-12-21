/* ------------------------------------------------------------
 *  /room/scholars_garden.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include <paths.h>
#include "room_common.h"

void create() {
    room_type = ROOM_OUTDOORS;
    light_level = LIGHT_BRIGHT;

    short_desc = "Scholar's Garden";
    long_desc =
"Low stone borders frame beds of herbs and quiet benches.\n"
"Scribes take their meals here while reciting lessons under the open sky.\n";

    exits = ([
        "west": R_TEMPLE_STEPS,
        "east": R_RIVER_BRIDGE,
        "south": R_WORKSHOP_LANE
    ]);

    items = ([
        OBJ_HERB_SATCHEL: "A satchel forgotten on a bench, still fragrant with fresh cuttings.",
        "benches": "Polished benches invite a moment of study."
    ]);

    npcs = ({ });
}
