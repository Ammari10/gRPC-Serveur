package Achraf.ace.serveur;

import Achraf.ace.services.BankGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class grpcServer  {

    Server server ;

    private void start() throws IOException {
        int port = 9999;
        server = ServerBuilder.forPort(port)
                .addService(new BankGrpcService())
                .build()
                .start();
        System.out.println("Server started, listening on " + port);
    }
    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        final grpcServer server = new grpcServer();
        server.start();
        server.blockUntilShutdown();
    }
}