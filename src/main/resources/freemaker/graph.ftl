<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <META HTTP-EQUIV="refresh" CONTENT="5">
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
        google.load("visualization", "1", {packages:['corechart', 'line']});
        google.setOnLoadCallback(function() {
            <#list data?keys as key>
                drawChart${key_index}();
            </#list>
        });
        <#list data?keys as key>
        function drawChart${key_index}() {
            var data = google.visualization.arrayToDataTable([
                    ['', ''],
                <#list data[key] as val>
                    ['|', ${val}],
                </#list>
            ]);

            var options = {
                title: '${key}',
                hAxis: {title: '',  titleTextStyle: {color: '#333'}},
                vAxis: {minValue: 0},
                legend: { position: 'bottom' },
                width:600,
                height:300
            };

            var chart = new google.visualization.LineChart(document.getElementById('chart_div${key_index}'));
            chart.draw(data, options);
        };
        </#list>
    </script>
</head>
<body>
<#list data?keys as key>
    <div id="chart_div${key_index}" style="width: 600px; height: 300px; float:left;"></div>
</#list>

</body>
</html>