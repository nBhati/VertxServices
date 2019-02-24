package io.vertx.blog.first;

import io.netty.handler.codec.http.HttpResponse;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by nBhati on 2/23/19.
 */
public class MyFirstVertical extends AbstractVerticle {
    // Store our product
    private Map<Integer, Whisky> products = new LinkedHashMap<>();
    // Create some product
    private void createSomeData() {
        Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
        products.put(bowmore.getId(), bowmore);
        Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
        products.put(talisker.getId(), talisker);
    }

    @Override
    public void start(Future<Void> fut) {
        this.createSomeData();

        Router router=Router.router(vertx);

        router.route("/").handler(routingContext -> {
            HttpServerResponse response=routingContext.response();
            response.putHeader("Content-type","text/html")
                    .end("<h1>Hello from my first Vert.x 3 application to you</h1>");
        });

        // Serve static resources from the /assets directory
        router.route("/assets/*").handler(StaticHandler.create("assets"));

        router.get("/api/whiskies").handler(this::getAll);
        router.route("/api/whiskies*").handler(BodyHandler.create());
        router.post("/api/whiskies").handler(this::addOne);
        router.delete("/api/whiskies/:id").handler(this::deleteOne);
        router.get("/api/whiskies/:id").handler(this::getOne);
        router.put("/api/whiskies/:id").handler(this::updateOne);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        config().getInteger("http.port", 8080),
                        result -> {
                            if (result.succeeded()) {
                                fut.complete();
                            } else {
                                fut.fail(result.cause());
                            }
                        });

    }
    private void getAll(RoutingContext routingContext) {
        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(products.values()));
    }

    private void addOne(RoutingContext routingContext) {
        final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(),
                Whisky.class);
        products.put(whisky.getId(), whisky);
        routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(whisky));
    }

    private void deleteOne(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        if (id == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            Integer idAsInteger = Integer.valueOf(id);
            products.remove(idAsInteger);
        }
        routingContext.response().setStatusCode(204).end();
    }

    private void getOne(RoutingContext routingContext){
        String id=routingContext.request().getParam("id");
        if(id==null){
            routingContext.response().setStatusCode(400).end();
        }else{
            Integer idAsInteger = Integer.valueOf(id);
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(products.get(idAsInteger)));
        }
    }

    private void updateOne(RoutingContext routingContext){
        String id=routingContext.request().getParam("id");
        JsonObject object=routingContext.getBodyAsJson();
        System.out.println(id+" "+object);
        if(id==null){
            routingContext.response().setStatusCode(400).end();
        }else{
            Integer idAsInteger = Integer.valueOf(id);
            Whisky  whisky= products.get(idAsInteger);
            whisky.setName(object.getString("name"));
            whisky.setOrigin(object.getString("origin"));
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(200)
                    .end(Json.encodePrettily(whisky));
        }
    }
}
