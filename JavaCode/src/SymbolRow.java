public class SymbolRow
{
    private String variableName;
    private String type;
    private String flow = "-";

    public SymbolRow(String variableName, String type)
    {
        this.variableName = variableName;
        this.type = type;
    }

    public SymbolRow(SymbolRow symbolRow)
    {
        this.variableName = symbolRow.getVariableName();
        this.type = symbolRow.getType();
        this.flow = symbolRow.getFlow();
    }

    public String getVariableName()
    {
        return variableName;
    }

    public void setVariableName(String variableName)
    {
        this.variableName = variableName;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getFlow()
    {
        return this.flow;
    }

    public void setFlow(String flow)
    {
        this.flow = flow;
    }
}
