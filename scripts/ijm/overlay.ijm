function overlay() {
	dirOri = getDirectory("original images");
	dirSplitOri = split(dirOri, File.separator);
	dirNameOri = dirSplitOri[dirSplitOri.length-1];
	filesOri = getFileList(dirOri);
	run("Image Sequence...", "open=["+dirOri+filesOri[0]+"] number="+filesOri.length+" starting=1 increment=1 scale=100 file=[] or=[] sort");

	dirMask = getDirectory("masks images");
	dirSplitMask = split(dirMask, File.separator);
	dirNameMask = dirSplitMask[dirSplitMask.length-1];
	filesMask = getFileList(dirMask);
	run("Image Sequence...", "open=["+dirMask+filesMask[0]+"] number="+filesMask.length+" starting=1 increment=1 scale=100 file=[] or=[] sort");

	selectWindow(dirNameOri);
	run("Add Image...", "image="+dirNameMask+" x=0 y=0 opacity=50");
}

overlay();