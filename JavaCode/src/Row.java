public class Row
{
    private final int uid;
    private final String nodeText;
    private final String scope;
    private String newName = "";

    public Row(int uid, String nodeText, String scope)
    {
        this.uid = uid;
        this.nodeText = nodeText;
        this.scope = scope;
    }

    public int getUid()
    {
        return uid;
    }

    public String getNodeText()
    {
        return nodeText;
    }

    public String getScope()
    {
        return scope;
    }

    public String getNewName()
    {
        return newName;
    }

    public void setNewName(String newName)
    {
        this.newName = newName;
    }

    @Override
    public String toString()
    {
        return "Row{" +
                "uid=" + uid +
                ", nodeText='" + nodeText + '\'' +
                ", scope='" + scope + '\'' +
                ", newName='" + newName + '\'' +
                '}';
    }
}
