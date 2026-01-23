package org.embulk.input.pardot.accessor;

import com.darksci.pardot.api.response.email.Email;
import com.darksci.pardot.api.response.visitoractivity.VisitorActivity;
import org.embulk.input.pardot.PluginTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class EmailAccessor implements AccessorInterface
{
    private final PluginTask task;
    private final Email email;
    private final VisitorActivity emailActivity;

    private final Logger logger = LoggerFactory.getLogger(EmailAccessor.class);

    public EmailAccessor(PluginTask task, Email email, VisitorActivity emailActivity)
    {
        this.task = task;
        this.email = email;
        this.emailActivity = emailActivity;
    }

    @Override
    public String get(String name)
    {
        String methodName = "";
        try {
            methodName = "get" + lowerUnderscoreToUpperCamel(name);
            Class<Email> clazz = (Class<Email>) email.getClass();
            Method method = clazz.getDeclaredMethod(methodName);
            Object res =  method.invoke(email);
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
