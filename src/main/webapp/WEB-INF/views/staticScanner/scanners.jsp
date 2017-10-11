<%--
  Created by IntelliJ IDEA.
  User: deshani
  Date: 9/19/17
  Time: 9:58 AM
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
            <h1>Static Scan Methods</h1>
            <h4>Select a scanning method/s to start scanning process </h4>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <p>Select scanning method/s and enter email address to send generated reports</p>
                <form action="productUploader.jsp" method="post"
                      enctype="multipart/form-data">
                    <div class="input-group input-group-lg">
                        <span class="input-group-addon">
                                <input type="checkbox" name="chkFindSecBugs" id="chkFindSecBugs">
                            <input type="hidden" name="chkFindSecBugs" value="0">
                        </span>
                        <label class="form-control">Find Security Bugs</label>
                    </div>
                    <br>
                    <div class="input-group input-group-lg">
                        <span class="input-group-addon">
                            <input type="checkbox" name="chkDependencyCheck" id="chkDependencyCheck">
                            <input type="hidden" name="chkDependencyCheck" value="0">
                        </span>
                        <label class="form-control">OWASP Dependency Check</label>
                    </div>
                    <br>
                    <div class="input-group input-group-lg">
                        <span class="input-group-addon"><i class="glyphicon glyphicon-envelope"></i></span>
                        <input name="email" class="form-control" type="email" placeholder="Email Address">
                    </div>
                    <div class="form-group">
                        <label>Container Id</label>
                        <input name="containerId" class="form-control">
                    </div>
                    <button class="btn btn-primary btn-block">Submit</button>
                </form>
            </div>
        </div>
    </div>
</div>
<%@include file="../fragments/footer.jsp" %>
</body>
</html>
