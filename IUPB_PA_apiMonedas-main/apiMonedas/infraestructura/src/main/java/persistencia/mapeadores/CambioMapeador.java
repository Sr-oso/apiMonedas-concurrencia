package monedas.api.infraestructura.persistencia.mapeadores;

import monedas.api.dominio.entidades.Cambio;
import monedas.api.infraestructura.persistencia.entidades.CambioEntidad;

public class CambioMapeador {

    public static Cambio haciaDominio(CambioEntidad entidad) {
        if (entidad == null) {
            return null;
        }
        return new Cambio(
            entidad.getId(),
            MonedaMapeador.haciaDominio(entidad.getMoneda()), 
            entidad.getValor(),
            entidad.getFecha()
        );
    }

    public static CambioEntidad haciaEntidad(Cambio cambio) {
        if (cambio == null) {
            return null;
        }
        return new CambioEntidad(
            cambio.getId(),
            MonedaMapeador.haciaEntidad(cambio.getMoneda()), 
            cambio.getValor(),
            cambio.getFecha()
        );
    }
}