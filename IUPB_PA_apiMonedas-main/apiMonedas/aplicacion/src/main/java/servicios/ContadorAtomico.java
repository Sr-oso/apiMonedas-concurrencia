package monedas.api.aplicacion.servicios;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Variante C: usa un contador de Java hecho para varios hilos (AtomicLong).
 * Suma de forma segura sin hacer esperar a los demas hilos; es la que usa la aplicacion.
 */
@Component("contadorAtomico")
@Primary
public class ContadorAtomico implements IContadorIntentosLogin {

    private final AtomicLong total = new AtomicLong(0);

    @Override
    public void registrarIntento() {
        total.incrementAndGet();
    }

    @Override
    public long obtenerTotalIntentos() {
        return total.get();
    }

    @Override
    public void reiniciar() {
        total.set(0);
    }

    @Override
    public String nombreEstrategia() {
        return "C - AtomicLong";
    }
}
