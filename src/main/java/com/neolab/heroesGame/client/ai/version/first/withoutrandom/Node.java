package com.neolab.heroesGame.client.ai.version.first.withoutrandom;

import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.server.answers.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Node {
    private final Answer prevAnswer;
    private final Node parent;
    private final List<Node> children;
    private int winCounter;
    private int simulationsCounter;
    private int tiesCounter;
    private List<Double> basicActionPriority = null;

    public Node(final Answer prevAnswer, final Node parent) {
        this.prevAnswer = prevAnswer;
        this.parent = parent;
        children = new ArrayList<>();
        winCounter = 0;
        simulationsCounter = 0;
        tiesCounter = 0;
    }

    public Answer getPrevAnswer() {
        return prevAnswer;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setActionPriority(final List<Double> actionPriority) {
        basicActionPriority = actionPriority;
    }

    public Node getChild(final int index) {
        if (children.size() <= index) {
            return null;
        }
        return Optional.ofNullable(children.get(index)).orElse(null);
    }

    public void createChild(final int index, final Answer prevAnswer) {
        final Node child = new Node(prevAnswer, this);
        for (int i = children.size(); i <= index; i++) {
            children.add(null);
        }
        children.set(index, child);
    }

    public Node getParent() {
        return parent;
    }

    public int getScorePercent() {
        return simulationsCounter != 0 ? ((100 * (winCounter + tiesCounter / 2)) / simulationsCounter) : 0;
    }

    public boolean isNewNode() {
        return children.isEmpty();
    }

    public void increase(final GameEvent event) {
        if (event == GameEvent.YOU_WIN_GAME) {
            winCounter++;
        } else if (event == GameEvent.GAME_END_WITH_A_TIE) {
            tiesCounter++;
        }
        simulationsCounter++;
    }

    public List<Double> getActionPriorityForChoose() {
        final List<Double> actionPriority = new ArrayList<>(basicActionPriority.size());
        double priority = 0d;
        for (int i = 0; i < basicActionPriority.size(); i++) {
            priority += basicActionPriority.get(i) + basicActionPriority.get(i) / 100 * children.get(i).getScorePercent();
            actionPriority.add(priority);
        }
        return actionPriority;
    }

    public List<Double> getActionPriority() {
        final List<Double> actionPriority = new ArrayList<>(basicActionPriority.size());
        for (int i = 0; i < basicActionPriority.size(); i++) {
            actionPriority.add(basicActionPriority.get(i) + basicActionPriority.get(i) / 100 * children.get(i).getScorePercent());
        }
        return actionPriority;
    }
}

