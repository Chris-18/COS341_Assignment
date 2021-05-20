import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Semantics
{
    private Node tree = null;
    private Vector<Row> Table;
    private static int variableNameNumber = 0;
    private static int procedureNameNumber = 0;
    private boolean isCallError = false;

    public Semantics(Node tree, Vector<Row> Table)
    {
        this.tree = tree;
        this.Table = Table;
    }

    public boolean checkRuleP1()
    {
        Node currNode = tree;
        return iterateThroughTreeToFindProc(currNode);
    }

    public boolean checkRuleP2()
    {
        Node currNode = tree;
        Vector<String> ProcNames = new Vector<String>();
        return checkProcNamesInProcTreesAreDifferent(currNode, ProcNames);
    }

    public boolean checkForLoopRule1()
    {
        Node currNode = this.tree;
        return recursivelyCheckForLoopRule1(currNode);
    }

    public boolean checkForLoopRule2()
    {
        Node currNode = this.tree;
        return recursivelyCheckForLoopRule2(currNode);
    }

    public void changeTreeVariableNames()
    {
        Node currNode = tree;
        recursivelyIterateThroughTreeAndChangeVariableNames(currNode);
    }

    public boolean changeTreeProcedureNames()
    {
        Node currNode = this.tree;
        recursivelyIterateThroughTreeAndChangeProcedureNames(currNode);
        currNode = this.tree;
        recursivelyIterateAndRemoveUnusedProcedureCallsAndDefs(currNode);
        return isCallError;
    }

    private boolean recursivelyIterateAndRemoveUnusedProcedureCallsAndDefs(Node currNode)
    {
        if(currNode != null)
        {
            if(currNode.getNodeDetail().equals("INSTR") && currNode.getChildren().get(0).getNodeDetail().equals("CALL"))
            {
                Node child = currNode.getChildren().get(0);
                if(child.getChildren().get(0).getRowInTable().getNewName().equals(""))
                {
                    isCallError = true;
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else if(currNode.getNodeDetail().equals("PROC_DEFS"))
            {
                Node proc = currNode.getChildren().get(0);
                if(proc.getChildren().get(1).getRowInTable().getNewName().equals(""))
                {
                    return true;
                }
                else
                {
                    return recursivelyIterateAndRemoveUnusedProcedureCallsAndDefs(proc.getChildren().get(2));
                }
            }
            else
            {
                for(int i = 0; i < currNode.getChildren().size(); i++)
                {
                    if(recursivelyIterateAndRemoveUnusedProcedureCallsAndDefs(currNode.getChildren().get(i)))
                    {
                        currNode.getChildren().remove(i);
                        i--;
                    }
                }
            }
        }
        return false;
    }

    private void recursivelyIterateThroughTreeAndChangeProcedureNames(Node currNode)
    {
        if(currNode != null)
        {
            if(currNode.getNodeDetail().equals("CALL"))
            {
                findMatchingProcedure(currNode.getChildren().get(0));
            }
            else
            {
                for(int i = 0; i < currNode.getChildren().size(); i++)
                {
                    recursivelyIterateThroughTreeAndChangeProcedureNames(currNode.getChildren().get(i));
                }
            }
        }
    }

    private void findMatchingProcedure(Node callNode)
    {
        Node currNode = this.tree;
        recursivelyIterateToFindMatchingProcedure(currNode, callNode);
    }

    private void recursivelyIterateToFindMatchingProcedure(Node currNode, Node callNode)
    {
        if(currNode != null)
        {
            if(currNode.getNodeDetail().equals("PROC"))
            {
                if(currNode.getChildren().get(1).getNodeDetail().equals(callNode.getNodeDetail()))
                {
                    if(currNode.getRowInTable().getScope().length() == callNode.getRowInTable().getScope().length() ||
                            currNode.getRowInTable().getScope().length() == callNode.getRowInTable().getScope().length() + 1 ||
                            currNode.getRowInTable().getScope().length() == callNode.getRowInTable().getScope().length() + 2)
                    {
                        if(currNode.getRowInTable().getNewName().equals(""))
                        {
                            currNode.getChildren().get(1).getRowInTable().setNewName("p" + procedureNameNumber);
                            callNode.getRowInTable().setNewName("p" + procedureNameNumber);
                            procedureNameNumber++;
                        }
                        else
                        {
                            callNode.getRowInTable().setNewName(currNode.getRowInTable().getNewName());
                        }
                    }
                    else
                    {
                        recursivelyIterateToFindMatchingProcedure(currNode.getChildren().get(2), callNode);
                    }
                }
                else
                {
                    recursivelyIterateToFindMatchingProcedure(currNode.getChildren().get(2), callNode);
                }
            }
            else
            {
                for(int i = 0; i < currNode.getChildren().size(); i++)
                {
                    recursivelyIterateToFindMatchingProcedure(currNode.getChildren().get(i), callNode);
                }
            }
        }
    }

    private void recursivelyIterateThroughTreeAndChangeVariableNames(Node currNode)
    {
        if(currNode.getTypeOfNode().equals("Non-Terminal"))
        {
            if(currNode.getNodeDetail().equals("CALL") || currNode.getNodeDetail().equals("PROC"))
            {
                if(currNode.getNodeDetail().equals("PROC"))
                {
                    recursivelyIterateThroughTreeAndChangeVariableNames(currNode.getChildren().get(2));
                }
            }
            else
            {
                for(int i = 0; i < currNode.getChildren().size(); i++)
                {
                    recursivelyIterateThroughTreeAndChangeVariableNames(currNode.getChildren().get(i));
                }
            }
        }
        else
        {
            if(checkIfTerminalNodeIsAUserDefinedName(currNode.getNodeDetail()))
            {
                String answer = checkIfVariableAlreadyExitsInScope(currNode);
                if(answer.equals(""))
                {
                    currNode.getRowInTable().setNewName("V" + variableNameNumber);
                    variableNameNumber++;
                }
                else
                {
                    currNode.getRowInTable().setNewName(answer);
                }
            }
        }
    }

    private boolean checkIfTerminalNodeIsAUserDefinedName(String terminalNode)
    {
        if(terminalNode.equals("eq") ||
                terminalNode.equals("<") ||
                terminalNode.equals(">") ||
                terminalNode.equals("and") ||
                terminalNode.equals("or") ||
                terminalNode.equals("not") ||
                terminalNode.equals("add") ||
                terminalNode.equals("sub") ||
                terminalNode.equals("mult") ||
                terminalNode.equals("if") ||
                terminalNode.equals("then") ||
                terminalNode.equals("else") ||
                terminalNode.equals("while") ||
                terminalNode.equals("for") ||
                terminalNode.equals("input") ||
                terminalNode.equals("output") ||
                terminalNode.equals("halt") ||
                terminalNode.equals("proc") ||
                terminalNode.charAt(0) == '0' ||
                terminalNode.charAt(0) == '1' ||
                terminalNode.charAt(0) == '2'||
                terminalNode.charAt(0) == '3' ||
                terminalNode.charAt(0) == '4' ||
                terminalNode.charAt(0) == '5' ||
                terminalNode.charAt(0) == '6' ||
                terminalNode.charAt(0) == '7'||
                terminalNode.charAt(0) == '8' ||
                terminalNode.charAt(0) == '9' ||
                terminalNode.charAt(0) == '"')
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    private String checkIfVariableAlreadyExitsInScope(Node currNode)
    {
        for (Row row : Table)
        {
            if (row.getNodeText().equals(currNode.getNodeDetail()))
            {
                if (currNode.getRowInTable().getScope().contains(row.getScope()))
                {
                    if(row.getNewName().equals(""))
                    {
                        return "";
                    }
                    else
                    {
                        return row.getNewName();
                    }
                }
            }
        }
        return "";
    }

    private boolean iterateThroughTreeToFindProc(Node currNode)
    {
        boolean answer = false;
        if(currNode != null)
        {
            if(currNode.getNodeDetail().equals("PROC"))
            {
                Node prevNode = currNode;
                currNode = currNode.getChildren().get(1);
                answer = iterateTreeAndCheckForVariablesWithSameName(currNode.getNodeDetail());
                if(answer)
                {
                    return true;
                }
                else
                {
                    answer = iterateThroughTreeToFindProc(prevNode.getChildren().get(2));
                }
            }
            else
            {
                for(int i = 0; i < currNode.getChildren().size(); i++)
                {
                    Node childNode = currNode.getChildren().get(i);
                    answer = iterateThroughTreeToFindProc(childNode);
                    if(answer)
                    {
                        return true;
                    }
                }
            }
        }
        return answer;
    }

    private boolean iterateTreeAndCheckForVariablesWithSameName(String name)
    {
        Node currNode = tree;
        return recursiveSearchForVariable(currNode, name);
    }

    private boolean recursiveSearchForVariable(Node currNode, String name)
    {
        if(currNode != null)
        {
            if(currNode.getTypeOfNode().equals("Terminal"))
            {
                return currNode.getNodeDetail().equals(name);
            }
            else
            {
                if(currNode.getNodeDetail().equals("CALL") || currNode.getNodeDetail().equals("PROC"))
                {
                    if(currNode.getNodeDetail().equals("PROC"))
                    {
                        return recursiveSearchForVariable(currNode.getChildren().get(2), name);
                    }
                }
                else
                {
                    for(int i = 0; i < currNode.getChildren().size(); i++)
                    {
                        if(recursiveSearchForVariable(currNode.getChildren().get(i), name))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean checkProcNamesInProcTreesAreDifferent(Node currNode, Vector<String> ProcNames)
    {
        for(int i = 0; i < currNode.getChildren().size(); i++)
        {
            if(currNode.getChildren().get(i).getNodeDetail().equals("PROC_DEFS"))
            {
                Node child = currNode.getChildren().get(i);
                Node granChild = child.getChildren().get(0);
                if(!checkIfNameInInList(ProcNames, granChild.getChildren().get(1).getNodeDetail()))
                {
                    ProcNames.add(granChild.getChildren().get(1).getNodeDetail());
                    Vector<String> nextProcNames = new Vector<>();
                    nextProcNames.add(granChild.getChildren().get(1).getNodeDetail());
                    if(checkProcNamesInProcTreesAreDifferent(granChild.getChildren().get(2), nextProcNames))
                    {
                        return true;
                    }
                }
                else
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkIfNameInInList(Vector<String> List, String name)
    {
        for (String s : List)
        {
            if (name.equals(s))
            {
                return true;
            }
        }
        return false;
    }

    private boolean recursivelyCheckForLoopRule1(Node currNode)
    {
        if(currNode != null)
        {
            if(currNode.getNodeDetail().equals("COND_LOOP") && currNode.getChildren().get(0).getNodeDetail().equals("for"))
            {
                Node child1 = currNode.getChildren().get(1);
                Node child2 = currNode.getChildren().get(2);
                Node child3 = currNode.getChildren().get(3);
                Node child3child = child3.getChildren().get(1);
                Node child4 = currNode.getChildren().get(4);

                String loopVarName = child1.getChildren().get(0).getRowInTable().getNewName();

                if(child2.getChildren().get(0).getRowInTable().getNewName().equals(loopVarName) &&
                        child3.getChildren().get(0).getRowInTable().getNewName().equals(loopVarName) &&
                        child3child.getChildren().get(1).getRowInTable().getNewName().equals(loopVarName))
                {
                    for(int i = 4; i < currNode.getChildren().size(); i++)
                    {
                        if(recursivelyCheckForLoopRule1(currNode.getChildren().get(i)))
                        {
                            return true;
                        }
                    }
                    return false;
                }
                else
                {
                    return true;
                }
            }
            else
            {
                for(int i = 0; i < currNode.getChildren().size(); i++)
                {
                    if(recursivelyCheckForLoopRule1(currNode.getChildren().get(i)))
                    {
                        return true;
                    }
                }
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    private boolean recursivelyCheckForLoopRule2(Node currNode)
    {
        if(currNode != null)
        {
            if(currNode.getNodeDetail().equals("COND_LOOP") && currNode.getChildren().get(0).getNodeDetail().equals("for"))
            {
                Node child1 = currNode.getChildren().get(1);
                String loopVarName = child1.getChildren().get(0).getRowInTable().getNewName();

                for(int i = 4; i < currNode.getChildren().size(); i++)
                {
                    if(recursivelyCheckLoopBranchForVar(currNode.getChildren().get(i), loopVarName))
                    {
                        return true;
                    }
                }
                return false;
            }
            else
            {
                for(int i = 0; i < currNode.getChildren().size(); i++)
                {
                    if(recursivelyCheckForLoopRule2(currNode.getChildren().get(i)))
                    {
                        return true;
                    }
                }
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    private boolean recursivelyCheckLoopBranchForVar(Node currNode, String var)
    {
        if(currNode != null)
        {
            if(currNode.getRowInTable().getNewName().equals(var))
            {
                return true;
            }
            else
            {
                for(int i = 0; i < currNode.getChildren().size(); i++)
                {
                    if(recursivelyCheckLoopBranchForVar(currNode.getChildren().get(i), var))
                    {
                        return true;
                    }
                }
                return false;
            }
        }
        else
        {
            return false;
        }
    }
}
