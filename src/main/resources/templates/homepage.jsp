<!DOCTYPE html>
<html>
<head>
    <title>Automotive Maps API Prober</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script>
      function showListTilesResults() {
        $.get("/listMaps", function(data) {
          $("#listTilesResults").text(data);
        });
      }
    </script>
</head>
<body>
    <h1>Dynamic Content Example</h1>
    <button onclick="showListTilesResults()">List Tiles</button>
    <div id="listTilesResults"></div>
</body>
</html>