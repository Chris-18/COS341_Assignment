public class Main
{
    public static void main(String[] args)
    {
        Lexer lexer = new Lexer("Accept_0.txt");
        Token result = lexer.lex();
        if(result.getType().equals("Lexical Error") || result.getType().equals("File Error"))
        {
            System.out.println(lexer.getTokens(result));
        }
        else
        {
            Parser parser = new Parser(result);
            Node tree = parser.parse();
            if(tree.getUid() == -1)
            {
                System.out.println(tree.getTypeOfNode() + ": " + tree.getNodeDetail());
            }
            else
            {
                System.out.print(tree.printTree());
                //System.out.print(tree.printTreeWithNoNewLines());
            }
        }
    }
}
