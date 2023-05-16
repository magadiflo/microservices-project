package com.magadiflo.msp.infrastructure.zuul.app.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Como esta clase es un componente y además
 * hereda de ZuulFilter, automáticamente
 * Spring lo registra como un filtro de Zuul
 * <p>
 * TIPOS DE FILTRO
 * Pre -> Se ejecuta antes de que el request sea enrutado -> Se usa para pasar datos al request
 * Post -> Se ejecuta después de que el request haya sido enrutado -> Se usa para modificar la respuesta
 * Route -> Se ejecuta durante el enrutado del request, aquí se resuelve la ruta -> Se usa para la comunicación con el microservicio
 */

@Component
public class PreTiempoTranscurridoFilter extends ZuulFilter {

    private final Logger LOG = LoggerFactory.getLogger(PreTiempoTranscurridoFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    // Validamos si ejecutamos o no el filtro
    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        LOG.info("{} request enrutado a {}", request.getMethod(), request.getRequestURL().toString());

        Long tiempoInicio = System.currentTimeMillis();
        request.setAttribute("tiempoInicio", tiempoInicio);
        return null;
    }
}
