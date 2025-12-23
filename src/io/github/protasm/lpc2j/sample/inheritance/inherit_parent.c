/* Parent object with initialized fields used by inheritance tests. */
int parent_field = 10;
int shadowed_field = 1;
int parent_init_order = parent_field + 5;
int parent_manual_flag = -1;

int shout() { return 100 + shadowed_field; } /* Expected 101 using defaults. */
int read_shadowed() { return shadowed_field; } /* Returns 1 from parent scope. */
int bump_parent_field(int delta) { parent_field += delta; return parent_field; } /* Adds delta to the starting value 10. */
int parent_sum() { return parent_field + parent_init_order; } /* Starts at 25 before mutations. */
int manual_marker() { return parent_manual_flag; } /* Returns -1 unless updated manually. */
