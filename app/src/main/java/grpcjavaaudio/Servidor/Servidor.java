package grpcjavaaudio.Servidor;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class Servidor {
    public static void main(String[] args) throws IOException, InterruptedException {
        int puerto = 6067;

        Server servidor = ServerBuilder.forPort(puerto).addService(new ServidorImpl()).build();
        servidor.start();

        System.out.println("Servidor iniciado...");
        System.out.println("Escuchando en el puerto: " + puerto);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Recibiendo solicitud de apagado...");
                servidor.shutdown();
                System.out.println("Servidor detenido.");
            }
        });

        servidor.awaitTermination();
    }
}