package com.researchspace.webapp.filter;

import com.researchspace.Constants;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Filter to wrap request with a request including user preferred locale. */
@Slf4j
public class LocaleFilter extends OncePerRequestFilter {

  /**
   * This method looks for a "locale" request parameter. If it finds one, it sets it as the
   * preferred locale and also configures it to work with JSTL.
   *
   * @param request the current request
   * @param response the current response
   * @param chain the chain
   * @throws IOException when something goes wrong
   * @throws ServletException when a communication failure happens
   */
  @SuppressWarnings("unchecked")
  public void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    long tStart = System.currentTimeMillis();
    String locale = request.getParameter("locale");
    Locale preferredLocale = null;

    if (locale != null) {
      int indexOfUnderscore = locale.indexOf('_');
      if (indexOfUnderscore != -1) {
        String language = locale.substring(0, indexOfUnderscore);
        String country = locale.substring(indexOfUnderscore + 1);
        preferredLocale = new Locale(language, country);
      } else {
        preferredLocale = new Locale(locale);
      }
    }

    HttpSession session = request.getSession(false);

    if (session != null) {
      if (preferredLocale == null) {
        preferredLocale = (Locale) session.getAttribute(Constants.PREFERRED_LOCALE_KEY);
      } else {
        session.setAttribute(Constants.PREFERRED_LOCALE_KEY, preferredLocale);
        Config.set(session, Config.FMT_LOCALE, preferredLocale);
      }

      if (preferredLocale != null && !(request instanceof LocaleRequestWrapper)) {
        request = new LocaleRequestWrapper(request, preferredLocale);
        LocaleContextHolder.setLocale(preferredLocale);
      }
    }

    String theme = request.getParameter("theme");
    if (theme != null && request.isUserInRole(Constants.ADMIN_ROLE)) {
      Map<String, Object> config = (Map) getServletContext().getAttribute(Constants.CONFIG);
      config.put(Constants.CSS_THEME, theme);
    }

    chain.doFilter(request, response);

    // Reset thread-bound LocaleContext.
    LocaleContextHolder.setLocaleContext(null);

    /* RSDEV-760: LocaleFilter is the first of RSpace filters configured in web.xml, and measuring
    the request processing time here, rather than e.g. in PerformanceLoggingInterceptor, may
    sometimes give a better picture. To enable logging just uncomment the line in log4j2.xml */
    if (log.isDebugEnabled()) {
      long tEnd = System.currentTimeMillis();
      String requestUrl = request.getRequestURI();
      log.debug(
          "It took [{}] ms to complete request to {} (as measured by LocaleFilter)",
          tEnd - tStart,
          requestUrl);
    }
  }
}
