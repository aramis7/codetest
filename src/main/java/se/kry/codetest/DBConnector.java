package se.kry.codetest;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.UpdateResult;
import se.kry.codetest.model.Service;

import java.util.List;

import static java.util.Optional.ofNullable;

public class DBConnector {
    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS Services (" +
            "alias VARCHAR(128) NOT NULL, " +
            "status VARCHAR(128) NOT NULL, " +
            "url VARCHAR(128) NOT NULL, " +
            "modifyDate DATE NOT NULL, " +
            "CONSTRAINT uc_alias UNIQUE (alias)) ";
    public static final String STATUS_UNKNOWN = "UNKNOWN";
    private final String DB_PATH = "poller.db";
    private final String DS_KRY = "KRY_DS";
    private final SQLClient client;

    public DBConnector(Vertx vertx) {
        JsonObject config = new JsonObject()
                .put("url", "jdbc:sqlite:" + DB_PATH)
                .put("driver_class", "org.sqlite.JDBC")
                .put("max_pool_size", 30);

        client = JDBCClient.createShared(vertx, config, DS_KRY);
    }


    public void initData(Handler<AsyncResult<Void>> next, Future<Void> fut) {
        client.query(CREATE_TABLE_QUERY, ar -> {
            if (ar.failed()) {
                fut.fail(ar.cause());
                return;
            }
            getAll(select -> {
                if (select.failed()) {
                    fut.fail(ar.cause());
                    return;
                }
                if (select.result().isEmpty()) {
                    insert(new Service("kry", STATUS_UNKNOWN, "https://www.kry.se", System.currentTimeMillis()),
                            (r) -> next.handle(Future.<Void>succeededFuture()));
                } else {
                    next.handle(Future.<Void>succeededFuture());
                }
            });
        });
    }

    public void getAll(Handler<AsyncResult<List<JsonObject>>> next) {

        client.query("SELECT * FROM Services;", asyncResult -> {
            if (asyncResult.failed()) {
                next.handle(Future.failedFuture(asyncResult.cause()));
                return;
            }
            System.out.println(asyncResult.result().getRows().toString());
            next.handle(Future.succeededFuture(asyncResult.result().getRows()));
        });
    }

    public Future<ResultSet> query(String query) {
        return query(query, new JsonArray());
    }


    public Future<ResultSet> query(String query, JsonArray params) {
        if (query == null || query.isEmpty()) {
            return Future.failedFuture("Query is null or empty");
        }
        if (!query.endsWith(";")) {
            query = query + ";";
        }

        Future<ResultSet> queryResultFuture = Future.future();

        client.queryWithParams(query, params, result -> {
            if (result.failed()) {
                queryResultFuture.fail(result.cause());
            } else {
                queryResultFuture.complete(result.result());
            }
        });
        return queryResultFuture;
    }

    public void insert(Service service, Handler<AsyncResult<UpdateResult>> next) {
        final String sql = "INSERT INTO Services (alias, status, url, modifyDate) VALUES (?, ?, ?, ?);";
        final JsonArray params = new JsonArray().add(service.getAlias())
                .add(ofNullable(service.getStatus()).orElse(STATUS_UNKNOWN))
                .add(service.getUrl())
                .add(ofNullable(service.getModifyDate()).orElse(System.currentTimeMillis()));

        client.updateWithParams(sql, params, (done) -> {
            if (done.failed()) {
                next.handle(Future.failedFuture(done.cause()));
                return;
            }
            next.handle(Future.succeededFuture(done.result()));
        });
    }

    public void delete(String alias, Handler<AsyncResult<UpdateResult>> next) {
        final String sql = "DELETE FROM Services where alias = ?;";
        final JsonArray params = new JsonArray().add(alias);

        client.updateWithParams(sql, params, (done) -> {
            if (done.failed()) {
                next.handle(Future.failedFuture(done.cause()));
                return;
            }
            next.handle(Future.succeededFuture(done.result()));
        });
    }

    public void update(Service service) {
        final String sql = "UPDATE Services SET status = ? where alias = ?;";
        final JsonArray params = new JsonArray().add(service.getStatus())
                .add(service.getAlias());

        client.updateWithParams(sql, params, (done) -> {
        });
    }
}
