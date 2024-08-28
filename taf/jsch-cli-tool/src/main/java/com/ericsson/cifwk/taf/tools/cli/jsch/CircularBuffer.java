package com.ericsson.cifwk.taf.tools.cli.jsch;

import com.google.common.base.Preconditions;

import java.util.LinkedList;

/**
 * Created by emccojo on 09/06/2016.
 */
public class CircularBuffer<T> {
    private final LinkedList<T> buffer;
    private int size;

    public CircularBuffer(int size) {
        Preconditions.checkArgument(size > 0, "Size should be greater than 0");
        this.size = size;
        this.buffer = new LinkedList<>();
    }

    public void add(T element) {
        if (buffer.size() >= size) {
            buffer.pollLast();
        }
        buffer.push(element);
    }

    public  boolean isEmpty(){
        return buffer.isEmpty();
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
