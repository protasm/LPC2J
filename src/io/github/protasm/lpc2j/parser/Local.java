package io.github.protasm.lpc2j.parser;

import io.github.protasm.lpc2j.LPCType;

public class Local {
    private LPCType lpcType;
    private String name;
    private int slot;
    private int scopeDepth;

    public Local(LPCType lpcType, String name) {
	this.lpcType = lpcType;
	this.name = name;

	slot = -1;
	scopeDepth = -1;
    }

    public LPCType lpcType() {
	return lpcType;
    }

    public String name() {
	return name;
    }

    public int slot() {
	return slot;
    }

    public int scopeDepth() {
	return scopeDepth;
    }

    public void setSlot(int slot) {
	this.slot = slot;
    }

    public void setScopeDepth(int scopeDepth) {
	this.scopeDepth = scopeDepth;
    }

    public String descriptor() {
	return lpcType.jType().descriptor();
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("Local(type=%s, name=%s, slot=%d, depth=%d)", lpcType, name, slot, scopeDepth));

	return sb.toString();
    }
}
