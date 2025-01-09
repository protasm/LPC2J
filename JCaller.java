class JCaller {
  JCallee callee;

  public JCaller() {
    this.callee = new JCallee();
  }

  void foo() {
    callee.bar();
  }
}
