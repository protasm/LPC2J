inherit "/room/room.c";

void configure_room(string room_id);

void create() {
    string name;
    string *parts;

    ::create();

    name = object_name(this_object());
    parts = explode(name, "#");
    if (sizeof(parts) < 2) {
        return;
    }

    configure_room(parts[1]);
}

void configure_room(string room_id) {
    mapping data;
    mapping exits;
    string direction;
    string destination;

    data = "/domain/original/world/world_loader"->get_room_data(room_id);
    if (!mapp(data)) {
        return;
    }

    set_short("Virtual room " + room_id);
    set_long("This is a virtual room.");

    exits = data["exits"];
    if (mapp(exits)) {
        foreach (direction, destination in exits) {
            add_exit(direction, "/domain/original/world/vroom#" + destination);
        }
    }
}
