import java.util.Vector;

public class ValueFlow
{
    private Node tree;
    private Boolean flowErrorFound = false;
    private String flowErrorMsg = "";
    private boolean procFound = false;
    private Vector<SymbolRow> symbolTable;
    private Vector<SymbolRow> tempSymbolTable = new Vector<>();
    private int tableNo = 0;

    public ValueFlow(Node tree, Vector<SymbolRow> symbolTable)
    {
        this.tree = tree;
        this.symbolTable = symbolTable;
    }

    public Boolean analyseValueFlow()
    {
        Node root = this.tree;
        analyseProg(root);
        if(flowErrorFound)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public String getFlowErrorMsg()
    {
        return this.flowErrorMsg;
    }

    private String getVariableFlow(String VarName)
    {
        if(tableNo == 0)
        {
            for(SymbolRow symbolRow: this.symbolTable)
            {
                if(symbolRow.getVariableName().equals(VarName))
                {
                    return symbolRow.getFlow();
                }
            }
        }
        else
        {
            for(SymbolRow symbolRow: this.tempSymbolTable)
            {
                if(symbolRow.getVariableName().equals(VarName))
                {
                    return symbolRow.getFlow();
                }
            }
        }
        return "";
    }

    private void setSymbolTableVariableFlow(String VarName, String flow)
    {
        if(tableNo == 0)
        {
            for(SymbolRow symbolRow: this.symbolTable)
            {
                if(symbolRow.getVariableName().equals(VarName))
                {
                    symbolRow.setFlow(flow);
                }
            }
        }
        else
        {
            for(SymbolRow symbolRow: this.tempSymbolTable)
            {
                if(symbolRow.getVariableName().equals(VarName))
                {
                    symbolRow.setFlow(flow);
                }
            }
        }
    }

    private void analyseProg(Node Prog)
    {
        boolean flowCheck = true;
        for(Node child: Prog.getChildren())
        {
            if(child.getNodeDetail().equals("INSTR"))
            {
                analyseInstr(child);
                if(flowErrorFound)
                {
                    return;
                }
            }
            else if(child.getNodeDetail().equals("PROC_DEFS"))
            {
                analyseProcDef(child);
                if(flowErrorFound)
                {
                    return;
                }
            }
        }

        if(!flowErrorFound)
        {
            Prog.setFlow("+");
        }
        else
        {
            Prog.setFlow("-");
        }
    }

    private void analyseInstr(Node Instr)
    {
        if(Instr != null)
        {
            Node child = Instr.getChildren().get(0);
            if(child.getNodeDetail().equals("halt"))
            {
                Instr.setFlow("+");
            }
            else if(child.getNodeDetail().equals("IO"))
            {
                analyseIO(child);
                if(flowErrorFound)
                {
                    return;
                }

                if(child.getFlow().equals("+"))
                {
                    Instr.setFlow("+");
                }
            }
            else if(child.getNodeDetail().equals("CALL"))
            {
                analyseCall(child);
                if(flowErrorFound)
                {
                    return;
                }

                if(child.getFlow().equals("+"))
                {
                    Instr.setFlow("+");
                }
            }
            else if(child.getNodeDetail().equals("ASSIGN"))
            {
                analyseAssign(child);
                if(flowErrorFound)
                {
                    return;
                }

                if(child.getFlow().equals("+"))
                {
                    Instr.setFlow("+");
                }
            }
            else if(child.getNodeDetail().equals("COND_BRANCH"))
            {
                analyseCond_Branch(child);
                if(flowErrorFound)
                {
                    return;
                }

                if(child.getFlow().equals("+"))
                {
                    Instr.setFlow("+");
                }
            }
            else if(child.getNodeDetail().equals("COND_LOOP"))
            {
                analyseCond_Loop(child);
                if(flowErrorFound)
                {
                    return;
                }

                if(child.getFlow().equals("+"))
                {
                    Instr.setFlow("+");
                }
            }
        }
    }

    private void analyseIO(Node IO)
    {
        if(IO != null)
        {
            Node ioType = IO.getChildren().get(0);
            if(ioType.getNodeDetail().equals("input"))
            {
                setSymbolTableVariableFlow(IO.getChildren().get(1).getRowInTable().getNewName(), "+");
                IO.getChildren().get(1).setFlow("+");
                IO.setFlow("+");
            }
            else if(ioType.getNodeDetail().equals("output"))
            {
                Node var = IO.getChildren().get(1);
                if(getVariableFlow(var.getRowInTable().getNewName()).equals("+"))
                {
                    IO.setFlow("+");
                }
                else
                {
                    flowErrorFound = true;
                    flowErrorMsg = "Error: Attempting to output a variable that has not been initialised yet.";
                }
            }
        }
    }

    private void analyseCall(Node Call)
    {
        if(Call != null)
        {
            goToCorrespondingProc_Def(Call.getChildren().get(0).getRowInTable().getNewName());
            if(!flowErrorFound)
            {
                Call.setFlow("+");
            }
        }
    }

    private void goToCorrespondingProc_Def(String procName)
    {
        Node root = this.tree;
        recursivelyFindCorrespondingProcNode(root, procName);
        procFound = false;
    }

    private void recursivelyFindCorrespondingProcNode(Node currNode, String procName)
    {
        if (currNode != null)
        {
            if(currNode.getNodeDetail().equals("PROC"))
            {
                if(currNode.getChildren().get(1).getRowInTable().getNewName().equals(procName))
                {
                    procFound = true;
                    analyseProc(currNode);
                }
            }
            else
            {
                for(Node child: currNode.getChildren())
                {
                    recursivelyFindCorrespondingProcNode(child, procName);
                    if(procFound)
                    {
                        break;
                    }
                }
            }
        }
    }

    private void analyseProcDef(Node Proc_Def)
    {
        if(Proc_Def != null)
        {
            analyseProc(Proc_Def.getChildren().get(0));
            if(flowErrorFound)
            {
                return;
            }
            if(Proc_Def.getChildren().get(0).getFlow().equals("+"))
            {
                Proc_Def.setFlow("+");
            }
            else
            {
                flowErrorFound = true;
                flowErrorMsg = "Error: Proc_Def was not initialised properly.";
            }
        }
    }

    private void analyseProc(Node Proc)
    {
        if (Proc != null)
        {
            analyseProg(Proc.getChildren().get(2));
            if(flowErrorFound)
            {
                return;
            }
            if(Proc.getChildren().get(2).getFlow().equals("+"))
            {
                Proc.setFlow("+");
            }
        }
    }

    private void analyseAssign(Node Assign)
    {
        if (Assign != null)
        {
            Node child1 = Assign.getChildren().get(0);
            Node child2 = Assign.getChildren().get(1);
            if(child2.getTypeOfNode().equals("Non-Terminal"))
            {
                analyseCalc(child2);
                if(flowErrorFound)
                {
                    return;
                }
                if(child2.getFlow().equals("+"))
                {
                    setSymbolTableVariableFlow(child1.getRowInTable().getNewName(), "+");
                    child1.setFlow("+");
                    Assign.setFlow("+");
                }
                else
                {
                    flowErrorFound = true;
                    flowErrorMsg = "Error: NumExpr assign to var not initialised properly.";
                }
            }
            else if(!child2.getRowInTable().getNewName().equals(""))
            {
                if(getVariableFlow(child2.getRowInTable().getNewName()).equals("+"))
                {
                    setSymbolTableVariableFlow(child1.getRowInTable().getNewName(), "+");
                    child1.setFlow("+");
                    Assign.setFlow("+");
                }
                else
                {
                    flowErrorFound = true;
                    flowErrorMsg = "Error: Right-hand side of assign not initialised.";
                }
            }
            else
            {
                setSymbolTableVariableFlow(child1.getRowInTable().getNewName(), "+");
                child1.setFlow("+");
                Assign.setFlow("+");
            }
        }
    }

    private void analyseCalc(Node Calc)
    {
        if (Calc != null)
        {
            Node child2 = Calc.getChildren().get(1);
            Node child3 = Calc.getChildren().get(2);
            analyseNumExpr(child2);
            if(flowErrorFound)
            {
                return;
            }
            analyseNumExpr(child3);
            if(flowErrorFound)
            {
                return;
            }
            if(child2.getFlow().equals("+") && child3.getFlow().equals("+"))
            {
                Calc.setFlow("+");
            }
            else
            {
                flowErrorFound = true;
                flowErrorMsg = "Error: NumExpr in calc not initialised correctly.";
            }
        }
    }

    private void analyseNumExpr(Node NumExpr)
    {
        if(NumExpr != null)
        {
            if(NumExpr.getTypeOfNode().equals("Non-Terminal"))
            {
                analyseCalc(NumExpr);
            }
            else if(!NumExpr.getRowInTable().getNewName().equals(""))
            {
                if(!getVariableFlow(NumExpr.getRowInTable().getNewName()).equals("+"))
                {
                    flowErrorFound = true;
                    flowErrorMsg = "Error: Var used in NumExpr not initialised.";
                }
                else
                {
                    NumExpr.setFlow("+");
                }
            }
            else
            {
                NumExpr.setFlow("+");
            }
        }
    }

    private void analyseCond_Branch(Node Cond_Branch)
    {
        if(Cond_Branch != null)
        {
            boolean hasElse = false;
            for(Node child: Cond_Branch.getChildren())
            {
                if(child.getNodeDetail().equals("else"))
                {
                    hasElse = true;
                    break;
                }
            }

            Node child2 = Cond_Branch.getChildren().get(1);
            analyseBool(child2);
            if(flowErrorFound)
            {
                return;
            }
            if(!child2.getFlow().equals("+"))
            {
                flowErrorFound = true;
                flowErrorMsg = "Error: Bool not initialised correctly in if(Bool) then{Code}";
                return;
            }

            if(hasElse)
            {
                if(runSafetyCheckOnCond_Branch(Cond_Branch))
                {
                    Cond_Branch.setFlow("+");
                }
                else
                {
                    flowErrorFound = true;
                    flowErrorMsg = "Error: Safety-oriented error in if(Bool) then{Code} else{Code}";
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
                    analyseBool(child2);
                    if(flowErrorFound)
                    {
                        return;
                    }
                    analyseBool(child3);
                    if(flowErrorFound)
                    {
                        return;
                    }
                    if(child2.getFlow().equals("+") && child3.getFlow().equals("+"))
                    {
                        Bool.setFlow("+");
                    }
                    else
                    {
                        flowErrorFound = true;
                        flowErrorMsg = "Error: Bool not initialised correctly in eq(Bool, Bool)";
                    }
                }
                else if(!child2.getRowInTable().getNewName().equals("") && !child3.getRowInTable().getNewName().equals(""))
                {
                    if(getVariableFlow(child2.getRowInTable().getNewName()).equals("+") && getVariableFlow(child3.getRowInTable().getNewName()).equals("+"))
                    {
                        Bool.setFlow("+");
                    }
                    else
                    {
                        flowErrorFound = true;
                        flowErrorMsg = "Error: Var not initialised correctly for eq(Var, Var)";
                    }
                }
                else
                {
                    analyseNumExpr(child2);
                    if(flowErrorFound)
                    {
                        return;
                    }
                    analyseNumExpr(child3);
                    if(flowErrorFound)
                    {
                        return;
                    }
                    if(child2.getFlow().equals("+") && child3.getFlow().equals("+"))
                    {
                        Bool.setFlow("+");
                    }
                    else
                    {
                        flowErrorFound = true;
                        flowErrorMsg = "Error: NumExpr not initialised correctly for eq(NumExpr, NumExpr)";
                    }
                }
            }
            else if(child1.getNodeDetail().equals("not"))
            {
                Node child2 = Bool.getChildren().get(1);
                analyseBool(child2);
                if(flowErrorFound)
                {
                    return;
                }
                if(child2.getFlow().equals("+"))
                {
                    Bool.setFlow("+");
                }
                else
                {
                    flowErrorFound = true;
                    flowErrorMsg = "Error: Bool not initialised correctly for (not Bool)";
                }
            }
            else if(child1.getNodeDetail().equals("and") || child1.getNodeDetail().equals("or"))
            {
                Node child2 = Bool.getChildren().get(1);
                Node child3 = Bool.getChildren().get(2);
                analyseBool(child2);
                if(flowErrorFound)
                {
                    return;
                }
                analyseBool(child3);
                if(flowErrorFound)
                {
                    return;
                }
                if(child2.getFlow().equals("+") && child3.getFlow().equals("+"))
                {
                    Bool.setFlow("+");
                }
                else
                {
                    flowErrorFound = true;
                    flowErrorMsg = "Error: Bool not initialised correctly in (and(Bool, Bool)) or (or(Bool, Bool))";
                }
            }
            else
            {
                Node child3 = Bool.getChildren().get(2);
                if(getVariableFlow(child1.getRowInTable().getNewName()).equals("+") && getVariableFlow(child3.getRowInTable().getNewName()).equals("+"))
                {
                    Bool.setFlow("+");
                }
                else
                {
                    flowErrorFound = true;
                    flowErrorMsg = "Error: Var not initialised correctly in (Var < Var) or (Var > Var)";
                }
            }
        }
    }

    private boolean runSafetyCheckOnCond_Branch(Node Cond_Branch)
    {
        if(Cond_Branch != null)
        {
            Vector<String> firstCodeBranchVariables = new Vector<String>();
            Vector<String> secondCodeBranchVariables = new Vector<String>();
            int i;
            for(i = 3; i < Cond_Branch.getChildren().size(); i++)
            {
                if(Cond_Branch.getChildren().get(i).getNodeDetail().equals("else"))
                {
                    break;
                }
                else
                {
                    firstCodeBranchVariables.addAll(recursivelyGetAllVariablesInBranch(Cond_Branch.getChildren().get(i)));
                }
            }
            i++;

            for(int j = i; j < Cond_Branch.getChildren().size(); j++)
            {
                secondCodeBranchVariables.addAll(recursivelyGetAllVariablesInBranch(Cond_Branch.getChildren().get(i)));
            }

            for(String var: firstCodeBranchVariables)
            {
                if(!secondCodeBranchVariables.contains(var))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private Vector<String> recursivelyGetAllVariablesInBranch(Node currNode)
    {
        Vector<String> listOfVars = new Vector<>();
        if(currNode != null)
        {
            if(currNode.getTypeOfNode().equals("Terminal"))
            {
                if(!currNode.getRowInTable().getNewName().equals(""))
                {
                    listOfVars.add(currNode.getRowInTable().getNewName());
                }
            }
            else
            {
                for(Node child: currNode.getChildren())
                {
                    Vector<String> tempVars = recursivelyGetAllVariablesInBranch(child);
                    listOfVars.addAll(tempVars);
                }
            }
        }
        return listOfVars;
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
                analyseBool(child2);
                if(flowErrorFound)
                {
                    return;
                }
                if(!child2.getFlow().equals("+"))
                {
                    flowErrorFound = true;
                    flowErrorMsg = "Error: Bool used in while(Bool){Code} was not initialised correctly.";
                }

                tableNo = 1;
                makeTempSymbolTable();
                Node newTree = duplicateTree();
                recursivelyIterateToStartNode(newTree, Cond_Loop.getUid(), "Cond_Loop_While");
                this.tempSymbolTable = new Vector<>();
                tableNo = 0;
            }
            else
            {
                setSymbolTableVariableFlow(Cond_Loop.getChildren().get(1).getChildren().get(0).getRowInTable().getNewName(), "+");
                Cond_Loop.getChildren().get(1).getChildren().get(0).setFlow("+");
                Cond_Loop.getChildren().get(2).getChildren().get(0).setFlow("+");
                if(!getVariableFlow(Cond_Loop.getChildren().get(2).getChildren().get(2).getRowInTable().getNewName()).equals("+"))
                {
                    flowErrorFound = true;
                    flowErrorMsg = "Error: Var used in for(var = 0; Var < VAR; var = add(var, 1), VAR was not initialised correctly.";
                }
                Cond_Loop.getChildren().get(3).getChildren().get(0).setFlow("+");
                Cond_Loop.getChildren().get(3).getChildren().get(1).getChildren().get(1).setFlow("+");

                tableNo = 1;
                makeTempSymbolTable();
                Node newTree = duplicateTree();
                recursivelyIterateToStartNode(newTree, Cond_Loop.getUid(), "Cond_Loop");
                this.tempSymbolTable = new Vector<>();
                tableNo = 0;
            }
        }
    }

    private void makeTempSymbolTable()
    {
        for(SymbolRow symbolRow: this.symbolTable)
        {
            SymbolRow newSR = new SymbolRow(symbolRow);
            this.tempSymbolTable.add(newSR);
        }
    }

    private Node duplicateTree()
    {
        Node newRoot = new Node(this.tree);
        for(Node child: this.tree.getChildren())
        {
            recursivelyDuplicateTree(newRoot, child);
        }
        return newRoot;
    }

    private void recursivelyDuplicateTree(Node newRoot, Node currNode)
    {
        if(currNode != null)
        {
            Node newChild = new Node(currNode);
            newRoot.addChild(newChild);

            for(Node child: currNode.getChildren())
            {
                recursivelyDuplicateTree(newChild, child);
            }
        }
    }

    private void recursivelyIterateToStartNode(Node currNode, int uid, String NodeDetail)
    {
        if(currNode != null)
        {
            if(currNode.getUid() == uid)
            {
                if(NodeDetail.equals("Cond_Loop"))
                {
                    for(int i = 4; i < currNode.getChildren().size(); i++)
                    {
                        analyseInstr(currNode.getChildren().get(i));
                        if(flowErrorFound)
                        {
                            return;
                        }
                    }
                    currNode.setFlow("+");
                }
                else if(NodeDetail.equals("Cond_Loop_While"))
                {
                    for(int i = 2; i < currNode.getChildren().size(); i++)
                    {
                        analyseInstr(currNode.getChildren().get(i));
                        if(flowErrorFound)
                        {
                            return;
                        }
                    }
                    currNode.setFlow("+");
                }
                else if(NodeDetail.equals("Cond_Branch"))
                {
                    boolean hasElse = false;
                    for(Node child: currNode.getChildren())
                    {
                        if(child.getNodeDetail().equals("else"))
                        {
                            hasElse = true;
                            break;
                        }
                    }
                }
            }
            else
            {
                for(Node child: currNode.getChildren())
                {
                    recursivelyIterateToStartNode(child, uid, NodeDetail);
                    if(flowErrorFound)
                    {
                        return;
                    }
                }
            }
        }
    }
}
