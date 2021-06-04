public class ValueFlow
{
    private Node tree;
    private Boolean flowErrorFound = false;
    private String flowErrorMsg = "";
    private boolean procFound = false;

    public ValueFlow(Node tree)
    {
        this.tree = tree;
    }

    /*public String analyseValueFlow()
    {
        Node root = this.tree;
        analyseProg(root);
        if(flowErrorFound)
        {
            return flowErrorMsg;
        }
        else
        {
            return "Program all good!";
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

            if(!child.getFlow().equals("+"))
            {
                flowCheck = false;
            }
        }

        if(flowCheck)
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
            else if(child.getNodeDetail().equals("COND_Loop"))
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
                IO.getChildren().get(1).setFlow("+");
                IO.setFlow("+");
            }
            else if(ioType.getNodeDetail().equals("output"))
            {
                Node var = IO.getChildren().get(1);
                if(var.getFlow().equals("+"))
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
            Node child2 = Assign.getChildren().get(2);
            if(child2.getTypeOfNode().equals("Non-Terminal"))
            {
                analyseCalc(child2);
                if(child2.getFlow().equals("+"))
                {
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
                if(child2.getFlow().equals("+"))
                {
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
            analyseNumExpr(child3);
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
                return;
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
            int size = Cond_Branch.getChildren().size();
            if(size == 4)
            {

            }
            else
            {

            }
        }
    }*/
}
