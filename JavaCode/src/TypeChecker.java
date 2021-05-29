import java.util.Vector;

public class TypeChecker
{
    private Node tree;
    private Vector<Row> syntaxTable;
    private Vector<SymbolRow> symbolTable = new Vector<>();

    public TypeChecker(Node tree, Vector<Row> table)
    {
        this.tree = tree;
        this.syntaxTable = table;
        buildSymbolTable();
        populateDefaultTypeValues();
    }

    private void buildSymbolTable()
    {
        for(Row row: syntaxTable)
        {
            if(!row.getNewName().equals("") && row.getNewName().charAt(0) == 'V')
            {
                if(!isInSymbolTable(row.getNewName()))
                {
                    SymbolRow symbolRow = new SymbolRow(row.getNewName(), "u");
                    symbolTable.add(symbolRow);
                }
            }
        }
    }

    private boolean isInSymbolTable(String varName)
    {
        for(SymbolRow symbolRow: this.symbolTable)
        {
            if(symbolRow.getVariableName().equals(varName))
            {
                return true;
            }
        }
        return false;
    }

    private void populateDefaultTypeValues()
    {
        Node currNode = this.tree;
        recursivelySetDefaultNonTerminalTypes(currNode);
    }

    private void recursivelySetDefaultNonTerminalTypes(Node currNode)
    {
        if(currNode != null)
        {
            if(currNode.getTypeOfNode().equals("Non-Terminal"))
            {
                currNode.setType("u");
            }

            for(int i = 0; i < currNode.getChildren().size(); i++)
            {
                recursivelySetDefaultNonTerminalTypes(currNode.getChildren().get(i));
            }
        }
    }

    private String getVariableType(String VarName)
    {
        for(SymbolRow symbolRow: this.symbolTable)
        {
            if(symbolRow.getVariableName().equals(VarName))
            {
                return symbolRow.getType();
            }
        }
        return "";
    }

    private void setSymbolTableVariableType(String VarName, String type)
    {
        for(SymbolRow symbolRow: this.symbolTable)
        {
            if(symbolRow.getVariableName().equals(VarName))
            {
                symbolRow.setType(type);
            }
        }
    }

   private void analyseProg(Node Prog)
    {
        boolean typeCheck = true;
        for(Node child: Prog.getChildren())
        {
            if(!child.getType().equals("c"))
            {
                if(child.getNodeDetail().equals("INSTR"))
                {
                    analyseInstr(child);
                }
                else if(child.getNodeDetail().equals("PROC_DEFS"))
                {
                    analyseProcDef(child);
                }

                if(!child.getType().equals("c"))
                {
                    typeCheck = false;
                }
            }
        }

        if(!typeCheck)
        {
            Prog.setType("e");
        }
        else
        {
            Prog.setType("c");
        }
    }

    private void analyseInstr(Node Instr)
    {
        if(Instr != null)
        {
            Node child = Instr.getChildren().get(0);
            if(child.getNodeDetail().equals("halt"))
            {
                Instr.setType("c");
            }
            else if(child.getNodeDetail().equals("IO"))
            {
                if(child.getType().equals("c"))
                {
                    Instr.setType("c");
                }
                else
                {
                    analyseIo(child);
                    if(child.getType().equals("c"))
                    {
                        Instr.setType("c");
                    }
                }
            }
            else if(child.getNodeDetail().equals("CALL"))
            {
                if(child.getType().equals("c"))
                {
                    Instr.setType("c");
                }
                else
                {
                    analyseCall(child);
                    if(child.getType().equals("c"))
                    {
                        Instr.setType("c");
                    }
                }
            }
            else if(child.getNodeDetail().equals("ASSIGN"))
            {
                if(child.getType().equals("c"))
                {
                    Instr.setType("c");
                }
                else
                {
                    analyseAssign(child);
                    if(child.getType().equals("c"))
                    {
                        Instr.setType("c");
                    }
                }
            }
            else if(child.getNodeDetail().equals("COND_BRANCH"))
            {
                if(child.getType().equals("c"))
                {
                    Instr.setType("c");
                }
                else
                {
                    analyseCond_Branch(child);
                    if(child.getType().equals("c"))
                    {
                        Instr.setType("c");
                    }
                }
            }
            else if(child.getNodeDetail().equals("COND_LOOP"))
            {
                if(child.getType().equals("c"))
                {
                    Instr.setType("c");
                }
                else
                {
                    analyseCond_Loop(child);
                    if(child.getType().equals("c"))
                    {
                        Instr.setType("c");
                    }
                }
            }
        }
    }

    private void analyseIo(Node Io)
    {
        if(Io != null)
        {
            Node ioType = Io.getChildren().get(0);
            if(ioType.getNodeDetail().equals("input"))
            {
                String varName = ioType.getChildren().get(1).getRowInTable().getNewName();
                if(getVariableType(varName).equals("s"))
                {
                    Io.setType("e");
                }
                else
                {
                    ioType.getChildren().get(1).setType("n");
                    setSymbolTableVariableType(varName, "n");
                    Io.setType("c");
                }
            }
            else if(ioType.getNodeDetail().equals("output"))
            {
                String varName = ioType.getChildren().get(1).getRowInTable().getNewName();
                if(getVariableType(varName).equals("n") || getVariableType(varName).equals("s"))
                {
                    Io.setType("c");
                }
                else
                {
                    ioType.getChildren().get(1).setType("o");
                    setSymbolTableVariableType(varName, "o");
                    Io.setType("c");
                }
            }
        }
    }

    private void analyseCall(Node Call)
    {
        if(Call != null)
        {
            Call.setType("c");
        }
    }

    private void analyseAssign(Node Assign)
    {
        if(Assign != null)
        {
            Node child1 = Assign.getChildren().get(0);
            Node child2 = Assign.getChildren().get(1);
            if(child2.getTypeOfNode().equals("Non-Terminal"))
            {
                String varName1 = child1.getRowInTable().getNewName();
                if(getVariableType(varName1).equals("s"))
                {
                    Assign.setType("e");
                }
                else if(!getVariableType(varName1).equals("s") && child2.getType().equals("n"))
                {
                    setSymbolTableVariableType(varName1, "n");
                    Assign.setType("c");
                }
                else
                {
                    setSymbolTableVariableType(varName1, "n");
                    analyseCalc(child2);
                    if(child2.getType().equals("n"))
                    {
                        Assign.setType("c");
                    }
                    else
                    {
                        Assign.setType("e");
                    }
                }
            }
            else if(!child2.getRowInTable().getNewName().equals(""))
            {
                String varName1 = child1.getRowInTable().getNewName();
                String varName2 = child2.getRowInTable().getNewName();
                if(getVariableType(varName1).equals("n") && getVariableType(varName2).equals("s"))
                {
                    Assign.setType("e");
                }
                else if(getVariableType(varName1).equals("s") && getVariableType(varName2).equals("n"))
                {
                    Assign.setType("e");
                }
                else if(getVariableType(varName1).equals("n") && !getVariableType(varName2).equals("s"))
                {
                    setSymbolTableVariableType(varName2, "n");
                    Assign.setType("c");
                }
                else if(getVariableType(varName1).equals("s") && !getVariableType(varName2).equals("n"))
                {
                    setSymbolTableVariableType(varName2, "s");
                    Assign.setType("c");
                }
                else
                {
                    setSymbolTableVariableType(varName1, "o");
                    setSymbolTableVariableType(varName2, "o");
                    Assign.setType("c");
                }
            }
            else
            {
                String varName1 = child1.getRowInTable().getNewName();
                if(child2.getNodeDetail().charAt(0) == '\"')
                {
                    if(getVariableType(varName1).equals("n"))
                    {
                        Assign.setType("e");
                    }
                    else
                    {
                        child1.setType("s");
                        setSymbolTableVariableType(child1.getRowInTable().getNewName(), "s");
                        Assign.setType("s");
                    }
                }
                else
                {
                    if(getVariableType(varName1).equals("s"))
                    {
                        Assign.setType("e");
                    }
                    else
                    {
                        child1.setType("n");
                        setSymbolTableVariableType(varName1, "n");
                        Assign.setType("c");
                        Assign.setType("c");
                    }
                }
            }
        }
    }

    private void analyseCalc(Node Calc)
    {
        if(Calc != null)
        {
            Node child1 = Calc.getChildren().get(0);
            Node child2 = Calc.getChildren().get(1);
            Node child3 = Calc.getChildren().get(2);
            if(child2.getType().equals("n") && child3.getType().equals("n"))
            {
                Calc.setType("n");
            }
            else
            {
                analyseNumExpr(child2);
                analyseNumExpr(child3);
                if(child2.getType().equals("n") && child3.getType().equals("n"))
                {
                    Calc.setType("n");
                }
                else
                {
                    Calc.setType("e");
                }
            }
        }
    }

    private void analyseNumExpr(Node NumExpr)
    {
        if(NumExpr != null)
        {

            if(NumExpr.getTypeOfNode().equals("Non-Terminal"))
            {
                if(!NumExpr.getType().equals("n"))
                {
                    analyseCalc(NumExpr);
                }
            }
            else if(!NumExpr.getRowInTable().getNewName().equals(""))
            {
                if(getVariableType(NumExpr.getRowInTable().getNewName()).equals("s"))
                {
                    NumExpr.setType("e");
                }
                else
                {
                    NumExpr.setType("n");
                    setSymbolTableVariableType(NumExpr.getRowInTable().getNewName(), "n");
                }
            }
            else
            {
                NumExpr.setType("n");
            }
        }
    }

    private void analyseCond_Branch(Node Cond_Branch)
    {
        if(Cond_Branch != null)
        {
            int size = Cond_Branch.getChildren().size();
            if(size == 4)
            {
                Node child2 = Cond_Branch.getChildren().get(1);
                Node child4 = Cond_Branch.getChildren().get(3);
                if((child2.getType().equals("b") || child2.getType().equals("f")) && child4.getType().equals("c"))
                {
                    Cond_Branch.setType("c");
                }
                else
                {
                    analyseBool(child2);
                    analyseInstr(child4);
                    if((child2.getType().equals("b") || child2.getType().equals("f")) && child4.getType().equals("c"))
                    {
                        Cond_Branch.setType("c");
                    }
                }
            }
            else if(size == 6)
            {
                Node child2 = Cond_Branch.getChildren().get(1);
                Node child4 = Cond_Branch.getChildren().get(3);
                Node child6 = Cond_Branch.getChildren().get(5);
                if((child2.getType().equals("b") || child2.getType().equals("f")) && child4.getType().equals("c") && child6.getType().equals("c"))
                {
                    Cond_Branch.setType("c");
                }
                else
                {
                    analyseBool(child2);
                    analyseInstr(child4);
                    analyseInstr(child6);
                    if((child2.getType().equals("b") || child2.getType().equals("f")) && child4.getType().equals("c") && child6.getType().equals("c"))
                    {
                        Cond_Branch.setType("c");
                    }
                }
            }
        }
    }

    private void analyseBool(Node Bool)
    {
        if(Bool != null)
        {
            Node child1 = Bool.getChildren().get(0);
            if(child1.getNodeDetail().equals("eq"))
            {

            }
            else if(child1.getNodeDetail().equals("not"))
            {

            }
            else if(child1.getNodeDetail().equals("and") || child1.getNodeDetail().equals("or"))
            {

            }
            else
            {
                
            }
        }
    }
}
