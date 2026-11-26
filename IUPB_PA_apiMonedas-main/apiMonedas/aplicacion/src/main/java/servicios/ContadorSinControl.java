package monedas.api.aplicacion.servicios;

import org.springframework.stereotype.Component;

/**
 * Variante A: cuenta los intentos con un numero normal, sin ninguna proteccion.
 * Si varios hilos suman al mismo tiempo, algunos intentos se pierden.
 */
@Component("contadorSinControl")
public class ContadorSinControl implements IContadorIntentosLogin {

    private long total = 0;

    @Override
    public void registrarIntento() {
        total++;
    }

    @Override
    public long obtenerTotalIntentos() {
        return total;
    }

    @Override
    public void reiniciar() {
        total = 0;
    }

    @Override
    public String nombreEstrategia() {
        return "A - Sin control";
    }
}
