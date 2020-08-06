package com.neolab.heroesGame.client.ai.version.mechanics.nodes;

import com.neolab.heroesGame.server.answers.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Абстрактный узел дерева. Содержит три поля: родитель, список дочерних узлов, ответ игрока, который привел в этот узел
 * из родительского
 */
public abstract class ANode {
    private final Answer prevAnswer;
    private final ANode parent;
    private final List<ANode> children;

    public ANode(final Answer prevAnswer, final ANode parent) {
        this.prevAnswer = prevAnswer;
        this.parent = parent;
        children = new ArrayList<>();
    }

    public Answer getPrevAnswer() {
        return prevAnswer;
    }

    public List<ANode> getChildren() {
        return children;
    }

    public ANode getChild(final int index) {
        if (children.size() <= index) {
            return null;
        }
        return Optional.ofNullable(children.get(index)).orElse(null);
    }

    public void createChild(final Answer prevAnswer) {
        final ANode child = createChild(prevAnswer, this);
        children.add(child);
    }

    public ANode getParent() {
        return parent;
    }

    public boolean isNewNode() {
        return children.isEmpty();
    }

    public abstract ANode createChild(final Answer prevAnswer, final ANode aNode);

}
