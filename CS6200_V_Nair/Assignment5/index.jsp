
<html>
    <head>
        <title>Relevance of documents</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
    </head>
    <body>
        <div class="panel panel-default">
            <div class="panel-body">
                <center><h1>Generate QREL file</h1></center>
            </div>
        </div>
        <div class ='panel panel-defualt  center-align'>
            <div class="panel-body" style='width:50%; margin-left:25%;'>
                <div class="form-group">
                    <form action ="DisplayResults" method="post">
                        <label for="assessorID">Enter the Assessor ID</label>
                        <input type="text" class="form-control" id ="assessorID" name ="assessorID" placeholder="Enter the assessor's ID">
                        <label for="queryID">Enter the Query ID</label>
                        <input type="text" class="form-control" id ="queryID" name ="queryID" placeholder="Enter the query ID">
                        <label for="query">Enter the query here</label>
                        <input type="text" class="form-control" id ="query" name ="query" placeholder="Enter the query here">
                        <br>
                        <button type="submit"  class='btn btn-default'>Submit</button>
                    </form>
                    
                </div>
            </div>
        </div>
    </body>
</html>
