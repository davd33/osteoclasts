function paintAround(x, y) {
	setForegroundColor(0, 0, 255);
	doWand(x, y);
	fill();
}

function isRGB(x, y, r, g, b) {
	pixel = getPixel(x, y);
	red = (pixel>>16)&0xff;  // extract red byte (bits 23-17)
	green = (pixel>>8)&0xff; // extract green byte (bits 15-8)
	blue = pixel&0xff;       // extract blue byte (bits 7-0)
	if (red == r && green == g && blue == b) {
		return true;
	}
	return false;
}

function isWhite(x, y) {
	return isRGB(x, y, 255, 255, 255);
}

function run() {
	for (s = 1; s <= nSlices; s++) {
		Stack.setPosition(1,s,1);
		showStatus(s+"/"+nSlices);
		
		for (i = 0; i < (getWidth()*getHeight()); i++) {
			x = i % getWidth();
			y = floor(i / getWidth());
			v = getPixel(x, y);
			red = (v>>16)&0xff;  // extract red byte (bits 23-17)
			green = (v>>8)&0xff; // extract green byte (bits 15-8)
			blue = v&0xff;       // extract blue byte (bits 7-0)
	
			if (red == 0 && green == 255 && blue == 0) {
				red = 255; green = 255; blue = 255;
				setPixel(x,y,256*256*red+256*green+blue);
			} else if (red == 255 && green == 0 && blue == 0) {
				if (isWhite(x+1, y)) {
					paintAround(x+1, y);
				}
				if (isWhite(x+1, y+1)) {
					paintAround(x+1, y+1);
				}
				if (isWhite(x+1, y-1)) {
					paintAround(x+1, y-1);
				}
				if (isWhite(x, y+1)) {
					paintAround(x, y+1);
				}
				if (isWhite(x, y-1)) {
					paintAround(x, y-1);
				}
				if (isWhite(x-1, y-1)) {
					paintAround(x-1, y-1);
				}
				if (isWhite(x-1, y+1)) {
					paintAround(x-1, y+1);
				}
				if (isWhite(x-1, y)) {
					paintAround(x-1, y);
				}
			}
		}
	}
}

run();