<%-- 
    Document   : register.jsp
    Created on : 12-Mar-2018, 10:52:09
    Author     : Rande001
--%>

<%@page import="nl.wur.agrodatacube.token.Registration"%>
<%
    String reg_response = null;
    String email = request.getParameter("email");
    if (email != null) {
        Registration r = new Registration(email);
        reg_response = r.saveFreeRegistration();
    }
%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <link rel="stylesheet" type="text/css" href="css/agrodatacube.css">
        <title>AgroDataCube Registration Page</title>
        <script src="https://code.jquery.com/jquery-3.2.1.js" crossorigin="anonymous"></script>

        <script type="text/javascript">

            // AJAX and servlet caused some isues so simple JSP solution.

            function register() {
                if (document.frm_register.email.value !== null) {
                    document.frm_register.submit();
                }
            }

        </script>

    <center>
        <img src="images/logo_color_rgb.jpg" width="300" alt="Welcome to AgroDataCube"/>
    </center>

</head>
<body style="width:60%; left-margin:20%">
    <form name="frm_register" action="register.jsp" method="post">
        <h1><center>Welcome at the registration page of the Agro Data Cube </center></h1>
        <br>
        <br>
        <span>
            This page allows you to register to get access to the data in the AgroDataCube. For a valid registration 
            we require your email address. If your request for access can be granted we will inform you by email
            how you can access the data in the AgroDataCube.                
            <BR>
            This page only allows you to request limited FREE access, this provides limited access to the data in the AgroDataCube. For other use 
            you can contact us <a HREF="mailto:info.agrodatacube@wur.nl">E-mail</a>. See the table below for possible access options.
            <BR>
            <BR>
            <table>
                <tr><th>Access type</th><th>Number of Requests per year</th><th>Area Limit per year</th><th>Costs</th><th>Remark</th></tr>
                <tr><td>FREE</td><td>25000</td><td>1000000 ha</td><td>None</td><td>For development purposes</td></tr>
                <tr><td>PRO</td><td>Tailored</td><td>Tailored</td><td>Tailored</td><td></td></tr>
                <tr><td>EXPERT</td><td>Tailored</td><td>Tailored</td><td>Tailored</td><td></td></tr>
                <tr><td>HEAVY</td><td>Tailored</td><td>Tailored</td><td>Tailored</td><td></td></tr>
            </table>
        </span>
        <br>
        <label>For a free token please fill in your email address and we will send a token to the email you supplied<input id="email" name="email" type="email"> 
            <input type="button" value="Register" name="Register" onclick="javascript:register()" >
            <br><br><span>Your E-mail address will be stored in the AgroDataCube for billing and/or other AgroDataCube related issues. Your E-mail address will
                not be used for other purposes nor will it be provided for other non AgroDataCube usage.
                </form>
                <% if (reg_response != null) {%>
                <br>
                <br>
                <span><%= reg_response%></span> 
                <% }%>

                </body>
                <p>
                    <br>
                <center>
                    <img src="images/logo_wur_100years.png" width="400" alt="WUR"/>
                </center></p></html>

