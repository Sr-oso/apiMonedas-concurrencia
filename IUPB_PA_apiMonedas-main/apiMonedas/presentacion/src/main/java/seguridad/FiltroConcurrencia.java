package monedas.api.presentacion.seguridad;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import monedas.api.aplicacion.seguridad.CubetaTokens;
import java.io.IOException;

@Component
public class FiltroConcurrencia extends OncePerRequestFilter {

    private final CubetaTokens tokenBucket = new CubetaTokens(200, 100);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        //System.out.println("🔍 Filtro interceptando URI: " + request.getRequestURI()); // <-- Añade esto temporalmente

        // Aplicar el límite únicamente a los endpoints de la API de monedas
        if (request.getRequestURI().startsWith("/api/monedas")) {
            // Intentar consumir un token de la cubeta
            boolean permitido = tokenBucket.intentarConsumir(); //

            if (!permitido) {
                // Si la cubeta está vacía, rechazamos la petición inmediatamente con HTTP 429
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("429 Too Many Requests - Limite de velocidad excedido");
                return;
            }
        }

        // Si hay tokens disponibles, la petición continúa su flujo normal hacia el controlador
        filterChain.doFilter(request, response);
    }


}
