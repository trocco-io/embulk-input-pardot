package org.embulk.input.pardot;

import com.google.common.base.Optional;
import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.config.Task;

import java.util.List;

public interface PluginTask extends Task
{
    @Config("object_type")
    @ConfigDefault("\"visitor_activity\"")
    String getObjectType();

    @Config("user_name")
    String getUserName();

    @Config("password")
    String getPassword();

    @Config("app_client_id")
    @ConfigDefault("null")
    Optional<String> getAppClientId();

    @Config("app_client_secret")
    @ConfigDefault("null")
    Optional<String> getAppClientSecret();

    @Config("business_unit_id")
    @ConfigDefault("null")
    Optional<String> getBusinessUnitId();

    @Config("created_before")
    @ConfigDefault("null")
    Optional<String> getCreatedBefore();

    @Config("created_after")
    @ConfigDefault("null")
    Optional<String> getCreatedAfter();

    @Config("fetch_row_limit")
    @ConfigDefault("200")
    Optional<Integer> getFetchRowLimit();

    @Config("activity_type_ids")
    @ConfigDefault("null")
    Optional<List<Integer>> getActivityTypeIds();

    @Config("prospect_ids")
    @ConfigDefault("null")
    Optional<List<Long>> getProspectIds();

    @Config("sort_key")
    @ConfigDefault("null")
    Optional<String> getSortKey();

    @Config("sort_order")
    @ConfigDefault("null")
    Optional<String> getSortOrder();
}
