function wrapContext(g) {
  var clipRegion = null;
  return {
    color : function(color) {
      g.fillStyle = color;
      g.strokeStyle = color;
      return this;
    },
    fillRect : function(x, y, width, height) {
      g.fillRect(x, y, width, height);
      return this;
    },
    fillOval : function(x, y, width, height) {
      g.beginPath();
      g.arc(x + width / 2, y + height / 2, width / 2, 0, 2 * Math.PI, false);
      g.fill();
      return this;
    },
    drawLine : function(x, y, x2, y2) {
      g.beginPath();
      g.moveTo(x, y);
      g.lineTo(x2, y2);
      g.stroke();
      return this;
    },
    drawString : function(s, x, y, width, height) {
      g.fillText(s, x, y);
      return this;
    },
    drawImage : function(img, x, y, width, height) {
      if (!img.loaded) {
        return this;
      }
      if (arguments.length == 3) {
        width = img.width;
        height = img.height;
      }
      if (inClip(x, y, width, height, clip)) {
        g.drawImage(img.img, x, y, width, height);
      }
      return this;
    },
    font : function(font) {
      g.font = font;
      return this;
    },
    translate : function(x, y) {
      g.translate(x, y);
      return this;
    },
    zoom : function(zoom) {
      g.scale(zoom, zoom);
      return this;
    },
    getWidth : function(text) {
      return g.measureText(text).width;
    },
    save : function() {
      g.save();
      return this;
    },
    restore : function() {
      g.restore();
      return this;
    },
    clip : function(clip) {
      clipRegion = clip;
      return this;
    }
  };
}

function inClip(x, y, w, h, clip) {
  if (!clip) {
    return true;
  }
  return !(x > clip.x + clip.w || x + w < clip.x || y > clip.y + clip.h || y + h < clip.y);
}
