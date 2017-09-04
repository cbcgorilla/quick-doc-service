package cn.techfan.quickdoc.management;

import org.springframework.boot.endpoint.Endpoint;
import org.springframework.boot.endpoint.ReadOperation;
import org.springframework.stereotype.Component;

import javax.websocket.server.ServerEndpoint;

@Endpoint(id="mongo")
@ServerEndpoint("mongo")
@Component
public class MongoEndpoint {

    @ReadOperation
    public String getMongo(){
        return "mongo";
    }
}
