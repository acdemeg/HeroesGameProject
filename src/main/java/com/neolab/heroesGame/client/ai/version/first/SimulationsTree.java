package com.neolab.heroesGame.client.ai.version.first;

import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.server.answers.Answer;

import java.util.List;

public class SimulationsTree {
    private final Node root;
    private Node currentNode;

    public SimulationsTree() {
        root = new Node(null, null);
        currentNode = root;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public List<Node> getChildren() {
        return currentNode.getChildren();
    }

    public boolean upToParent() {
        if (currentNode != root) {
            currentNode = currentNode.getParent();
            return true;
        }
        return false;
    }

    public boolean downToChild(int index) {
        final Node temp = currentNode.getChild(index);
        if (temp != null) {
            currentNode = temp;
            return true;
        }
        return false;
    }

    public Node createChild(final int index, final Answer prevAnswer) {
        currentNode.createChild(index, prevAnswer);
        return currentNode.getChild(index);
    }

    public double calculatePoints() {
        return (currentNode.getWinCounter() * 3.0 + currentNode.getTiesCounter() * 1.0)
                / currentNode.getSimulationsCounter();
    }

    public double calculatePointsForChild(final int index) {
        Node child = currentNode.getChild(index);
        if (child == null) {
            return 0;
        }
        return 0.5 - (child.getWinCounter() + child.getTiesCounter() * 0.5)
                / child.getSimulationsCounter();
    }

    public void increase(final GameEvent event) {
        currentNode.increase(event);
    }

    public void toRoot() {
        currentNode = root;
    }
}
