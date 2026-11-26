package monedas.api.aplicacion.servicios;

import org.springframework.stereotype.Component;

/**
 * Variante B: solo deja que un hilo a la vez sume o lea el contador (synchronized).
 * El resultado siempre es correcto, pero los hilos tienen que esperar su turno.
 */
@Component("contadorSincronizado")
public class ContadorSincronizado implements IContadorIntentosLogin {

    private long total = 0;

    @Override
    public synchronized void registrarIntento() {
        total++;
    }

    @Override
    public synchronized long obtenerTotalIntentos() {
        return total;
    }

    @Override
    public synchronized void reiniciar() {
        total = 0;
    }

    @Override
    public String nombreEstrategia() {
        return "B - synchronized";
    }
}
