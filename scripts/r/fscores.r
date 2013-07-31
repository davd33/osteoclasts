compute_fscores <- function () {
  results<-data.frame();
  
  for (i in list.files()) {
    filestr<-paste(toString(i),"/fscores.csv", sep="");
    
    if (file.exists(filestr) && !file.info(filestr)$isdir) {
      fscores<-read.csv(filestr, sep=",");
      
      fscores$recall<-fscores$tp / fscores$tp.fn;
      fscores$fn<-fscores$tp.fn - fscores$tp;
      fscores$precision<-fscores$tp / (fscores$tp + fscores$fp);
      
      # delete NaN values
      fscores<-fscores[complete.cases(fscores),];
      # remove recalls above one
      fscores<-fscores[fscores$recall <= 1,];
      
      recall<-mean(fscores$recall);
      precision<-mean(fscores$precision);
      maxRecall<-max(fscores$recall);
      minRecall<-min(fscores$recall);
      minPrecision<-min(fscores$precision);
      maxPrecision<-max(fscores$precision);
      
      results<-rbind(results, data.frame(
        recall=recall, 
        precision=precision,
        max_recall=maxRecall,
        min_recall=minRecall,
        max_precision=maxPrecision,
        min_precision=minPrecision
      ));
    }
  }
  
  results<-results[order(-results$recall, -results$precision),]
  return(results);
}

setwd("~/data2/davdNewImages/Benchmark/csv/p_space_assess/")
r<-compute_fscores();

write.table(r, file="fscores.csv", sep=",");
