package org.embulk.input.pardot.reporter;

import com.darksci.pardot.api.PardotClient;
import com.darksci.pardot.api.request.DateParameter;
import com.darksci.pardot.api.request.prospect.ProspectQueryRequest;
import com.darksci.pardot.api.response.prospect.Prospect;
import com.darksci.pardot.api.response.prospect.ProspectQueryResponse;
import org.embulk.config.ConfigException;
import org.embulk.input.pardot.PluginTask;
import org.embulk.input.pardot.accessor.AccessorInterface;
import org.embulk.input.pardot.accessor.ProspectAccessor;
import org.embulk.spi.Column;
import org.embulk.spi.type.Types;

import java.util.ArrayList;
import java.util.List;

public class ProspectReporter implements ReporterInterface
{
    private final PluginTask task;
    private final ProspectQueryRequest queryRequest;
    private ProspectQueryResponse.Result results;

    public ProspectReporter(PluginTask task)
    {
        this.task = task;
        this.queryRequest = buildQueryRequest();
    }

    public List<Column> createColumns()
    {
        List<Column> columns = new ArrayList<>();
        int i = 0;
        // @see https://developer.pardot.com/kb/object-field-references/#prospect
        columns.add(new Column(i++, "id", Types.LONG));
        columns.add(new Column(i++, "first_name", Types.STRING));
        columns.add(new Column(i++, "last_name", Types.STRING));
        columns.add(new Column(i++, "email", Types.STRING));
        // columns.add(new Column(i++, "password", Types.STRING));
        columns.add(new Column(i++, "company", Types.STRING));
        columns.add(new Column(i++, "prospect_account_id", Types.STRING));
        columns.add(new Column(i++, "website", Types.STRING));
        columns.add(new Column(i++, "job_title", Types.STRING));
        columns.add(new Column(i++, "department", Types.STRING));
        columns.add(new Column(i++, "country", Types.STRING));
        columns.add(new Column(i++, "address_one", Types.STRING));
        columns.add(new Column(i++, "address_two", Types.STRING));
        columns.add(new Column(i++, "city", Types.STRING));
        columns.add(new Column(i++, "state", Types.STRING));
        columns.add(new Column(i++, "territory", Types.STRING));
        columns.add(new Column(i++, "zip", Types.STRING));
        columns.add(new Column(i++, "phone", Types.STRING));
        columns.add(new Column(i++, "fax", Types.STRING));
        columns.add(new Column(i++, "source", Types.STRING));
        columns.add(new Column(i++, "annual_revenue", Types.STRING));
        columns.add(new Column(i++, "employees", Types.STRING));
        columns.add(new Column(i++, "industry", Types.STRING));
        columns.add(new Column(i++, "years_in_business", Types.STRING));
        columns.add(new Column(i++, "comments", Types.STRING));
        columns.add(new Column(i++, "notes", Types.STRING));
        columns.add(new Column(i++, "score", Types.LONG));
        columns.add(new Column(i++, "grade", Types.STRING));
        columns.add(new Column(i++, "last_activity_at", Types.TIMESTAMP));
        columns.add(new Column(i++, "recent_interaction", Types.STRING));
        columns.add(new Column(i++, "crm_lead_fid", Types.STRING));
        columns.add(new Column(i++, "crm_contact_fid", Types.STRING));
        columns.add(new Column(i++, "crm_owner_fid", Types.STRING));
        columns.add(new Column(i++, "crm_account_fid", Types.STRING));
        columns.add(new Column(i++, "crm_last_sync", Types.STRING));
        columns.add(new Column(i++, "crm_url", Types.STRING));
        columns.add(new Column(i++, "is_do_not_email", Types.BOOLEAN));
        columns.add(new Column(i++, "is_do_not_call", Types.BOOLEAN));
        columns.add(new Column(i++, "is_opted_out", Types.BOOLEAN));
        columns.add(new Column(i++, "is_reviewed", Types.BOOLEAN));
        columns.add(new Column(i++, "is_starred", Types.BOOLEAN));
        columns.add(new Column(i++, "is_archived", Types.BOOLEAN));
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
        this.results = client.prospectQuery(queryRequest);
    }

    @Override
    public Integer queryResultSize()
    {
        if (this.results == null) {
            return 0;
        }
        return this.results.getProspects().size();
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
        List<ProspectAccessor> res = new ArrayList<>();
        for (Prospect p : this.results.getProspects()) {
            res.add(new ProspectAccessor(task, p));
        }
        Iterable<ProspectAccessor> itrable = res;
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

    public ProspectQueryRequest buildQueryRequest()
    {
        if (task.getActivityTypeIds().isPresent()) {
            throw new ConfigException("cannot set activity_ids with using object: `prospect`");
        }
        ProspectQueryRequest req = new ProspectQueryRequest();
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
        if (task.getSortKey().isPresent()) {
            req = req.withSortBy(task.getSortKey().get());
        }
        if (task.getSortOrder().isPresent()) {
            req = req.withSortOrder(task.getSortOrder().get());
        }
        return req;
    }
}
