function renderWorld() {
  g.color("black").fillRect(0, 0, width, height);

  var x = 0, y = 50;
  var w = 500;
  var h = 300;
  for (id in tables) {
    var table = tables[id];
    renderTable(table, x, y, w, h);
    x += w;
    if (x + w > width) {
      x = 0;
      y += h;
    }
  }
}

function renderTable(table, x, y, width, height) {
  var xGap = width / 10;
  var yGap = height / 10;
  g.color("rgb(50, 150, 0)").fillOval(x + xGap, y + yGap, width - xGap * 2, height - yGap * 2);

  g.lineWidth(2);
  g.color("rgba(200,200,200,.7)").drawOval(x + xGap, y + yGap, width - xGap * 2, height - yGap * 2);
  g.lineWidth(1);

  drawBoard(table, x, y, width, height);
}

function drawBoard(table, x, y, width, height) {
  var board = table.board;

  if (!board || !board.length) {
    return;
  }

  var h = height * .8 / 3;
  var w = getCardRatio() * h;
  var xGap = w / 10;
  var totalWidth = w * board.length + xGap * (board.length - 1);

  x = x + width / 2 - totalWidth / 2;
  y = y + height / 2 - h / 2;

  for (var i = 0; i < board.length; i++) {
    var card = board[i];
    // g.color("white").fillRect(x, y, w, h);
    drawCard(card, x, y, w, h);
    // g.drawImage(cardImages[0], x, y, w, h);
    x += w + xGap;
  }
}

