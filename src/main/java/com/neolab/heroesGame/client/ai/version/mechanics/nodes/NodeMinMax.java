package com.neolab.heroesGame.client.ai.version.mechanics.nodes;

import com.neolab.heroesGame.server.answers.Answer;

public class NodeMinMax extends ANode {
    private final int depth;
    private int heuristic = Integer.MIN_VALUE;

    public NodeMinMax(final Answer prevAnswer, final ANode parent) {
        super(prevAnswer, parent);
        depth = ((NodeMinMax) parent).depth + 1;
    }

    public NodeMinMax() {
        super(null, null);
        depth = 0;
    }

    @Override
    public ANode createChild(final Answer prevAnswer, final ANode aNode) {
        return new NodeMinMax(prevAnswer, aNode);
    }

    public int getDepth() {
        return depth;
    }

    public int getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(final int heuristic) {
        this.heuristic = heuristic;
    }
}
