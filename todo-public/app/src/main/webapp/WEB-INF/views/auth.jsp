<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Authenticate</title>
    </head>
    <body>
        <form action='<spring:url value="auth"/>' method="post" accept-charset="UTF-8">
        	<label for="name">Enter your name: </label><input type="text" name="name">
        	<button type="submit">Log-in</button>
        </form>
    </body>
</html>
