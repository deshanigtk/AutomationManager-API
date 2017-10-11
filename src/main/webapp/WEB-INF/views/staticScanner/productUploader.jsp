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
            <h1>Product Source Code Uploader</h1>
            <h4>Upload the source code in one of the following ways</h4>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <h2>Upload a zip file</h2>
                <p>Upload a zip file of the product source code
                ${chkFindSecBugs}</p>
                <form action="/staticScanner/productUploaded" method="post"
                      enctype="multipart/form-data">
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Zip File</span>
                        <input type="file" name="zipFile" id="zipFile" class="form-control">
                    </div>
                    <br>
                    <div class="dropdown">
                        <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            Dropdown button
                        </button>
                        <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                            <a class="dropdown-item" href="#">Action</a>
                            <a class="dropdown-item" href="#">Another action</a>
                            <a class="dropdown-item" href="#">Something else here</a>
                        </div>
                    </div>
                    <br>
                    <input name="containerId" value="${containerId}" hidden>

                    <button class="btn btn-primary btn-block">Submit</button>
                </form>
            </div>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <h2>Clone from GitHub</h2>
                <p>Clone a product source code from GitHub repository</p>
                <form action="/staticScanner/ProductCloned" method="post">
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">GitHub URL</span>
                        <input name="url" id="url" class="form-control">
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Branch</span>
                        <input name="branch" id="branch" class="form-control">
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Tag</span>
                        <input name="tag" id="tag" class="form-control">
                    </div>
                    <br>
                        <input name="containerId" value="${containerId}" hidden>
                    <button class="btn btn-primary btn-block">Submit</button>
                </form>
            </div>
        </div>
    </div>
</div>
<%@include file="../fragments/footer.jsp" %>
<%@ include file="../fragments/styles.html" %>

</body>
</html>
