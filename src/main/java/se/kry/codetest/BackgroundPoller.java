package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import se.kry.codetest.model.Service;

import java.util.List;

import static se.kry.codetest.model.Service.ServiceStatus.FAIL;
import static se.kry.codetest.model.Service.ServiceStatus.OK;
import static se.kry.codetest.model.Service.from;

public class BackgroundPoller {

    private DBConnector connector;
    private Vertx vertx;

    public BackgroundPoller(DBConnector connector, Vertx vertx) {
        this.connector = connector;
        this.vertx = vertx;
    }

    public void pollServices() {
        connector.getAll(ar -> {
            if (ar.succeeded()) {
                ar.result().stream().forEach(jsonObject -> {
                    WebClient.create(vertx)
                            .getAbs(jsonObject.getString("url"))
                            .putHeader("Accept", "text/html")
                            .ssl(true)
                            .expect(ResponsePredicate.SC_OK)
                            .send(httpResponse -> {
                                Service service = from(jsonObject);
                                service.setStatus(httpResponse.succeeded() ?
                                        OK : FAIL);
                                connector.update(service);
                            });
                });
            }
        });
    }
}
