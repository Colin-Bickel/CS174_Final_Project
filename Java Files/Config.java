package org.ivc.dbms.Main;

import java.util.ArrayList;

public class Config {
    public static final int MAX_CLASSES = 5;
    public static final String CURRENT_QTR = "S";
    public static final String CURRENT_YEAR = "2025";

    public static final ArrayList<ArrayList<String>> QTR_LIST = new ArrayList();
    static {
        QTR_LIST.add(new ArrayList(2));
        QTR_LIST.add(new ArrayList(2));
        QTR_LIST.add(new ArrayList(2));

        QTR_LIST.get(0).add("2025");
        QTR_LIST.get(0).add("S");
        QTR_LIST.get(1).add("2024");
        QTR_LIST.get(1).add("F");
        QTR_LIST.get(2).add("2025");
        QTR_LIST.get(2).add("W");
    }
}

