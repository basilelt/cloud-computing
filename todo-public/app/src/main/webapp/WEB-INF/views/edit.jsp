<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Tasks list</title>
    </head>
    <body>
        <form action='<spring:url value="save"/>' method="post" accept-charset="UTF-8">
        	<input type="hidden" name="id" value="${ task.id }">
        	<label for="name">Name</label><input type="text" name="name" value="${ task.name }">
        	<button type="submit">Save</button>
        </form>
    </body>
</html>
