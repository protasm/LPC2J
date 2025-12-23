/* Standalone object verifying fields and methods without inheritance. */
string label = "standalone";
int counter = 2;

int increment(int delta) { counter += delta; return counter; } /* Returns the new counter (starts at 2). */
string describe() { return label; } /* Returns \"standalone\". */
int squared_counter() { return counter * counter; } /* Computes the square of the current counter (4 initially). */
