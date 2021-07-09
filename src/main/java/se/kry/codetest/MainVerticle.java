package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import se.kry.codetest.model.Service;

import java.util.List;

public class MainVerticle extends AbstractVerticle {

    private DBConnector connector;
    private BackgroundPoller poller;

    @Override
    public void start(Future<Void> startFuture) {
        connector = new DBConnector(vertx);
        poller = new BackgroundPoller(connector, vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        setRoutes(router);
        vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices());
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        System.out.println("KRY code test service started");
                        startFuture.complete();
                    } else {
                        System.out.println("KRY code fail to start");
                        startFuture.fail(result.cause());
                    }
                });
    }


    private void getServices(Handler<AsyncResult<List<JsonObject>>> next) {
        connector.getAll(next);
    }

    private void setRoutes(Router router) {
        router.route("/*").handler(StaticHandler.create());
        router.get("/service").handler(req ->
                getServices((ar) -> req.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonArray(ar.result()).encode()))
        );
        router.post("/add").handler(req -> {
            JsonObject jsonBody = req.getBodyAsJson();
            connector.insert(Service.from(jsonBody), getAsyncResultHandler(req));

        });
        router.post("/delete").handler(req -> {
            JsonObject jsonBody = req.getBodyAsJson();
            connector.delete(jsonBody.getString("alias"), getAsyncResultHandler(req));

        });
    }

    private Handler<AsyncResult<UpdateResult>> getAsyncResultHandler(io.vertx.ext.web.RoutingContext req) {
        return (ar) -> {
            if (ar.succeeded()) {
                req.response()
                        .putHeader("content-type", "text/plain")
                        .end("OK");
            } else {
                req.response()
                        .putHeader("content-type", "text/plain")
                        .setStatusCode(400)
                        .end(ar.cause().getMessage());
            }
        };
    }

}



