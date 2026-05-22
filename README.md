# Pardot input plugin for Embulk

Now only 'Querying Visitor Activities' is supported.
- https://developer.pardot.com/kb/api-version-4/visitor-activities/

## Overview

* **Plugin type**: input
* **Resume supported**: yes
* **Cleanup supported**: yes
* **Guess supported**: no

## Configuration

- **user_name**: pardot/salesforce email (string, required)
- **password**: pardot/salesforce password (string, required)
- **user_key**: pardot user-key (string, default: `null`)
- **app_client_id**: salesforce app-client-id (string, default: `null`)
- **app_client_secret**: salesforce app-client-secret (string, default: `null`)
- **business_unit_id**: salesforce business-unit-id (string, default: `null`)
- **object_type**: salesforce Object
  - visitor_activity(default), email_stats, email(from visitor activities), visitor, prospect
- **created_after**: Selects visitor activities created after the specified time. If a <custom_time> is used, ensure that the specified date is formatted using GNU Date Input Syntax.
  - today, yesterday, last_7_days, this_month, last_month, <custom_time>
- **created_before**: Selects visitor activities created before the specified time. If a <custom_time> is used, ensure that the specified date is formatted using GNU Date Input Syntax.
  - today, yesterday, last_7_days, this_month, last_month, <custom_time>
- **fetch_row_limit**: Specifies the number of results(per 1 request) to be returned. Default value: 200. Note: This number cannot be larger than 200.
- **activity_type_ids**: Selects visitor activities of the specified types. See a list of available [Visitor Activity Types](https://developer.pardot.com/kb/object-field-references/#visitor-activity-types) in [Visitor Activity](https://developer.pardot.com/kb/object-field-references/#visitor-activity) in [Object Field References](https://developer.pardot.com/kb/object-field-references/).
- **prospect_ids**: Selects only visitor activities associated with one of the specified prospect IDs.
- **sort_key**: Specifies the field to be used to sort the results of the query.
  - created_at, id, prospect_id, visitor_id
- **sort_order**: Specifies the ordering to be used when sorting the results of the query. The default value varies based on the value of the sort_by parameter
  - descending(default), ascending
- **auth_method**: Specifies the authentication method to be used.
  - user_password(default), oauth
- **access_token**: Specifies the access token to be used for authentication.
- **use_demo_host**: Specifies whether to use the pardot demo host.
  - true, false (default)

see API document
- https://developer.pardot.com/kb/api-version-4/visitor-activities/

## Example

### With salesforce client ID / secret


- visitor activity

when auth_method is user_password

```yaml
in:
  type: pardot
  user_name: dummy@example.com
  password: password**
  app_client_id: app-client-id**
  app_client_secret: app-client-secret**
  business_unit_id: business-unit-id**
  created_after: 2020-12-01
  created_before: 2020-12-02
  auth_method: user_password
  prospect_ids:
    - 1234
  sort_key: created_at
  sort_order: descending
```

when auth_method is oauth

```yaml
in:
  type: pardot
  access_token: access-token** 
  business_unit_id: business-unit-id**
  created_after: 2020-12-01
  created_before: 2020-12-02
  auth_method: oauth
  prospect_ids:
    - 1234
  sort_key: created_at
  sort_order: descending
```

- email_stats

```yaml
in:
  type: pardot
  user_name: dummy@example.com
  password: password**
  app_client_id: app-client-id**
  app_client_secret: app-client-secret**
  business_unit_id: business-unit-id**
  auth_method: user_password
  object_type: email_stats
  created_after: 2020-12-01
  created_before: 2020-12-02
  sort_key: created_at
  sort_order: descending
```

when auth_method is oauth

```yaml
in:
  type: pardot
  access_token: access-token** 
  business_unit_id: business-unit-id**
  auth_method: oauth
  object_type: email_stats
  created_after: 2020-12-01
  created_before: 2020-12-02
  sort_key: created_at
  sort_order: descending
```

- email

```yaml
in:
  type: pardot
  user_name: dummy@example.com
  password: password**
  app_client_id: app-client-id**
  app_client_secret: app-client-secret**
  business_unit_id: business-unit-id**
  object_type: email
  created_after: 2020-12-01
  created_before: 2020-12-02
  sort_key: created_at
  sort_order: descending
```

when auth_method is oauth

```yaml
in:
  type: pardot
  access_token: access-token** 
  business_unit_id: business-unit-id**
  auth_method: oauth
  object_type: email
  created_after: 2020-12-01
  created_before: 2020-12-02
  sort_key: created_at
  sort_order: descending
```

- prospect

```yaml
in:
  type: pardot
  user_name: dummy@example.com
  password: password**
  app_client_id: app-client-id**
  app_client_secret: app-client-secret**
  business_unit_id: business-unit-id**
  object_type: prospect
  created_after: 2020-12-01
  created_before: 2020-12-02
  sort_key: created_at
  sort_order: descending
```

when auth_method is oauth

```yaml
in:
  type: pardot
  access_token: access-token** 
  business_unit_id: business-unit-id**
  auth_method: oauth
  object_type: prospect
  created_after: 2020-12-01
  created_before: 2020-12-02
  sort_key: created_at
  sort_order: descending
```

- visitor

```yaml
in:
  type: pardot
  user_name: dummy@example.com
  password: password**
  app_client_id: app-client-id**
  app_client_secret: app-client-secret**
  business_unit_id: business-unit-id**
  object_type: visitor
  created_after: 2020-12-01
  created_before: 2020-12-02
  sort_key: created_at
  sort_order: descending
```

when auth_method is oauth

```yaml
in:
  type: pardot
  access_token: access-token** 
  business_unit_id: business-unit-id**
  auth_method: oauth
  object_type: visitor
  created_after: 2020-12-01
  created_before: 2020-12-02
  sort_key: created_at
  sort_order: descending
```

## Build

```
$ ./gradlew gem  # -t to watch change of files and rebuild continuously
```
