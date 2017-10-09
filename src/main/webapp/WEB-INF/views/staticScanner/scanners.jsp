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
    <%@ include file="../fragments/header.jsp" %>
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
                <p>Upload a zip file of the product source code</p>
                <form action="/staticScanner/uploadProductZipFileAndExtract" method="post"
                      enctype="multipart/form-data">
                    <div class="form-group">
                        <label for="zipFile">Browse zip file of the source code</label>
                        <input type="file" name="zipFile" id="zipFile" class="form-control">
                    </div>
                    <div class="form-group">
                        <label>Container Id</label>
                        <input name="containerId" class="form-control">
                    </div>
                    <button class="btn btn-primary btn-block">Submit</button>
                </form>
            </div>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <h2>Clone from GitHub</h2>
                <p>Clone a product source code from GitHub repository</p>
                <form action="/staticScanner/cloneProductFromGitHub" method="post">
                    <div class="form-group">
                        <label for="url">GitHub url</label>
                        <input name="url" id="url" class="form-control">
                    </div>
                    <div class="form-group">
                        <label for="branch">Branch</label>
                        <input name="branch" id="branch" class="form-control">
                    </div>
                    <div class="form-group">
                        <label for="tag">Tag</label>
                        <input name="tag" id="tag" class="form-control">
                    </div>
                    <div class="form-group">
                        <label>Container ID</label>
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
