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
    public static PardotClient getClient(String userName, String password, String appClientId, String appClientSecret, String businessUnitId, Integer pardotApiVersion)
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
        if (pardotApiVersion == 4) {
            configBuilder.withApiVersion4();
        }
        return new PardotClient(configBuilder);
    }

    public static PardotClient getClient(PluginTask task)
    {
        AuthMethodType authMethod = task.getAuthMethod().orElse(AuthMethodType.USER_PASSWORD);

        if (authMethod == AuthMethodType.OAUTH) {
            if (task.getAccessToken().isPresent()
             && task.getBusinessUnitId().isPresent()
             && task.getPardotApiVersion().isPresent()) {
                return getClient(
                        task.getAccessToken().get(),
                        task.getBusinessUnitId().get(),
                        task.getUseDemoHost().orElse(false),
                        task.getPardotApiVersion().get()
                );
            }
            throw new ConfigException("For OAuth authentication, please set access_token, business_unit_id");
        }

        if (task.getUserName().isPresent()
         && task.getPassword().isPresent()
         && task.getAppClientId().isPresent()
         && task.getAppClientSecret().isPresent()
         && task.getBusinessUnitId().isPresent()
         && task.getPardotApiVersion().isPresent()) {
            return getClient(
                    task.getUserName().get(),
                    task.getPassword().get(),
                    task.getAppClientId().get(),
                    task.getAppClientSecret().get(),
                    task.getBusinessUnitId().get(),
                    task.getPardotApiVersion().get()
            );
        }
        throw new ConfigException("For user/password authentication, please set user_name, password, app_client_id, app_client_secret, business_unit_id");
    }

    public static PardotClient getClient(String accessToken, String businessUnitId, Boolean useDemoHost, Integer apiVersion)
    {
        final ConfigurationBuilder configBuilder;
        configBuilder = Configuration.newBuilder()
                .withAccessTokenAndBusinessUnitId(accessToken, businessUnitId);
        if (useDemoHost) {
            configBuilder.withDemoApiHost();
        }
        if (apiVersion == 4) {
            configBuilder.withApiVersion4();
        }
        return new PardotClient(configBuilder);
    }
}
