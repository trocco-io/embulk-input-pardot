package org.embulk.input.pardot.accessor;

import com.darksci.pardot.api.response.email.EmailStatsResponse;
import com.darksci.pardot.api.response.visitoractivity.VisitorActivity;
import org.embulk.input.pardot.PluginTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class EmailStatsAccessor implements AccessorInterface
{
    private final PluginTask task;
    private final EmailStatsResponse.Stats stats;
    private final VisitorActivity listEmailActivity;

    private final Logger logger = LoggerFactory.getLogger(EmailStatsAccessor.class);

    public EmailStatsAccessor(PluginTask task, EmailStatsResponse.Stats stats, VisitorActivity listEmail)
    {
        this.task = task;
        this.stats = stats;
        this.listEmailActivity = listEmail;
    }

    @Override
    public String get(String name)
    {
        String methodName = "";
        try {
            switch (name) {
                case "list_email_id":
                    return this.listEmailActivity.getListEmailId().toString();
                case "subject":
                    return this.listEmailActivity.getDetails().toString();
                case "campaign_id":
                    if (this.listEmailActivity.getCampaign() == null || this.listEmailActivity.getCampaign().getId() == null) {
                        return null;
                    }
                    return this.listEmailActivity.getCampaign().getId().toString();
                case "campaign_name":
                    if (this.listEmailActivity.getCampaign() == null || this.listEmailActivity.getCampaign().getName() == null) {
                        return null;
                    }
                    return this.listEmailActivity.getCampaign().getName();
                case "campaign_cost":
                    if (this.listEmailActivity.getCampaign() == null || this.listEmailActivity.getCampaign().getCost() == null) {
                        return null;
                    }
                    return this.listEmailActivity.getCampaign().getCost().toString();
                case "campaign_folder_id":
                    if (this.listEmailActivity.getCampaign() == null || this.listEmailActivity.getCampaign().getFolderId() == null) {
                        return null;
                    }
                    return this.listEmailActivity.getCampaign().getFolderId().toString();
                default:
            }
            methodName = "get" + lowerUnderscoreToUpperCamel(name);
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
