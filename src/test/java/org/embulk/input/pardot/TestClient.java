package org.embulk.input.pardot;

import com.darksci.pardot.api.PardotClient;
import org.embulk.EmbulkTestRuntime;
import org.embulk.config.ConfigException;
import org.embulk.config.ConfigLoader;
import org.embulk.config.ConfigSource;
import org.embulk.util.config.ConfigMapper;
import org.embulk.util.config.ConfigMapperFactory;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class TestClient
{
    private static final ConfigMapperFactory CONFIG_MAPPER_FACTORY = ConfigMapperFactory.builder().addDefaultModules().build();

    @Rule
    public EmbulkTestRuntime runtime = new EmbulkTestRuntime();

    private ConfigSource getConfigFromYaml(String yaml)
    {
        ConfigLoader loader = new ConfigLoader(runtime.getExec().getModelManager());
        return loader.fromYamlString(yaml);
    }

    @Test
    public void test__getClient__OAuth()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "access_token: xxx\n"
                + "auth_method: oauth\n"
                + "business_unit_id: business-unit-id\n"
                + "use_demo_host: true\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        ConfigMapper configMapper = CONFIG_MAPPER_FACTORY.createConfigMapper();
        PluginTask task = configMapper.map(config, PluginTask.class);

        PardotClient client = Client.getClient(task);
        assertNotNull(client);
    }

    @Test
    public void test__getClient__UserPassword__MissingRequiredFields()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "user_name: dummy@example.com\n"
                + "password: password**\n"
                + "business_unit_id: business-unit-id\n"
                + "auth_method: user_password\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        ConfigMapper configMapper = CONFIG_MAPPER_FACTORY.createConfigMapper();
        PluginTask task = configMapper.map(config, PluginTask.class);

        ConfigException exception = assertThrows(ConfigException.class, () -> Client.getClient(task));
        assertEquals("For user/password authentication, please set user_name, password, app_client_id, app_client_secret, business_unit_id", exception.getMessage());
    }

    @Test
    public void test__getClient__OAuth__MissingAccessToken()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "auth_method: oauth\n"
                + "business_unit_id: business-unit-id\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        ConfigMapper configMapper = CONFIG_MAPPER_FACTORY.createConfigMapper();
        PluginTask task = configMapper.map(config, PluginTask.class);

        ConfigException exception = assertThrows(ConfigException.class, () -> Client.getClient(task));
        assertEquals("For OAuth authentication, please set access_token, business_unit_id", exception.getMessage());
    }
}
