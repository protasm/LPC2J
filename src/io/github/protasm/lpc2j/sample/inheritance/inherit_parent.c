/* Parent object with initialized fields used by inheritance tests. */
int parent_field = 10;
int shadowed_field = 1;
int parent_init_order = parent_field + 5;
int parent_manual_flag = -1;

int shout() { return 100 + shadowed_field; }
int read_shadowed() { return shadowed_field; }
int bump_parent_field(int delta) { parent_field += delta; return parent_field; }
int parent_sum() { return parent_field + parent_init_order; }
int manual_marker() { return parent_manual_flag; }
