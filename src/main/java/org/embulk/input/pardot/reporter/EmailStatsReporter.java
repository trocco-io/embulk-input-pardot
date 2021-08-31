package org.embulk.input.pardot.reporter;

import com.darksci.pardot.api.PardotClient;
import com.darksci.pardot.api.request.email.EmailStatsRequest;
import com.darksci.pardot.api.request.visitoractivity.VisitorActivityQueryRequest;
import com.darksci.pardot.api.response.email.EmailStatsResponse;
import com.darksci.pardot.api.response.visitoractivity.VisitorActivity;
import com.darksci.pardot.api.response.visitoractivity.VisitorActivityQueryResponse;
import com.google.common.collect.ImmutableList;
import org.embulk.config.ConfigException;
import org.embulk.input.pardot.Client;
import org.embulk.input.pardot.PluginTask;
import org.embulk.input.pardot.accessor.AccessorInterface;
import org.embulk.input.pardot.accessor.EmailStatsAccessor;
import org.embulk.spi.Column;
import org.embulk.spi.type.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class EmailStatsReporter implements ReporterInterface
{
    private final Logger logger = LoggerFactory.getLogger(EmailStatsReporter.class);
    private final PluginTask task;
    private HashMap<Long, VisitorActivity> listEmails = new HashMap<>();
    private HashMap<Long, EmailStatsResponse.Stats> results = new HashMap<>();
    private Integer offset;

    public EmailStatsReporter(PluginTask task)
    {
        this.task = task;
    }

    public ImmutableList.Builder<Column> createColumnBuilder()
    {
        ImmutableList.Builder<Column> columns = ImmutableList.builder();
        int i = 0;
        // @see https://developer.pardot.com/kb/api-version-3/emails/#supported-operations_1
        columns.add(new Column(i++, "list_email_id", Types.LONG));
        columns.add(new Column(i++, "sent", Types.LONG));
        columns.add(new Column(i++, "delivered", Types.LONG));
        columns.add(new Column(i++, "total_clicks", Types.LONG));
        columns.add(new Column(i++, "unique_clicks", Types.LONG));
        columns.add(new Column(i++, "soft_bounced", Types.LONG));
        columns.add(new Column(i++, "hard_bounced", Types.LONG));
        columns.add(new Column(i++, "opt_outs", Types.LONG));
        columns.add(new Column(i++, "spam_complaints", Types.LONG));
        columns.add(new Column(i++, "opens", Types.LONG));
        columns.add(new Column(i++, "unique_opens", Types.LONG));
        columns.add(new Column(i++, "delivery_rate", Types.STRING));
        columns.add(new Column(i++, "opens_rate", Types.STRING));
        columns.add(new Column(i++, "click_through_rate", Types.STRING));
        columns.add(new Column(i++, "unique_click_through_rate", Types.STRING));
        columns.add(new Column(i++, "click_open_ratio", Types.STRING));
        columns.add(new Column(i++, "opt_out_rate", Types.STRING));
        columns.add(new Column(i++, "spam_complaint_rate", Types.STRING));
        columns.add(new Column(i++, "campaign_id", Types.LONG));
        columns.add(new Column(i++, "campaign_cost", Types.LONG));
        columns.add(new Column(i++, "campaign_folder_id", Types.LONG));
        columns.add(new Column(i++, "campaign_name", Types.STRING));
        columns.add(new Column(i++, "subject", Types.STRING));
        return columns;
    }

    @Override
    public void withOffset(Integer offset)
    {
        this.offset = offset;
    }

    @Override
    public boolean hasResults()
    {
        return this.results != null;
    }

    @Override
    public void executeQuery(PardotClient client)
    {
        for (Long listEmailId : listEmails.keySet()) {
            EmailStatsRequest queryRequest = new EmailStatsRequest().selectByListEmailId(listEmailId);
            Optional<EmailStatsResponse.Stats> stats = client.emailStats(queryRequest);
            if (stats.isPresent()) {
                results.put(listEmailId, stats.get());
            }
        }
    }

    @Override
    public Integer queryResultSize()
    {
        return results.size();
    }

    @Override
    public Integer getTotalResults()
    {
        if (this.listEmails == null) {
            return 0;
        }
        return this.listEmails.size();
    }

    @Override
    public Iterable<? extends AccessorInterface> accessors()
    {
        List<EmailStatsAccessor> res = new ArrayList<>();
        int i = 0;
        for (Long listEmailId : this.results.keySet()) {
            res.add(new EmailStatsAccessor(task, this.results.get(listEmailId), this.listEmails.get(listEmailId)));
            i++;
        }
        Iterable<EmailStatsAccessor> itrable = res;
        return itrable;
    }

    @Override
    public void beforeExecuteQueries()
    {
        if (task.getActivityTypeIds().isPresent()) {
            throw new ConfigException("cannot set activity_ids with using object: `email_stats`");
        }
        // visitor activities からlist_email_idを取得する
        VisitorActivityReporter r = new VisitorActivityReporter(task);
        VisitorActivityQueryRequest req = r.buildQueryRequest();
        req.withEmailActivitiesOnly();
        int offset = 0;
        int totalResults = 0;
        PardotClient cli = Client.getClient(task);
        do {
            req.withOffset(offset);
            VisitorActivityQueryResponse.Result res = cli.visitorActivityQuery(req);
            offset += res.getVisitorActivities().size();
            logger.debug("offset: " + offset);
            logger.debug("visitor activities size: " + res.getVisitorActivities().size());
            logger.debug("visitor activities total results: " + res.getTotalResults());
            totalResults = res.getTotalResults();
            for (VisitorActivity va : res.getVisitorActivities()) {
                if (va.getListEmailId() != null) {
                    this.listEmails.put(va.getListEmailId(), va);
                }
            }
        }
        while (offset < totalResults);

        logger.info("fetched list_emails size: " + listEmails.size());
    }

    @Override
    public void afterExecuteQueries()
    {
        // do nothing
    }
}
