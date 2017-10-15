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
            <h1>Static Scanner</h1>
            <h4>Upload the source code in one of the following ways and select scan/s you want to proceed</h4>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <h3><b>Upload a zip file</b></h3>
                <p>Upload a zip file of the product source code</p>
                <form action="/staticScanner/productUploaded" method="post"
                      enctype="multipart/form-data">
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">Zip File</span>
                        <input type="file" name="zipFile" id="zipFile" class="form-control">
                    </div>
                    <br>
                    <h3><b>Select Scan/s</b></h3>
                    <p>Please select scan/s that you want to proceed</p>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">
                                <input type="checkbox" name="chkFindSecBugs" id="chkFindSecBugs">
                            <input type="hidden" name="chkFindSecBugs" value="0">
                        </span>
                        <label class="form-control">Find Security Bugs</label>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">
                            <input type="checkbox" name="chkDependencyCheck" id="chkDependencyCheck">
                            <input type="hidden" name="chkDependencyCheck" value="0">
                        </span>
                        <label class="form-control">OWASP Dependency Check</label>
                    </div>
                    <br>
                    <button class="btn btn-primary btn-block">Submit & Start Scan</button>
                </form>
            </div>
        </div>
        <div class="col-lg-6 col-md-12 col-sm-12">
            <div class="jumbotron" style="background-color: #96978d">
                <h3><b>Clone from GitHub</b></h3>
                <p>Clone a product source code from GitHub repository</p>
                <form action="/staticScanner/ProductCloned" method="post" enctype="multipart/form-data">
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
                    <h3><b>Select Scan/s</b></h3>
                    <p>Please select scan/s that you want to proceed</p>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">
                                <input type="checkbox" name="chkFindSecBugs">
                            <input type="hidden" name="chkFindSecBugs" value="0">
                        </span>
                        <label class="form-control">Find Security Bugs</label>
                    </div>
                    <br>
                    <div class="input-group input-group-md">
                        <span class="input-group-addon">
                            <input type="checkbox" name="chkDependencyCheck">
                            <input type="hidden" name="chkDependencyCheck" value="0">
                        </span>
                        <label class="form-control">OWASP Dependency Check</label>
                    </div>
                    <br>
                    <button class="btn btn-primary btn-block">Submit & Start Scan</button>
                </form>
            </div>
        </div>
    </div>
</div>
<%@include file="../fragments/footer.jsp" %>
<%@ include file="../fragments/styles.html" %>

</body>
</html>
