# 1 "room/rum2.c"
# 1 "<built-in>" 1
# 1 "<built-in>" 3
# 466 "<built-in>" 3
# 1 "<command line>" 1
# 1 "<built-in>" 2
# 1 "room/rum2.c" 2
int door_open;

long() {
    write("This is room 2. There is an exit to the west.\n");
}

go_west() {
    if (!door_open)
 write("The door is closed.\n");
    if (door_open)
 call_other(this_player(), "move_object", "west#room/test");
}

init() {
    add_action("go_west"); add_verb("west");
    door_open = call_other("room/test", "door_open", 0);
    write("You come into room 2.\n");
    if (door_open)
 write("There is an open door to the west.\n");
}
