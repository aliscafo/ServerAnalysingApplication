package ru.spbau.erokhina.common;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static List<Integer> selectedSort(List<Integer> list) {
        List<Integer> sortedList = new ArrayList<>(list);

        for (int i = 0; i < sortedList.size() - 1; i++) {
            for (int j = i + 1; j < sortedList.size(); j++) {
                if (sortedList.get(i) > sortedList.get(j)) {
                    Integer tmp = sortedList.get(i);
                    sortedList.set(i, sortedList.get(j));
                    sortedList.set(j, tmp);
                }
            }
        }

        return sortedList;
    }
}
