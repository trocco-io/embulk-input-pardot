package org.embulk.input.pardot;

import com.darksci.pardot.api.PardotClient;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigException;
import org.embulk.config.ConfigSource;
import org.embulk.config.TaskReport;
import org.embulk.config.TaskSource;
import org.embulk.input.pardot.accessor.AccessorInterface;
import org.embulk.input.pardot.reporter.ReporterInterface;
import org.embulk.input.pardot.type.AuthMethodType;
import org.embulk.spi.Column;
import org.embulk.spi.Exec;
import org.embulk.spi.InputPlugin;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.PageOutput;
import org.embulk.spi.Schema;
import org.embulk.util.config.ConfigMapper;
import org.embulk.util.config.ConfigMapperFactory;
import org.embulk.util.config.TaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PardotInputPlugin
        implements InputPlugin
{
    private static final Logger logger = LoggerFactory.getLogger(PardotInputPlugin.class);
    private static final ConfigMapperFactory CONFIG_MAPPER_FACTORY = ConfigMapperFactory.builder().addDefaultModules().build();

    private ReporterInterface reporter;

    @Override
    @SuppressWarnings("deprecation") // For compatibility with Embulk v0.9
    public ConfigDiff transaction(ConfigSource config,
                                  InputPlugin.Control control)
    {
        ConfigMapper configMapper = CONFIG_MAPPER_FACTORY.createConfigMapper();
        PluginTask task = configMapper.map(config, PluginTask.class);
        validateConfig(task);

        reporter = ReporterBuilder.create(task);
        List<Column> columns = reporter.createColumns();

        final Schema schema = new Schema(columns);
        int taskCount = 1;  // number of run() method calls

        return resume(task.toTaskSource(), schema, taskCount, control);
    }

    @Override
    public ConfigDiff resume(TaskSource taskSource,
                             Schema schema, int taskCount,
                             InputPlugin.Control control)
    {
        control.run(taskSource, schema, taskCount);
        return CONFIG_MAPPER_FACTORY.newConfigDiff();
    }

    @Override
    public void cleanup(TaskSource taskSource,
                        Schema schema, int taskCount,
                        List<TaskReport> successTaskReports)
    {
    }

    @Override
    @SuppressWarnings("deprecation") // For compatibility with Embulk v0.9
    public TaskReport run(TaskSource taskSource,
                          Schema schema, int taskIndex,
                          PageOutput output)
    {
        TaskMapper taskMapper = CONFIG_MAPPER_FACTORY.createTaskMapper();
        PluginTask task = taskMapper.map(taskSource, PluginTask.class);
        final PageBuilder pageBuilder = new PageBuilder(Exec.getBufferAllocator(), schema, output);
        final PardotClient pardotClient = Client.getClient(task);
        reporter = ReporterBuilder.create(task);

        Integer totalResults;
        Integer rowIndex = 0;
        reporter.beforeExecuteQueries();
        do {
            reporter.withOffset(rowIndex);
            reporter.executeQuery(pardotClient);
            if (reporter.hasResults()) {
                rowIndex += reporter.queryResultSize();
            }
            totalResults = reporter.getTotalResults();
            logger.info("total results: {}", totalResults);
            for (AccessorInterface accessor : reporter.accessors()) {
                schema.visitColumns(new ColVisitor(accessor, pageBuilder, task));
                pageBuilder.addRecord();
            }
            pageBuilder.flush();
            logger.info("fetched rows: {} total: {}", rowIndex, totalResults);
        }
        while (rowIndex < totalResults);
        reporter.afterExecuteQueries();
        pageBuilder.finish();
        return CONFIG_MAPPER_FACTORY.newTaskReport();
    }

    @Override
    public ConfigDiff guess(ConfigSource config)
    {
        return CONFIG_MAPPER_FACTORY.newConfigDiff();
    }

    private void validateConfig(PluginTask task)
    {
        if (AuthMethodType.oauth == task.getAuthMethod()) {
            if (!task.getAccessToken().isPresent() || !task.getBusinessUnitId().isPresent()) {
                throw new ConfigException("`access_token` and `business_unit_id` is required when `auth_method` is oauth");
            }
            return;
        }

        if (!task.getUserName().isPresent()
                || !task.getPassword().isPresent()
                || !task.getAppClientId().isPresent()
                || !task.getAppClientSecret().isPresent()
                || !task.getBusinessUnitId().isPresent()) {
            throw new ConfigException("All fields are required when `auth_method` is user_password");
        }
    }
}
