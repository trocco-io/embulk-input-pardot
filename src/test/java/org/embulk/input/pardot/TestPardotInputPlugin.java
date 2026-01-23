package org.embulk.input.pardot;

import org.embulk.EmbulkTestRuntime;
import org.embulk.config.ConfigException;
import org.embulk.config.ConfigLoader;
import org.embulk.config.ConfigSource;
import org.embulk.util.config.ConfigMapper;
import org.embulk.util.config.ConfigMapperFactory;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestPardotInputPlugin
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
            PardotInputPlugin.getClient(task);
        }
        catch (ConfigException e) {
            assertEquals("please set app_client_id, app_client_secret, business_unit_id", e.getMessage());
            return;
        }
        assertTrue("Exception must be occurred", false);
    }

    @Test
    public void test__ColumnBuilder()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "object_type: fake_object_type\n"
                + "user_name: dummy@example.com\n"
                + "password: dummy-password\n"
                + "app_client_id: app-client-id**\n"
                + "app_client_secret: app-client-secret**\n"
                + "business_unit_id: business-unit-id**\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        ConfigMapper configMapper = CONFIG_MAPPER_FACTORY.createConfigMapper();
        PluginTask task = configMapper.map(config, PluginTask.class);
        try {
            ReporterBuilder.create(task);
        }
        catch (ConfigException e) {
            assertEquals("undefined object_type: fake_object_type", e.getMessage());
            return;
        }
        assertTrue("Exception must be occurred", false);
    }
}
