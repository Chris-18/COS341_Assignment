import java.util.Vector;

public class Node
{
    private int uid;
    private String typeOfNode;
    private String NodeDetail;
    private Row rowInTable = null;
    private Vector<Node> children = new Vector<>();
    private static int scopeValue = 0;
    private String type = "";
    private int level;
    private String flow = "-";

    public Node(int uid, String typeOfNode, String nodeDetail)
    {
        this.uid = uid;
        this.typeOfNode = typeOfNode;
        NodeDetail = nodeDetail;
    }

    public Node(Node oldNode)
    {
        this.uid = oldNode.uid;
        this.typeOfNode = oldNode.getTypeOfNode();
        this.NodeDetail = oldNode.getNodeDetail();
        this.rowInTable = oldNode.getRowInTable();
        this.type = oldNode.getType();
        this.level = oldNode.getLevel();
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

    public Row getRowInTable()
    {
        return rowInTable;
    }

    public void setRowInTable(Row rowInTable)
    {
        this.rowInTable = rowInTable;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public String getFlow()
    {
        return flow;
    }

    public void setFlow(String flow)
    {
        this.flow = flow;
    }

    public String printTree()
    {
        if(this.rowInTable == null)
        {
            this.buildSyntaxTable();
        }

        String result = "{\n";
        result = printTreeRecursively(result, 1);
        result += "}";
        return result;
    }

    public void addTreeLevelDescription()
    {
        recursivelyAddTreeLevelDescription(0);
    }

    private String printTreeRecursively(String result, int numIndentations)
    {
        if(this.typeOfNode.equals("Non-Terminal"))
        {
            for(int i = 0; i < numIndentations; i++)
            {
                result += "\t";
            }
            result += this.rowInTable.getScope() + "-" + this.rowInTable.getUid() + "-" + this.rowInTable.getNodeText() + "-" + this.rowInTable.getNewName() +  "-" + this.getType() + "-Flow: " + this.flow + "\n";
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
            result += "Terminal Node: " + this.rowInTable.getScope() + "-" + this.rowInTable.getUid() + "-" + this.rowInTable.getNodeText() + "-" + this.rowInTable.getNewName() + "-Flow: " + this.flow +  "\n";
        }
        return result;
    }

    public String printTreeWithNoNewLines()
    {
        if(this.rowInTable == null)
        {
            this.buildSyntaxTable();
        }

        String result = "{";
        result = printTreeRecursivelyWithNoNewLines(result, 1);
        result += "}";
        return result;
    }

    private String printTreeRecursivelyWithNoNewLines(String result, int numIndentations)
    {
        if(this.typeOfNode.equals("Non-Terminal"))
        {
            for(int i = 0; i < numIndentations; i++)
            {
                result += "";
            }
            result += this.rowInTable.getScope() + "-" + this.rowInTable.getUid() + "-" + this.rowInTable.getNodeText() + "-" + this.rowInTable.getNewName() +  "-" + this.getType()  + "-Flow: " + this.flow + " ";
            for(int i = 0; i < numIndentations; i++)
            {
                result += "";
            }
            result += "{ ";

            for (Node child : this.children)
            {
                result = child.printTreeRecursivelyWithNoNewLines(result, numIndentations + 1);
            }

            for(int i = 0; i < numIndentations; i++)
            {
                result += "";
            }
            result += "} ";
        }
        else
        {
            for(int i = 0; i < numIndentations; i++)
            {
                result += "";
            }
            result += "Terminal Node: " + this.rowInTable.getScope() + "-" + this.rowInTable.getUid() + "-" + this.rowInTable.getNodeText() + "-" + this.rowInTable.getNewName()  + "-Flow: " + this.flow +  "  ";
        }
        return result;
    }

    public Vector<Row> buildSyntaxTable()
    {
        return this.buildSyntaxTableRecursively("");
    }

    private Vector<Row> buildSyntaxTableRecursively(String previousScope)
    {
        Row newRow;
        Vector<Row> Table = new Vector<>();

        if(this.NodeDetail.equals("PROG"))
        {
            if(previousScope.equals(""))
            {
                previousScope = previousScope + "0";
                scopeValue++;
            }
            newRow = new Row(this.uid, this.NodeDetail, previousScope);
        }
        else if(this.NodeDetail.equals("COND_LOOP") && this.children.get(0).NodeDetail.equals("for"))
        {
            previousScope = previousScope + "." + scopeValue;
            scopeValue++;
            newRow = new Row(this.uid, this.NodeDetail, previousScope);
        }
        else if(this.NodeDetail.equals("PROC"))
        {
            previousScope = previousScope + "." + scopeValue;
            scopeValue++;
            newRow = new Row(this.uid, this.NodeDetail, previousScope);
        }
        else
        {
            newRow = new Row(this.uid, this.NodeDetail, previousScope);
        }

        Table.addElement(newRow);
        this.rowInTable = newRow;

        for (Node child : this.children)
        {
            Vector<Row> tempTable = child.buildSyntaxTableRecursively(previousScope);
            for (Row tempRow : tempTable)
            {
                Table.addElement(tempRow);
            }
        }

        return Table;
    }

    private void recursivelyAddTreeLevelDescription(int previousLevel)
    {
        if(this.NodeDetail.equals("PROC"))
        {
            this.level = previousLevel + 1;
        }
        else
        {
            this.level = previousLevel;
        }

        for(Node child: this.children)
        {
            child.recursivelyAddTreeLevelDescription(this.level);
        }
    }
}
