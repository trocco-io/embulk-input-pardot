package org.embulk.input.pardot.reporter;

import com.darksci.pardot.api.PardotClient;
import com.darksci.pardot.api.request.DateParameter;
import com.darksci.pardot.api.request.visitor.VisitorQueryRequest;
import com.darksci.pardot.api.response.visitor.Visitor;
import com.darksci.pardot.api.response.visitor.VisitorQueryResponse;
import org.embulk.config.ConfigException;
import org.embulk.input.pardot.PluginTask;
import org.embulk.input.pardot.accessor.AccessorInterface;
import org.embulk.input.pardot.accessor.VisitorAccessor;
import org.embulk.spi.Column;
import org.embulk.spi.type.Types;

import java.util.ArrayList;
import java.util.List;

public class VisitorReporter implements ReporterInterface
{
    private final PluginTask task;
    private final VisitorQueryRequest queryRequest;
    private VisitorQueryResponse.Result results;

    public VisitorReporter(PluginTask task)
    {
        this.task = task;
        this.queryRequest = buildQueryRequest();
    }

    public List<Column> createColumns()
    {
        List<Column> columns = new ArrayList<>();
        int i = 0;
        // @see https://developer.pardot.com/kb/object-field-references/#visitor
        columns.add(new Column(i++, "id", Types.LONG));
        columns.add(new Column(i++, "page_view_count", Types.LONG));
        columns.add(new Column(i++, "ip_address", Types.STRING));
        columns.add(new Column(i++, "hostname", Types.STRING));
        columns.add(new Column(i++, "campaign_parameter", Types.STRING));
        columns.add(new Column(i++, "medium_parameter", Types.STRING));
        columns.add(new Column(i++, "source_parameter", Types.STRING));
        columns.add(new Column(i++, "content_parameter", Types.STRING));
        columns.add(new Column(i++, "term_parameter", Types.STRING));
        columns.add(new Column(i++, "created_at", Types.TIMESTAMP));
        columns.add(new Column(i++, "updated_at", Types.TIMESTAMP));
        return columns;
    }

    @Override
    public void withOffset(Integer offset)
    {
        this.queryRequest.withOffset(offset);
    }

    @Override
    public boolean hasResults()
    {
        return this.results != null;
    }

    @Override
    public void executeQuery(PardotClient client)
    {
        this.results = client.visitorQuery(queryRequest);
    }

    @Override
    public Integer queryResultSize()
    {
        if (this.results == null) {
            return 0;
        }
        return this.results.getVisitors().size();
    }

    @Override
    public Integer getTotalResults()
    {
        if (this.results == null) {
            return 0;
        }
        return this.results.getTotalResults();
    }

    @Override
    public Iterable<? extends AccessorInterface> accessors()
    {
        List<VisitorAccessor> res = new ArrayList<>();
        for (Visitor p : this.results.getVisitors()) {
            res.add(new VisitorAccessor(task, p));
        }
        Iterable<VisitorAccessor> itrable = res;
        return itrable;
    }

    @Override
    public void beforeExecuteQueries()
    {
        // do nothing
    }

    @Override
    public void afterExecuteQueries()
    {
        // do nothing
    }

    public VisitorQueryRequest buildQueryRequest()
    {
        if (task.getActivityTypeIds().isPresent()) {
            throw new ConfigException("cannot set activity_ids with using object: `visitor`");
        }
        VisitorQueryRequest req = new VisitorQueryRequest();
        if (task.getFetchRowLimit().isPresent()) {
            req = req.withLimit(task.getFetchRowLimit().get());
        }
        if (task.getCreatedBefore().isPresent()) {
            req = req.withCreatedBefore(new DateParameter(task.getCreatedBefore().get()));
        }
        if (task.getCreatedAfter().isPresent()) {
            req = req.withCreatedAfter(new DateParameter(task.getCreatedAfter().get()));
        }
        if (task.getUpdatedBefore().isPresent()) {
            req = req.withUpdatedBefore(new DateParameter(task.getUpdatedBefore().get()));
        }
        if (task.getUpdatedAfter().isPresent()) {
            req = req.withUpdatedAfter(new DateParameter(task.getUpdatedAfter().get()));
        }
        if (task.getProspectIds().isPresent()) {
            req = req.withProspectIds(task.getProspectIds().get());
        }
        if (task.getSortKey().isPresent()) {
            req = req.withSortBy(task.getSortKey().get());
        }
        if (task.getSortOrder().isPresent()) {
            req = req.withSortOrder(task.getSortOrder().get());
        }
        return req;
    }
}
