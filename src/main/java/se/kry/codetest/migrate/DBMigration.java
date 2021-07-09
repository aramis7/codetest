package se.kry.codetest.migrate;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import se.kry.codetest.DBConnector;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class DBMigration {



    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DBConnector connector = new DBConnector(vertx);
        Future fut = Future.succeededFuture();
        connector.initData((a) -> {
            connector.getAll(ar -> System.out.println("services: " + ar.result()));
            vertx.close(shutdown -> {
                System.exit(0);
            });
        }, fut);
    }
}
