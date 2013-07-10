data<-read.csv(file="data/chart-area-correlationNB.csv", sep=",")

plotData<-function (data) {
  plot(x=data$abscisse, 
       y=data$ordonnee, 
       main="Correlation between the predicted area and the number of osteoclasts", 
       xlab="number of osteoclasts", ylab="squared ratio of predicted area")
}

corrCoeff<-cor(data$abscisse, data$ordonnee)
cat("Correlation Coefficient: ", corrCoeff)

plotData(data)

outliers<-identify(x=data$abscisse, y=data$ordonnee)

if (length(outliers) > 0) {
  data<-data[-outliers,]
  plotData(data)
}

abline(lm(ordonnee ~ abscisse, data))