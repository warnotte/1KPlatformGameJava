package com.warnotte.pf1kwaxgdx;

import java.util.ArrayList;
import java.util.List;

public class LevelGDX {
    public List<CaseGDX> casesList = new ArrayList<>();
    public float caseWidth = 30;

    public CaseGDX getCase(float x) {
        int idx = (int)x/(int)caseWidth;
        if (idx < casesList.size())
            return casesList.get(idx);
        return null;
    }

    public int getNbrCases() {
        return casesList.size();
    }
}
