public class Parser
{
    private final Token Head;
    private Boolean Error = false;
    private Token ErrorToken = null;
    private boolean ErrorDeclared = false;
    private int nextUid = 0;
    private Node SyntaxTree;

    public Parser(Token Head)
    {
        this.Head = Head;
    }

    public Node parse()
    {
        if(Head == null)
        {
            this.Error = true;
            String errorMsg = "No tokens present.";
            this.ErrorToken = new Token("Parser Error", errorMsg);
            declareError();
        }
        else
        {
            //add prog to tree
            SyntaxTree = new Node(this.nextUid, "Non-Terminal", "PROG");
            this.nextUid++;

            parseCode(this.Head, this.SyntaxTree);
        }

        return this.SyntaxTree;
    }

    private Token parseCode(Token currToken, Node currNode)
    {
        while(instructionCheck(currToken))
        {
            Node newNode = new Node(this.nextUid, "Non-Terminal", "INSTR");
            currNode.addChild(newNode);
            this.nextUid++;

            currToken = decideWhichParseRouteToTake(currToken, newNode);
            if(Error)
            {
                //display error
                return null;
            }
            else if(currToken == null)
            {
                return null;
            }
            else if(currToken.getValue().equals(";"))
            {
                currToken = currToken.getNext();
                if(currToken == null)
                {
                    return null;
                }
            }
            else if(currToken.getValue().equals("}"))
            {
                return currToken;
            }
            else
            {
                this.Error = true;
                String errorMsg = "Grouping character: ; expected after CODE";
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
        }
        return currToken;
    }

    private boolean instructionCheck(Token curr)
    {
        return switch (curr.getType())
                {
                    case "Special Command character", "I/O character", "User defined name" -> true;
                    case "Control structure character" -> !curr.getValue().equals("then") && !curr.getValue().equals("else");
                    default -> false;
                };
    }

    private Token decideWhichParseRouteToTake(Token currToken, Node currNode)
    {
        Node newNode;
        switch (currToken.getType())
        {
            case "Special Command character":
                //add halt instruction to list
                newNode = new Node(this.nextUid, "Terminal", "halt");
                currNode.addChild(newNode);
                this.nextUid++;

                currToken = currToken.getNext();
                break;
            case "I/O character":
                if (currToken.getValue().equals("input"))
                {
                    //add input instruction to list
                    newNode = new Node(this.nextUid, "Non-Terminal", "IO");
                    currNode.addChild(newNode);
                    this.nextUid++;
                    currToken = inputOutputRoute(currToken, newNode, "input");
                } else
                {
                    //add output instruction to list
                    newNode = new Node(this.nextUid, "Non-Terminal", "IO");
                    currNode.addChild(newNode);
                    this.nextUid++;
                    currToken = inputOutputRoute(currToken, newNode, "output");
                }
                break;
            case "User defined name":
                currToken = assignmentRoute(currToken, currNode);
                break;
            case "Control structure character":
                currToken = conditionRoute(currToken, currNode);
                break;
        }
        return currToken;
    }

    private Token inputOutputRoute(Token currToken, Node currNode, String IO)
    {
        Token prevToken = currToken;
        currToken = currToken.getNext();
        if(currToken == null)
        {
            this.Error = true;
            String errorMsg = "Grouping character: ( expected after " + prevToken.getType() + ": " + prevToken.getValue();
            this.ErrorToken = new Token("Parser Error", errorMsg);
            declareError();
            return null;
        }
        if(currToken.getValue().equals("("))
        {
            prevToken = currToken;
            currToken = currToken.getNext();
            if(currToken == null)
            {
                this.Error = true;
                String errorMsg = "User defined name expected after " + prevToken.getType() + ": " + prevToken.getValue();
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
            if(currToken.getType().equals("User defined name"))
            {
                prevToken = currToken;
                currToken = currToken.getNext();
                if(currToken == null)
                {
                    this.Error = true;
                    String errorMsg = "Grouping character: ) expected after " + prevToken.getType() + ": " + prevToken.getValue();
                    this.ErrorToken = new Token("Parser Error", errorMsg);
                    declareError();
                    return null;
                }
                if(currToken.getValue().equals(")"))
                {
                    //add to list
                    Node newNode = new Node(this.nextUid, "Terminal", IO+"(" + prevToken.getValue() + ")");
                    currNode.addChild(newNode);
                    this.nextUid++;
                    currToken = currToken.getNext();
                }
                else
                {
                    this.Error = true;
                    String errorMsg = "Grouping character: ) expected after " + prevToken.getType() + ": " + prevToken.getValue();
                    this.ErrorToken = new Token("Parser Error", errorMsg);
                    declareError();
                    return null;
                }
            }
            else
            {
                this.Error = true;
                String errorMsg = "User defined name expected after " + prevToken.getType() + ": " + prevToken.getValue();
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
        }
        else
        {
            this.Error = true;
            String errorMsg = "Grouping character: ( expected after " + prevToken.getType() + ": " + prevToken.getValue();
            this.ErrorToken = new Token("Parser Error", errorMsg);
            declareError();
            return null;
        }

        return currToken;
    }

    private Token assignmentRoute(Token currToken, Node currNode)
    {
        Node newNode;

        Token prevToken = currToken;
        String assignToValue = prevToken.getValue();
        currToken = currToken.getNext();
        if(currToken == null)
        {
            newNode = new Node(this.nextUid, "Non-Terminal", "CALL");
            currNode.addChild(newNode);
            this.nextUid++;

            currNode = newNode;
            newNode = new Node(this.nextUid, "Terminal", prevToken.getValue());
            currNode.addChild(newNode);
            this.nextUid++;

            return null;
        }
        else if(currToken.getValue().equals("="))
        {
            newNode = new Node(this.nextUid, "Non-Terminal", "ASSIGN");
            currNode.addChild(newNode);
            this.nextUid++;
            currNode = newNode;
            newNode = new Node(this.nextUid, "Terminal", assignToValue);
            currNode.addChild(newNode);
            this.nextUid++;

            prevToken = currToken;
            currToken = currToken.getNext();
            if(currToken == null)
            {
                this.Error = true;
                String errorMsg = "String, Variable or number expression expected after " + prevToken.getType() + ": " + prevToken.getValue();
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
            else if(currToken.getType().equals("Short String") ||
                    currToken.getType().equals("User defined name") ||
                    currToken.getType().equals("Number") ||
                    currToken.getType().equals("Number operators"))
            {
                switch (currToken.getType())
                {
                    case "Short String", "Number", "User defined name" -> {
                        //add short string
                        newNode = new Node(this.nextUid, "Terminal", currToken.getValue());
                        currNode.addChild(newNode);
                        this.nextUid++;

                        currToken = currToken.getNext();
                    }
                    case "Number operators" -> {
                        //number expression
                        newNode = new Node(this.nextUid, "Non-Terminal", "CALC");
                        this.nextUid++;
                        currNode.addChild(newNode);

                        currToken = outerNumberExpression(currToken, newNode);
                    }
                }
            }
            else
            {
                this.Error = true;
                String errorMsg = "String, Variable or number expression expected after " + prevToken.getType() + ": " + prevToken.getValue();
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
        }
        else
        {
            newNode = new Node(this.nextUid, "Non-Terminal", "CALL");
            currNode.addChild(newNode);
            this.nextUid++;

            currNode = newNode;
            newNode = new Node(this.nextUid, "Terminal", prevToken.getValue());
            currNode.addChild(newNode);
            this.nextUid++;
        }
        return currToken;
    }

    private Token outerNumberExpression(Token currToken, Node currNode)
    {
        Token prevToken = currToken;
        currToken = currToken.getNext();
        if(currToken == null)
        {
            this.Error = true;
            String errorMsg = "Grouping character: ( expected after " + prevToken.getType() + ": " + prevToken.getValue();
            this.ErrorToken = new Token("Parser Error", errorMsg);
            declareError();
            return null;
        }
        else if(currToken.getValue().equals("("))
        {
            currToken = innerNumberExpression(currToken, currNode);
            if(currToken == null)
            {
                this.Error = true;
                String errorMsg = "Grouping character: , expected after Number expression";
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
            else if(currToken.getValue().equals(","))
            {
                currToken = innerNumberExpression(currToken, currNode);
                if(currToken == null)
                {
                    this.Error = true;
                    String errorMsg = "Grouping character: ) expected after Number expression";
                    this.ErrorToken = new Token("Parser Error", errorMsg);
                    declareError();
                    return null;
                }
                else if(currToken.getValue().equals(")"))
                {
                    //end of number expression
                    currToken = currToken.getNext();
                }
                else
                {
                    this.Error = true;
                    String errorMsg = "Grouping character: ) expected after Number expression";
                    this.ErrorToken = new Token("Parser Error", errorMsg);
                    declareError();
                    return null;
                }
            }
            else
            {
                this.Error = true;
                String errorMsg = "Grouping character: , expected after Number expression";
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
        }
        else
        {
            this.Error = true;
            String errorMsg = "Grouping character: ( expected after " + prevToken.getType() + ": " + prevToken.getValue();
            this.ErrorToken = new Token("Parser Error", errorMsg);
            declareError();
            return null;
        }
        return currToken;
    }

    private Token innerNumberExpression(Token currToken, Node currNode)
    {
        Node newNode;

        Token prevToken = currToken;
        currToken = currToken.getNext();
        if(currToken == null)
        {
            this.Error = true;
            String errorMsg = "Variable name, Number or Number expression expected after " + prevToken.getType() + ": " + prevToken.getValue();
            this.ErrorToken = new Token("Parser Error", errorMsg);
            declareError();
            return null;
        }
        else if(currToken.getType().equals("User defined name") || currToken.getType().equals("Number") || currToken.getType().equals("Number operators"))
        {
            switch (currToken.getType())
            {
                case "User defined name", "Number" -> {
                    //add variable to tree
                    newNode = new Node(this.nextUid, "Terminal", currToken.getValue());
                    this.nextUid++;
                    currNode.addChild(newNode);
                    currToken = currToken.getNext();
                }
                //add number to tree
                case "Number operators" -> {
                    //
                    newNode = new Node(this.nextUid, "Non-Terminal", "CALC");
                    this.nextUid++;
                    currNode.addChild(newNode);
                    currToken = outerNumberExpression(currToken, newNode);
                }
            }
        }
        else
        {
            this.Error = true;
            String errorMsg = "Variable name, Number or Number expression expected after " + prevToken.getType() + ": " + prevToken.getValue();
            this.ErrorToken = new Token("Parser Error", errorMsg);
            declareError();
            return null;
        }
        return currToken;
    }

    private Token conditionRoute(Token currToken, Node currNode)
    {
        switch(currToken.getValue())
        {
            case "if" -> {
                Node newNode = new Node(this.nextUid, "Non-Terminal", "COND_BRANCH");
                this.nextUid++;
                currNode.addChild(newNode);

                currNode = newNode;
                newNode = new Node(this.nextUid, "Terminal", "if");
                this.nextUid++;
                currNode.addChild(newNode);
                currToken = ifRoute(currToken, currNode);
            }
            case "while" -> {
                Node newNode = new Node(this.nextUid, "Non-Terminal", "COND_LOOP");
                this.nextUid++;
                currNode.addChild(newNode);

                currNode = newNode;
                newNode = new Node(this.nextUid, "Terminal", "while");
                this.nextUid++;
                currNode.addChild(newNode);
                currToken = whileRoute(currToken, currNode);
            }
            case "for" -> {
                Node newNode = new Node(this.nextUid, "Non-Terminal", "COND_LOOP");
                this.nextUid++;
                currNode.addChild(newNode);

                currNode = newNode;
                newNode = new Node(this.nextUid, "Terminal", "for");
                this.nextUid++;
                currNode.addChild(newNode);
                currToken = forRoute(currToken, currNode);
            }
        }
        return currToken;
    }

    private Token ifRoute(Token currToken, Node currNode)
    {
        Node newNode;
        Token prevToken = currToken;
        currToken = currToken.getNext();
        if(currToken == null)
        {
            this.Error = true;
            String errorMsg = "Grouping character: ( expected after " + prevToken.getType() + ": " + prevToken.getValue();
            this.ErrorToken = new Token("Parser Error", errorMsg);
            declareError();
            return null;
        }
        else if(currToken.getValue().equals("("))
        {
            newNode = new Node(this.nextUid, "Non-Terminal", "BOOL");
            this.nextUid++;
            currNode.addChild(newNode);

            currToken = outerBoolRoute(currToken, newNode);
            if(currToken == null)
            {
                this.Error = true;
                String errorMsg = "Grouping character: ) expected.";
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
            else if(currToken.getValue().equals(")"))
            {
                prevToken = currToken;
                currToken = currToken.getNext();
                if(currToken == null)
                {
                    this.Error = true;
                    String errorMsg = "Control structure keyword: \"then\" expected.";
                    this.ErrorToken = new Token("Parser Error", errorMsg);
                    declareError();
                    return null;
                }
                else if(currToken.getValue().equals("then"))
                {
                    newNode = new Node(this.nextUid, "Terminal", "then");
                    this.nextUid++;
                    currNode.addChild(newNode);

                    prevToken = currToken;
                    currToken = currToken.getNext();
                    if(currToken == null)
                    {
                        this.Error = true;
                        String errorMsg = "Grouping character: \"{\" expected after Control structure keyword: \"then\".";
                        this.ErrorToken = new Token("Parser Error", errorMsg);
                        declareError();
                        return null;
                    }
                    else if(currToken.getValue().equals("{"))
                    {
                        prevToken = currToken;
                        currToken = currToken.getNext();
                        if(currToken == null)
                        {
                            this.Error = true;
                            String errorMsg = "Expected CODE after Grouping character: \"{\"";
                            this.ErrorToken = new Token("Parser Error", errorMsg);
                            declareError();
                            return null;
                        }
                        else
                        {
                            currToken = parseCode(currToken, currNode);
                            if(currToken == null)
                            {
                                this.Error = true;
                                String errorMsg = "Grouping character: \"}\" expected after CODE";
                                this.ErrorToken = new Token("Parser Error", errorMsg);
                                declareError();
                                return null;
                            }
                            else if(currToken.getValue().equals("}"))
                            {
                                currToken = currToken.getNext();
                            }
                            else
                            {
                                this.Error = true;
                                String errorMsg = "Grouping character: \"}\" expected after CODE";
                                this.ErrorToken = new Token("Parser Error", errorMsg);
                                declareError();
                                return null;
                            }
                        }
                    }
                    else
                    {
                        this.Error = true;
                        String errorMsg = "Grouping character: \"{\" expected after Control structure keyword: \"then\".";
                        this.ErrorToken = new Token("Parser Error", errorMsg);
                        declareError();
                        return null;
                    }
                }
                else
                {
                    this.Error = true;
                    String errorMsg = "Control structure keyword: \"then\" expected.";
                    this.ErrorToken = new Token("Parser Error", errorMsg);
                    declareError();
                    return null;
                }
            }
            else
            {
                this.Error = true;
                String errorMsg = "Grouping character: ) expected.";
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
        }
        else
        {
            this.Error = true;
            String errorMsg = "Grouping character: ( expected after " + prevToken.getType() + ": " + prevToken.getValue();
            this.ErrorToken = new Token("Parser Error", errorMsg);
            declareError();
            return null;
        }
        return currToken;
    }

    private Token whileRoute(Token currToken, Node currNode)
    {
        return null;
    }

    private Token forRoute(Token currToken, Node currNode)
    {
        return null;
    }

    private Token outerBoolRoute(Token currToken, Node currNode)
    {
        Node newNode;
        Token prevToken = currToken;
        currToken = currToken.getNext();
        if(currToken == null)
        {
            this.Error = true;
            String errorMsg = "Start of Boolean expression expected.";
            this.ErrorToken = new Token("Parser Error", errorMsg);
            declareError();
            return null;
        }
        else if(currToken.getType().equals("Comparison character") ||
                currToken.getType().equals("Boolean character") ||
                currToken.getValue().equals("("))
        {
            //Boolean expression

            if(currToken.getType().equals("Comparison character"))
            {
                newNode = new Node(this.nextUid, "Terminal", "eq");
                this.nextUid++;
                currNode.addChild(newNode);

                currToken = innerComparisonBoolRoute(currToken, currNode);
            }
            else if(currToken.getType().equals("Boolean character"))
            {
                newNode = new Node(this.nextUid, "Terminal", currToken.getValue());
                this.nextUid++;
                currNode.addChild(newNode);

                if(currToken.getValue().equals("not"))
                {
                    currToken = innerBooleanBoolRoute(currToken, currNode);
                    return currToken;
                }
                else
                {
                    currToken = innerBooleanBoolRoute(currToken, currNode);
                }
            }
            else
            {
                currToken = innerBoolRoute(currToken, currNode);
            }

            if(currToken == null)
            {
                this.Error = true;
                String errorMsg = "Grouping character: ) expected.";
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
            else if(currToken.getValue().equals(")"))
            {
                currToken = currToken.getNext();
            }
            else
            {
                this.Error = true;
                String errorMsg = "Grouping character: ) expected.";
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
        }
        else
        {
            this.Error = true;
            String errorMsg = "Start of Boolean expression expected.";
            this.ErrorToken = new Token("Parser Error", errorMsg);
            declareError();
            return null;
        }
        return currToken;
    }

    private Token innerComparisonBoolRoute(Token currToken, Node currNode)
    {
        Node newNode;
        Token prevToken = currToken;
        currToken = currToken.getNext();
        if(currToken == null)
        {
            this.Error = true;
            String errorMsg = "Grouping character: ( expected after " + prevToken.getType() + ": " + prevToken.getValue();
            this.ErrorToken = new Token("Parser Error", errorMsg);
            declareError();
            return null;
        }
        else if(currToken.getValue().equals("("))
        {
            prevToken = currToken;
            currToken = currToken.getNext();
            if(currToken == null)
            {
                this.Error = true;
                String errorMsg = "Expected Variable, Boolean expression or Number expression.";
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
            else if(currToken.getType().equals("User defined name") ||
                    currToken.getType().equals("Number") ||
                    currToken.getType().equals("Number operators") ||
                    currToken.getType().equals("Comparison character") ||
                    currToken.getType().equals("Boolean character") ||
                    currToken.getValue().equals("("))
            {
                switch (currToken.getType())
                {
                    case "User defined name" -> {
                        newNode = new Node(this.nextUid, "Terminal", currToken.getValue());
                        this.nextUid++;
                        currNode.addChild(newNode);
                        prevToken = currToken;
                        currToken = currToken.getNext();
                        if (currToken == null)
                        {
                            this.Error = true;
                            String errorMsg = "Grouping character: , expected after " + prevToken.getType() + ": " + prevToken.getValue();
                            this.ErrorToken = new Token("Parser Error", errorMsg);
                            declareError();
                            return null;
                        } else if (currToken.getValue().equals(","))
                        {
                            prevToken = currToken;
                            currToken = currToken.getNext();
                            if (currToken == null)
                            {
                                this.Error = true;
                                String errorMsg = "Expected User defined name";
                                this.ErrorToken = new Token("Parser Error", errorMsg);
                                declareError();
                                return null;
                            }
                            else if (currToken.getType().equals("User defined name"))
                            {
                                newNode = new Node(this.nextUid, "Terminal", currToken.getValue());
                                this.nextUid++;
                                currNode.addChild(newNode);

                                currToken = currToken.getNext();
                            } else
                            {
                                this.Error = true;
                                String errorMsg = "Expected User defined name";
                                this.ErrorToken = new Token("Parser Error", errorMsg);
                                declareError();
                                return null;
                            }
                        } else
                        {
                            this.Error = true;
                            String errorMsg = "Grouping character: , expected after " + prevToken.getType() + ": " + prevToken.getValue();
                            this.ErrorToken = new Token("Parser Error", errorMsg);
                            declareError();
                            return null;
                        }
                    }
                    case "Number" -> {
                        newNode = new Node(this.nextUid, "Terminal", currToken.getValue());
                        this.nextUid++;
                        currNode.addChild(newNode);
                        prevToken = currToken;
                        currToken = currToken.getNext();
                        if (currToken == null)
                        {
                            this.Error = true;
                            String errorMsg = "Grouping character: , expected after " + prevToken.getType() + ": " + prevToken.getValue();
                            this.ErrorToken = new Token("Parser Error", errorMsg);
                            declareError();
                            return null;
                        } else if (currToken.getValue().equals(","))
                        {
                            prevToken = currToken;
                            currToken = currToken.getNext();
                            if (currToken == null)
                            {
                                this.Error = true;
                                String errorMsg = "Expected Number";
                                this.ErrorToken = new Token("Parser Error", errorMsg);
                                declareError();
                                return null;
                            }
                            else if (currToken.getType().equals("Number"))
                            {
                                newNode = new Node(this.nextUid, "Terminal", currToken.getValue());
                                this.nextUid++;
                                currNode.addChild(newNode);

                                currToken = currToken.getNext();
                            }
                            else
                            {
                                this.Error = true;
                                String errorMsg = "Expected Number";
                                this.ErrorToken = new Token("Parser Error", errorMsg);
                                declareError();
                                return null;
                            }
                        } else
                        {
                            this.Error = true;
                            String errorMsg = "Grouping character: , expected after " + prevToken.getType() + ": " + prevToken.getValue();
                            this.ErrorToken = new Token("Parser Error", errorMsg);
                            declareError();
                            return null;
                        }
                    }
                    case "Number operators" -> {
                        newNode = new Node(this.nextUid, "Non-Terminal", "CALC");
                        this.nextUid++;
                        currNode.addChild(newNode);
                        currToken = outerNumberExpression(currToken, newNode);
                        if (currToken == null)
                        {
                            this.Error = true;
                            String errorMsg = "Grouping character: , expected after " + prevToken.getType() + ": " + prevToken.getValue();
                            this.ErrorToken = new Token("Parser Error", errorMsg);
                            declareError();
                            return null;
                        } else if (currToken.getValue().equals(","))
                        {
                            prevToken = currToken;
                            currToken = currToken.getNext();
                            if (currToken == null)
                            {
                                this.Error = true;
                                String errorMsg = "Expected Number expression";
                                this.ErrorToken = new Token("Parser Error", errorMsg);
                                declareError();
                                return null;
                            }
                            else if (currToken.getType().equals("User defined name") || currToken.getType().equals("Number"))
                            {
                                newNode = new Node(this.nextUid, "Terminal", currToken.getValue());
                                this.nextUid++;
                                currNode.addChild(newNode);

                                currToken = currToken.getNext();
                            }
                            else if (currToken.getType().equals("Number operators"))
                            {
                                newNode = new Node(this.nextUid, "Non-Terminal", "CALC");
                                this.nextUid++;
                                currNode.addChild(newNode);

                                currToken = outerNumberExpression(currToken, newNode);
                            }
                            else
                            {
                                this.Error = true;
                                String errorMsg = "Expected Number expression";
                                this.ErrorToken = new Token("Parser Error", errorMsg);
                                declareError();
                                return null;
                            }
                        }
                        else
                        {
                            this.Error = true;
                            String errorMsg = "Grouping character: , expected after " + prevToken.getType() + ": " + prevToken.getValue();
                            this.ErrorToken = new Token("Parser Error", errorMsg);
                            declareError();
                            return null;
                        }
                    }
                    case "Comparison character", "Boolean character", "Grouping character" -> {
                        currToken = prevToken;
                        newNode = new Node(this.nextUid, "Non-Terminal", "BOOL");
                        this.nextUid++;
                        currNode.addChild(newNode);

                        currToken = outerBoolRoute(currToken, newNode);
                        if(currToken == null)
                        {
                            this.Error = true;
                            String errorMsg = "Expected Grouping character: , after Boolean expression.";
                            this.ErrorToken = new Token("Parser Error", errorMsg);
                            declareError();
                            return null;
                        }
                        else if(currToken.getValue().equals(","))
                        {
                            newNode = new Node(this.nextUid, "Non-Terminal", "BOOL");
                            this.nextUid++;
                            currNode.addChild(newNode);

                            currToken = outerBoolRoute(currToken, newNode);
                        }
                        else
                        {
                            this.Error = true;
                            String errorMsg = "Expected Grouping character: , after Boolean expression.";
                            this.ErrorToken = new Token("Parser Error", errorMsg);
                            declareError();
                            return null;
                        }
                    }
                }
            }
            else
            {
                this.Error = true;
                String errorMsg = "Expected Variable, Boolean expression or Number expression.";
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
        }
        else
        {
            this.Error = true;
            String errorMsg = "Grouping character: ( expected after " + prevToken.getType() + ": " + prevToken.getValue();
            this.ErrorToken = new Token("Parser Error", errorMsg);
            declareError();
            return null;
        }
        return currToken;
    }

    private Token innerBooleanBoolRoute(Token currToken, Node currNode)
    {
        Node newNode;
        Token prevToken;

        if(currToken.getValue().equals("not"))
        {
            prevToken = currToken;
            currToken = currToken.getNext();
            if(currToken == null)
            {
                this.Error = true;
                String errorMsg = "Boolean expression expected after " + prevToken.getType() + ": " + prevToken.getValue();
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
            else if(currToken.getType().equals("Comparison character") ||
                    currToken.getType().equals("Boolean character") ||
                    currToken.getValue().equals("("))
            {
                currToken = prevToken;
                newNode = new Node(this.nextUid, "Non-Terminal", "BOOL");
                this.nextUid++;
                currNode.addChild(newNode);

                currToken = outerBoolRoute(currToken, newNode);
            }
            else
            {
                this.Error = true;
                String errorMsg = "Boolean expression expected after " + prevToken.getType() + ": " + prevToken.getValue();
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
        }
        else if(currToken.getValue().equals("and") || currToken.getValue().equals("or"))
        {
            prevToken = currToken;
            currToken = currToken.getNext();
            if(currToken == null)
            {
                this.Error = true;
                String errorMsg = "Grouping character: ( expected after " + prevToken.getType() + ": " + prevToken.getValue();
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
            else if(currToken.getValue().equals("("))
            {
                newNode = new Node(this.nextUid, "Non-Terminal", "BOOL");
                this.nextUid++;
                currNode.addChild(newNode);
                currToken = outerBoolRoute(currToken, newNode);
                if(currToken == null)
                {
                    this.Error = true;
                    String errorMsg = "Grouping character: \",\" expected after Boolean expression.";
                    this.ErrorToken = new Token("Parser Error", errorMsg);
                    declareError();
                    return null;
                }
                else if(currToken.getValue().equals(","))
                {
                    newNode = new Node(this.nextUid, "Non-Terminal", "BOOL");
                    this.nextUid++;
                    currNode.addChild(newNode);
                    currToken = outerBoolRoute(currToken, newNode);
                }
                else
                {
                    this.Error = true;
                    String errorMsg = "Grouping character: \",\" expected after Boolean expression.";
                    this.ErrorToken = new Token("Parser Error", errorMsg);
                    declareError();
                    return null;
                }
            }
            else
            {
                this.Error = true;
                String errorMsg = "Grouping character: ( expected after " + prevToken.getType() + ": " + prevToken.getValue();
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
        }
        return currToken;
    }

    private Token innerBoolRoute(Token currToken, Node currNode)
    {
        Node newNode;
        Token prevToken = currToken;
        currToken = currToken.getNext();
        if(currToken == null)
        {
            this.Error = true;
            String errorMsg = "User defined name expected after " + prevToken.getType() + ": " + prevToken.getValue() + ".";
            this.ErrorToken = new Token("Parser Error", errorMsg);
            declareError();
            return null;
        }
        else if(currToken.getType().equals("User defined name"))
        {
            newNode = new Node(this.nextUid, "Terminal", currToken.getValue());
            this.nextUid++;
            currNode.addChild(newNode);

            prevToken = currToken;
            currToken = currToken.getNext();
            if(currToken == null)
            {
                this.Error = true;
                String errorMsg = "Comparison character: \"<\" or \">\" expected after " + prevToken.getType() + ": " + prevToken.getValue() + ".";
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
            else if(currToken.getValue().equals("<") || currToken.getValue().equals(">"))
            {
                newNode = new Node(this.nextUid, "Terminal", currToken.getValue());
                this.nextUid++;
                currNode.addChild(newNode);

                prevToken = currToken;
                currToken = currToken.getNext();
                if(currToken == null)
                {
                    this.Error = true;
                    String errorMsg = "User defined name expected after " + prevToken.getType() + ": " + prevToken.getValue() + ".";
                    this.ErrorToken = new Token("Parser Error", errorMsg);
                    declareError();
                    return null;
                }
                else if(currToken.getType().equals("User defined name"))
                {
                    newNode = new Node(this.nextUid, "Terminal", currToken.getValue());
                    this.nextUid++;
                    currNode.addChild(newNode);

                    currToken = currToken.getNext();
                }
                else
                {
                    this.Error = true;
                    String errorMsg = "User defined name expected after " + prevToken.getType() + ": " + prevToken.getValue() + ".";
                    this.ErrorToken = new Token("Parser Error", errorMsg);
                    declareError();
                    return null;
                }
            }
            else
            {
                this.Error = true;
                String errorMsg = "Comparison character: \"<\" or \">\" expected after " + prevToken.getType() + ": " + prevToken.getValue() + ".";
                this.ErrorToken = new Token("Parser Error", errorMsg);
                declareError();
                return null;
            }
        }
        else
        {
            this.Error = true;
            String errorMsg = "User defined name expected after " + prevToken.getType() + ": " + prevToken.getValue() + ".";
            this.ErrorToken = new Token("Parser Error", errorMsg);
            declareError();
            return null;
        }
        return currToken;
    }

    private void declareError()
    {
        if(!this.ErrorDeclared)
        {
            this.ErrorDeclared = true;
            this.SyntaxTree = new Node(-1, this.ErrorToken.getType(), this.ErrorToken.getValue());
        }
    }
}
