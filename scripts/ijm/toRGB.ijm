dir = getDirectory("non-rgb images");
dirSplit = split(dir, File.separator);
dirName = dirSplit[dirSplit.length-1];
files = getFileList(dir);
for (i=0; i<files.length;i++){
	open(files[i]);
	run("RGB Color");
	saveAs("Tiff", dir + files[i] + "_rgb.TIF");
	close(files[i] + "_rgb.tif");
	close(files[i]);
}
