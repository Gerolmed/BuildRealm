package net.endrealm.lostsouls.utils;

import lombok.Data;

import java.util.Comparator;

@Data
public class NameComparator<T> implements Comparator<T> {
    private final NameFactory<T> nameFactory;
    @Override
    public int compare(T o1, T o2) {
        //Flipped to be descending
        String name1 = nameFactory.from(o1);
        String name2 = nameFactory.from(o2);

        int lengthComp = name1.length() - name2.length();
        int compDist = Math.min(name1.length(), name2.length());

        for (int i = 0; i < compDist; i++) {
            int compChar = name1.charAt(i) - name2.charAt(i);

            if(compChar != 0)
                return compChar;
        }

        return lengthComp;
    }

    @FunctionalInterface
    public interface NameFactory<T> {
        String from(T type);
    }
}
