package org.apache.skywalking.oap.server.core.query.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Dots {

    private final List<List<Long>> nodes;

    public Dots() {
        this.nodes = new ArrayList<>();
    }

    public void addNode(int columnNum, int rowNum, Long value) {
        List<Long> element = new ArrayList<>(3);
        element.add((long) columnNum);
        element.add((long) rowNum);
        element.add(value);
        nodes.add(element);
    }
}
