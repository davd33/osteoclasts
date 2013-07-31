prb = getDirectory("and now select probability images directory");
prbSplit = split(prb, File.separator);
prbName = prbSplit[prbSplit.length-1];
prbfiles = getFileList(prb);

for (i = 0; i < orifiles.length; i++) {
	open(orifiles[i]);
	if (i < prbfiles.length) {
		open(prbfiles[i]);
	}
}
run("Images to Stack", "name=make_labels title=[] use");