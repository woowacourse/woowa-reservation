package com.h3.reservation.slack.filter;

import com.h3.reservation.slack.service.VerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;

@Component
public class VerificationFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(VerificationFilter.class);

    private static final String SIGNING_SECRET = System.getenv("SIGNING_SECRET");

    private final VerificationService verificationService;
    private FilterConfig filterConfig;

    public VerificationFilter() throws NoSuchAlgorithmException, InvalidKeyException {
        this.verificationService = new VerificationService(Clock.systemUTC(), SIGNING_SECRET);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {
        CachedRequestWrapper wrapper = new CachedRequestWrapper((HttpServletRequest) req);
        String timestamp = wrapper.getHeader("X-Slack-Request-Timestamp");
        String requestBody = wrapper.getBody();
        String signature = wrapper.getHeader("X-Slack-Signature");
        logger.debug("Request Body: {}", wrapper.getBody());
        if (verificationService.verify(timestamp, requestBody, signature)) {
            filterChain.doFilter(wrapper, res);
        } else {
            ((HttpServletResponse) res).sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }
}
