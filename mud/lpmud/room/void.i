# 1 "room/void.c"
# 1 "<built-in>" 1
# 1 "<built-in>" 3
# 466 "<built-in>" 3
# 1 "<command line>" 1
# 1 "<built-in>" 2
# 1 "room/void.c" 2
short() {
    return "The void";
}

long() {
    write(short() + ".\n");
    write("You come to the void if you fall out of a room, and have nowhere to go.\n");
    write("Give the command 'church', and you will come back to village church.\n");
}

init() {
    add_action("church"); add_verb("church");
    add_action("no_get"); add_verb("get");
}

no_get() {
    if (call_other(this_player(),"query_level",0) < 20) {
 write("You can't get things in the void!\n");
 return 1;
    }
    else return 0;
}

church() {
    call_other(this_player(), "move_player", "away#room/church");
    return 1;
}

reset(arg)
{
    object ob, next_ob;

    if (!arg) {
 set_light(1);
 return;
    }
    ob = first_inventory(this_object());
    if (!ob) return;

    while (ob) {
 next_ob = next_inventory(ob);

 if (!call_other(ob, "is_player", 0)) {
     destruct(ob);
 }
 ob = next_ob;
    }
}

id(str) { return str == "void"; }

realm() {return "NT";}
