package com.autorizacoes.core.security;

public enum SecurityLevel {
    PUBLIC(1),
    CONFIDENTIAL(2),
    SECRET(3);

    private int level;

    SecurityLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
