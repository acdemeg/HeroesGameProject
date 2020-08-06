package com.neolab.heroesGame.client.ai.version.first.minmax;

import com.neolab.heroesGame.server.answers.Answer;

import java.util.ArrayList;
import java.util.List;

public class NodeMinMax {
    private final Answer prevAnswer;
    private final NodeMinMax parent;
    private final int depth;
    private final List<NodeMinMax> children;
    private int heuristic = Integer.MIN_VALUE;

    public NodeMinMax(final Answer prevAnswer, final NodeMinMax parent) {
        this.prevAnswer = prevAnswer;
        this.parent = parent;
        depth = parent.depth + 1;
        children = new ArrayList<>();
    }

    NodeMinMax() {
        prevAnswer = null;
        parent = null;
        depth = 0;
        children = new ArrayList<>();
    }

    public Answer getPrevAnswer() {
        return prevAnswer;
    }

    public NodeMinMax getParent() {
        return parent;
    }

    public int getDepth() {
        return depth;
    }

    public List<NodeMinMax> getChildren() {
        return children;
    }

    public NodeMinMax getChild(final int index) {
        return children.get(index);
    }

    public void addChild(final Answer answer) {
        children.add(new NodeMinMax(answer, this));
    }

    public int getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(final int heuristic) {
        this.heuristic = heuristic;
    }
}
