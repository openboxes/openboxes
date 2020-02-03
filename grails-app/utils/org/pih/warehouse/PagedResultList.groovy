package org.pih.warehouse

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * THIS IS TEMPORARY PagedResultList class from old GORM version compatible with grails 1.3.9
 * TODO: Replace this workaround with proper PagedResultList approach
 */
public class PagedResultList implements List, Serializable {
    private static final long serialVersionUID = -5820655628956173929L;
    protected List list;
    protected int totalCount;

    public PagedResultList(List list) {
        this.list = list;
    }

    public PagedResultList(List list, int totalCount) {
        this.list = list;
        this.totalCount = totalCount;
    }

    public int size() {
        return this.list.size();
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public boolean contains(Object o) {
        return this.list.contains(o);
    }

    public Iterator iterator() {
        return this.list.iterator();
    }

    public Object[] toArray() {
        return this.list.toArray();
    }

    public Object[] toArray(Object[] objects) {
        return this.list.toArray(objects);
    }

    public boolean add(Object o) {
        return this.list.add(o);
    }

    public boolean remove(Object o) {
        return this.list.remove(o);
    }

    public boolean containsAll(Collection collection) {
        return this.list.containsAll(collection);
    }

    public boolean addAll(Collection collection) {
        return this.list.addAll(collection);
    }

    public boolean addAll(int i, Collection collection) {
        return this.list.addAll(i, collection);
    }

    public boolean removeAll(Collection collection) {
        return this.list.removeAll(collection);
    }

    public boolean retainAll(Collection collection) {
        return this.list.retainAll(collection);
    }

    public void clear() {
        this.list.clear();
    }

    public boolean equals(Object o) {
        return this.list.equals(o);
    }

    public int hashCode() {
        return this.list.hashCode();
    }

    public Object get(int i) {
        return this.list.get(i);
    }

    public Object set(int i, Object o) {
        return this.list.set(i, o);
    }

    public void add(int i, Object o) {
        this.list.add(i, o);
    }

    public Object remove(int i) {
        return this.list.remove(i);
    }

    public int indexOf(Object o) {
        return this.list.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return this.list.lastIndexOf(o);
    }

    public ListIterator listIterator() {
        return this.list.listIterator();
    }

    public ListIterator listIterator(int i) {
        return this.list.listIterator(i);
    }

    public List subList(int i, int i1) {
        return this.list.subList(i, i1);
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
