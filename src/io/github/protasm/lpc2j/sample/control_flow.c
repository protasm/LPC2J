int clamp(int value, int minimum, int maximum) {
  if (value < minimum) {
    return minimum;
  }
  if (value > maximum) {
    return maximum;
  }
  return value;
}

string parity_label(int value) {
  switch (value % 2) {
    case 0:
      return "even";
    default:
      return "odd";
  }
}
