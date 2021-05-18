import java.util.Vector;

public class Semantics
{
    private Node tree = null;
    private Vector<Row> Table;
    private static int variableNameNumber = 0;

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

    public void changeTreeVariableNames()
    {
        Node currNode = tree;
        recursivelyIterateThroughTreeAndChangeVariableNames(currNode);
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
}
