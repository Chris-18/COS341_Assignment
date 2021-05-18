public class Main
{
    public static void main(String[] args)
    {
        Lexer lexer = new Lexer("test.txt");
        Token result = lexer.lex();
        if(result.getType().equals("Lexical Error") || result.getType().equals("File Error"))
        {
            System.out.println(lexer.getTokens(result));
        }
        else
        {
            System.out.println("Go to parser.");
            System.out.println(lexer.getTokens(result));
            System.out.println();
            Parser parser = new Parser(result);
            Node tree = parser.parse();
            if(tree.getUid() == -1)
            {
                System.out.println(tree.getTypeOfNode() + ": " + tree.getNodeDetail());
            }
            else
            {
                System.out.println("Print tree");
                System.out.println(tree.printTree());
            }
        }
    }
}
