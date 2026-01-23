package org.embulk.input.pardot;

import org.embulk.input.pardot.accessor.AccessorInterface;
import org.embulk.spi.Column;
import org.embulk.spi.ColumnVisitor;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.time.Timestamp;
import org.embulk.util.timestamp.TimestampFormatter;

import java.time.Instant;

public class ColVisitor implements ColumnVisitor
{
    private final AccessorInterface accessor;
    private final PluginTask task;
    private final PageBuilder pageBuilder;
    private final TimestampFormatter parser;

    public ColVisitor(AccessorInterface accessor, PageBuilder pageBuilder, PluginTask task)
    {
        this.accessor = accessor;
        this.pageBuilder = pageBuilder;
        this.task = task;
        this.parser = TimestampFormatter.builder("%Y-%m-%dT%H:%M:%S.%L", true)
                .setDefaultZoneFromString("UTC")
                .build();
    }

    @Override
    public void booleanColumn(Column column)
    {
        String data = accessor.get(column.getName());
        if (data == null) {
            pageBuilder.setNull(column);
        }
        else {
            pageBuilder.setBoolean(column, Boolean.parseBoolean(data));
        }
    }

    @Override
    public void longColumn(Column column)
    {
        String data = accessor.get(column.getName());
        if (data == null) {
            pageBuilder.setNull(column);
        }
        else {
            pageBuilder.setLong(column, Long.parseLong(data));
        }
    }

    @Override
    public void doubleColumn(Column column)
    {
        try {
            String data = accessor.get(column.getName());
            pageBuilder.setDouble(column, Double.parseDouble(data));
        }
        catch (Exception e) {
            pageBuilder.setNull(column);
        }
    }

    @Override
    public void stringColumn(Column column)
    {
        String data = accessor.get(column.getName());
        if (data == null) {
            pageBuilder.setNull(column);
        }
        else {
            pageBuilder.setString(column, data);
        }
    }

    @Override
    @SuppressWarnings("deprecation") // For compatibility with Embulk v0.9
    public void timestampColumn(Column column)
    {
        String data = accessor.get(column.getName());
        if (data == null) {
            pageBuilder.setNull(column);
        }
        else {
            Instant instant = parser.parse(data);
            pageBuilder.setTimestamp(column, Timestamp.ofInstant(instant));
        }
    }

    @Override
    public void jsonColumn(Column column)
    {
        // TODO:
        pageBuilder.setNull(column);
    }
}
