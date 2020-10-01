package com.launchableinc.client.maven;

import org.apache.maven.surefire.util.TestsToRun;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * {@link Set} backed by {@link List}.
 *
 * <p>
 * In calling {@link TestsToRun}, this allows me to avoid one copy.
 * Not that I care about the performance, I'm just morally against the stupid API
 * that imposes an unnecessary type constraint.
 */
final class ListBackedBySet<T> extends AbstractSet<T> {
    private final List<T> store;

    ListBackedBySet(List<T> store) {
        this.store = store;
    }

    @Override
    public Iterator<T> iterator() {
        return store.iterator();
    }

    @Override
    public int size() {
        return store.size();
    }
}
