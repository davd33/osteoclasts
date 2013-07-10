function fscores() {
	dir = getDirectory(str_ostdir);
	dirSplit = split(dir, File.separator);
	dirName = dirSplit[dirSplit.length-1];
	files = getFileList(dir);
	run("Image Sequence...", "open=["+dir+files[0]+"] number="+files.length+" starting=1 increment=1 scale=100 file=[] or=[] sort");
	
	run("Gray Morphology", "radius=2 type=circle operator=dilate");
	run("Invert LUT");

	// save number of positive labels
	labelsResults = newArray();
	for (i = 1; i <= nSlices; i++) {
		Stack.setPosition(1,i,1);
		run("Analyze Particles...", "size=50-Infinity circularity=0.00-1.00 show=Nothing clear include in_situ");
		labelsResults = Array.concat(labelsResults, nResults);
	}

	dirSeg = getDirectory(str_maskdir);
	dirSegSplit = split(dirSeg, File.separator);
	dirSegName = dirSegSplit[dirSegSplit.length-1];
	filesSeg = getFileList(dirSeg);
	run("Image Sequence...", "open=["+dirSeg+filesSeg[0]+"] number="+filesSeg.length+" starting=1 increment=1 scale=100 file=[] or=[] sort");

	imageCalculator("AND create stack", dirName, dirSegName);
	run("Gray Morphology", "radius=4 type=circle operator=dilate");
	run("Gray Morphology", "radius=2 type=circle operator=erode");

	// count number of true positives
	results = newArray();
	for (i = 1; i <= nSlices; i++) {
		Stack.setPosition(1,i,1);
		run("Set Measurements...", "  redirect=None decimal=3");
		run("Analyze Particles...", "size=0-Infinity circularity=0.00-1.00 show=Nothing clear include in_situ");
		results = Array.concat(results, nResults);
	}

	close(dirName);
	close(dirSegName);

	// false positive
	resultsCrossingNegLabels = rmCrossingNegLabels(dir, dirSeg);
	
	run("Clear Results");
	for (i = 0; i < results.length; i++) {
		setResult("tp", i, results[i]);
		setResult("name", i, filesSeg[i]);
	}
	for (i = 0; i < labelsResults.length; i++) {
		setResult("tp+fn", i, labelsResults[i]);
	}
	for (i = 0; i < resultsCrossingNegLabels.length; i++) {
		setResult("fp", i, resultsCrossingNegLabels[i]);
	}

	// save final computed results
	saveAs("Results", getDirectory("save fscore data") + "fscores.csv");

	close(dirName);
	close(dirSegName);
	close("Result of " + dirName);
}

function getObject(x, y) {
	object = newArray();
	if (getPixel(x,y) == 0) return object;

	visited = newArray();
	pixelsToCheck = newArray(x, y);
	for (i = 0; i < pixelsToCheck.length; i += 2) {
		pixelX = pixelsToCheck[i];
		pixelY = pixelsToCheck[i+1];
		value = getPixel(pixelX, pixelY);

		if (!in(visited, pixelX, pixelY) && value == 255) {
			pixelPoint = newArray(pixelX, pixelY);
			// register as visited
			visited = Array.concat(visited, pixelPoint);
			// add the pixel to the list of pixels
			// belonging to the object
			object = Array.concat(object, pixelPoint);
			// add to the list of pixels that 
			// need to be checked
			pixelsArray = newArray(
				pixelX  ,pixelY+1,
				pixelX-1,pixelY+1,
				pixelX-1,pixelY  ,
				pixelX-1,pixelY-1,
				pixelX  ,pixelY-1,
				pixelX+1,pixelY+1,
				pixelX+1,pixelY  ,
				pixelX+1,pixelY-1
				);
			pixelsToCheck = Array.concat(pixelsToCheck, pixelsArray);
		}
	}
	return object;
}

function printArray(array) {
	output = "";
	for (i = 0; i < array.length; i++) {
		if (i == (array.length - 1)) {
			output += toString(array[i]);
		} else {
			output += toString(array[i]) + ",";
		}
	}
}

function in(object, x, y) {
	for (i = 0; i < object.length; i += 2) {
		objectX = object[i]; objectY = object[i+1];
		if (objectX == x && objectY == y) {
			return true;
		}
	}
	return false;
}

function fillObject(object, value) {
	for (i = 0; i < object.length; i += 2) {
		x = object[i]; y = object[i+1];
		setPixel(x, y, value);
	}
}

function rmCrossingNegLabels(ost, mask) {
	ostSplit = split(ost, File.separator);
	ostName = ostSplit[ostSplit.length-1];
	files = getFileList(ost);
	run("Image Sequence...", "open=["+ost+files[0]+"] number="+files.length+" starting=1 increment=1 scale=100 file=[] or=[] sort");
	
	maskSplit = split(mask, File.separator);
	maskName = maskSplit[maskSplit.length-1];
	files = getFileList(mask);
	run("Image Sequence...", "open=["+mask+files[0]+"] number="+files.length+" starting=1 increment=1 scale=100 file=[] or=[] sort");
	run("Invert LUT");
	
	setBatchMode(true);
	for (stacki = 1; stacki <= nSlices; stacki++) {
		
		selectImage(ostName);
		Stack.setPosition(1,stacki,1);
		len = getHeight() * getWidth();
		for (i = 0; i < len; i++) {
			selectImage(ostName);
			ostX = i % getWidth();
			ostY = floor(i / getWidth());
			ostValue = getPixel(ostX, ostY);
	
			if (ostValue == 255) {
				selectImage(maskName);
				Stack.setPosition(1,stacki,1);
				maskValue = getPixel(ostX, ostY);
				if (maskValue == 255) {
					doWand(ostX, ostY);
					setForegroundColor(0, 0, 0);
					run("Fill", "slice");
				}
			}
		}
	}

	selectImage(maskName);
	run("Invert LUT");
	results = newArray();
	for (stacki = 0; stacki <= nSlices; stacki++) {
		Stack.setPosition(1,stacki,1);
		run("Set Measurements...", "  redirect=None decimal=3");
		run("Analyze Particles...", "size=0-Infinity circularity=0.00-1.00 show=Nothing clear include in_situ");
		if (stacki > 0)
			results = Array.concat(results, nResults);
	}
	
	return results;
}

_debug = false;
str_maskdir = "select mask's directory";
str_ostdir = "select ost binary's directory";

fscores();
