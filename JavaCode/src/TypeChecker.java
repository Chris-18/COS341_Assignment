import java.util.Vector;

public class TypeChecker
{
    private Node tree;
    private Vector<Row> syntaxTable;
    private Vector<SymbolRow> symbolTable = new Vector<>();
    private Vector<String> warningMsg = new Vector<>();
    private Vector<String> ErrorMsgs = new Vector<>();

    public TypeChecker(Node tree, Vector<Row> table)
    {
        this.tree = tree;
        this.syntaxTable = table;
        buildSymbolTable();
        populateDefaultTypeValues();
    }

    public TypeCheckResult runTypeChecking()
    {
        Node currNode = this.tree;
        analyseProg(currNode);
        if(currNode.getType().equals("c"))
        {
            checkForAndRemoveDeadCode();
            return new TypeCheckResult(false, this.tree, this.ErrorMsgs, this.warningMsg);
        }
        else
        {
            return new TypeCheckResult(true, this.tree, this.ErrorMsgs, this.warningMsg);
        }
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
            this.ErrorMsgs.addElement("Error: PROG node encountered an error.");
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
                    else
                    {
                        Instr.setType("e");
                        this.ErrorMsgs.addElement("Error: IO Node " + child.getRowInTable().getUid() + " encountered an error.");
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
                    else
                    {
                        Instr.setType("e");
                        this.ErrorMsgs.addElement("Error: CALL node " + child.getRowInTable().getUid() + " encountered an error.");
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
                    else
                    {
                        Instr.setType("e");
                        this.ErrorMsgs.addElement("Error: ASSIGN node " + child.getRowInTable().getUid() + " encountered an error.");
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
                    else
                    {
                        Instr.setType("e");
                        this.ErrorMsgs.addElement("Error: COND_BRANCH node " + child.getRowInTable().getUid() + " encountered an error.");
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
                    else
                    {
                        Instr.setType("e");
                        this.ErrorMsgs.addElement("Error: COND_LOOP node " + child.getRowInTable().getUid() + " encountered an error.");
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
                String varName = Io.getChildren().get(1).getRowInTable().getNewName();
                if(getVariableType(varName).equals("s"))
                {
                    Io.setType("e");
                    this.ErrorMsgs.addElement("Error: Input var cannot be of type string.");
                }
                else
                {
                    Io.getChildren().get(1).setType("n");
                    setSymbolTableVariableType(varName, "n");
                    Io.setType("c");
                }
            }
            else if(ioType.getNodeDetail().equals("output"))
            {
                String varName = Io.getChildren().get(1).getRowInTable().getNewName();
                if(getVariableType(varName).equals("n") || getVariableType(varName).equals("s"))
                {
                    Io.setType("c");
                }
                else
                {
                    Io.getChildren().get(1).setType("o");
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
                    this.ErrorMsgs.addElement("Error: Type mis-match in Assign " + Assign.getRowInTable().getUid());
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
                        this.ErrorMsgs.addElement("Error: Type mis-match in Assign " + Assign.getRowInTable().getUid());
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
                    this.ErrorMsgs.addElement("Error: Type mis-match in Assign " + Assign.getRowInTable().getUid());
                }
                else if(getVariableType(varName1).equals("s") && getVariableType(varName2).equals("n"))
                {
                    Assign.setType("e");
                    this.ErrorMsgs.addElement("Error: Type mis-match in Assign " + Assign.getRowInTable().getUid());
                }
                else if(getVariableType(varName1).equals("n") && !getVariableType(varName2).equals("s"))
                {
                    setSymbolTableVariableType(varName2, "n");
                    Assign.setType("c");
                    this.ErrorMsgs.addElement("Error: Type mis-match in Assign " + Assign.getRowInTable().getUid());
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
                        this.ErrorMsgs.addElement("Error: Type mis-match in Assign " + Assign.getRowInTable().getUid());
                    }
                    else
                    {
                        child1.setType("s");
                        setSymbolTableVariableType(child1.getRowInTable().getNewName(), "s");
                        Assign.setType("c");
                    }
                }
                else
                {
                    if(getVariableType(varName1).equals("s"))
                    {
                        Assign.setType("e");
                        this.ErrorMsgs.addElement("Error: Type mis-match in Assign " + Assign.getRowInTable().getUid());
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
                    this.ErrorMsgs.addElement("Error: Type mis-match in CALC " + Calc.getRowInTable().getUid());
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
                    this.ErrorMsgs.addElement("Error: Type mis-match in NUMEXPR " + NumExpr.getRowInTable().getUid());
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
                    else
                    {
                        this.ErrorMsgs.addElement("Error: Type mis-match in COND_BRANCH " + Cond_Branch.getRowInTable().getUid());
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
                    else
                    {
                        this.ErrorMsgs.addElement("Error: Type mis-match in COND_BRANCH " + Cond_Branch.getRowInTable().getUid());
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
                Node child2 = Bool.getChildren().get(1);
                Node child3 = Bool.getChildren().get(2);
                if(child2.getNodeDetail().equals("BOOL"))
                {
                    if((child2.getType().equals("b") || child2.getType().equals("f")) &&
                            (child3.getType().equals("b") || child3.getType().equals("f")))
                    {
                        Bool.setType("b");
                    }
                    else
                    {
                        analyseBool(child2);
                        analyseBool(child3);
                        if((child2.getType().equals("b") || child2.getType().equals("f")) &&
                                (child3.getType().equals("b") || child3.getType().equals("f")))
                        {
                            Bool.setType("b");
                        }
                        else
                        {
                            this.ErrorMsgs.addElement("Error: Type mis-match in BOOL " + Bool.getRowInTable().getUid());
                        }
                    }
                }
                else if(!child2.getRowInTable().getNewName().equals("") &&
                        !child3.getRowInTable().getNewName().equals(""))
                {
                    String varName2 = child2.getRowInTable().getNewName();
                    String varName3 = child3.getRowInTable().getNewName();
                    if(getVariableType(varName2).equals("n") && getVariableType(varName3).equals("s"))
                    {
                        Bool.setType("f");
                    }
                    else if(getVariableType(varName2).equals("s") && getVariableType(varName3).equals("n"))
                    {
                        Bool.setType("f");
                    }
                    else if(getVariableType(varName2).equals("n") && !getVariableType(varName3).equals("s"))
                    {
                        setSymbolTableVariableType(varName3, "n");
                        Bool.setType("b");
                    }
                    else if(getVariableType(varName2).equals("s") && !getVariableType(varName3).equals("n"))
                    {
                        setSymbolTableVariableType(varName3, "s");
                        Bool.setType("b");
                    }
                    else
                    {
                        setSymbolTableVariableType(varName2, "o");
                        setSymbolTableVariableType(varName3, "o");
                        Bool.setType("b");
                    }
                }
                else
                {
                    if(child2.getType().equals("n") && child3.getType().equals("n"))
                    {
                        Bool.setType("b");
                    }
                    else
                    {
                        analyseNumExpr(child2);
                        analyseNumExpr(child3);
                        if(child2.getType().equals("n") && child3.getType().equals("n"))
                        {
                            Bool.setType("b");
                        }
                        else
                        {
                            this.ErrorMsgs.addElement("Error: Type mis-match in BOOL " + Bool.getRowInTable().getUid());
                        }
                    }
                }
            }
            else if(child1.getNodeDetail().equals("not"))
            {
                Node child2 = Bool.getChildren().get(1);
                if(child2.getType().equals("b") || child2.getType().equals("f"))
                {
                    Bool.setType("b");
                }
                else
                {
                    analyseBool(child2);
                    if(child2.getType().equals("b") || child2.getType().equals("f"))
                    {
                        Bool.setType("b");
                    }
                    else
                    {
                        this.ErrorMsgs.addElement("Error: Type mis-match in BOOL " + Bool.getRowInTable().getUid());
                    }
                }
            }
            else if(child1.getNodeDetail().equals("and") || child1.getNodeDetail().equals("or"))
            {
                Node child2 = Bool.getChildren().get(1);
                Node child3 = Bool.getChildren().get(2);
                if(child1.getNodeDetail().equals("and"))
                {
                    if(child2.getType().equals("f") && child3.getType().equals("b"))
                    {
                        Bool.setType("f");
                    }
                    else if(child2.getType().equals("b") && child3.getType().equals("f"))
                    {
                        Bool.setType("f");
                    }
                    else if(child2.getType().equals("f") && child3.getType().equals("f"))
                    {
                        Bool.setType("f");
                    }
                    else if(child2.getType().equals("b") && child3.getType().equals("b"))
                    {
                        Bool.setType("b");
                    }
                    else
                    {
                        analyseBool(child2);
                        analyseBool(child3);
                        if(child2.getType().equals("f") && child3.getType().equals("b"))
                        {
                            Bool.setType("f");
                        }
                        else if(child2.getType().equals("b") && child3.getType().equals("f"))
                        {
                            Bool.setType("f");
                        }
                        else if(child2.getType().equals("f") && child3.getType().equals("f"))
                        {
                            Bool.setType("f");
                        }
                        else if(child2.getType().equals("b") && child3.getType().equals("b"))
                        {
                            Bool.setType("b");
                        }
                        else
                        {
                            this.ErrorMsgs.addElement("Error: Type mis-match in BOOL " + Bool.getRowInTable().getUid());
                        }
                    }
                }
                else
                {
                    if((child2.getType().equals("b") || child2.getType().equals("f")) && (child3.getType().equals("b") || child3.getType().equals("f")))
                    {
                        Bool.setType("b");
                    }
                    else
                    {
                        analyseBool(child2);
                        analyseBool(child3);
                        if((child2.getType().equals("b") || child2.getType().equals("f")) && (child3.getType().equals("b") || child3.getType().equals("f")))
                        {
                            Bool.setType("b");
                        }
                        else
                        {
                            this.ErrorMsgs.addElement("Error: Type mis-match in BOOL " + Bool.getRowInTable().getUid());
                        }
                    }
                }
            }
            else
            {
                Node child3 = Bool.getChildren().get(2);

                String varName1 = child1.getRowInTable().getNewName();
                String varName3 = child3.getRowInTable().getNewName();
                if(getVariableType(varName1).equals("s") || getVariableType(varName3).equals("s"))
                {
                    Bool.setType("e");
                }
                else
                {
                    setSymbolTableVariableType(varName1, "n");
                    setSymbolTableVariableType(varName3, "n");
                    Bool.setType("b");
                }
            }
        }
    }

    private void analyseCond_Loop(Node Cond_Loop)
    {
        if(Cond_Loop != null)
        {
            Node child1 = Cond_Loop.getChildren().get(0);
            if(child1.getNodeDetail().equals("while"))
            {
                Node child2 = Cond_Loop.getChildren().get(1);
                Node child3 = Cond_Loop.getChildren().get(2);
                if((child2.getType().equals("b") || child2.getType().equals("f")) && child3.getType().equals("c"))
                {
                    Cond_Loop.setType("c");
                }
                else
                {
                    analyseBool(child2);
                    analyseInstr(child3);
                    if((child2.getType().equals("b") || child2.getType().equals("f")) && child3.getType().equals("c"))
                    {
                        Cond_Loop.setType("c");
                    }
                }
            }
            else
            {
                Node child2 = Cond_Loop.getChildren().get(1);
                Node child3 = Cond_Loop.getChildren().get(2);
                Node child4 = Cond_Loop.getChildren().get(3);

                if(getVariableType(child2.getChildren().get(0).getRowInTable().getNewName()).equals("s"))
                {
                    Cond_Loop.setType("e");
                }
                else if(getVariableType(child3.getChildren().get(0).getRowInTable().getNewName()).equals("s"))
                {
                    Cond_Loop.setType("e");
                }
                else if(getVariableType(child3.getChildren().get(2).getRowInTable().getNewName()).equals("s"))
                {
                    Cond_Loop.setType("e");
                }
                else if(getVariableType(child4.getChildren().get(0).getRowInTable().getNewName()).equals("s"))
                {
                    Cond_Loop.setType("e");
                }
                else if(getVariableType(child4.getChildren().get(1).getChildren().get(1).getRowInTable().getNewName()).equals("s"))
                {
                    Cond_Loop.setType("e");
                }
                else
                {
                    boolean codeIsCorrect = true;
                    for(int i = 4; i < Cond_Loop.getChildren().size(); i++)
                    {
                        if (!Cond_Loop.getChildren().get(i).getType().equals("c"))
                        {
                            codeIsCorrect = false;
                            break;
                        }
                    }

                    if(codeIsCorrect)
                    {
                        Cond_Loop.setType("c");
                        setSymbolTableVariableType(child2.getChildren().get(0).getRowInTable().getNewName(), "n");
                        setSymbolTableVariableType(child3.getChildren().get(0).getRowInTable().getNewName(), "n");
                        setSymbolTableVariableType(child3.getChildren().get(2).getRowInTable().getNewName(), "n");
                        setSymbolTableVariableType(child4.getChildren().get(0).getRowInTable().getNewName(), "n");
                        setSymbolTableVariableType(child4.getChildren().get(1).getChildren().get(1).getRowInTable().getNewName(), "n");
                    }
                    else
                    {
                        for(int i = 4; i < Cond_Loop.getChildren().size(); i++)
                        {
                            analyseInstr(Cond_Loop.getChildren().get(i));
                        }

                        codeIsCorrect = true;
                        for(int i = 4; i < Cond_Loop.getChildren().size(); i++)
                        {
                            if (!Cond_Loop.getChildren().get(i).getType().equals("c"))
                            {
                                codeIsCorrect = false;
                                break;
                            }
                        }

                        if(codeIsCorrect)
                        {
                            Cond_Loop.setType("c");
                            setSymbolTableVariableType(child2.getChildren().get(0).getRowInTable().getNewName(), "n");
                            setSymbolTableVariableType(child3.getChildren().get(0).getRowInTable().getNewName(), "n");
                            setSymbolTableVariableType(child3.getChildren().get(2).getRowInTable().getNewName(), "n");
                            setSymbolTableVariableType(child4.getChildren().get(0).getRowInTable().getNewName(), "n");
                            setSymbolTableVariableType(child4.getChildren().get(1).getChildren().get(1).getRowInTable().getNewName(), "n");
                        }
                        else
                        {
                            this.ErrorMsgs.addElement("Error: Type mis-match for one or more var's in COND_LOOP " + Cond_Loop.getRowInTable().getUid());
                        }
                    }
                }
            }
        }
    }

    private void analyseProcDef(Node Proc_Def)
    {
        if(Proc_Def != null)
        {
            Node child1 = Proc_Def.getChildren().get(0);
            if(child1.getType().equals("c"))
            {
                Proc_Def.setType("c");
            }
            else
            {
                analyseProc(child1);
                if(child1.getType().equals("c"))
                {
                    Proc_Def.setType("c");
                }
                else
                {
                    this.ErrorMsgs.addElement("Error: Type mis-match found in PROC_DEF " + Proc_Def.getRowInTable().getUid());
                }
            }
        }
    }

    private void analyseProc(Node Proc)
    {
        if(Proc != null)
        {
            Node child3 = Proc.getChildren().get(2);
            if(child3.getType().equals("c"))
            {
                Proc.setType("c");
            }
            else
            {
                analyseProg(child3);
                if(child3.getType().equals("c"))
                {
                    Proc.setType("c");
                }
                else
                {
                    this.ErrorMsgs.addElement("Error: Type mis-match in body of PROC " + Proc.getRowInTable().getUid());
                }
            }
        }
    }

    private void checkForAndRemoveDeadCode()
    {
        Node currNode = this.tree;
        recursivelyIterateThroughTreeForDeadCode(currNode);
        recursivelyAnalyseDeadCode(currNode);
        if(currNode.getType().equals("d"))
        {
            this.tree = null;
        }
        else
        {
            Node newRoot = new Node(currNode);
            recursivelyRemoveDeadCode(newRoot, currNode);
            this.tree = newRoot;
        }
    }

    private void recursivelyIterateThroughTreeForDeadCode(Node currNode)
    {
        if(currNode != null)
        {
             if(currNode.getNodeDetail().equals("COND_BRANCH"))
             {
                analyseCond_BranchForDeadCode(currNode);
             }
             else if(currNode.getNodeDetail().equals("COND_LOOP"))
             {
                 analyseCond_LoopForDeadCode(currNode);
             }
             else
             {
                 for(Node child: currNode.getChildren())
                 {
                     recursivelyIterateThroughTreeForDeadCode(child);
                 }
             }
        }
    }

    private void analyseCond_BranchForDeadCode(Node Cond_Branch)
    {
        if(Cond_Branch != null)
        {
            int size = Cond_Branch.getChildren().size();
            if(size == 4)
            {
                Node child2 = Cond_Branch.getChildren().get(1);
                if(child2.getChildren().get(0).getNodeDetail().equals("not"))
                {
                    if(child2.getChildren().get(1).getType().equals("f"))
                    {
                        Node temp = new Node(-1, "Temp", "Temp");
                        for(int i = 3; i < Cond_Branch.getChildren().size(); i++)
                        {
                            temp.addChild(Cond_Branch.getChildren().get(i));
                        }
                        Cond_Branch.setChildren(temp.getChildren());
                    }
                }
                else
                {
                    if(child2.getType().equals("f"))
                    {
                        Cond_Branch.setType("d");
                    }
                }
            }
            else
            {
                Node child2 = Cond_Branch.getChildren().get(1);
                if(child2.getChildren().get(0).getNodeDetail().equals("not"))
                {
                    if(child2.getChildren().get(1).getType().equals("f"))
                    {
                        Node temp = new Node(-1, "Temp", "Temp");
                        for(int i = 3; i < Cond_Branch.getChildren().size(); i++)
                        {
                            Node child = Cond_Branch.getChildren().get(i);
                            if(child.getNodeDetail().equals("else"))
                            {
                                break;
                            }
                            else
                            {
                                temp.addChild(Cond_Branch.getChildren().get(i));
                            }
                        }
                        Cond_Branch.setChildren(temp.getChildren());
                    }
                }
                else
                {
                    if(child2.getType().equals("f"))
                    {
                        int index = 0;
                        for(int i = 0; i < Cond_Branch.getChildren().size(); i++)
                        {
                            if(Cond_Branch.getChildren().get(i).getNodeDetail().equals("else"))
                            {
                                index++;
                                break;
                            }
                            else
                            {
                                index++;
                            }
                        }

                        Node temp = new Node(-1, "Temp", "Temp");
                        for(int i = index; i < Cond_Branch.getChildren().size(); i++)
                        {
                            temp.addChild(Cond_Branch.getChildren().get(i));
                        }
                        Cond_Branch.setChildren(temp.getChildren());
                    }
                }
            }
        }
    }

    private void analyseCond_LoopForDeadCode(Node Cond_Loop)
    {
        if(Cond_Loop != null)
        {
            Node child1 = Cond_Loop.getChildren().get(0);
            if(child1.getNodeDetail().equals("while"))
            {
                Node child2 = Cond_Loop.getChildren().get(1);
                if(child2.getChildren().get(0).getNodeDetail().equals("not"))
                {
                    Node Bool = child2.getChildren().get(1);
                    if(Bool.getType().equals("f"))
                    {
                        this.warningMsg.addElement("Warning: Infinite Loop Detected.");
                    }
                }
                else
                {
                    if(child2.getType().equals("f"))
                    {
                        Cond_Loop.setType("d");
                    }
                }
            }
            else
            {
                Node Bool = Cond_Loop.getChildren().get(2);
                if(Bool.getChildren().get(0).getRowInTable().getNewName().equals(Bool.getChildren().get(2).getRowInTable().getNewName()))
                {
                    Cond_Loop.setType("d");
                }
            }
        }
    }

    private void recursivelyAnalyseDeadCode(Node currNode)
    {
        if(currNode != null)
        {
            if(currNode.getTypeOfNode().equals("Non-Terminal"))
            {
                boolean wholeBranchIsNotDead = false;
                for(Node child: currNode.getChildren())
                {
                    if (!child.getType().equals("d"))
                    {
                        wholeBranchIsNotDead = true;
                        break;
                    }
                }

                if(!wholeBranchIsNotDead)
                {
                    currNode.setType("d");
                }
                else
                {
                    for(Node child: currNode.getChildren())
                    {
                        recursivelyAnalyseDeadCode(child);
                    }

                    wholeBranchIsNotDead = false;
                    for(Node child: currNode.getChildren())
                    {
                        if (!child.getType().equals("d"))
                        {
                            wholeBranchIsNotDead = true;
                            break;
                        }
                    }

                    if(!wholeBranchIsNotDead)
                    {
                        currNode.setType("d");
                    }
                }
            }
        }
    }

    private void recursivelyRemoveDeadCode(Node newRoot, Node currNode)
    {
        if(currNode != null)
        {
            for(Node child: currNode.getChildren())
            {
                if(!child.getType().equals("d"))
                {
                    Node newNode = new Node(child);
                    newRoot.addChild(newNode);
                    recursivelyRemoveDeadCode(newNode, child);
                }
            }
        }
    }
}
