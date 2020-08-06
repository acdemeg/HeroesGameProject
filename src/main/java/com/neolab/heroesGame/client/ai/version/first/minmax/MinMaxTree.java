package com.neolab.heroesGame.client.ai.version.first.minmax;

import com.neolab.heroesGame.server.answers.Answer;

import java.util.List;

public class MinMaxTree {
    private final NodeMinMax root;
    private NodeMinMax currentNode;

    public MinMaxTree() {
        root = new NodeMinMax();
        currentNode = root;
    }

    public boolean upToParent() {
        if (currentNode != root) {
            currentNode = currentNode.getParent();
            return true;
        }
        return false;
    }

    public boolean downToChild(final int index) {
        final NodeMinMax temp = currentNode.getChild(index);
        if (temp != null) {
            currentNode = temp;
            return true;
        }
        return false;
    }

    public void createAllChildren(List<Answer> answers) {
        for (Answer answer : answers) {
            currentNode.addChild(answer);
        }
    }

    public boolean isMaxDepth(final int maxDepth) {
        return currentNode.getDepth() == maxDepth;
    }

    public int getCurrentDepth() {
        return currentNode.getDepth();
    }

    public Answer getAnswerFromChild(final int childIndex) {
        return currentNode.getChild(childIndex).getPrevAnswer();
    }

    public void setHeuristic(final int heuristic) {
        currentNode.setHeuristic(heuristic);
    }

    public Answer getBestHeuristicAnswer() {
        final List<NodeMinMax> children = currentNode.getChildren();
        NodeMinMax temp = new NodeMinMax();
        for (final NodeMinMax child : children) {
            if (temp.getHeuristic() < child.getHeuristic()) {
                temp = child;
            }
        }
        return temp.getPrevAnswer();
    }

    public int getMaxHeuristicAnswer() {
        final List<NodeMinMax> children = currentNode.getChildren();
        NodeMinMax temp = new NodeMinMax();
        for (final NodeMinMax child : children) {
            if (temp.getHeuristic() < child.getHeuristic()) {
                temp = child;
            }
        }
        return temp.getHeuristic();
    }

    public int getMinHeuristicAnswer() {
        final List<NodeMinMax> children = currentNode.getChildren();
        NodeMinMax temp = new NodeMinMax();
        temp.setHeuristic(Integer.MAX_VALUE);
        for (final NodeMinMax child : children) {
            if (temp.getHeuristic() > child.getHeuristic()) {
                temp = child;
            }
        }
        return temp.getHeuristic();
    }
}
