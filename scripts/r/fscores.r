setwd("~/data2/davdNewImages/Benchmark/csv/anasparamsa")
fscores<-read.csv("fscores.csv", sep=",")
fscores$recall<-fscores$tp / fscores$tp.fn
fscores$fn<-(fscores$tp / fscores$recall) - fscores$tp
fscores$precision<-fscores$tp / (fscores$tp + fscores$fp)

recall<-mean(fscores$recall)
precision<-mean(fscores$precision)
summary<-data.frame(recall=recall, precision=precision)