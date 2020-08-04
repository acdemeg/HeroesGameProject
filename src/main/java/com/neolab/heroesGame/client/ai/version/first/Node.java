package com.neolab.heroesGame.client.ai.version.first;

import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.client.ai.PlayerBot;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.server.answers.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Node {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerBot.class);
    private final Answer prevAnswer;
    private final Node parent;
    private final List<Node> children;
    private int winCounter;
    private int simulationsCounter;
    private int tiesCounter;

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

    public Node getChild(final int index) {
        if (children.size() <= index) {
            return null;
        }
        return Optional.ofNullable(children.get(index)).orElse(null);
    }

    public Node createChild(final int index, final Answer prevAnswer) {
        Node child = new Node(prevAnswer, this);
        for (int i = children.size(); i <= index; i++) {
            children.add(null);
        }
        children.set(index, child);
        return child;
    }

    public Node getParent() {
        return parent;
    }

    public int getWinCounter() {
        return winCounter;
    }

    public int getSimulationsCounter() {
        return simulationsCounter;
    }

    public int getTiesCounter() {
        return tiesCounter;
    }

    public void increase(final GameEvent event) {
        if (event == GameEvent.YOU_WIN_GAME) {
            winCounter++;
        } else if (event == GameEvent.GAME_END_WITH_A_TIE) {
            tiesCounter++;
        }
        simulationsCounter++;
    }
}
