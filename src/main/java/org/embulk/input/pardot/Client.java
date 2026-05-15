package org.embulk.input.pardot;

import com.darksci.pardot.api.ConfigurationBuilder;
import com.darksci.pardot.api.PardotClient;
import com.darksci.pardot.api.config.Configuration;

public class Client
{
    private Client()
    { }
    public static PardotClient getClient(String userName, String password, String appClientId, String appClientSecret, String businessUnitId)
    {
        final ConfigurationBuilder configBuilder;
        configBuilder = Configuration.newBuilder()
                .withSsoLogin(
                        userName,
                        password,
                        appClientId,
                        appClientSecret,
                        businessUnitId
                );
        return new PardotClient(configBuilder);
    }

    public static PardotClient getClient(PluginTask task)
    {
        return getClient(
                task.getUserName(),
                task.getPassword(),
                task.getAppClientId().get(),
                task.getAppClientSecret().get(),
                task.getBusinessUnitId().get()
        );
    }

    public static PardotClient getClient(String accessToken)
    {
        final ConfigurationBuilder configBuilder;
        configBuilder = Configuration.newBuilder().withAccessToken(accessToken);
        return new PardotClient(configBuilder);
    }
}
