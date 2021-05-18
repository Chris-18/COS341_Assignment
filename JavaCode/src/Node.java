import java.util.Vector;

public class Node
{
    private int uid;
    private String typeOfNode;
    private String NodeDetail;
    private Row rowInTable = null;
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

    public Row getRowInTable()
    {
        return rowInTable;
    }

    public void setRowInTable(Row rowInTable)
    {
        this.rowInTable = rowInTable;
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

    private String printTreeRecursively(String result, int numIndentations)
    {
        if(this.typeOfNode.equals("Non-Terminal"))
        {
            for(int i = 0; i < numIndentations; i++)
            {
                result += "\t";
            }
            result += this.rowInTable.getScope() + "-" + this.rowInTable.getUid() + "-" + this.rowInTable.getNodeText() + "\n";
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
            result += "Terminal Node: " + this.rowInTable.getScope() + "-" + this.rowInTable.getUid() + "-" + this.rowInTable.getNodeText() + "\n";
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
            result += this.rowInTable.getScope() + "-" + this.rowInTable.getUid() + "-" + this.rowInTable.getNodeText();
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
            result += "Terminal Node: " + this.rowInTable.getScope() + "-" + this.rowInTable.getUid() + "-" + this.rowInTable.getNodeText() + " ";
        }
        return result;
    }

    public Vector<Row> buildSyntaxTable()
    {
        int scope = 0;
        return this.buildSyntaxTableRecursively("", scope);
    }

    private Vector<Row> buildSyntaxTableRecursively(String previousScope, int currentScope)
    {
        Row newRow;
        Vector<Row> Table = new Vector<>();

        if(this.NodeDetail.equals("PROG"))
        {
            if(previousScope.equals(""))
            {
                previousScope = previousScope + "0";
                currentScope++;
            }
            newRow = new Row(this.uid, this.NodeDetail, previousScope);
        }
        else if(this.NodeDetail.equals("COND_LOOP") && this.children.get(0).NodeDetail.equals("for"))
        {
            previousScope = previousScope + "." + currentScope;
            currentScope++;
            newRow = new Row(this.uid, this.NodeDetail, previousScope);
        }
        else if(this.NodeDetail.equals("PROC"))
        {
            previousScope = previousScope + "." + currentScope;
            currentScope++;
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
            Vector<Row> tempTable = child.buildSyntaxTableRecursively(previousScope, currentScope);
            for (Row tempRow : tempTable)
            {
                Table.addElement(tempRow);
            }
        }

        return Table;
    }
}
