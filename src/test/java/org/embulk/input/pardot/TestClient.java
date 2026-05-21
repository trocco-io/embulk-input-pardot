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
import static org.junit.Assert.fail;

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
    public void test__getClient()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "user_name: dummy@example.com\n"
                + "password: password**\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        ConfigMapper configMapper = CONFIG_MAPPER_FACTORY.createConfigMapper();
        PluginTask task = configMapper.map(config, PluginTask.class);
        try {
            Client.getClient(task);
        }
        catch (ConfigException e) {
            assertEquals("For user/password authentication, please set user_name, password, app_client_id, app_client_secret, business_unit_id", e.getMessage());
            return;
        }
        fail("Exception must be occurred");
    }

    @Test
    public void test__getClient__OAuth()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "access_token: xxx\n"
                + "auth_method: OAUTH\n"
                + "business_unit_id: business-unit-id\n"
                + "use_demo_host: true\n"
                + "pardot_api_version: 4\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        ConfigMapper configMapper = CONFIG_MAPPER_FACTORY.createConfigMapper();
        PluginTask task = configMapper.map(config, PluginTask.class);

        PardotClient client = null;
        try {
            client = Client.getClient(task);
        }
        catch (ConfigException e) {
            fail(e.getMessage());
            return;
        }
        assertNotNull(client);
    }

    @Test
    public void test__getClient__OAuth__MissingAccessToken()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "auth_method: OAUTH\n"
                + "business_unit_id: business-unit-id\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        ConfigMapper configMapper = CONFIG_MAPPER_FACTORY.createConfigMapper();
        PluginTask task = configMapper.map(config, PluginTask.class);
        try {
            Client.getClient(task);
        }
        catch (ConfigException e) {
            assertEquals("For OAuth authentication, please set access_token, business_unit_id", e.getMessage());
            return;
        }
        fail("Exception must be occurred");
    }
}
