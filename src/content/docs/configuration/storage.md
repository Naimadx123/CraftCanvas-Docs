---
title: "Player storage"
---

CraftCanvas uses a separate `storage.yml`, HikariCP connection pools and SQL backends. Paper loads the JDBC libraries declared in the plugin metadata.

SQLite is the default and stores player variables in `plugins/CraftCanvas/.data/data.db`. No database server is required. MySQL and MariaDB use the MySQL JDBC driver; PostgreSQL accepts either `postgres` or `postgresql` as its type.

```yaml
type: sqlite
host: localhost
port: 3306
database: craftcanvas
username: root
password: ''
table-prefix: craftcanvas_
pool-size: 10
queue-size: 500
sqlite-fallback: false
```

For an external database, create the database and configure credentials with permission to create tables, select, insert and update. PostgreSQL normally uses port `5432`. Table prefixes accept letters, numbers and underscores (up to 40 characters). SQLite always uses one pooled connection with a five-second busy timeout; external databases use `pool-size`.

Storage settings take effect after a server restart. `/cc reload` synchronizes missing default fields in `storage.yml` but keeps the active storage connection. Changing the backend, database or table prefix selects a different dataset; it does not copy data between SQL databases.

## Loading and saving

Only variables with `scope: player` and `persistent: true` are saved, when `persistent-player-data` is enabled in `config.yml`. Session and global variables and display profiles are not persisted.

Startup loads players already online; subsequent players load during asynchronous pre-login. The cache is removed on disconnect after sessions save their state. A failed login-time read rejects login rather than replacing existing data with defaults. SQL initialization, reads and writes use the file executor, outside the player's owning thread.

Pending snapshots for the same player are merged. `queue-size` limits the number of players waiting for a write; a full queue rejects new entries and logs the loss. Failed writes are logged and retained for a subsequent flush or shutdown. Shutdown queues a final flush before closing the pool. A prolonged database outage can still prevent successful persistence; check severe log entries.

External database initialization failures stop startup by default. `sqlite-fallback: true` explicitly permits local SQLite instead, with a warning. Local fallback data is not merged back into the external database automatically.

Multiple servers can access the same tables, but CraftCanvas does not provide live cache synchronization or conflict resolution. Each save replaces that player's complete persistent snapshot; concurrent sessions use the last successful write.

## Backups

There is no migration, import or export of legacy JSON player files. Existing files under `data/players/` are ignored and left untouched. SQL storage starts with the data already present in the selected database.

Keep `storage.yml`, `.data/data.db` (copy with the server stopped) or an external database backup, and the other internal records under `.data/` in backups.

SQLite uses only `.data/data.db`. Files in older locations are ignored and left untouched; no automatic database relocation or data migration is performed.
