package isep.algoproject.models;

import isep.algoproject.models.enums.NodeType;

public class Node {
    private String id;
    private String name;
    private NodeType type;

    public Node(String id, String name, NodeType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }
}
