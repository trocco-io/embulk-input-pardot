package org.embulk.input.pardot.reporter;

import com.darksci.pardot.api.PardotClient;
import com.darksci.pardot.api.request.DateParameter;
import com.darksci.pardot.api.request.visitoractivity.VisitorActivityQueryRequest;
import com.darksci.pardot.api.response.visitoractivity.VisitorActivity;
import com.darksci.pardot.api.response.visitoractivity.VisitorActivityQueryResponse;
import org.embulk.input.pardot.PluginTask;
import org.embulk.input.pardot.accessor.AccessorInterface;
import org.embulk.input.pardot.accessor.VisitorActivityAccessor;
import org.embulk.spi.Column;
import org.embulk.spi.type.Types;

import java.util.ArrayList;
import java.util.List;

public class VisitorActivityReporter implements ReporterInterface
{
    private final PluginTask task;
    private final VisitorActivityQueryRequest queryRequest;
    private VisitorActivityQueryResponse.Result results;

    public VisitorActivityReporter(PluginTask task)
    {
        this.task = task;
        this.queryRequest = buildQueryRequest();
    }

    public List<Column> createColumns()
    {
        List<Column> columns = new ArrayList<>();
        int i = 0;
        //<id> integer
        columns.add(new Column(i++, "id", Types.LONG));
        //<prospect_id> integer
        columns.add(new Column(i++, "prospect_id", Types.LONG));
        //<visitor_id> integer
        columns.add(new Column(i++, "visitor_id", Types.LONG));
        //<type> integer
        columns.add(new Column(i++, "type", Types.LONG));
        //<type_name> string
        columns.add(new Column(i++, "type_name", Types.STRING));
        //<details> string
        columns.add(new Column(i++, "details", Types.STRING));
        //<email_id> integer
        columns.add(new Column(i++, "email_id", Types.LONG));
        //<email_template_id> integer
        columns.add(new Column(i++, "email_template_id", Types.LONG));
        //<list_email_id> integer
        columns.add(new Column(i++, "list_email_id", Types.LONG));
        //<form_id> integer
        columns.add(new Column(i++, "form_id", Types.LONG));
        //<form_handler_id> integer
        columns.add(new Column(i++, "form_handler_id", Types.LONG));
        //<site_search_query_id> integer
        columns.add(new Column(i++, "site_search_query_id", Types.LONG));
        //<landing_page_id> integer
        columns.add(new Column(i++, "landing_page_id", Types.LONG));
        //<paid_search_ad_id> integer
        columns.add(new Column(i++, "paid_search_id", Types.LONG));
        //<multivariate_test_variation_id> integer
        columns.add(new Column(i++, "multivariate_test_variation_id", Types.LONG));
        //<visitor_page_view_id> integer
        columns.add(new Column(i++, "visitor_page_view_id", Types.LONG));
        //<file_id> integer
        columns.add(new Column(i++, "file_id", Types.LONG));
        //<custom_redirect_id> integer
        // columns.add(new Column(i++, "custom_redirect_id", Types.LONG)); // INFO not found on api client
        //<campaign> object
        // columns.add(new Column(i++, "campaign", Types.STRING));
        columns.add(new Column(i++, "campaign_id", Types.LONG));
        columns.add(new Column(i++, "campaign_name", Types.STRING));
        columns.add(new Column(i++, "campaign_cost", Types.LONG));
        columns.add(new Column(i++, "campaign_folder_id", Types.LONG));
        //<created_at> timestamp
        columns.add(new Column(i++, "created_at", Types.TIMESTAMP));
        columns.add(new Column(i++, "updated_at", Types.TIMESTAMP));
        //<updated_at> timestamp
        //columns.add(new Column(i++, "timestamp", Types.TIMESTAMP)); // INFO not found on api client
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
        this.results = client.visitorActivityQuery(queryRequest);
    }

    @Override
    public Integer queryResultSize()
    {
        if (this.results == null) {
            return 0;
        }
        return this.results.getVisitorActivities().size();
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
        List<VisitorActivityAccessor> res = new ArrayList<VisitorActivityAccessor>();
        for (VisitorActivity va : this.results.getVisitorActivities()) {
            res.add(new VisitorActivityAccessor(task, va));
        }
        Iterable<VisitorActivityAccessor> itrable = res;
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

    public VisitorActivityQueryRequest buildQueryRequest()
    {
        VisitorActivityQueryRequest req = new VisitorActivityQueryRequest();
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
        if (task.getActivityTypeIds().isPresent()) {
            req = req.withActivityTypeIds(task.getActivityTypeIds().get());
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
