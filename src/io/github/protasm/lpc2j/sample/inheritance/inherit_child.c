inherit "inherit_parent.c";

/* Child object overrides behavior, shadows fields, and relies on parent initialization. */
int shadowed_field = 5; /* Shadows the parent field. */
int child_only = shadowed_field + 1;
int child_init_order = parent_init_order + shadowed_field;
int manual_only = 0;

int shout() { return 200 + shadowed_field; }
int call_parent_shout() { return ::shout(); }
int call_self_shout() { return shout(); }
int combined_shadow() { return shadowed_field + ::read_shadowed(); }
int parent_field_via_method(int delta) { return ::bump_parent_field(delta); }
int totals() { return parent_field + child_only + child_init_order; }
int manual_check() { return manual_only; }
void run_manual_setup() { manual_only = child_only + 10; }
