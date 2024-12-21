package io.github.protasm.lpc2j;

public enum J_Type {
  BOOLEAN ("Z"),
  CHAR    ("C"),
  FLOAT   ("F"),
  DOUBLE  ("D"),
  BYTE    ("B"),
  SHORT   ("S"),
  INT     ("I"),
  LONG    ("J"),
  STRING  ("Ljava/lang/String;"),
  OBJECT  ("Ljava/lang/Object;"),
//  ARRAY   ("["),   // Followed by the type descriptor of the array element type
  VOID    ("V");

  private final String rawValue;

  J_Type(String rawValue) {
    this.rawValue = rawValue;
  }

  public String rawValue() {
    return rawValue;
  }
}