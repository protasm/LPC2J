inherit "inherit_parent.c";

/* Child object overrides behavior, shadows fields, and relies on parent initialization. */
int shadowed_field = 5; /* Shadows the parent field. */
int child_only = shadowed_field + 1;
int child_init_order = parent_init_order + shadowed_field;
int manual_only = 0;

int shout() { return 200 + shadowed_field; } /* Expected 205 with child default. */
int call_parent_shout() { return ::shout(); } /* Calls parent shout (101 with defaults). */
int call_self_shout() { return shout(); } /* Uses child shout, still 205. */
int combined_shadow() { return shadowed_field + ::read_shadowed(); } /* 5 + 1 = 6 initially. */
int parent_field_via_method(int delta) { return ::bump_parent_field(delta); } /* Parent field starts at 10 before bumps. */
int totals() { return parent_field + child_only + child_init_order; } /* Starts at 36 before mutations. */
int manual_check() { return manual_only; } /* 0 until run_manual_setup is invoked. */
void run_manual_setup() { manual_only = child_only + 10; } /* Sets manual_only to 16 with defaults. */
