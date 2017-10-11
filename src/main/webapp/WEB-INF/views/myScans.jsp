<%--
  Created by IntelliJ IDEA.
  User: deshani
  Date: 10/10/17
  Time: 12:56 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="fragments/header.html" %>
    <%@include file="fragments/navBar.jsp" %>
</head>
<body>
<div class="container">
    <div class="row">
        <div class="page-header">
            <h1>My Scans History</h1>
        </div>
        <h3>Dynamic Scanners</h3>
        <c:forEach items="${dynamicScanners}" var="dynamicScanner">
            <div class="col-lg-4 col-md-4 col-sm-6">
                <div class="thumbnail">
                    <table class="table table-bordered table-striped table-hover">
                        <tbody>
                        <tr>
                            <th>Created Time</th>
                            <td>${dynamicScanner.getCreatedTime()}</td>
                        </tr>
                            <%--<tr>--%>
                            <%--<th>Product Extracted Status</th>--%>
                            <%--<td>${dynamicScanner.isFileExtracted()}</td>--%>
                            <%--</tr>--%>
                            <%--<tr>--%>
                            <%--<th>Product Extracted Time</th>--%>
                            <%--<td>${dynamicScanner.getFileExtractedTime()}</td>--%>
                            <%--</tr>--%>
                        <tr>
                            <th>Scan Progress</th>
                            <td>
                                <div class="progress">
                                    <div class="progress-bar progress-bar-success" role="progressbar"
                                         aria-valuenow="${dynamicScanner.getZapScanProgress()}" aria-valuemax="100"
                                         aria-valuemin="0" style="width: ${dynamicScanner.getZapScanProgress()}%;">

                                    </div>
                                </div>
                            </td>
                        </tr>

                        </tbody>
                    </table>
                </div>
            </div>
        </c:forEach>
        <h3>Static Scanners</h3>
        <c:forEach items="${staticScanners}" var="staticScanner">
            <div class="col-lg-4 col-md-4 col-sm-6">
                <div class="thumbnail">
                    <table class="table table-bordered table-striped table-hover">
                        <tbody>
                        <tr>
                            <th>Created Time</th>
                            <td>${staticScanner.getCreatedTime()}</td>
                        </tr>
                        <tr>
                            <th>Product Extracted Status</th>
                            <td>${staticScanner.isFileExtracted()}</td>
                        </tr>
                        <tr>
                            <th>Product Extracted Time</th>
                            <td>${staticScanner.getFileExtractedTime()}</td>
                        </tr>
                        <tr>
                            <th>Product Cloned Status</th>
                            <td>${staticScanner.isProductCloned()}</td>
                        </tr>
                        <tr>
                            <th>Product Cloned Time</th>
                            <td>${staticScanner.getProductClonedTime()}</td>
                        </tr>

                        </tbody>
                    </table>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

<%@include file="fragments/footer.jsp" %>

</body>
</html>