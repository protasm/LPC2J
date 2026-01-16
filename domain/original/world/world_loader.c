mapping _room_data;
int _loaded;

void load_world() {
    string raw;
    mixed decoded;

    if (_loaded) {
        return;
    }

    _loaded = 1;
    raw = read_file("/domain/original/world/world.json");
    if (!stringp(raw)) {
        _room_data = ([]);
        return;
    }

    decoded = json_decode(raw);
    if (mapp(decoded)) {
        _room_data = decoded;
        return;
    }

    _room_data = ([]);
}

mapping get_room_data(string room_id) {
    load_world();

    if (!mapp(_room_data)) {
        return 0;
    }

    return _room_data[room_id];
}
