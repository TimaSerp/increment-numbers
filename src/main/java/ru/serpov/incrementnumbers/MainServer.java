package ru.serpov.incrementnumbers;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MainServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 6565;
        Server server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new IncrementServiceImpl())
                .build();
        server.start();
        log.info("Server started on port {}", port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("shutting down grpc server");
            try {
                server.shutdown().awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("server shut down");
        }));
        server.awaitTermination();
    }


}
