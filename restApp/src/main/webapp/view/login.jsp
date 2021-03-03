<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<form action="/restApp/prepareLoginWithGoogle" method="post">
<table id="">
<tr><td>Email: </td><td><input type="text" name="email"/></td></tr>
<tr><td>Password: </td><td><input type="password" name="password" /></td></tr>
<tr><td colspan="2"><input type="submit" value="Accedi"/></td></tr>
</table>
<div style="color:red;">${msg}</div>
</form>

<form action="/restApp/prepareLoginWithGoogle" method="post">
<div><input type="submit" value="Accedi con Google "/></div>
</form>

<form action="/restApp/prepareLoginWithFacebook" method="post">
<div><input type="submit" value="Accedi Con Facebook"/></div>
</form>
</body>
</html>