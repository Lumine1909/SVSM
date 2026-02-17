package io.github.lumine1909.svsm.util;

import ca.spottedleaf.concurrentutil.collection.MultiThreadedQueue;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Iterator;

public class DummyQueue<E> extends MultiThreadedQueue<E> {

    @SuppressWarnings("rawtypes")
    public static final DummyQueue INSTANCE = new DummyQueue();

    @SuppressWarnings("rawtypes")
    private static final Iterator DUMMY_ITR = new Iterator() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            return null;
        }
    };

    @Override
    public boolean add(E element) {
        return true;
    }

    @Override
    public boolean forceAdd(E element) {
        return true;
    }

    @Override
    public boolean remove(Object object) {
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public E peek() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull Iterator<E> iterator() {
        return DUMMY_ITR;
    }
}
