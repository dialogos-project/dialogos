package com.clt.util;

import java.util.Comparator;
import java.util.List;

/**
 * This is a generic version of C.A.R Hoare's Quick Sort algorithm. This will
 * handle arrays that are already sorted, and arrays with duplicate keys.
 */
public class Sort<T> {

    private Comparator<? super T> comparator;

    /**
     * Construct a new Sort object using the given comparator
     */
    public Sort(Comparator<? super T> comparator) {

        this.comparator = comparator;
    }

    /**
     * @param a an Object array
     * @param lo0 left boundary of array partition
     * @param hi0 right boundary of array partition
     */
    private <Type extends T> void qsort(Type[] a, int lo0, int hi0) {

        int lo = lo0;
        int hi = hi0;

        if (hi0 > lo0) {
            /*
       * Arbitrarily establishing partition element as the midpoint of the
       * array.
             */
            T mid = a[(lo0 + hi0) / 2];

            // loop through the array until indices cross
            while (lo <= hi) {
                /*
         * find the first element that is greater than or equal to the partition
         * element starting from the left Index.
                 */
                while ((lo < hi0) && (this.comparator.compare(a[lo], mid) < 0)) {
                    ++lo;
                }

                /*
         * find an element that is smaller than or equal to the partition
         * element starting from the right Index.
                 */
                while ((hi > lo0) && (this.comparator.compare(a[hi], mid) > 0)) {
                    --hi;
                }

                // if the indexes have not crossed, swap
                if (lo <= hi) {
                    this.swap(a, lo++, hi--);
                }
            }

            /*
       * If the right index has not reached the left side of array must now sort
       * the left partition.
             */
            if (lo0 < hi) {
                this.qsort(a, lo0, hi);
            }

            /*
       * If the left index has not reached the right side of array must now sort
       * the right partition.
             */
            if (lo < hi0) {
                this.qsort(a, lo, hi0);
            }
        }
    }

    private <Type extends T> void qsort(List<Type> a, int lo0, int hi0) {

        int lo = lo0;
        int hi = hi0;

        if (hi0 > lo0) {
            /*
       * Arbitrarily establishing partition element as the midpoint of the
       * array.
             */
            T mid = a.get((lo0 + hi0) / 2);

            // loop through the array until indices cross
            while (lo <= hi) {
                /*
         * find the first element that is greater than or equal to the partition
         * element starting from the left Index.
                 */
                while ((lo < hi0) && (this.comparator.compare(a.get(lo), mid) < 0)) {
                    ++lo;
                }

                /*
         * find an element that is smaller than or equal to the partition
         * element starting from the right Index.
                 */
                while ((hi > lo0) && (this.comparator.compare(a.get(hi), mid) > 0)) {
                    --hi;
                }

                // if the indexes have not crossed, swap
                if (lo <= hi) {
                    this.swap(a, lo++, hi--);
                }
            }

            /*
       * If the right index has not reached the left side of array must now sort
       * the left partition.
             */
            if (lo0 < hi) {
                this.qsort(a, lo0, hi);
            }

            /*
       * If the left index has not reached the right side of array must now sort
       * the right partition.
             */
            if (lo < hi0) {
                this.qsort(a, lo, hi0);
            }
        }
    }

    private <Type extends T> void swap(Type[] a, int i, int j) {

        Type temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    private <Type extends T> void swap(List<Type> a, int i, int j) {

        Type temp = a.get(i);
        a.set(i, a.get(j));
        a.set(j, temp);
    }

    /**
     * Sort the given array in place.
     */
    public <Type extends T> void sort(Type[] a) {

        this.qsort(a, 0, a.length - 1);
    }

    /**
     * Sort the first n elements of the given array in place.
     */
    public <Type extends T> void sort(Type[] a, int length) {

        this.qsort(a, 0, length - 1);
    }

    /**
     * Sort the given list in place.
     */
    public <Type extends T> void sort(List<? extends Type> a) {

        this.qsort(a, 0, a.size() - 1);
    }

    /**
     * Sort the first n elements of the given list in place.
     */
    public <Type extends T> void sort(List<? extends Type> a, int length) {

        this.qsort(a, 0, length - 1);
    }

    /**
     * Sort an array of strings according to German collation rules.
     */
    public static void stringArray(String[] a) {

        new Sort<String>(new StringComparator()).sort(a);
    }

    /**
     * Sort a list of strings according to German collation rules.
     */
    public static void stringVector(List<String> a) {

        new Sort<String>(new StringComparator()).sort(a);
    }

    /**
     * Sort an array of objects according to their natural ordering.
     */
    public static <Type extends Comparable<? super Type>> void sortArray(
            Type[] array) {

        Comparator<Type> comparator = new Comparator<Type>() {

            public int compare(Type o1, Type o2) {

                return o1.compareTo(o2);
            }
        };
        new Sort<Type>(comparator).sort(array);
    }

    /**
     * Sort a list of objects according to their natural ordering.
     */
    public static <Type extends Comparable<? super Type>> void sortList(
            List<? extends Type> list) {

        Comparator<Type> comparator = new Comparator<Type>() {

            public int compare(Type o1, Type o2) {

                return o1.compareTo(o2);
            }
        };
        new Sort<Type>(comparator).sort(list);
    }

    private static class StringComparator
            implements Comparator<String> {

        public int compare(String a, String b) {

            return StringTools.compare(a, b);
        }
    }

}
