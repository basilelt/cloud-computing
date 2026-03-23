<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="hostname" content="${ hostname }">
        <meta name="daobk" content="${ daobk }">
        <title>Tasks list</title>
    </head>
    <body>
    	<c:if test="${ userName != null }" ><div align="right"><span id="loggedName"><c:out value="${ userName }"/></span>&nbsp;<a href='<spring:url value="/auth/disconnect" />' id="disconnect">disconnect</a></div></c:if>
        <ul id="tasks">
        	<c:forEach items="${ tasks }" var="task">
        		<li id="task_${ task.id }">
        			<a href='<spring:url value="/edit?id=" />${ task.id }' id="edit_task_${ task.id }"><c:out value="${ task.name }"/></a>
        			<a href='<spring:url value="/delete?id=" />${ task.id }' id="delete_task_${ task.id }">Delete</a>
        		</li>
        	</c:forEach>
        </ul>
        <a href='<spring:url value="/edit" />' id="new_task">Create new</a>
    </body>
</html>
