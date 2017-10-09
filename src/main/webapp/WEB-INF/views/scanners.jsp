<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="fragments/header.jsp" %>
    <%@include file="fragments/navBar.jsp" %>
</head>
<body>
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
                    <input type="hidden" name="userId" value="oiooo"/>
                    <input type="hidden" name="ipAddress" value="0.0.0.0">
                    <input type="hidden" name="hostPort" value=8081>
                    <input type="hidden" name="containerPort" value=8081>
                    <button name="btnStartStaticScanner" class="btn btn-default btn-lg" onclick="this.disabled=true">
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
                <form action="dynamicScanner/start" method="post">
                    <input type="hidden" name="userId" value="oiooo"/>
                    <input type="hidden" name="ipAddress" value="0.0.0.0">
                    <input type="hidden" name="hostPort" value=8082>
                    <input type="hidden" name="containerPort" value=8082>
                    <button name="btnStartDynamicScanner" class="btn btn-default btn-lg" onclick="this.disabled=true">
                        Click here to start Dynamic Scanner
                    </button>
                </form>
            </div>

        </div>
    </div>
    <%@include file="fragments/footer.jsp" %>
</body>

</html>