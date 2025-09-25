package com.warnotte.pf1kwaxgdx;

public class CaseGDX {
    public float hauteur;
    public enum TypeCase {STATIC, HOLE, DYNAMIC}
    public TypeCase type = TypeCase.STATIC;
    public boolean isTree = false;

    public CaseGDX(float hauteur) {
        this.hauteur = hauteur;
    }
}
