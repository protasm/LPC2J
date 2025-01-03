class JMain {
  public static void main(String[] args) {
    LPCObject lpcObject = new LPCObject();

    System.out.println(lpcObject.x);
    System.out.println(lpcObject.s);
    System.out.println(lpcObject.f);

    lpcObject.foo();

    System.out.println(lpcObject.x);
    System.out.println(lpcObject.s);
    System.out.println(lpcObject.f);
  }
}
