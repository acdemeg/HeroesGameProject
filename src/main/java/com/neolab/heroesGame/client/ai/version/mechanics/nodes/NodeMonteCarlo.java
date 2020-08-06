package com.neolab.heroesGame.client.ai.version.mechanics.nodes;

import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.server.answers.Answer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NodeMonteCarlo extends ANode {
    private int winCounter;
    private int simulationsCounter;
    private int tiesCounter;
    private List<Double> basicActionPriority = Collections.emptyList();

    public NodeMonteCarlo(final Answer prevAnswer, final ANode parent) {
        super(prevAnswer, parent);
        winCounter = 0;
        simulationsCounter = 0;
        tiesCounter = 0;
    }

    public void setActionPriority(final List<Double> actionPriority) {
        basicActionPriority = actionPriority;
    }

    @Override
    public ANode createChild(final Answer prevAnswer, final ANode aNode) {
        return new NodeMonteCarlo(prevAnswer, aNode);
    }

    public void increase(final GameEvent event) {
        if (event == GameEvent.YOU_WIN_GAME) {
            winCounter++;
        } else if (event == GameEvent.GAME_END_WITH_A_TIE) {
            tiesCounter++;
        }
        simulationsCounter++;
    }

    /**
     * @return Возвращает функцию распределения для доступных в этом узле действий: a[i] = a[i-1]
     */
    public List<Double> getActionPriorityForChoose() {
        final List<Double> actionPriority = getActionPriority();
        double priority = 0d;
        for (int i = 0; i < basicActionPriority.size(); i++) {
            priority += actionPriority.get(i);
            actionPriority.set(i, priority);
        }
        return actionPriority;
    }

    /**
     * Функция рассчитывает текущие вероятности для доступных действий опираясь на базовые значения вероятностей, которые
     * задаются ботом через функцию setActionPriority.
     * Чтобы избежать лишних рассчетов базовые вероятности рассчитываются один раз при первом заходе в узел
     *
     * @return возвращает распределение вероятностей для доступных действий
     */
    public List<Double> getActionPriority() {
        final List<Double> actionPriority = new ArrayList<>(basicActionPriority.size());
        for (int i = 0; i < basicActionPriority.size(); i++) {
            actionPriority.add(basicActionPriority.get(i) + basicActionPriority.get(i) / 100
                    * ((NodeMonteCarlo) getChildren().get(i)).getScorePercent());
        }
        return actionPriority;
    }

    public int getScorePercent() {
        return simulationsCounter != 0 ? ((100 * (winCounter + tiesCounter / 2)) / simulationsCounter) : 0;
    }

}
