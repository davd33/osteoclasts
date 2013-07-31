prb = getDirectory("select img directory");
prbSplit = split(prb, File.separator);
prbName = prbSplit[prbSplit.length-1];
prbfiles = getFileList(prb);

for (i = 0; i < prbfiles.length; i++) {
	open(prbfiles[i]);
	iinfos = getImageInfo();
	//setSlice(2);
	//run("Delete Slice");
}
run("Images to Stack", "name="+prbName+" title=[] use");
