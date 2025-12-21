/* ============================================================
 *  /living/npc_common.h
 *
 *  Local helpers for sample living NPCs
 * ============================================================
 */

int health;
int stamina;
int attack_power;
string npc_name;
string npc_description;
string stationed_room;

void set_stats(int hp, int sp, int atk) {
    health = hp;
    stamina = sp;
    attack_power = atk;
}

void set_identity(string name, string desc) {
    npc_name = name;
    npc_description = desc;
}

void set_station(string room_path) {
    stationed_room = room_path;
}

string short() {
    return npc_name;
}

void long() {
    write(npc_description);
}

int query_health() {
    return health;
}

int query_stamina() {
    return stamina;
}

int query_attack_power() {
    return attack_power;
}

string query_station() {
    return stationed_room;
}
