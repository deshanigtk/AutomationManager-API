<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="fragments/header.html" %>
</head>
<body>
<%@include file="fragments/navBar.jsp" %>
<div class="container">
    <div class="row">
        <div class="page-header">
            <h1>Scanners</h1>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <h2>Static Scanner</h2>
                <p>This scanner will accept a zip file of the source code of the product, or else a GitHub url, with
                    specific branches and tags. Then Static Scanner can start scanning your source code using
                    FindSecBugs or/and OWASP Dependency Check.</p>
                <form action="staticScanner/started" method="post">
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">User ID</span>
                        <input name="userId" class="form-control" placeholder="Enter user ID" required>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">IP Address</span>
                        <input name="ipAddress" class="form-control"
                               placeholder="Please enter IP Address to host container" required>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Host Port</span>
                        <input name="hostPort" class="form-control" placeholder="Please enter host port" required>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Container Port</span>
                        <input name="containerPort" class="form-control" placeholder="Please enter container port"
                               required>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Test Name</span>
                        <input name="name" class="form-control" placeholder="Please enter a name to test" required>
                    </div>
                    <br>
                    <button name="btnStartStaticScanner" class="btn btn-primary btn-block">
                        Click here to start Static Scanner
                    </button>
                </form>
            </div>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <h2>Dynamic Scanner</h2>
                <p>This scanner will accept a zip file of the product, or else a url of already running server. Then
                    Dynamic Scanner can start scanning your up and running product using OWASP Zed Attack
                    proxy(ZAP).</p>
                <form action="dynamicScanner/started" method="post">
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">User ID</span>
                        <input name="userId" class="form-control" placeholder="Enter user ID" required>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">IP Address</span>
                        <input name="ipAddress" class="form-control"
                               placeholder="Please enter IP Address to host container" required>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Host Port</span>
                        <input name="hostPort" class="form-control" placeholder="Please enter host port" required>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Container Port</span>
                        <input name="containerPort" class="form-control" placeholder="Please enter container port"
                               required>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Test Name</span>
                        <input name="name" class="form-control" placeholder="Please enter a name to test" required>
                    </div>
                    <br>
                    <button name="btnStartDynamicScanner" class="btn btn-primary btn-block">
                        Click here to start Dynamic Scanner
                    </button>
                </form>
            </div>

        </div>
    </div>
    <%@include file="fragments/footer.jsp" %>
    <%@ include file="fragments/styles.html" %>
</body>

</html>