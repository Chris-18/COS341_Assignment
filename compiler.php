<?php
    $textFile = $_FILES["myfile"];

    $FileName = $textFile['name'];
    $FileType = $textFile["type"];
    $fileTmpName = $textFile["tmp_name"];
    $fileError =$textFile["error"];
    $fileSize =$textFile["size"];

    $java = "";
    $results = "";

    if($fileError === 0)
    {
        $executableCommand = 'java Lexer '.$fileTmpName;
        $java = exec(''.$executableCommand);
    }
    else
    {
        $results = "The file could not be uploaded.";
    }

    $html = 
    '
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Processed Output</title>
    
        <!--BootStrap required scripts-->
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.min.js"></script>
    </head>
    <body>
        <div class="container">
            <div class="jumbotron">
                <h1>Compiler Contruction</h1>      
                <p>Christian Still, webserver used to demo practicals for COS341.</p>
            </div>

            <nav class="navbar navbar-expand-sm bg-dark navbar-dark justify-content-center">
                <ul class="navbar-nav">
                <li class="nav-item">
                    <a class="nav-link" href="./index.html">Home</a>
                </li>
                </ul>
            </nav>
            <br>
            
            <center>
                <div>
                    <h3>
                        Processed Output:
                    </h3>
                </div>
                <p>
    ' . $results . '<br>' . $java .'
                </p>
            </center>
        </div>
    </body>
    </html>
    ';

    print($html);
?>

