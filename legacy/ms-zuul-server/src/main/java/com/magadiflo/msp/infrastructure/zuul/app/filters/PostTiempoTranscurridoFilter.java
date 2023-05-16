package com.magadiflo.msp.infrastructure.zuul.app.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class PostTiempoTranscurridoFilter extends ZuulFilter {

    private final Logger LOG = LoggerFactory.getLogger(PostTiempoTranscurridoFilter.class);

    @Override
    public String filterType() {
        return "post";
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
        LOG.info("Entrando al post filter");

        Long tiempoInicio = (Long) request.getAttribute("tiempoInicio");
        Long tiempoFinal = System.currentTimeMillis();
        Long tiempTranscurrido = tiempoFinal - tiempoInicio;

        LOG.info("Tiempo transcurrido en milisegundos: {}", tiempTranscurrido);
        LOG.info("Tiempo transcurrido en segundos: {}", (tiempTranscurrido.doubleValue() / 1000.0));
        return null;
    }
}
