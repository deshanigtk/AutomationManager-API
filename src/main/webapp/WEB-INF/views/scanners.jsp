<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="fragments/header.jsp" %>
    <%@include file="fragments/navBar.jsp" %>
</head>
<body>
<div class="container">
    <div class="col-lg-10">
        <a href="staticScanner.jsp" class="btn btn-primary btn-block">StaticScanner</a>
        <%--<button type="button" id="static_scanner" class="btn btn-primary btn-block" >Static Scanner</button>--%>
        <button type="button" id="dynamic_scanner" class="btn btn-primary btn-block">Dynamic Scanner</button>
    </div>
    <%@include file="fragments/footer.jsp" %>
</body>

</html>