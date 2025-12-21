/* ============================================================
 *  /obj/room_macros.h
 *
 *  Local helper macros for room objects
 * ============================================================
 */

#define SHORT_DESC   "A quiet stone chamber"
#define LONG_DESC    \
"This is a small stone room lit by flickering torches.\n" \
"The air is damp, and the walls are covered in moss.\n"

#define VALID_EXIT(dir) ((dir) == "north" || (dir) == "south")

