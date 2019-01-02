<html>
<body>


<script>

    var ws = new WebSocket("ws://localhost:8080/websocket");

    ws.onopen = function() { console.log('connection established') }
    ws.onmessage = (msg) => {console.log("server message: ", msg.data)}
    ws.onclose = () => {console.log("disconnected")}

    window.ws = ws;


</script>


</body>
</html>