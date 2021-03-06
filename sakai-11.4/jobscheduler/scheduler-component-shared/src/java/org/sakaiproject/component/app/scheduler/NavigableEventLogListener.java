package org.sakaiproject.component.app.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 26, 2010
 * Time: 11:41:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class NavigableEventLogListener implements TriggerListener, JobListener
{
    private final static Logger
        LOG = LoggerFactory.getLogger(NavigableEventLogListener.class.getName() + ".jobExecutions");

    private static enum EVENTTYPE
    {
        JOB_EXECUTING, JOB_VETOED, JOB_EXECUTED, TRIGGER_FIRED, TRIGGER_MISFIRED, TRIGGER_COMPLETED
    };

    @Override
    public void jobToBeExecuted(JobExecutionContext context)
    {
        info (EVENTTYPE.JOB_EXECUTING, null, context, null, null);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context)
    {
        info (EVENTTYPE.JOB_VETOED, null, context, null, null);
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException e)
    {
        info (EVENTTYPE.JOB_EXECUTED, null, context, e, null);
    }

    @Override
    public String getName()
    {
        return NavigableEventLogListener.class.getName();
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context)
    {
        info (EVENTTYPE.TRIGGER_FIRED, trigger, context, null, null);
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext jobExecutionContext)
    {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger)
    {
        info (EVENTTYPE.TRIGGER_MISFIRED, trigger, null, null, null);
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction triggerInstructionCode) {
        info (EVENTTYPE.TRIGGER_COMPLETED, trigger, context, null, triggerInstructionCode);        
    }

    private void info (EVENTTYPE eventType, Trigger trig, JobExecutionContext context, JobExecutionException exception, CompletedExecutionInstruction instructionCode) {
        JobDetail
            detail = (context != null)?context.getJobDetail():null;
        final JobDataMap
            dataMap = (context != null)?context.getMergedJobDataMap():null;
        final String
            jobName = (detail != null)?detail.getKey().getName():null,
            jobDesc = (detail != null)?detail.getDescription():null;
        final Class
            jobClass = (detail != null)?detail.getJobClass():null;
        final Trigger
            trigger = (trig != null)?trig:((context != null)?context.getTrigger():null);
        final String
            trigName = (trigger != null)?trigger.getKey().getName():null,
            trigDesc = (trigger != null)?trigger.getDescription():null;
        final Date
            trigStart = (trigger != null)?trigger.getStartTime():null,
            trigEnd = (trigger != null)?trigger.getEndTime():null;

        StringBuilder
            sb = new StringBuilder();

        switch (eventType)
        {
            case JOB_EXECUTING:
            {
                sb.append("Job Executing: [");
                sb.append("name: ").append(jobName).append(", description: ").append((jobDesc != null)?jobDesc:"")
                        .append(", class: ").append(jobClass.getName());
                sb.append("]");
                break;
            }
            case JOB_VETOED:
            {
                sb.append("Job Vetoed: [");
                sb.append("name: ").append(jobName).append(", description: ").append((jobDesc != null)?jobDesc:"")
                        .append(", class: ").append(jobClass.getName());
                break;
            }
            case JOB_EXECUTED:
            {
                sb.append("Job Executed: [");
                sb.append("name: ").append(jobName).append(", description: ").append((jobDesc != null)?jobDesc:"")
                  .append(", class: ").append(jobClass.getName());

                if (exception != null)
                {
                    sb.append (", exception: ").append(exception.getMessage());
                    if (exception.getCause() != null)
                    {
                      sb.append(", exception cause: ").append(exception.getCause().getClass().getName());
                    }
                }
                sb.append("]");

                break;
            }
            case TRIGGER_FIRED:
            {
                sb.append("Trigger Fired: [");
                sb.append("trigger: ").append(trigName).append(", trigger description: ").append((trigDesc != null)?trigDesc:"")
                  .append(", start: ").append((trigStart != null)?trigStart.toString():null)
                  .append(", end: ").append((trigEnd != null)?trigEnd.toString():null);
                sb.append(", job: ").append(jobName).append(", job description: ").append((jobDesc != null)?jobDesc:"")
                        .append(", class: ").append(jobClass.getName());
                sb.append("]");
                break;
            }
            case TRIGGER_MISFIRED:
            {
                sb.append("Trigger Misfired: [");
                sb.append("trigger: ").append(trigName).append(", trigger description: ").append((trigDesc != null)?trigDesc:"")
                  .append(", start: ").append((trigStart!=null)?trigStart.toString():null)
                  .append(", end: ").append((trigEnd!=null)?trigEnd.toString():null);
                sb.append("]");
                break;
            }
            case TRIGGER_COMPLETED:
            {
                sb.append("Trigger Completed: [");
                sb.append("trigger: ").append(trigName).append(", trigger description: ").append((trigDesc != null)?trigDesc:"")
                  .append(", start: ").append((trigStart!=null)?trigStart.toString():null)
                  .append(", end: ").append((trigEnd!=null)?trigEnd.toString():null);
                sb.append(", job: ").append(jobName).append(", job description: ").append((jobDesc != null)?jobDesc:"")
                  .append(", class: ").append(jobClass.getName())
                  .append(", execution result: ").append(instructionCode);
                sb.append("]");
                break;
            }
        }
        if (LOG.isDebugEnabled())
        {
        	LOG.debug(sb.toString());
        }
    }
}
