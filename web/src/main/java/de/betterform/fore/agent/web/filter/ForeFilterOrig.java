
package de.betterform.fore.agent.web.filter;

import java.io.IOException;
import java.net.URLDecoder;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.betterform.fore.xml.dom.DOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exist.EXistException;
import org.exist.http.servlets.Authenticator;
import org.exist.http.servlets.BasicAuthenticator;
import org.exist.dom.DocumentImpl;
import org.exist.security.AuthenticationException;
import org.exist.security.Subject;
import org.exist.security.internal.web.HttpAccount;
import org.exist.storage.BrokerPool;
import org.exist.xquery.XPathException;
import org.exist.xquery.value.Sequence;
import org.exist.storage.DBBroker;
import org.exist.xmldb.XmldbURI;
import org.exist.storage.lock.Lock;


/**
 *
 * @author zwobit
 */

public class ForeFilterOrig implements Filter {
    public static final String ATTR_XQUERY_USER = "xquery.user";
    public static final String ATTR_XQUERY_PASSWORD = "xquery.password";

    private static final Log LOG = LogFactory.getLog(ForeFilterOrig.class);
    private BrokerPool pool;
    private Subject defaultUser;
    private Authenticator authenticator;


    private FilterConfig filterConfig;

    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            this.filterConfig = filterConfig;
            this.pool = BrokerPool.getInstance();
            this.defaultUser = this.pool.getSecurityManager().getGuestSubject();
            this.authenticator = new BasicAuthenticator(this.pool);
        } catch (final EXistException ex) {
            LOG.fatal(ex);
            throw new ServletException(ex);
        }
    }

    public BrokerPool getPool() {
        return pool;
    }

    public void setPool(BrokerPool pool) {
        this.pool = pool;
    }

    public Subject getDefaultUser() {
        return defaultUser;
    }

    public void setDefaultUser(Subject defaultUser) {
        this.defaultUser = defaultUser;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Subject user = authenticate((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);


        if( servletRequest.getParameter("test") != null) {
            if ( user.isAuthenticated() ) {
                DBBroker broker = null;
                try {
                    broker = getPool().get(user);
                    final XmldbURI pathUri = XmldbURI.create(URLDecoder.decode("/apps/eXide/build.xml", "UTF-8"));
                    DocumentImpl resource = null;
                    resource = broker.getXMLResource(pathUri, Lock.READ_LOCK);

                    DOMUtil.prettyPrintDOM(resource);
                } catch (Exception e) {

                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
        filterConfig = null;
    }


    protected Subject authenticate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Subject user = getDefaultUser();

        final Object userAttrib = request.getAttribute(ForeFilterOrig.ATTR_XQUERY_USER);
        final HttpSession session = request.getSession( false );

        if(userAttrib != null || (session != null && request.isRequestedSessionIdValid())) {
            final Object passwdAttrib = request.getAttribute(ForeFilterOrig.ATTR_XQUERY_PASSWORD);
            String username;
            String password;
            if (userAttrib != null) {
                username = getValue(userAttrib);
                password = getValue(passwdAttrib);
            } else {
                username = getSessionAttribute(session, "user");
                password = getSessionAttribute(session, "password");
            }

            //TODO authentication should use super.authenticate(...) !!!
            try {
                if( username != null && password != null ) {
                    Subject newUser = getPool().getSecurityManager().authenticate(username, password);
                    if (newUser != null && newUser.isAuthenticated())
                    {user = newUser;}
                }

            } catch (final AuthenticationException e) {
                ForeFilterOrig.LOG.error("User can not be authenticated ("+username+").");
            }
        }

        if (user == getDefaultUser()) {
            Subject requestUser = HttpAccount.getUserFromServletRequest(request);
            if (requestUser != null) {
                user = requestUser;
            } else {
                requestUser = getAuthenticator().authenticate(request, response, false);
                if (requestUser != null)
                {user = requestUser;}
            }
        }


        return user;
/*
//if(isInternalOnly() && request.getAttribute(XQueryURLRewrite.RQ_ATTR) == null) {

        Principal principal = HttpAccount.getUserFromServletRequest(request);
        if (principal != null) {return (Subject) principal;}

        // Try to validate the principal if passed from the Servlet engine
        principal = request.getUserPrincipal();

        if (principal != null) {
            if ( XmldbPrincipal.class.isAssignableFrom( principal.getClass() ) ) {
    
                final String username = ((XmldbPrincipal) principal).getName();
                final String password = ((XmldbPrincipal) principal).getPassword();
    
                ForeFilter.LOG.info("Validating Principle: " + username);
                try {
                    return pool.getSecurityManager().authenticate(username, password);
                } catch (final AuthenticationException e) {
                   ForeFilter. LOG.info(e.getMessage());
                }
            }
    
            if (principal instanceof Subject) {
                return (Subject)principal;
            }
        }

        // Secondly try basic authentication
        final String auth = request.getHeader("Authorization");
        if (auth == null && this.defaultUser != null) {
            return this.defaultUser;
        }
        return authenticator.authenticate(request, response);
        */
    }

    private String getSessionAttribute(HttpSession session, String attribute) {
        final Object obj = session.getAttribute(attribute);
        return getValue(obj);
    }

    private String getValue(Object obj) {
        if(obj == null)
        {return null;}

        if(obj instanceof Sequence)
            try {
                return ((Sequence)obj).getStringValue();
            } catch (final XPathException e) {
                return null;
            }
        return obj.toString();
    }
}

