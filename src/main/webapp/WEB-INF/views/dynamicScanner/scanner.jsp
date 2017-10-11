<%--
  Created by IntelliJ IDEA.
  User: deshani
  Date: 10/10/17
  Time: 10:58 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <%@ include file="../fragments/header.html" %>
    <%@include file="../fragments/navBar.jsp" %>
</head>
<body>
<div class="container">
    <div class="row">
        <div class="page-header">
            <h1>Dynamic Scanner</h1>
            <p>Upload a zip file of product or else fill the
                details of already up and running server. Don't forget to enter email address to send the generated
                reports</p>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <h2>Upload a zip file</h2>
                <p>Upload a zip file of the product</p>
                <form action="#" method="post"
                      enctype="multipart/form-data">
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Zip File</span>
                        <input type="file" name="zipFile" id="zipFile" class="form-control">
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Container ID</span>
                        <input name="containerId" class="form-control">
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon"><i class="glyphicon glyphicon-envelope"></i></span>
                        <input name="email" class="form-control" type="email" placeholder="Email Address" required>
                    </div>
                    <br>
                    <button class="btn btn-primary btn-block">Submit and Start Scan</button>
                </form>
            </div>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <h2>Enter Details of Server</h2>
                <p>Enter Details of up and running server</p>
                <form action="#" method="post">
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">IP Address</span>
                        <input name="ipAddress" id="ipAddress" class="form-control">
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Port</span>
                        <input name="port" id="port" class="form-control">
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Container ID</span>
                        <input name="containerId" class="form-control">
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon"><i class="glyphicon glyphicon-envelope"></i></span>
                        <input name="email" class="form-control" type="email" placeholder="Email Address" required>
                    </div>
                    <br>
                    <button class="btn btn-primary btn-block">Submit and Start Scan</button>
                </form>
            </div>
        </div>
    </div>
</div>
<%@include file="../fragments/footer.jsp" %>
</body>
</html>
