package ru.otus.hw02;

import java.util.*;

public class DIYArrayList<E> implements List<E> {
    private static int DEFAULT_CAPACITY = 10;

    private Object[] elements;
    private int size;

    public DIYArrayList(int capacity) {
        this.elements = new Object[capacity];
        this.size = 0;
    }

    public DIYArrayList() {
        this.elements = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(this.elements, this.size);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    private void increaseCapacity() {
        int newCapacity;

        if (this.elements.length == 0) {
            newCapacity = DEFAULT_CAPACITY;
        } else {
            newCapacity = (int) (1.5 * this.elements.length);
        }

        if (newCapacity < 0) {
            throw new OutOfMemoryError();
        }

        this.elements = Arrays.copyOf(this.elements, newCapacity);
    }

    @Override
    public boolean add(E e) {
        if (this.size == this.elements.length) {
            this.increaseCapacity();
        }

        this.elements[this.size] = e;
        this.size++;

        return true;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public E get(int index) {
        if (index > this.size) {
            throw new IndexOutOfBoundsException(index);
        }

        return (E) this.elements[index];
    }

    @Override
    @SuppressWarnings("unchecked")
    public E set(int index, E element) {
        if (index > this.size) {
            throw new IndexOutOfBoundsException(index);
        }

        E oldValue = (E) this.elements[index];
        this.elements[index] = element;
        return oldValue;
    }

    @Override
    public void add(int index, E element) {
        if (this.size == this.elements.length) {
            this.increaseCapacity();
        }

        System.arraycopy(this.elements, index, this.elements, index + 1, this.size - index);
        this.elements[index] = element;
        this.size++;
    }

    @Override
    public E remove(int index) {
        E item = this.get(index);
        this.size--;

        if (this.size > index) {
            System.arraycopy(this.elements, index + 1, this.elements, index, this.size - index);
        }

        this.elements[this.size] = null;
        return item;
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<E> listIterator() {
        return new DIYListIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new DIYListIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    private class DIYListIterator implements ListIterator<E> {
        private int cursor;
        private int lastReturnedIndex = -1;

        DIYListIterator(int index) {
            this.cursor = index;
        }

        @Override
        public boolean hasNext() {
            return this.cursor < size;
        }

        @Override
        public E next() {
            try {
                E item = get(this.cursor);
                this.lastReturnedIndex = cursor;
                this.cursor++;
                return item;
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        @Override
        public boolean hasPrevious() {
            return this.cursor > 0;
        }

        @Override
        public E previous() {
            try {
                this.cursor--;
                E item = get(this.cursor);
                this.lastReturnedIndex = this.cursor;
                return item;
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void remove() {
            if (this.lastReturnedIndex < 0) {
                throw new IllegalStateException();
            }

            DIYArrayList.this.remove(this.lastReturnedIndex);
            this.cursor = this.lastReturnedIndex;
            this.lastReturnedIndex = -1;
        }

        @Override
        public void set(E e) {
            if (this.lastReturnedIndex < 0) {
                throw new IllegalStateException();
            }

            DIYArrayList.this.set(this.lastReturnedIndex, e);
        }

        @Override
        public void add(E e) {
            DIYArrayList.this.add(this.cursor, e);
            this.lastReturnedIndex = -1;
            this.cursor++;
        }
    }
}
