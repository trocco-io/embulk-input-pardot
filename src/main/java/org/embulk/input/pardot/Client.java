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
                task.getUserName().get(),
                task.getPassword().get(),
                task.getAppClientId().get(),
                task.getAppClientSecret().get(),
                task.getBusinessUnitId().get()
        );
    }

    public static PardotClient getClient(String authMethod, String accessToken, String businessUnitId, String pardotApiHost)
    {
        final ConfigurationBuilder configBuilder;
        configBuilder = Configuration.newBuilder()
                .withAuthMethod(authMethod)
                .withAccessTokenAndBusinessUnitId(accessToken, businessUnitId)
                .withPardotApiHost(pardotApiHost)
                .withApiVersion4();
        return new PardotClient(configBuilder);
    }
}
