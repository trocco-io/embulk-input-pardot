package org.embulk.input.pardot.reporter;

import com.darksci.pardot.api.PardotClient;
import com.darksci.pardot.api.request.email.EmailReadRequest;
import com.darksci.pardot.api.request.visitoractivity.VisitorActivityQueryRequest;
import com.darksci.pardot.api.response.email.Email;
import com.darksci.pardot.api.response.visitoractivity.VisitorActivity;
import com.darksci.pardot.api.response.visitoractivity.VisitorActivityQueryResponse;
import com.darksci.pardot.api.response.visitoractivity.VisitorActivityType;
import org.embulk.config.ConfigException;
import org.embulk.input.pardot.Client;
import org.embulk.input.pardot.PluginTask;
import org.embulk.input.pardot.accessor.AccessorInterface;
import org.embulk.input.pardot.accessor.EmailAccessor;
import org.embulk.spi.Column;
import org.embulk.spi.type.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class EmailReporter implements ReporterInterface
{
    private final Logger logger = LoggerFactory.getLogger(EmailReporter.class);
    private final PluginTask task;
    private HashMap<Long, VisitorActivity> emails = new HashMap<>();
    private HashMap<Long, Email> results = new HashMap<>();
    private Integer offset;

    public EmailReporter(PluginTask task)
    {
        this.task = task;
    }

    public List<Column> createColumns()
    {
        List<Column> columns = new ArrayList<>();
        int i = 0;
        // @see https://developer.pardot.com/kb/object-field-references/#email
        columns.add(new Column(i++, "id", Types.LONG));
        columns.add(new Column(i++, "name", Types.STRING));
        columns.add(new Column(i++, "subject", Types.STRING));
        columns.add(new Column(i++, "message", Types.STRING));
        columns.add(new Column(i++, "created_at", Types.TIMESTAMP));
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
        for (Long emailId : emails.keySet()) {
            EmailReadRequest queryRequest = new EmailReadRequest().selectById(emailId);
            Optional<Email> email = client.emailRead(queryRequest);
            if (email.isPresent()) {
                results.put(emailId, email.get());
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
        if (this.emails == null) {
            return 0;
        }
        return this.emails.size();
    }

    @Override
    public Iterable<? extends AccessorInterface> accessors()
    {
        List<EmailAccessor> res = new ArrayList<>();
        int i = 0;
        for (Long emailId : this.results.keySet()) {
            res.add(new EmailAccessor(task, this.results.get(emailId), this.emails.get(emailId)));
            i++;
        }
        Iterable<EmailAccessor> itrable = res;
        return itrable;
    }

    @Override
    public void beforeExecuteQueries()
    {
        if (task.getActivityTypeIds().isPresent()) {
            throw new ConfigException("cannot set activity_ids with using object: `email`");
        }
        // visitor activities からlist_email_idを取得する
        VisitorActivityReporter r = new VisitorActivityReporter(task);
        VisitorActivityQueryRequest req = r.buildQueryRequest();
        req.withEmailActivitiesOnly();
        req.withActivityType(VisitorActivityType.fromValue(6));
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
                this.emails.put(va.getEmailId(), va);
            }
        }
        while (offset < totalResults);

        logger.debug("fetched emails count: " + emails.size());
    }

    @Override
    public void afterExecuteQueries()
    {
        // do nothing
    }
}
