/* ------------------------------------------------------------
 *  /room/guild_hall.c
 * ------------------------------------------------------------
 */

#include <world.h>
#include <paths.h>
#include "room_common.h"

void create() {
    room_type = ROOM_UNDERROOF;
    light_level = LIGHT_DIM;

    short_desc = "Guild Hall";
    long_desc =
"Long tables and battered benches fill the hall.\n"
"Guild banners hang from the rafters, muffling the hum of conversation.\n";

    exits = ([
        "north": R_TEMPLE_STEPS,
        "west": R_CARAVAN_ROAD,
        "east": R_WORKSHOP_LANE
    ]);

    items = ([
        OBJ_GUILD_LEDGER: "The ledger sits open, pages marked with sigils.",
        "banners": "Colorful banners mark each craft with pride."
    ]);

    npcs = ({ NPC_ADVENTURER });
}
