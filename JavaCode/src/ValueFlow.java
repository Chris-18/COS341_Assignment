public class ValueFlow
{
    private Node tree;
    private Boolean flowErrorFound = false;
    private String flowErrorMsg = "";

    public ValueFlow(Node tree)
    {
        this.tree = tree;
    }

    public String analyseValueFlow()
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
            
        }
    }
}
