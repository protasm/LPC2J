/* Standalone object verifying fields and methods without inheritance. */
string label = "standalone";
int counter = 2;

int increment(int delta) { counter += delta; return counter; }
string describe() { return label; }
int squared_counter() { return counter * counter; }
