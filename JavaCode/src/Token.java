public class Token
{
    private String type;
    private String value;
    private Token next;

    public Token(String t, String v)
    {
        this.type = t;
        this.value = v;
        this.next = null;
    }

    public String getType()
    {
        return this.type;
    }

    public void setType(String t)
    {
        this.type = t;
    }

    public String getValue()
    {
        return this.value;
    }

    public void setValue(String v)
    {
        this.value = v;
    }

    public Token getNext()
    {
        return this.next;
    }

    public void setNext(Token n)
    {
        this.next = n;
    }
}
