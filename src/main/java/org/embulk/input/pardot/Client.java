package org.embulk.input.pardot;

import com.darksci.pardot.api.ConfigurationBuilder;
import com.darksci.pardot.api.PardotClient;
import com.darksci.pardot.api.config.Configuration;
import org.embulk.config.ConfigException;
import org.embulk.input.pardot.type.AuthMethodType;

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
        AuthMethodType authMethod = task.getAuthMethod();

        if (authMethod == AuthMethodType.oauth) {
            if (task.getAccessToken().isPresent()
             && task.getBusinessUnitId().isPresent()) {
                return getClient(
                        task.getAccessToken().get(),
                        task.getBusinessUnitId().get(),
                        task.getUseDemoHost().orElse(false)
                );
            }
            throw new ConfigException("For OAuth authentication, please set access_token, business_unit_id");
        }

        if (task.getUserName().isPresent()
         && task.getPassword().isPresent()
         && task.getAppClientId().isPresent()
         && task.getAppClientSecret().isPresent()
         && task.getBusinessUnitId().isPresent()) {
            return getClient(
                    task.getUserName().get(),
                    task.getPassword().get(),
                    task.getAppClientId().get(),
                    task.getAppClientSecret().get(),
                    task.getBusinessUnitId().get()
            );
        }
        throw new ConfigException("For user/password authentication, please set user_name, password, app_client_id, app_client_secret, business_unit_id");
    }

    public static PardotClient getClient(String accessToken, String businessUnitId, Boolean useDemoHost)
    {
        final ConfigurationBuilder configBuilder;
        configBuilder = Configuration.newBuilder().withOAuthLogin(accessToken, businessUnitId);
        if (useDemoHost) {
            configBuilder.withDemoApiHost();
        }
        return new PardotClient(configBuilder);
    }
}
