public class VariableRow
{
    private String variableName;
    private String type;

    public VariableRow(String variableName, String type)
    {
        this.variableName = variableName;
        this.type = type;
    }

    public String getVariableName()
    {
        return variableName;
    }

    public String getType()
    {
        return type;
    }
}
