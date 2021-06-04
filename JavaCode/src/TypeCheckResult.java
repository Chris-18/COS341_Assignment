import java.util.Vector;

public class TypeCheckResult
{
    private boolean isError;
    private Node tree;
    private Vector<String> ErrorMsgs = new Vector<>();
    private Vector<String> WarningMsgs = new Vector<>();

    public TypeCheckResult(boolean isError, Node tree, Vector<String> errorMsgs, Vector<String> warningMsgs)
    {
        this.isError = isError;
        this.tree = tree;
        ErrorMsgs = errorMsgs;
        this.WarningMsgs = warningMsgs;
    }

    public boolean isError()
    {
        return isError;
    }

    public Node getTree()
    {
        return tree;
    }

    public Vector<String> getErrorMsgs()
    {
        return ErrorMsgs;
    }

    public Vector<String> getWarningMsgs()
    {
        return WarningMsgs;
    }
}
