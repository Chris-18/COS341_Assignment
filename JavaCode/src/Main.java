import java.util.Vector;

public class Main
{
    public static void main(String[] args)
    {
        Lexer lexer = new Lexer(args[0]);
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
                Vector<Row> Table = tree.buildSyntaxTable();
                Semantics semantics = new Semantics(tree, Table);
                semantics.changeTreeVariableNames();
                if(semantics.checkRuleP1())
                {
                    System.out.println("Error:Procedure Call with User defined name.");
                }
                else if(semantics.checkRuleP2())
                {
                    System.out.println("Error: Parent and child Procedures share the same name.");
                }
                else
                {
                    System.out.print(tree.printTree());
                    //System.out.print(tree.printTreeWithNoNewLines());
                }
            }
        }
    }
}
