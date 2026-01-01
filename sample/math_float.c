float interpolate(float start, float end, float t) {
  return start + (end - start) * t;
}

int is_between(int value, int low, int high) {
  return value >= low && value <= high;
}
