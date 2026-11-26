package monedas.api.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import monedas.api.aplicacion.servicios.ContadorAtomico;
import monedas.api.aplicacion.servicios.ContadorSinControl;
import monedas.api.aplicacion.servicios.ContadorSincronizado;
import monedas.api.aplicacion.servicios.IContadorIntentosLogin;
import monedas.api.aplicacion.servicios.UsuarioServicio;
import monedas.api.core.repositorios.IUsuarioRepositorio;

/**
 * Lanza muchos intentos de login al mismo tiempo contra cada variante del contador
 * y muestra cuanto contaron, cuanto tardaron y cuantos intentos se perdieron.
 */
public class PruebaContadoresConcurrenciaTest {

    private static final int[] NIVELES_CARGA = {500, 1000, 2000, 100000};
    private static final int HILOS = 100;

    @Test
    @DisplayName("Comparacion de contadores: sin control vs synchronized vs AtomicLong")
    public void compararContadores() throws Exception {
        List<String> tabla = new ArrayList<>();

        for (int total : NIVELES_CARGA) {
            IContadorIntentosLogin[] variantes = {
                    new ContadorSinControl(), new ContadorSincronizado(), new ContadorAtomico()
            };
            for (IContadorIntentosLogin contador : variantes) {
                tabla.add(ejecutar(contador, total));
            }
        }

        System.out.println();
        System.out.printf("%-18s %-10s %-10s %-12s %-10s %-10s%n",
                "Variante", "Esperado", "Final", "Tiempo(ms)", "Perdidos", "Errores");
        tabla.forEach(System.out::println);
    }

    private String ejecutar(IContadorIntentosLogin contador, int total) throws Exception {
        UsuarioServicio servicio = crearServicio(contador);
        ExecutorService ejecutor = Executors.newFixedThreadPool(HILOS);
        CountDownLatch salida = new CountDownLatch(1);
        AtomicInteger errores = new AtomicInteger(0);
        List<Future<?>> futuros = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            futuros.add(ejecutor.submit(() -> {
                try {
                    salida.await(); // todos esperan la señal para arrancar juntos
                    servicio.login("usuario", "clave");
                } catch (Exception e) {
                    errores.incrementAndGet();
                }
            }));
        }

        long inicio = System.nanoTime();
        salida.countDown();
        for (Future<?> f : futuros) {
            f.get(60, TimeUnit.SECONDS);
        }
        long milisegundos = (System.nanoTime() - inicio) / 1_000_000;
        ejecutor.shutdown();

        long valorFinal = contador.obtenerTotalIntentos();
        long perdidos = total - valorFinal;

        // Las variantes B y C deben contar todo; la A solo se muestra porque su fallo es aleatorio.
        if (!(contador instanceof ContadorSinControl)) {
            assertEquals(total, valorFinal, contador.nombreEstrategia() + " perdio intentos");
        } else {
            assertTrue(valorFinal <= total);
        }
        assertEquals(0, errores.get());

        return String.format("%-18s %-10d %-10d %-12d %-10d %-10d",
                contador.nombreEstrategia() + " (" + total + ")", total, valorFinal, milisegundos,
                perdidos, errores.get());
    }

    // Arma el servicio real de login con un repositorio falso que no consulta base de datos.
    private UsuarioServicio crearServicio(IContadorIntentosLogin contador) throws Exception {
        IUsuarioRepositorio repositorioFalso = (IUsuarioRepositorio) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{IUsuarioRepositorio.class},
                (proxy, metodo, args) -> null);

        UsuarioServicio servicio = new UsuarioServicio();
        asignar(servicio, "repositorio", repositorioFalso);
        asignar(servicio, "contadorIntentos", contador);
        return servicio;
    }

    private void asignar(Object objeto, String campo, Object valor) throws Exception {
        Field f = objeto.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        f.set(objeto, valor);
    }
}
