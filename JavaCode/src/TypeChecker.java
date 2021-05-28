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

            }
            else if(child.getNodeDetail().equals("COND_LOOP"))
            {

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

        }
    }
}
