public class SymbolRow
{
    private String variableName;
    private String type;

    public SymbolRow(String variableName, String type)
    {
        this.variableName = variableName;
        this.type = type;
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
}
