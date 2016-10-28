<!DOCTYPE html>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<html lang="en">
<head>
	<title>Home Page</title>
</head>
<body>
	<spring:url var="springUrl" value="/resources/text.txt" htmlEscape="true" />
	Spring URL: ${springUrl} at ${time}
	<br/ >

	<c:url var="url" value="/resources/text.txt" />
	JSTL URL: ${url}
	<br/ >

	Message: ${message}
</body>
</html>

