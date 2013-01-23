package utility;


import java.util.Map;
 
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


public class RunJob implements Job {
	
	public void execute(JobExecutionContext context)
	throws JobExecutionException {
 
		Map dataMap = context.getJobDetail().getJobDataMap();
		StockDataProcessor task = StockDataProcessor.getInstance();
		task = (StockDataProcessor)dataMap.get("runTask");
		task.runPerlScriptRealTime(); 
		//PerlSchedulerTask task = (PerlSchedulerTask)dataMap.get("runTask");
		
	}


}