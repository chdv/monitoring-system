package com.dch.app.monitor;

import java.util.Comparator;

/**
 * Created by ִלטענטי on 24.06.2015.
 */
public class StringComparator implements Comparator<String>  {

    @Override
    public int compare(String o1, String o2) {
        return o1.compareTo(o2);
    }

}
