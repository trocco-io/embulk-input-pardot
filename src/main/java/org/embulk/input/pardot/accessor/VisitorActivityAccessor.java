package org.embulk.input.pardot.accessor;

import com.darksci.pardot.api.response.visitoractivity.VisitorActivity;
import org.embulk.input.pardot.PluginTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class VisitorActivityAccessor implements AccessorInterface
{
    private final PluginTask task;
    private final VisitorActivity va;

    private final Logger logger = LoggerFactory.getLogger(VisitorActivityAccessor.class);

    public VisitorActivityAccessor(PluginTask task, VisitorActivity va)
    {
        this.task = task;
        this.va = va;
    }

    @Override
    public String get(String name)
    {
        String methodName = "";
        try {
            switch (name) {
                case "campaign_id":
                    if (va.getCampaign() == null || va.getCampaign().getId() == null) {
                        return null;
                    }
                    return va.getCampaign().getId().toString();
                case "campaign_name":
                    if (va.getCampaign() == null || va.getCampaign().getName() == null) {
                        return null;
                    }
                    return va.getCampaign().getName();
                case "campaign_cost":
                    if (va.getCampaign() == null || va.getCampaign().getCost() == null) {
                        return null;
                    }
                    return va.getCampaign().getCost().toString();
                case "campaign_folder_id":
                    if (va.getCampaign() == null || va.getCampaign().getFolderId() == null) {
                        return null;
                    }
                    return va.getCampaign().getFolderId().toString();
                default:
            }
            methodName = "get" + lowerUnderscoreToUpperCamel(name);
            Class<VisitorActivity> clazz = (Class<VisitorActivity>) va.getClass();
            Method method = clazz.getDeclaredMethod(methodName);
            Object res =  method.invoke(va);
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
