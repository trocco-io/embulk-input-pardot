package org.embulk.input.pardot.accessor;

import com.darksci.pardot.api.response.prospect.Prospect;
import org.embulk.input.pardot.PluginTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class ProspectAccessor implements AccessorInterface
{
    private final PluginTask task;
    private final Prospect prospect;

    private final Logger logger = LoggerFactory.getLogger(ProspectAccessor.class);

    public ProspectAccessor(PluginTask task, Prospect p)
    {
        this.task = task;
        this.prospect = p;
    }

    @Override
    public String get(String name)
    {
        String methodName = "";
        try {
            methodName = "get" + lowerUnderscoreToUpperCamel(name);
            Class<Prospect> clazz = (Class<Prospect>) prospect.getClass();
            Method method = clazz.getDeclaredMethod(methodName);
            Object res =  method.invoke(prospect);
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
