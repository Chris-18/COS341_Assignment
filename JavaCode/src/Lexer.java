import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer
{
    private static Token Head = null;
    private static Token Tail = null;
    private static Boolean Error = false;
    private static String ErrorMsg = "";

    public static void main(String[] args)
    {
        try
        {
            String data = "";
            String regex = "";
            Pattern p;
            Matcher m;

            File myFile = new File(args[0]);
            Scanner myReader = new Scanner(myFile);
            while (myReader.hasNextLine())
            {
                data += myReader.nextLine() + "\n";
            }
            myReader.close();

            data = data.substring(0,data.length()-1);
            //System.out.println(data + "\n");

            int pos = 0;
            while(pos < data.length())
            {
                String c = String.valueOf(data.charAt(pos));
                regex = "-|[a-z0-9 {}(),.;\"<>=\n]";
                p = Pattern.compile(regex);
                m = p.matcher(c);
                if(m.find())
                {
                    if(c.equals("\n"))
                    {
                        //System.out.println("Newline character");
                        pos++;
                        continue;
                    }

                    if(c.equals(" "))
                    {
                        //space identified, do nothing
                        //System.out.println("Space: " + c  + " : end");
                        pos++;
                        continue;
                    }

                    regex = "[<>]";
                    p = Pattern.compile(regex);
                    m = p.matcher(c);
                    if(m.find())
                    {
                        addToken("Comparison character", c);
                        //System.out.println("Comparison character: " + c);
                        pos++;
                        continue;
                    }

                    regex = "[{}(),;]";
                    p = Pattern.compile(regex);
                    m = p.matcher(c);
                    if(m.find())
                    {
                        addToken("Grouping character", c);
                        //System.out.println("Grouping character: " + c);
                        pos++;
                        continue;
                    }

                    regex = "[=]";
                    p = Pattern.compile(regex);
                    m = p.matcher(c);
                    if(m.find())
                    {
                        addToken("Assignment character", c);
                        //System.out.println("Assignment character: " + c);
                        pos++;
                        continue;
                    }

                    regex = "-|[0-9]";
                    p = Pattern.compile(regex);
                    m = p.matcher(c);
                    if(m.find())
                    {
                        pos = ScanForWholeNumber(pos, data);
                        if(pos == -1)
                        {
                            break;
                        }
                        continue;
                    }

                    regex = "\"";
                    p = Pattern.compile(regex);
                    m = p.matcher(c);
                    if(m.find())
                    {
                        pos = ScanForShortString(pos, data);
                        if(pos == -1)
                        {
                            break;
                        }
                        continue;
                    }

                    regex = "[a-z]";
                    p = Pattern.compile(regex);
                    m = p.matcher(c);
                    if(m.find())
                    {
                        pos = ScanForVariableName(pos, data);
                        if(pos == -1)
                        {
                            break;
                        }
                        continue;
                    }
                }
                else
                {
                    Error = true;
                    ErrorMsg = c + " = illegal character. Scanning aborted.";
                    break;
                }
                pos++;
            }

            if(Error)
            {
                System.out.println("Lexical Error: " + ErrorMsg);
            }
            else
            {
                String result = getTokens();
                System.out.println(result);
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static int ScanForWholeNumber(int pos, String data)
    {
        String result = String.valueOf(data.charAt(pos));
        String regex = "[0-9]";
        Pattern p = Pattern.compile(regex);
        Matcher m;
        if(pos+1 < data.length())
        {
            pos++;
            m = p.matcher(String.valueOf(data.charAt(pos)));
            while(m.find())
            {
                result += String.valueOf(data.charAt(pos));
                pos++;
                if(pos == data.length())
                {
                    break;
                }
                else
                {
                    m = p.matcher(String.valueOf(data.charAt(pos)));
                }
            }
        }

        if(result.equals("-"))
        {
            Error = true;
            ErrorMsg = result + " = illegal character. Scanning aborted.";
            pos = -1;
        }
        else
        {
            regex = "(^0$)|(^[1-9]{1}[0-9]*)|(^-[1-9]{1}[0-9]*)";
            p = Pattern.compile(regex);
            m = p.matcher(result);
            if(m.find())
            {
                addToken("Number", result);
                //System.out.println("Number: " + result);
            }
            else
            {
                Error = true;
                ErrorMsg = result + " = Invalid Integer. Scanning Aborted.";
                pos = -1;
            }
        }
        return pos;
    }

    private static int ScanForShortString(int pos, String data)
    {
        if(pos+1 < data.length())
        {
            pos++;
            String result = "";
            int count = 0;
            while(count <= 8)
            {
                String nextLetter = String.valueOf(data.charAt(pos));
                if(nextLetter.equals("\""))
                {
                    addToken("Short String",result);
                    pos++;
                    break;
                }

                String regex = "[ a-z0-9\n]";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(nextLetter);
                if(m.find())
                {
                    if(nextLetter.equals("\n"))
                    {
                        pos++;
                        break;
                    }
                    count++;
                    pos++;
                    if(pos == data.length())
                    {
                        Error = true;
                        ErrorMsg = "Invalid: No closing quotation mark. Scanning Aborted.";
                        pos = -1;
                        break;
                    }
                    else if(count == 9)
                    {
                        Error = true;
                        ErrorMsg = "\"" + result + " = string too long. Scanning Aborted.";
                        pos = -1;
                    }
                    else
                    {
                        result += nextLetter;
                    }
                }
                else
                {
                    Error = true;
                    ErrorMsg = nextLetter + " is not a valid short String character. Scanning Aborted.";
                    pos = -1;
                    break;
                }
            }
        }
        else
        {
            Error = true;
            ErrorMsg = "Invalid: No closing quotation mark. Scanning Aborted.";
            pos = -1;
        }
        return pos;
    }

    private static int ScanForVariableName(int pos, String data)
    {
        String result = String.valueOf(data.charAt(pos));
        if(pos+1 < data.length())
        {
            pos++;
            String nextLetter = String.valueOf(data.charAt(pos));
            String regex = "[a-z0-9]";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(nextLetter);
            while(m.find())
            {
                result += nextLetter;
                pos++;
                if(pos < data.length())
                {
                    nextLetter = String.valueOf(data.charAt(pos));
                    m = p.matcher(nextLetter);
                }
                else
                {
                    break;
                }
            }
            regex = "(^eq$)|(^and$)|(^or$)|(^not$)|(^add$)|(^sub$)|(^mult$)|(^if$)|(^then$)|(^while$)|(^for$)|(^else$)|(^input$)|(^output$)|(^halt$)|(^proc$)";
            p = Pattern.compile(regex);
            m = p.matcher(result);
            if(m.find())
            {
                assignToAppropriateGroup(result);
            }
            else
            {
                addToken("Variable name", result);
            }
        }
        else
        {
            String regex = "(^eq$)|(^and$)|(^or$)|(^not$)|(^add$)|(^sub$)|(^mult$)|(^if$)|(^then$)|(^while$)|(^for$)|(^else$)|(^input$)|(^output$)|(^halt$)|(^proc$)";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(result);
            if(m.find())
            {
                assignToAppropriateGroup(result);
            }
            else
            {
                addToken("Variable name", result);
            }
            pos++;
        }
        return pos;
    }

    private static void assignToAppropriateGroup(String result)
    {
        String regex = "(^eq$)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(result);
        if(m.find())
        {
            addToken("Comparison character", result);
        }

        regex = "(^and$)|(^or$)|(^not$)";
        p = Pattern.compile(regex);
        m = p.matcher(result);
        if(m.find())
        {
            addToken("Boolean character", result);
        }

        regex = "(^add$)|(^sub$)|(^mult$)";
        p = Pattern.compile(regex);
        m = p.matcher(result);
        if(m.find())
        {
            addToken("Number character", result);
        }

        regex = "(^if$)|(^then$)|(^while$)|(^for$)|(^else$)";
        p = Pattern.compile(regex);
        m = p.matcher(result);
        if(m.find())
        {
            addToken("Control structure character", result);
        }

        regex = "(^input$)|(^output$)";
        p = Pattern.compile(regex);
        m = p.matcher(result);
        if(m.find())
        {
            addToken("I/O character", result);
        }

        regex = "(^halt$)";
        p = Pattern.compile(regex);
        m = p.matcher(result);
        if(m.find())
        {
            addToken("Special Command character", result);
        }

        regex = "(^proc$)";
        p = Pattern.compile(regex);
        m = p.matcher(result);
        if(m.find())
        {
            addToken("Procedure definition character", result);
        }
    }

    private static void addToken(String Type, String Value)
    {
        Token newToken = new Token(Type, Value);
        if(Head == null)
        {
            Head = newToken;
            Tail = newToken;
        }
        else
        {
            Tail.setNext(newToken);
            Tail = newToken;
        }
    }

    private static String getTokens()
    {
        String result = "";

        if(Head == null)
        {
            return result;
        }
        else
        {
            Token curr = Head;
            result += "(Type: " + curr.getType() + " / Value: \"" + curr.getValue() + "\")";
            while(curr.getNext() != null)
            {
                curr = curr.getNext();
                result += " -> (Type: " + curr.getType() + " / Value: \"" + curr.getValue() + "\")";
            }
            return result;
        }
    }
}
