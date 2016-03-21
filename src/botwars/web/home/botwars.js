var host = window.location.host;
var i = host.indexOf(":");
if (i >= 0) {
  host = host.substring(0, i);
}

var canvas = $("canvas")[0];
var g = wrapContext(canvas.getContext("2d"));
var socket = connect(host, $$(serverPort));
var width, height;
var tables = {};

$(function() {
  socket.message(handleMessage);
  socket.open(function() {
    console.log("Connected to socket server.");
  });
  $(window).resize(resize);
  resize();
  loop();
});

function handleMessage(data) {
  var command = data.command;
  if (command == "tables") {
    tables = data.tables;
  }
}

function render() {
  g.save();
  renderWorld();
  g.restore();
  renderFPS();
}

function resize() {
  canvas.width = width = window.innerWidth;
  canvas.height = height = window.innerHeight;
}

var last = lastFPSUpdate = Date.now();
var frame, fps;
function loop() {
  var now = Date.now();
  var delta = now - last;

  frame++;
  if (now - lastFPSUpdate >= 1000) {
    fps = frame * 1000.0 / (now - lastFPSUpdate);
    lastFPSUpdate = now;
    frame = 0;
  }

  render();
  last = now;

  requestAnimationFrame(loop);
}

function renderFPS() {
  var text = "FPS: " + Math.round(fps);
  g.font("16px Arial").color("white").drawString(text, width - g.getWidth(text) - 10, 16 * 1.5);
}
