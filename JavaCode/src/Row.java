public class Row
{
    private int uid;
    private String nodeText;
    private String scope;

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

    @Override
    public String toString()
    {
        return "Row{" +
                "uid=" + uid +
                ", nodeText='" + nodeText + '\'' +
                ", scope='" + scope + '\'' +
                '}';
    }
}
