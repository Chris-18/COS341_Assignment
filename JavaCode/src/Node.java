import java.util.Vector;

public class Node
{
    private int uid;
    private String typeOfNode;
    private String NodeDetail;
    private Vector<Node> children = new Vector<Node>();

    public Node(int uid)
    {
        this.uid = uid;
    }

    public Node(int uid, String typeOfNode, String nodeDetail)
    {
        this.uid = uid;
        this.typeOfNode = typeOfNode;
        NodeDetail = nodeDetail;
    }

    public int getUid()
    {
        return uid;
    }

    public void setUid(int uid)
    {
        this.uid = uid;
    }

    public String getTypeOfNode()
    {
        return typeOfNode;
    }

    public void setTypeOfNode(String typeOfNode)
    {
        this.typeOfNode = typeOfNode;
    }

    public String getNodeDetail()
    {
        return NodeDetail;
    }

    public void setNodeDetail(String nodeDetail)
    {
        NodeDetail = nodeDetail;
    }

    public void addChild(Node child)
    {
        this.children.addElement(child);
    }

    public Vector<Node> getChildren()
    {
        return children;
    }

    public void setChildren(Vector<Node> children)
    {
        this.children = children;
    }

    public String printTree()
    {
        String result = "{\n";
        result = printTreeRecursively(result, 1);
        result += "}";
        return result;
    }

    private String printTreeRecursively(String result, int numIndentations)
    {
        if(this.typeOfNode.equals("Non-Terminal"))
        {
            for(int i = 0; i < numIndentations; i++)
            {
                result += "\t";
            }
            result += this.NodeDetail + "\n " +
                    "";
            for(int i = 0; i < numIndentations; i++)
            {
                result += "\t";
            }
            result += "{\n";

            for (Node child : this.children)
            {
                result = child.printTreeRecursively(result, numIndentations + 1);
            }

            for(int i = 0; i < numIndentations; i++)
            {
                result += "\t";
            }
            result += "}\n";
        }
        else
        {
            for(int i = 0; i < numIndentations; i++)
            {
                result += "\t";
            }
            result += "Terminal Node: " + this.NodeDetail + "\n";
        }
        return result;
    }
}
