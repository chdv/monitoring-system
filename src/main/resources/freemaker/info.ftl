<html><head><title>Server state info</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<META HTTP-EQUIV="refresh" CONTENT="2">
</head>
<body>
<h1>Server state info</h1>

<table cellspacing="1" bgcolor="#a9a9a9">
    <tr bgcolor="#f0f8ff">
        <#list header as x>
            <td><b> ${x}</b></td>
        </#list>
    </tr>
    <#list logList as map>
    <tr bgcolor="white">
        <#list map?values as value>
            <td> ${value}</td>
        </#list>
    </tr>
    </#list>
</table>
</body>
</html>