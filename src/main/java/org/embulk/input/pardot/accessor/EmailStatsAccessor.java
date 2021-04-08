package org.embulk.input.pardot.accessor;

import com.darksci.pardot.api.response.email.EmailStatsResponse;
import com.darksci.pardot.api.response.visitoractivity.VisitorActivity;
import com.google.common.base.CaseFormat;
import org.embulk.input.pardot.PluginTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class EmailStatsAccessor implements AccessorInterface
{
    private final PluginTask task;
    private final EmailStatsResponse.Stats stats;

    private final Logger logger = LoggerFactory.getLogger(EmailStatsAccessor.class);
    private final Long listEmailId;

    public EmailStatsAccessor(PluginTask task, EmailStatsResponse.Stats stats, Long listEmailId)
    {
        this.task = task;
        this.stats = stats;
        this.listEmailId = listEmailId;
    }

    @Override
    public String get(String name)
    {
        String methodName = "";
        try {
            switch (name) {
                case "list_email_id":
                    return this.listEmailId.toString();
                default:
            }
            methodName = "get" + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name);
            Class<EmailStatsResponse.Stats> clazz = (Class<EmailStatsResponse.Stats>) stats.getClass();
            Method method = clazz.getDeclaredMethod(methodName);
            Object res =  method.invoke(stats);
            if (res == null) {
                return null;
            }
            return res.toString();
        }
        catch (Exception e) {
            logger.warn("Accessor error: {} name: {}", e, name);
        }
        return null;
    }
}
