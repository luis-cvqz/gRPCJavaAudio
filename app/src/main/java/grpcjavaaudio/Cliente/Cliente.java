package grpcjavaaudio.Cliente;

import com.proto.audio.AudioServiceGrpc;
import com.proto.audio.Audio.DownloadFileRequest;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class Cliente {
    public static void main(String[] args) {
        String host = "localhost";
        int puerto = 6067;

        String nombre;

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host,puerto).usePlaintext().build();

        // nombre = "anyma.wav";
        // streamWav(channel, nombre, 44100F);

        // String nombreWav = "anyma.wav";
        // playWav(downloadFile(channel, nombreWav), nombreWav);

        String nombreMp3 = "tiesto.mp3";
        playMp3(downloadFile(channel, nombreMp3), nombreMp3);

        System.out.println("Apagando...");
        channel.shutdown();
    }

    private static void streamWav(ManagedChannel ch, String nombre, float sampleRate) {
        try {
            AudioFormat newFormat = new AudioFormat(sampleRate, 16, 2, true, false);
            SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(newFormat);
            sourceDataLine.open(newFormat);
            sourceDataLine.start();

            AudioServiceGrpc.AudioServiceBlockingStub stub = AudioServiceGrpc.newBlockingStub(ch);
            DownloadFileRequest downloadFileRequest = DownloadFileRequest.newBuilder().setNombre(nombre).build();

            int bufferSize = 1024;
            System.out.println("Reproduciendo el archivo: " + nombre);

            stub.downloadAudio(downloadFileRequest).forEachRemaining( response -> {
                try {
                    sourceDataLine.write(response.getData().toByteArray(), 0, bufferSize);
                    System.out.print(".");
                } catch (ArrayIndexOutOfBoundsException e) {}
            });
            System.out.println("\nRecepción de datos correcta.");
            System.out.println("Reproducción terminada. \n\n");

            sourceDataLine.drain();
            sourceDataLine.close();
            
        } catch (LineUnavailableException lineUnavailableException) {
            System.out.println(lineUnavailableException.getMessage());
        }
    }

    private static ByteArrayInputStream downloadFile (ManagedChannel ch, String nombre) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        AudioServiceGrpc.AudioServiceBlockingStub stub = AudioServiceGrpc.newBlockingStub(ch);
        DownloadFileRequest downloadFileRequest = DownloadFileRequest.newBuilder().setNombre(nombre).build();

        System.out.println("Recibiendo el archivo: " + nombre);
        stub.downloadAudio(downloadFileRequest).forEachRemaining( request -> {
            try {
                stream.write(request.getData().toByteArray());
                System.out.print(".");
            } catch (IOException ioException) {
                System.out.println("No se pudo obtener el archivo de audio." + ioException.getMessage());
            }
        });

        System.out.println("\nRecepción de datos correcta\n\n");

        return new ByteArrayInputStream(stream.toByteArray());
    }

    private static void playWav(ByteArrayInputStream inStream, String nombre) {
        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(inStream);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("Reproduciendo el archivo: " + nombre + "...\n\n");
            clip.start();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            clip.stop();
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException lineUnavailableException) {
            lineUnavailableException.printStackTrace();
        }
    }

    private static void playMp3(ByteArrayInputStream inStream, String nombre) {
        try {
            System.out.println("Reproduciendo el archivo: " + nombre + "...\n\n");
            Player player = new Player(inStream);
            player.play();
        } catch (JavaLayerException javaLayerException) {
            javaLayerException.printStackTrace();
        }
    }
}