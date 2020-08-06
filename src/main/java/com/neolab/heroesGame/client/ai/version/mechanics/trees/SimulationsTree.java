package com.neolab.heroesGame.client.ai.version.mechanics.trees;

import com.neolab.heroesGame.client.ai.version.mechanics.nodes.NodeMonteCarlo;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.server.answers.Answer;

import java.util.List;
import java.util.Optional;

public class SimulationsTree extends ATree {

    public SimulationsTree() {
        super(new NodeMonteCarlo(null, null), null);
        setCurrentNode(getRoot());
    }

    public List<Double> getActionPriority() {
        return Optional.ofNullable(((NodeMonteCarlo) getCurrentNode()).getActionPriorityForChoose()).orElseThrow();
    }

    /**
     * Ищем максимальное значение текущего приоретета среди доступных действий
     *
     * @return Answer для узла с наибольшим значением приоретета, даже если винрейт узла не максимальный
     */
    public Answer getBestAction() {
        final List<Double> actionPriority = ((NodeMonteCarlo) getCurrentNode()).getActionPriority();
        int max = 0;
        for (int i = 0; i < actionPriority.size(); i++) {
            if (actionPriority.get(max) < actionPriority.get(i)) {
                max = i;
            }
        }
        return getCurrentNode().getChild(max).getPrevAnswer();
    }

    public void increase(final GameEvent event) {
        ((NodeMonteCarlo) getCurrentNode()).increase(event);
    }

    /**
     * При заходе в узел впервые сразу создаем все дочерние узлы и записываем в текущий узел базовые приорететы действий
     *
     * @param actions                 Список доступных действий в текущем узле
     * @param calculateActionPriority базовые приорететы для действий
     */
    public void fieldNewNode(final List<Answer> actions, final List<Double> calculateActionPriority) {
        actions.forEach(answer -> getCurrentNode().createChild(answer, getCurrentNode()));
        ((NodeMonteCarlo) getCurrentNode()).setActionPriority(calculateActionPriority);
    }
}
