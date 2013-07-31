labelsResults = newArray();
for (i = 1; i <= nSlices; i++) {
	Stack.setPosition(1,i,1);
	labelsResults = Array.concat(labelsResults, getInfo("slice.label"));
}

for (i = 0; i < labelsResults.length; i++) {
	print(labelsResults[i]);
}
