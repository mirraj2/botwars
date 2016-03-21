var cardMap = {};
var suits = [ 'c', 'd', 'h', 's' ];
var ranks = [ '2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A' ];

var bigImage = new Image();
bigImage.onload = function() {
  cWidth = bigImage.width / ranks.length;
  cHeight = bigImage.height / suits.length;
};
bigImage.src = "cards.png";

for (var i = 0; i < suits.length; i++) {
  for (var j = 0; j < ranks.length; j++) {
    var key = ranks[j] + suits[i];
    cardMap[key] = {
      x : j,
      y : i
    };
  }
}

function drawCard(card, x, y, w, h) {
  if (cHeight) {
    var v = cardMap[card];
    g.getContext().drawImage(bigImage, cWidth * v.x, cHeight * v.y, cWidth, cHeight, x, y, w, h);
  } else {
    g.color("white").fillRect(x, y, w, h);
  }
}

function getCardRatio() {
  return 0.68870523416;
}

// var cardImages = [];
// bigImage.onload = cutImageUp;
// bigImage.src = "cards.png";
//
// function cutImageUp() {
// var rows = 4, cols = 13;
// var w = bigImage.width / cols;
// var h = bigImage.height / cols;
//
// var canvas = document.createElement("canvas");
// canvas.width = w;
// canvas.height = h;
// var context = canvas.getContext('2d');
//
// for (var x = 0; x < cols; x++) {
// for (var y = 0; y < rows; y++) {
// context.drawImage(bigImage, x * w, y * h, w, h, 0, 0, w, h);
// cardImages.push(canvas.toDataURL());
// }
// }
// }
