package org.embulk.input.pardot.reporter;

import com.darksci.pardot.api.PardotClient;
import org.embulk.input.pardot.accessor.AccessorInterface;
import org.embulk.spi.Column;

import java.util.List;

public interface ReporterInterface
{
    List<Column> createColumns();

    void withOffset(Integer rowIndex);

    boolean hasResults();

    void executeQuery(PardotClient client);

    Integer queryResultSize();

    Integer getTotalResults();

    Iterable<? extends AccessorInterface> accessors();

    void beforeExecuteQueries();
    void afterExecuteQueries();
}
