package monedas.api.aplicacion.seguridad;

import java.util.concurrent.atomic.AtomicLong;


public class CubetaTokens {
    private final long capacidadMaxima;
    private final long tasaRecargaPorSegundo;
    private AtomicLong tokensActuales;
    private long ultimaRecarga;

    public CubetaTokens(long capacidadMaxima, long tasaRecargaPorSegundo) {
        this.capacidadMaxima = capacidadMaxima;
        this.tasaRecargaPorSegundo = tasaRecargaPorSegundo;
        this.tokensActuales = new AtomicLong(capacidadMaxima);
        this.ultimaRecarga = System.currentTimeMillis();
    }

    public synchronized boolean intentarConsumir() {
        recarga();
        long actual = tokensActuales.get();
        if (actual > 0) {
            tokensActuales.decrementAndGet();
            return true; // Petición permitida
        }
        return false; // Petición rechazada (Rate Limit excedido)
    }

    private void recarga() {
        long ahora = System.currentTimeMillis();
        long tiempoPasado = ahora - ultimaRecarga;

        if (tiempoPasado > 1000) {
            long tokensNuevos = (tiempoPasado / 1000) * tasaRecargaPorSegundo;
            long calculados = Math.min(capacidadMaxima, tokensActuales.get() + tokensNuevos);
            tokensActuales.set(calculados);
            ultimaRecarga = ahora;
        }
    }
}