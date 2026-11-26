package monedas.api.infraestructura.persistencia.mapeadores;

import monedas.api.dominio.entidades.Pais;
import monedas.api.infraestructura.persistencia.entidades.PaisEntidad;

public class PaisMapeador {

    public static Pais haciaDominio(PaisEntidad entidad) {
        if (entidad == null) {
            return null;
        }
        return new Pais(
                entidad.getId(),
                entidad.getNombre(),
                entidad.getCodigoAlfa2(),
                entidad.getCodigoAlfa3(),
                MonedaMapeador.haciaDominio(entidad.getMoneda())
            );
    }

    public static PaisEntidad haciaEntidad(Pais pais) {
        if (pais == null) {
            return null;
        }
        return new PaisEntidad(
                pais.getId(),
                pais.getNombre(),
                pais.getCodigoAlfa2(),
                pais.getCodigoAlfa3(),
                MonedaMapeador.haciaEntidad(pais.getMoneda())
            );
    }
}
