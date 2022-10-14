package ru.kata.spring.boot_security.demo.configs.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Выводт в лог заголовки запросов и ответов. Удобен для отладки.
 *
 */
@Component // The Security Filters in SecurityFilterChain are typically Beans - вот и сделаем его бином )
public class HeadersSpyFilter extends GenericFilterBean {
    private static final Logger log = LoggerFactory.getLogger(HeadersSpyFilter.class);


    public HeadersSpyFilter() {
        super();
        log.debug("HeadersSpyFilter::");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        log.debug("doFilter: <- ");

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String url = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        log.debug("doFilter: request = '" + url +  ((queryString!=null) ?  ("?" + queryString+"'") : "'"));

        Enumeration<String> names = request.getHeaderNames();
        log.debug("doFilter: request headers =>");
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Enumeration<String > values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                String value = values.nextElement();
                log.debug(String.format("doFilter: %s: %s", name, value));
            }
        }

        chain.doFilter(req, res);

        Collection<String> n = response.getHeaderNames();
        Iterator<String> it = n.iterator();
        log.debug("doFilter: response headers =>");
        while (it.hasNext()) {
            String key = it.next();
            Collection<String> values = response.getHeaders(key);
            log.debug(String.format("doFilter: %s: %s", key, values));
        }
        log.debug("doFilter: -> ");
    }
}