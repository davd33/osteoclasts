library(ROCR)

fileFALSE<-"data/examples/nb/M+R25_4XCENTERB_PROB_SCORES-FALSE.CSV"
fileTRUE<-"data/examples/nb/M+R25_4XCENTERB_PROB_SCORES-TRUE.CSV"

false<-read.csv(fileFALSE, sep="\t")
true<-read.csv(fileTRUE, sep="\t")
examples<-rbind(true, false)

predictions<-examples$score
labels<-examples$positive

pred<-prediction(predictions, labels)
perf<-performance(pred, "tpr", "fpr")

plot(perf, colorize=TRUE)

#performance(pred, "auc") # area under the curve