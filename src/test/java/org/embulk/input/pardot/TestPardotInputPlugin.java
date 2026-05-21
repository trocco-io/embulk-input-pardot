package org.embulk.input.pardot;

import org.embulk.EmbulkTestRuntime;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigException;
import org.embulk.config.ConfigLoader;
import org.embulk.config.ConfigSource;
import org.embulk.config.TaskSource;
import org.embulk.spi.InputPlugin;
import org.embulk.spi.Schema;
import org.embulk.util.config.ConfigMapper;
import org.embulk.util.config.ConfigMapperFactory;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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

    @Test
    public void testTransaction_withValidUserPasswordAuth()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "object_type: prospect\n"
                + "user_name: dummy@example.com\n"
                + "password: dummy-password\n"
                + "app_client_id: app-client-id\n"
                + "app_client_secret: app-client-secret\n"
                + "business_unit_id: business-unit-id\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        PardotInputPlugin plugin = new PardotInputPlugin();
        InputPlugin.Control control = mock(InputPlugin.Control.class);

        ConfigDiff result = plugin.transaction(config, control);

        verify(control).run(any(TaskSource.class), any(Schema.class), anyInt());
    }

    @Test
    public void testTransaction_withValidOAuthAuth()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "auth_method: oauth\n"
                + "object_type: prospect\n"
                + "access_token: dummy-access-token\n"
                + "business_unit_id: business-unit-id\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        PardotInputPlugin plugin = new PardotInputPlugin();
        InputPlugin.Control control = mock(InputPlugin.Control.class);

        ConfigDiff result = plugin.transaction(config, control);

        verify(control).run(any(TaskSource.class), any(Schema.class), anyInt());
    }

    @Test
    public void testTransaction_missingAccessTokenForOAuth()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "auth_method: oauth\n"
                + "object_type: prospect\n"
                + "business_unit_id: business-unit-id\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        PardotInputPlugin plugin = new PardotInputPlugin();
        InputPlugin.Control control = mock(InputPlugin.Control.class);

        try {
            plugin.transaction(config, control);
            fail("Expected ConfigException for missing access_token");
        }
        catch (ConfigException e) {
            assertEquals("`access_token` and `business_unit_id` is required when `auth_method` is OAUTH", e.getMessage());
        }
    }

    @Test
    public void testTransaction_missingBusinessUnitIdForOAuth()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "auth_method: oauth\n"
                + "object_type: prospect\n"
                + "access_token: dummy-access-token\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        PardotInputPlugin plugin = new PardotInputPlugin();
        InputPlugin.Control control = mock(InputPlugin.Control.class);

        try {
            plugin.transaction(config, control);
            fail("Expected ConfigException for missing business_unit_id");
        }
        catch (ConfigException e) {
            assertEquals("`access_token` and `business_unit_id` is required when `auth_method` is OAUTH", e.getMessage());
        }
    }

    @Test
    public void testTransaction_missingUserNameForUserPasswordAuth()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "object_type: prospect\n"
                + "password: dummy-password\n"
                + "app_client_id: app-client-id\n"
                + "app_client_secret: app-client-secret\n"
                + "business_unit_id: business-unit-id\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        PardotInputPlugin plugin = new PardotInputPlugin();
        InputPlugin.Control control = mock(InputPlugin.Control.class);

        try {
            plugin.transaction(config, control);
            fail("Expected ConfigException for missing user_name");
        }
        catch (ConfigException e) {
            assertEquals("All fields are required when `auth_method` is USER_PASSWORD", e.getMessage());
        }
    }

    @Test
    public void testTransaction_missingPasswordForUserPasswordAuth()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "object_type: prospect\n"
                + "user_name: dummy@example.com\n"
                + "app_client_id: app-client-id\n"
                + "app_client_secret: app-client-secret\n"
                + "business_unit_id: business-unit-id\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        PardotInputPlugin plugin = new PardotInputPlugin();
        InputPlugin.Control control = mock(InputPlugin.Control.class);

        try {
            plugin.transaction(config, control);
            fail("Expected ConfigException for missing password");
        }
        catch (ConfigException e) {
            assertEquals("All fields are required when `auth_method` is USER_PASSWORD", e.getMessage());
        }
    }

    @Test
    public void testTransaction_missingAppClientIdForUserPasswordAuth()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "object_type: prospect\n"
                + "user_name: dummy@example.com\n"
                + "password: dummy-password\n"
                + "app_client_secret: app-client-secret\n"
                + "business_unit_id: business-unit-id\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        PardotInputPlugin plugin = new PardotInputPlugin();
        InputPlugin.Control control = mock(InputPlugin.Control.class);

        try {
            plugin.transaction(config, control);
            fail("Expected ConfigException for missing app_client_id");
        }
        catch (ConfigException e) {
            assertEquals("All fields are required when `auth_method` is USER_PASSWORD", e.getMessage());
        }
    }

    @Test
    public void testTransaction_missingAppClientSecretForUserPasswordAuth()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "object_type: prospect\n"
                + "user_name: dummy@example.com\n"
                + "password: dummy-password\n"
                + "app_client_id: app-client-id\n"
                + "business_unit_id: business-unit-id\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        PardotInputPlugin plugin = new PardotInputPlugin();
        InputPlugin.Control control = mock(InputPlugin.Control.class);

        try {
            plugin.transaction(config, control);
            fail("Expected ConfigException for missing app_client_secret");
        }
        catch (ConfigException e) {
            assertEquals("All fields are required when `auth_method` is USER_PASSWORD", e.getMessage());
        }
    }

    @Test
    public void testTransaction_missingBusinessUnitIdForUserPasswordAuth()
    {
        String configYaml = ""
                + "type: pardot\n"
                + "object_type: prospect\n"
                + "user_name: dummy@example.com\n"
                + "password: dummy-password\n"
                + "app_client_id: app-client-id\n"
                + "app_client_secret: app-client-secret\n"
                + "created_after: 2020-12-01\n"
                + "created_before: 2020-12-02\n";

        ConfigSource config = getConfigFromYaml(configYaml);
        PardotInputPlugin plugin = new PardotInputPlugin();
        InputPlugin.Control control = mock(InputPlugin.Control.class);

        try {
            plugin.transaction(config, control);
            fail("Expected ConfigException for missing business_unit_id");
        }
        catch (ConfigException e) {
            assertEquals("All fields are required when `auth_method` is USER_PASSWORD", e.getMessage());
        }
    }
}
