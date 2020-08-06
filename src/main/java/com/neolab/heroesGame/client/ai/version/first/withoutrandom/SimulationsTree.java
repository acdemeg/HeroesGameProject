package com.neolab.heroesGame.client.ai.version.first.withoutrandom;

import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.server.answers.Answer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimulationsTree {
    private final Node root;
    private Node currentNode;

    public SimulationsTree() {
        root = new Node(null, null);
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
        final Node temp = currentNode.getChild(index);
        if (temp != null) {
            currentNode = temp;
            return true;
        }
        return false;
    }

    public List<Double> getActionPriority() {
        return Optional.ofNullable(currentNode.getActionPriorityForChoose()).orElseThrow();
    }

    public boolean isNodeNew() {
        return currentNode.isNewNode();
    }

    public Answer getBestAction() {
        final List<Double> actionPriority = currentNode.getActionPriority();
        int max = 0;
        for (int i = 0; i < actionPriority.size(); i++) {
            if (actionPriority.get(max) < actionPriority.get(i)) {
                max = i;
            }
        }
        return currentNode.getChild(max).getPrevAnswer();
    }

    public Answer getBestScoreRate() {
        final List<Node> children = currentNode.getChildren();
        Node result = children.get(0);
        for (final Node child : children) {
            if (child.getScorePercent() > result.getScorePercent()) {
                result = child;
            }
        }
        return result.getPrevAnswer();
    }

    public void increase(final GameEvent event) {
        currentNode.increase(event);
    }

    public void toRoot() {
        currentNode = root;
    }

    public void fieldNewNode(final Map<Integer, Answer> actions, final List<Double> calculateActionPriority) {
        actions.forEach((key, answer) -> currentNode.createChild(key, answer));
        currentNode.setActionPriority(calculateActionPriority);
    }

    public Answer getAnswer(final int actionNumber) {
        return currentNode.getChild(actionNumber).getPrevAnswer();
    }
}
