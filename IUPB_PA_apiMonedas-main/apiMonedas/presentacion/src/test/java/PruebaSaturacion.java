package monedas.api.presentacion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import monedas.api.ApiApplication;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.concurrent.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ApiApplication.class)
public class PruebaSaturacion {

    @LocalServerPort
    private int puerto;

    // Cliente HHTP
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Test

    @DisplayName("Prueba de Estrés Real con Sockets y Red (Tomcat)")
    public void pruebaEstres() throws InterruptedException {
        String url = "http://localhost:" + puerto + "/api/monedas";

        //Niveles de estrés: Cantidad de intentos simultáneos
        int[] nivelesCarga = {500, 1000, 2000, 5000};

        System.out.println(" INICIANDO PRUEBA DE ESTRÉS REAL (RED + TOMCAT)");

        for (int totalSolicitudes : nivelesCarga) {
            ExecutorService ejecutor = Executors.newFixedThreadPool(Math.min(totalSolicitudes, 100));
            CountDownLatch sincronizador = new CountDownLatch(1);

            AtomicInteger contadorExitosas = new AtomicInteger(0);
            AtomicInteger contadorRechazos = new AtomicInteger(0);
            AtomicInteger contadorErrores = new AtomicInteger(0);

            List<Future<?>> futuros = new ArrayList<>();
            for (int i = 0; i < totalSolicitudes; i++) {
                futuros.add(ejecutor.submit(() -> {
                    try {
                        HttpRequest solicitud = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .GET()
                                .build();
                        // solicitud real
                        HttpResponse<String> respueta = http.send(solicitud, HttpResponse.BodyHandlers.ofString());
                        int estado = respueta.statusCode();
                        if (estado == 200 || estado == 401 || estado == 403) {
                            contadorExitosas.incrementAndGet();
                        }
                        if (estado == 429){
                            contadorRechazos.incrementAndGet();
                        }



                    } catch (Exception e) {
                        contadorErrores.incrementAndGet();
                    }
                }));
            }

            //ejecución simultanea de las solicitudes
            sincronizador.countDown();

            ejecutor.shutdown();

            //espera a recibir las solicitudes
            ejecutor.awaitTermination(30, TimeUnit.SECONDS);

            System.out.println("Nivel de Estrés: " + totalSolicitudes + " solicitudes concurrentes");
            System.out.println("Solicitudes atendidas: " + contadorExitosas.get());
            System.out.println("Solicitudes rechazadas: " + contadorRechazos.get());
            System.out.println("Errores de conexión: " + contadorErrores.get());
        }

        System.out.println(" PRUEBA DE ESTRÉS FINALIZADA");
    }
}
