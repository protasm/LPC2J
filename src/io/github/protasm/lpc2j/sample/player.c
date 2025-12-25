string name;
object location;
string location_path;
string pending_move;

void set_name(string new_name) {
    name = new_name;
}

string query_name() {
    return name;
}

void set_location(object room, string room_path) {
    location = room;
    location_path = room_path;
}

string query_location() {
    return location_path;
}

string consume_pending_move() {
    string next_room = pending_move;
    pending_move = 0;
    return next_room;
}

string format_exits(mapping exits) {
    string *dirs;
    string summary;
    int i;

    if (!exits || !sizeof(exits)) {
        return "Exits: none.";
    }

    dirs = keys(exits);
    summary = "Exits: " + dirs[0];
    for (i = 1; i < sizeof(dirs); i++) {
        summary += ", " + dirs[i];
    }

    return summary + ".";
}

string look() {
    string short_desc;
    string long_desc;
    mapping exits;

    if (!location) {
        return "You are nowhere.\r\n";
    }

    short_desc = location->query_short();
    long_desc = location->query_long();
    exits = location->query_exits();

    return short_desc + "\r\n" + long_desc + "\r\n" + format_exits(exits) + "\r\n";
}

string handle_input(string input) {
    string command;
    string *parts;
    int i;
    mapping exits;

    if (!input || input == "") {
        return "Please enter a command.\r\n";
    }

    command = lower_case(input);
    parts = explode(command, " ");
    command = "";
    for (i = 0; i < sizeof(parts); i++) {
        if (parts[i] != "") {
            command = parts[i];
            break;
        }
    }
    if (command == "") {
        return "Please enter a command.\r\n";
    }
    if (command == "look") {
        return look();
    }

    exits = location ? location->query_exits() : 0;
    if (exits && exits[command]) {
        pending_move = exits[command];
        return "";
    }

    return "I don't know how to go that way.\r\n";
}
