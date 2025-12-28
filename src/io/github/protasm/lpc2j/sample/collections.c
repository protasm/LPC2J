int sum(int *values) {
  int total;
  int i;

  total = 0;
  for (i = 0; i < sizeof(values); i++) {
    total += values[i];
  }
  return total;
}

mapping index_lookup(string *names) {
  mapping lookup;
  int i;

  lookup = ([]);
  for (i = 0; i < sizeof(names); i++) {
    lookup[names[i]] = i;
  }
  return lookup;
}
