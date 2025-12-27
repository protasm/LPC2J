inherit "/inheritance/inherit_parent.c";

/* Child object overrides behavior, shadows fields, and relies on parent initialization. */
int shadowed_field = 5; /* Shadows the parent field. */
int child_only = shadowed_field + 1;
int child_init_order = parent_init_order + shadowed_field;
int manual_only = 0;

int shout() { return 200 + shadowed_field; } /* 205 */
int call_parent_shout() { return ::shout(); } /* 101 */
int call_self_shout() { return shout(); } /* 205 */
int combined_shadow() { return shadowed_field + ::read_shadowed(); } /* 5 + 1 = 6 */
int parent_field_via_method(int delta) { return ::bump_parent_field(delta); } /* 10  */
int totals() { return parent_field + child_only + child_init_order; } /* 36 */
int manual_check() { return manual_only; } /* 0 until run_manual_setup is invoked. */
void run_manual_setup() { manual_only = child_only + 10; } /* 16 */
