/* ============================================================
 *  /room/room_common.h
 *
 *  Local helpers and shared state declarations for room objects
 * ============================================================
 */

int room_type;
int light_level;
string short_desc;
string long_desc;
mapping exits;
mapping items;
string *npcs;

int query_light() {
    return light_level;
}

string short() {
    return short_desc;
}

void long() {
    write(long_desc);
}

string query_exit(string dir) {
    return exits[dir];
}

mapping query_exits() {
    return exits;
}

mapping query_items() {
    return items;
}

string *query_npcs() {
    return npcs;
}
