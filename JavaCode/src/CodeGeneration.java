import java.util.Vector;

public class CodeGeneration
{
    private final Node tree;
    private final Vector<VariableRow> variableTable = new Vector<>();
    private static int lineNo = 10;
    private static int labelNo = 0;
    private static String generatedProcCode = "";

    public CodeGeneration(Node tree)
    {
        this.tree = tree;
    }

    public void generateCode()
    {
        String phaseOne = generateProg(this.tree) + generatedProcCode;
        System.out.println(phaseOne);
        System.out.println();
        System.out.println();
        System.out.println(finaliseBASIC_Code(phaseOne));
    }

    private String finaliseBASIC_Code(String code)
    {
        String generatedCode = "";
        String phaseOneCode = code;
        while(phaseOneCode.indexOf('\n') != -1)
        {
            String line = phaseOneCode.substring(0, phaseOneCode.indexOf('\n'));
            String temp = line;

            if(line.charAt(0) != '*')
            {
                int lineNo = Integer.parseInt(line.substring(0, line.indexOf(' ')));
                line = line.substring(line.indexOf(' ') + 1);

                if(line.indexOf(' ') != -1)
                {
                    String command = line.substring(0, line.indexOf(' '));
                    line = line.substring(line.indexOf(' ') + 1);

                    if(command.equals("IF"))
                    {
                        String condition = line.substring(0, line.indexOf("THEN")-1);
                        line = line.substring(line.indexOf("THEN")+5);
                        String label1 = line.substring(0, line.indexOf(' '));
                        line = line.substring(line.indexOf("ELSE")+5);
                        generatedCode += lineNo + " IF " +
                                condition + " THEN " +
                                findProcLabelLineNo(code, label1) + " ELSE " +
                                findProcLabelLineNo(code, line) + "\n";
                    }
                    else if(command.equals("GOSUB"))
                    {
                        String proc = line;
                        int procLineNo = findProcLabelLineNo(code, proc);
                        generatedCode += lineNo + " GOSUB " + procLineNo + "\n";
                    }
                    else if(command.equals("GOTO"))
                    {
                        String label = line;
                        int labelLineNo = findProcLabelLineNo(code, label);
                        generatedCode += lineNo + " GOTO " + labelLineNo + "\n";
                    }
                    else
                    {
                        generatedCode += temp + "\n";
                    }
                }
                else
                {
                    generatedCode += temp + "\n";
                }
            }

            phaseOneCode = phaseOneCode.substring(phaseOneCode.indexOf('\n')+1);
        }
        generatedCode += phaseOneCode + "\n";
        return generatedCode;
    }

    private int findProcLabelLineNo(String code, String labelProc)
    {
        String phaseOneCode = code;
        while(phaseOneCode.indexOf('\n') != -1)
        {
            String line = phaseOneCode.substring(0, phaseOneCode.indexOf('\n'));

            int lineNo = Integer.parseInt(line.substring(1, line.indexOf(' ')));
            line = line.substring(line.indexOf(' ') + 1);
            if(line.equals(labelProc))
            {
                return findNextPermanentLineNo(lineNo, code);
            }

            phaseOneCode = phaseOneCode.substring(phaseOneCode.indexOf('\n')+1);
        }
        return -1;
    }

    private int findNextPermanentLineNo(int Num, String code)
    {
        String phaseOneCode = code;
        while(phaseOneCode.indexOf('\n') != -1)
        {
            String line = phaseOneCode.substring(0, phaseOneCode.indexOf('\n'));

            int lineNo = Integer.parseInt(line.substring(1, line.indexOf(' ')));
            if(lineNo == Num)
            {
                phaseOneCode = phaseOneCode.substring(phaseOneCode.indexOf('\n')+1);
                while(phaseOneCode.indexOf('\n') != -1)
                {
                    line = phaseOneCode.substring(0, phaseOneCode.indexOf('\n'));

                    if(line.charAt(0) != '*')
                    {
                        return Integer.parseInt(line.substring(0, line.indexOf(' ')));
                    }

                    phaseOneCode = phaseOneCode.substring(phaseOneCode.indexOf('\n')+1);
                }
            }

            phaseOneCode = phaseOneCode.substring(phaseOneCode.indexOf('\n')+1);
        }
        return Integer.parseInt(phaseOneCode.substring(0, phaseOneCode.indexOf(' ')));
    }

    private String getVariableType(String varName)
    {
        for(VariableRow variableRow: variableTable)
        {
            if(variableRow.getVariableName().equals(varName))
            {
                return variableRow.getType();
            }
        }
        return "";
    }

    private int getLineNo()
    {
        int num = lineNo;
        lineNo += 10;
        return num;
    }

    private String getLabel()
    {
        String label = "L" + labelNo;
        labelNo++;
        return label;
    }

    private String generateProg(Node Prog)
    {
        String generatedCode = "";
        if(Prog != null)
        {
            for(Node child: Prog.getChildren())
            {
                if (child.getNodeDetail().equals("INSTR"))
                {
                    generatedCode += generateInstr(child) + "\n";
                }
                else if (child.getNodeDetail().equals("PROC_DEFS"))
                {
                    generatedCode += getLineNo() + " END\n" + generateProcDef(child) + "\n";
                }
            }
        }
        return generatedCode;
    }

    private String generateInstr(Node Instr)
    {
        String generatedCode = "";
        if(Instr != null)
        {
            Node child = Instr.getChildren().get(0);
            switch (child.getNodeDetail())
            {
                case "halt" -> generatedCode += getLineNo() + " STOP";
                case "IO" -> generatedCode += generateIO(child);
                case "CALL" -> generatedCode += generateCall(child);
                case "ASSIGN" -> generatedCode += generateAssign(child);
                case "COND_BRANCH" -> generatedCode += generateCond_Branch(child);
                case "COND_LOOP" -> generatedCode += generateCond_Loop(child);
            }
        }
        return generatedCode;
    }

    private String generateIO(Node IO)
    {
        String generatedCode = "";
        if(IO != null)
        {
            Node ioType = IO.getChildren().get(0);
            if(ioType.getNodeDetail().equals("input"))
            {
                generatedCode += getLineNo() + " INPUT \"\"; " + IO.getChildren().get(1).getRowInTable().getNewName();
                for(VariableRow variableRow: variableTable)
                {
                    if(!variableRow.getVariableName().equals(IO.getChildren().get(1).getRowInTable().getNewName()))
                    {
                        VariableRow newRow = new VariableRow(IO.getChildren().get(1).getRowInTable().getNewName(), "number");
                        this.variableTable.addElement(newRow);
                    }
                }
            }
            else if(ioType.getNodeDetail().equals("output"))
            {
                if(IO.getChildren().get(1).getRowInTable().getNewName().equals(""))
                {
                    generatedCode += getLineNo() + " PRINT " + IO.getChildren().get(1).getNodeDetail();
                }
                else
                {
                    if(getVariableType(IO.getChildren().get(1).getRowInTable().getNewName()).equals("string"))
                    {
                        generatedCode += getLineNo() + " PRINT " + IO.getChildren().get(1).getRowInTable().getNewName() + "$";
                    }
                    else
                    {
                        generatedCode += getLineNo() + " PRINT " + IO.getChildren().get(1).getRowInTable().getNewName();
                    }
                }
            }
        }
        return generatedCode;
    }

    private String generateCall(Node Call)
    {
        String generatedCode = "";
        if(Call != null)
        {
            Node child = Call.getChildren().get(0);
            generatedCode += getLineNo() + " GOSUB " + child.getRowInTable().getNewName();
        }
        return generatedCode;
    }

    private String generateAssign(Node Assign)
    {
        String generatedCode = "";
        if(Assign != null)
        {
            Node child1 = Assign.getChildren().get(0);
            Node child2 = Assign.getChildren().get(1);
            if(child2.getTypeOfNode().equals("Non-Terminal"))
            {
                generatedCode += getLineNo() + " LET " + child1.getRowInTable().getNewName() + " = " + generateCalc(child2);
            }
            else if(!child2.getRowInTable().getNewName().equals(""))
            {
                String type = getVariableType(child2.getRowInTable().getNewName());
                if(type.equals("string"))
                {
                    generatedCode += getLineNo() + " LET " + child1.getRowInTable().getNewName() + "$ = " + child2.getNodeDetail();
                }
                else
                {
                    generatedCode += getLineNo() + " LET " + child1.getRowInTable().getNewName() + " = " + child2.getNodeDetail();
                }
            }
            else
            {
                if(child2.getNodeDetail().charAt(0) == '\"')
                {
                    generatedCode += getLineNo() + " LET " + child1.getRowInTable().getNewName() + "$ = " + child2.getNodeDetail();
                    VariableRow variableRow = new VariableRow(child1.getRowInTable().getNewName(), "string");
                    this.variableTable.addElement(variableRow);
                }
                else
                {
                    generatedCode += getLineNo() + " LET " + child1.getRowInTable().getNewName() + " = " + child2.getNodeDetail();
                    VariableRow variableRow = new VariableRow(child1.getRowInTable().getNewName(), "number");
                    this.variableTable.addElement(variableRow);
                }
            }
        }
        return generatedCode;
    }

    private String generateCalc(Node Calc)
    {
        String generatedCode = "";
        if(Calc != null)
        {
            Node child1 = Calc.getChildren().get(0);
            Node child2 = Calc.getChildren().get(1);
            Node child3 = Calc.getChildren().get(2);
            switch (child1.getNodeDetail())
            {
                case "add" -> generatedCode += generateNumExpr(child2) + " + " + generateNumExpr(child3);
                case "sub" -> generatedCode += generateNumExpr(child2) + " - " + generateNumExpr(child3);
                case "mult" -> generatedCode += generateNumExpr(child2) + " * " + generateNumExpr(child3);
            }
        }
        return generatedCode;
    }

    private String generateNumExpr(Node NumExpr)
    {
        String generatedCode = "";
        if(NumExpr != null)
        {
            if(NumExpr.getTypeOfNode().equals("Non-Terminal"))
            {
                generatedCode += "( " + generateCalc(NumExpr) + " )";
            }
            else if(!NumExpr.getRowInTable().getNewName().equals(""))
            {
                generatedCode += NumExpr.getRowInTable().getNewName();
            }
            else
            {
                generatedCode += NumExpr.getNodeDetail();
            }
        }
        return generatedCode;
    }

    private String generateCond_Branch(Node Cond_Branch)
    {
        String generatedCode = "";
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

            String trueLabel = getLabel();
            String falseLabel = getLabel();
            if(hasElse)
            {
                String endLabel = getLabel();
                generatedCode += getLineNo() + " IF ";
                generatedCode += generateBool(Cond_Branch.getChildren().get(1), trueLabel, falseLabel, true);

                generatedCode += "*" + getLineNo() + " " + trueLabel + "\n";
                int elsePos = 0;
                for(int i = 3; i < Cond_Branch.getChildren().size(); i++)
                {
                    if(Cond_Branch.getChildren().get(i).getNodeDetail().equals("else"))
                    {
                        elsePos = i;
                        break;
                    }
                    else
                    {
                        if(Cond_Branch.getChildren().get(i).getNodeDetail().equals("INSTR"))
                        {
                            generatedCode += generateInstr(Cond_Branch.getChildren().get(i)) + "\n";
                        }
                        else if(Cond_Branch.getChildren().get(i).getNodeDetail().equals("PROC_DEFS"))
                        {
                            generatedProcCode += generateProcDef(Cond_Branch.getChildren().get(i)) + "\n";
                        }
                    }
                }
                generatedCode += getLineNo() + " GOTO " + endLabel + "\n";

                generatedCode += "*" + getLineNo() + " " + falseLabel + "\n";
                for(int i = elsePos + 1; i < Cond_Branch.getChildren().size();i++)
                {
                    if(Cond_Branch.getChildren().get(i).getNodeDetail().equals("INSTR"))
                    {
                        generatedCode += generateInstr(Cond_Branch.getChildren().get(i)) + "\n";
                    }
                    else if(Cond_Branch.getChildren().get(i).getNodeDetail().equals("PROC_DEFS"))
                    {
                        generatedProcCode += generateProcDef(Cond_Branch.getChildren().get(i)) + "\n";
                    }
                }
                generatedCode += "*" + getLineNo() + " " + endLabel;
            }
            else
            {
                generatedCode += getLineNo() + " IF ";
                generatedCode += generateBool(Cond_Branch.getChildren().get(1), trueLabel, falseLabel, true);

                generatedCode += "*" + getLineNo() + " " + trueLabel + "\n";
                for(int i = 3; i < Cond_Branch.getChildren().size(); i++)
                {
                    if(Cond_Branch.getChildren().get(i).getNodeDetail().equals("INSTR"))
                    {
                        generatedCode += generateInstr(Cond_Branch.getChildren().get(i)) + "\n";
                    }
                    else if(Cond_Branch.getChildren().get(i).getNodeDetail().equals("PROC_DEFS"))
                    {
                        generatedProcCode += generateProcDef(Cond_Branch.getChildren().get(i)) + "\n";
                    }
                }
                generatedCode += "*" + getLineNo() + " " + falseLabel;
            }
        }
        return generatedCode;
    }

    private String generateCond_Loop(Node Cond_Loop)
    {
        String generatedCode = "";
        if(Cond_Loop != null)
        {
            Node child1 = Cond_Loop.getChildren().get(0);
            String startLabel = getLabel();
            String trueLabel = getLabel();
            String falseLabel = getLabel();
            if(child1.getNodeDetail().equals("while"))
            {
                generatedCode += "*" + getLineNo() + " " + startLabel + "\n" + getLineNo() + " IF ";
                generatedCode += generateBool(Cond_Loop.getChildren().get(1), trueLabel, falseLabel, true);

                generatedCode += "*" + getLineNo() + " " + trueLabel + "\n";
                for(int i = 2; i < Cond_Loop.getChildren().size(); i++)
                {
                    if(Cond_Loop.getChildren().get(i).getNodeDetail().equals("INSTR"))
                    {
                        generatedCode += generateInstr(Cond_Loop.getChildren().get(i)) + "\n";
                    }
                    else if(Cond_Loop.getChildren().get(i).getNodeDetail().equals("PROC_DEFS"))
                    {
                        generatedProcCode += generateProcDef(Cond_Loop.getChildren().get(i)) + "\n";
                    }
                }
            }
            else
            {
                generatedCode += generateAssign(Cond_Loop.getChildren().get(1)) + "\n";
                generatedCode += "*" + getLineNo() + " " + startLabel + "\n";

                generatedCode += getLineNo() + " IF " + generateBool(Cond_Loop.getChildren().get(2), trueLabel, falseLabel, true);
                generatedCode += "*" + getLineNo() + " " + trueLabel + "\n";

                for(int i = 4; i < Cond_Loop.getChildren().size(); i++)
                {
                    if(Cond_Loop.getChildren().get(i).getNodeDetail().equals("INSTR"))
                    {
                        generatedCode += generateInstr(Cond_Loop.getChildren().get(i)) + "\n";
                    }
                    else if(Cond_Loop.getChildren().get(i).getNodeDetail().equals("PROC_DEFS"))
                    {
                        generatedProcCode += generateProcDef(Cond_Loop.getChildren().get(i)) + "\n";
                    }
                }

                generatedCode += generateAssign(Cond_Loop.getChildren().get(3)) + "\n";
            }
            generatedCode += getLineNo() + " GOTO " + startLabel + "\n";
            generatedCode += "*" + getLineNo() + " " + falseLabel;
        }
        return generatedCode;
    }

    private String generateBool(Node Bool, String trueLabel, String falseLabel, boolean initialCall)
    {
        String generatedCode = "";
        if(Bool != null)
        {
            Node child1 = Bool.getChildren().get(0);
            switch (child1.getNodeDetail())
            {
                case "eq" -> {
                    Node child2 = Bool.getChildren().get(1);
                    Node child3 = Bool.getChildren().get(2);
                    if (child2.getNodeDetail().equals("BOOL"))
                    {
                        //eq(Bool, Bool)
                        if (initialCall)
                        {
                            generatedCode += "( " + generateBool(child2, trueLabel, falseLabel, false) + ") = (" + generateBool(child3, trueLabel, falseLabel, false) + ") THEN " + trueLabel + " ELSE " + falseLabel + "\n";
                        } else
                        {
                            generatedCode += "( " + generateBool(child2, trueLabel, falseLabel, false) + ") = (" + generateBool(child3, trueLabel, falseLabel, false) + ")";
                        }
                    } else if (!child2.getRowInTable().getNewName().equals("") &&
                            !child3.getRowInTable().getNewName().equals(""))
                    {
                        //eq(var, Var)
                        if (getVariableType(child2.getRowInTable().getNewName()).equals("string"))
                        {
                            generatedCode += child2.getRowInTable().getNewName() + "$";
                        } else
                        {
                            generatedCode += child2.getRowInTable().getNewName();
                        }

                        generatedCode += " = ";

                        if (getVariableType(child3.getRowInTable().getNewName()).equals("string"))
                        {
                            generatedCode += child3.getRowInTable().getNewName() + "$";
                        } else
                        {
                            generatedCode += child3.getRowInTable().getNewName();
                        }

                        if (initialCall)
                        {
                            generatedCode += " THEN " + trueLabel + " ELSE " + falseLabel + "\n";
                        }
                    } else
                    {
                        //eq(num, num)
                        generatedCode += child2.getNodeDetail() + " = " + child3.getNodeDetail();
                        if (initialCall)
                        {
                            generatedCode += " THEN " + trueLabel + " ELSE " + falseLabel + "\n";
                        }
                    }
                }
                case "not" ->
                        //not Bool
                        generatedCode += generateBool(Bool.getChildren().get(1), falseLabel, trueLabel, true);
                case "and", "or" -> {
                    Node child2 = Bool.getChildren().get(1);
                    Node child3 = Bool.getChildren().get(2);
                    if (child1.getNodeDetail().equals("and"))
                    {
                        //and(Bool, Bool)
                        String andLabel = getLabel();
                        generatedCode += generateBool(child2, andLabel, falseLabel, true);
                        generatedCode += "*" + getLineNo() + " " + andLabel + "\n";
                        generatedCode += getLineNo() + " IF " + generateBool(child3, trueLabel, falseLabel, true);
                    } else
                    {
                        //or(Bool, Bool)
                        String orLabel = getLabel();
                        generatedCode += generateBool(child2, trueLabel, orLabel, true);
                        generatedCode += "*" + getLineNo() + " " + orLabel + "\n";
                        generatedCode += getLineNo() + " IF " + generateBool(child3, trueLabel, falseLabel, true);
                    }
                }
                default -> {
                    //(Var < Var) || (Var > Var)
                    Node child2 = Bool.getChildren().get(1);
                    Node child3 = Bool.getChildren().get(2);
                    if (child1.getRowInTable().getNewName().equals(""))
                    {
                        generatedCode += child1.getNodeDetail();
                    } else
                    {
                        if (getVariableType(child1.getRowInTable().getNewName()).equals("string"))
                        {
                            generatedCode += child1.getRowInTable().getNewName() + "$";
                        } else
                        {
                            generatedCode += child1.getRowInTable().getNewName();
                        }
                    }

                    generatedCode += " " + child2.getNodeDetail() + " ";

                    if (child3.getRowInTable().getNewName().equals(""))
                    {
                        generatedCode += child3.getNodeDetail();
                    } else
                    {
                        if (getVariableType(child3.getRowInTable().getNewName()).equals("string"))
                        {
                            generatedCode += child3.getRowInTable().getNewName() + "$";
                        } else
                        {
                            generatedCode += child3.getRowInTable().getNewName();
                        }
                    }

                    generatedCode += " THEN " + trueLabel + " ELSE " + falseLabel + "\n";
                }
            }
        }
        return generatedCode;
    }

    private String generateProcDef(Node Proc_Def)
    {
        String generatedCode = "";
        if(Proc_Def != null)
        {
            generatedCode += generateProc(Proc_Def.getChildren().get(0));
            generatedCode += getLineNo() + " RETURN";
        }
        return generatedCode;
    }

    private String generateProc(Node Proc)
    {
        String generatedCode = "";
        if(Proc != null)
        {
            generatedCode += "*" + getLineNo() + " " + Proc.getChildren().get(1).getRowInTable().getNewName() + "\n";
            generatedCode += generateProg(Proc.getChildren().get(2));
        }
        return generatedCode;
    }
}
