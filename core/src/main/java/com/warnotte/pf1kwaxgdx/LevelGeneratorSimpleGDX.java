package com.warnotte.pf1kwaxgdx;

public class LevelGeneratorSimpleGDX {
    int len;
    public LevelGeneratorSimpleGDX(int len) {
        this.len = len;
    }

    public LevelGDX generateLevel() {
        int MAX_HEIGHT_DELTA = 15;
        LevelGDX lvl = new LevelGDX();
        boolean prevHole = false;
        for (int i = 0; i < len; i++) {
            float h = (float) (0 + (Math.random() * 20));
            if (!prevHole && Math.random() < 0.3) {
                h = 0;
                prevHole = true;
            } else {
                prevHole = false;
            }
            if (h > MAX_HEIGHT_DELTA) h = MAX_HEIGHT_DELTA;
            if (i == 0) h = 10;
            if (i == len - 1) h = 10;
            CaseGDX ca = new CaseGDX(h);
            if (h > 2) ca.isTree = (Math.random() >= 0.5);
            lvl.casesList.add(ca);
            if (Math.random() > 0.8)
                ca.type = CaseGDX.TypeCase.DYNAMIC;
            if (h == 0) ca.type = CaseGDX.TypeCase.HOLE;
            if (i == 0) ca.type = CaseGDX.TypeCase.STATIC;
            if (i == len - 1) ca.type = CaseGDX.TypeCase.STATIC;
        }
        return lvl;
    }
}
