package com.neolab.heroesGame.client.ai.version.mechanics.trees;

import com.neolab.heroesGame.client.ai.version.mechanics.nodes.ANode;
import com.neolab.heroesGame.server.answers.Answer;

public abstract class ATree {
    private final ANode root;
    private ANode currentNode;

    protected ATree(final ANode root, final ANode currentNode) {
        this.root = root;
        this.currentNode = currentNode;
    }

    public ANode getRoot() {
        return root;
    }

    public ANode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(final ANode currentNode) {
        this.currentNode = currentNode;
    }

    public boolean upToParent() {
        if (currentNode != root) {
            currentNode = currentNode.getParent();
            return true;
        }
        return false;
    }

    public boolean downToChild(final int index) {
        final ANode temp = currentNode.getChild(index);
        if (temp != null) {
            currentNode = temp;
            return true;
        }
        return false;
    }

    public boolean isNodeNew() {
        return currentNode.isNewNode();
    }

    public void toRoot() {
        currentNode = root;
    }

    public Answer getAnswer(final int actionNumber) {
        return currentNode.getChild(actionNumber).getPrevAnswer();
    }
}
