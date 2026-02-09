package org.embulk.input.pardot.accessor;

import com.darksci.pardot.api.response.visitor.Visitor;
import org.embulk.input.pardot.PluginTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class VisitorAccessor implements AccessorInterface
{
    private final PluginTask task;
    private final Visitor visitor;

    private final Logger logger = LoggerFactory.getLogger(VisitorAccessor.class);

    public VisitorAccessor(PluginTask task, Visitor p)
    {
        this.task = task;
        this.visitor = p;
    }

    @Override
    public String get(String name)
    {
        String methodName = "";
        try {
            methodName = "get" + lowerUnderscoreToUpperCamel(name);
            Class<Visitor> clazz = (Class<Visitor>) visitor.getClass();
            Method method = clazz.getDeclaredMethod(methodName);
            Object res =  method.invoke(visitor);
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
