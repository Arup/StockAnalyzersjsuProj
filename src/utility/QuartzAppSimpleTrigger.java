package utility;

import java.util.Date;
import java.util.Map;
 
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;
 
public class QuartzAppSimpleTrigger 
{
    public static void main( String[] args ) throws Exception
    {
    	StockDataProcessor task = StockDataProcessor.getInstance();
 
    	//specify your sceduler task details
    	JobDetail job = new JobDetail();
    	job.setName("runJob");
    	job.setJobClass(RunJob.class);
 
    	Map dataMap = job.getJobDataMap();
    	dataMap.put("runTask", task);
 
    	//configure the scheduler time
    	SimpleTrigger trigger = new SimpleTrigger();
    	trigger.setName("runJobTesting");
    	trigger.setStartTime(new Date(System.currentTimeMillis() + 1000));
    	trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
    	trigger.setRepeatInterval(1800000);


    	//schedule it
    	Scheduler scheduler = new StdSchedulerFactory().getScheduler();
    	scheduler.start();
    	scheduler.scheduleJob(job, trigger);
 

    }






    
}