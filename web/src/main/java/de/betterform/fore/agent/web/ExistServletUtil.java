package de.betterform.fore.agent.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exist.EXistException;
import org.exist.http.servlets.Authenticator;
import org.exist.security.AuthenticationException;
import org.exist.security.Subject;
import org.exist.security.XmldbPrincipal;
import org.exist.security.internal.web.HttpAccount;
import org.exist.storage.BrokerPool;
import org.exist.util.Configuration;
import org.exist.util.DatabaseConfigurationException;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.XMLDBException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import org.exist.http.urlrewrite.XQueryURLRewrite;
import org.exist.security.SecurityManager;
import org.exist.http.servlets.BasicAuthenticator;


public class ExistServletUtil implements Serializable {
    private static final Log LOG = LogFactory.getLog(ExistServletUtil.class);

    public static final long serialVersionUID = 804071766041263220L;
    private final static String DEFAULT_ENCODING = "UTF-8";
    private static BrokerPool pool;
    private static String formEncoding = DEFAULT_ENCODING;
    private static String containerEncoding = DEFAULT_ENCODING;
    private static String defaultUsername = SecurityManager.GUEST_USER;
    private static String defaultPassword = SecurityManager.GUEST_USER;
    private static Authenticator authenticator;
    private static Subject defaultUser = null;
    private static boolean internalOnly = false;

    public static BrokerPool getOrCreateBrokerPool(ServletConfig config) throws EXistException, DatabaseConfigurationException, ServletException {

        // Configure BrokerPool
        if (BrokerPool.isConfigured(BrokerPool.DEFAULT_INSTANCE_NAME)) {
            LOG.info("Database already started. Skipping configuration ...");
        } else {
            String confFile = config.getInitParameter("configuration");
            String dbHome = config.getInitParameter("basedir");
            final String start = config.getInitParameter("start");
            if (confFile == null) {
                confFile = "conf.xml";
            }

            if (dbHome == null) {
                dbHome = config.getServletContext().getRealPath("/");
            } else {
                dbHome = config.getServletContext().getRealPath(dbHome);
                if (dbHome == null) {
                    // tomcat 8 workaround: returns null on getRealPath("WEB-INF").
                    // try to detect it differently:
                    String dir = config.getServletContext().getRealPath("/");
                    if (dir != null) {
                        dbHome = new File(dir, "WEB-INF").getAbsolutePath();
                    }
                }
            }
            LOG.info("EXistServlet: exist.home=" + dbHome);

            final File f = new File(dbHome + File.separator + confFile);
            LOG.info("Reading configuration from " + f.getAbsolutePath());
            if (!f.canRead()) {
                throw new ServletException("Configuration file " + confFile + " not found or not readable");
            }

            final Configuration configuration = new Configuration(confFile, dbHome);
            if (start != null && "true".equals(start)) {
                doDatabaseStartup(configuration);
            }
        }

        return BrokerPool.getInstance();
    }

    public static void doDatabaseStartup(Configuration configuration) throws ServletException {
        if (configuration == null) {
            throw new ServletException("Database has not been " + "configured");
        }

        LOG.info("Configuring eXist instance");

        try {
            if (!BrokerPool.isConfigured(BrokerPool.DEFAULT_INSTANCE_NAME)) {
                BrokerPool.configure(1, 5, configuration);
            }
        } catch (final EXistException e) {
            throw new ServletException(e.getMessage(), e);
        } catch (final DatabaseConfigurationException e) {
            throw new ServletException(e.getMessage(), e);
        }

        try {
            LOG.info("Registering XMLDB driver");
            final Class<?> clazz = Class.forName("org.exist.xmldb.DatabaseImpl");
            final Database database = (Database) clazz.newInstance();
            DatabaseManager.registerDatabase(database);
        } catch (final ClassNotFoundException e) {
            LOG.info("ERROR", e);
        } catch (final InstantiationException e) {
            LOG.info("ERROR", e);
        } catch (final IllegalAccessException e) {
            LOG.info("ERROR", e);
        } catch (final XMLDBException e) {
            LOG.info("ERROR", e);
        }
    }

    public static void doGeneralExistServletConfig(ServletConfig config) {
        String option = config.getInitParameter("use-default-user");
        boolean useDefaultUser = true;
        if (option != null) {
            useDefaultUser = "true".equals(option.trim());
        }
        if (useDefaultUser) {
            option = config.getInitParameter("user");
            if (option != null) {
                setDefaultUsername(option);

                option = config.getInitParameter("password");
                if (option != null) {
                    setDefaultPassword(option);
                }

                if (getDefaultUsername() != null) {
                    try {
                        setDefaultUser(getPool().getSecurityManager().authenticate(getDefaultUsername(), getDefaultPassword()));
                    } catch (final AuthenticationException e) {
                        setDefaultUser(null);
                    }
                } else {
                    setDefaultUser(null);
                }
            } else {
                setDefaultUser(pool.getSecurityManager().getGuestSubject());
            }

            if (getDefaultUser() != null) {
                LOG.info("Using default user " + getDefaultUsername() + " for all unauthorized requests.");
            } else {
                LOG.error("Default user " + getDefaultUsername() + " cannot be found.  A BASIC AUTH challenge will be the default.");
            }
        } else {
            LOG.info("No default user.  All requires must be authorized or will result in a BASIC AUTH challenge.");
            setDefaultUser(null);
        }

        setAuthenticator(new BasicAuthenticator(getPool()));

        // get form and container encoding's
        final String configFormEncoding = config.getInitParameter("form-encoding");
        if (configFormEncoding != null) {
            setFormEncoding(configFormEncoding);
        }

        final String configContainerEncoding = config.getInitParameter("container-encoding");
        if (configContainerEncoding != null) {
            setContainerEncoding(configContainerEncoding);
        }

        final String param = config.getInitParameter("hidden");
        if (param != null) {
            internalOnly = Boolean.valueOf(param);
        }
    }

    private Subject authenticate(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (isInternalOnly() && request.getAttribute(XQueryURLRewrite.RQ_ATTR) == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        Principal principal = HttpAccount.getUserFromServletRequest(request);
        if (principal != null) {
            return (Subject) principal;
        }

        // Try to validate the principal if passed from the Servlet engine
        principal = request.getUserPrincipal();

        if (principal != null) {
            if (XmldbPrincipal.class.isAssignableFrom(principal.getClass())) {

                final String username = ((XmldbPrincipal) principal).getName();
                final String password = ((XmldbPrincipal) principal).getPassword();

                LOG.info("Validating Principle: " + username);
                try {
                    return getPool().getSecurityManager().authenticate(username, password);
                } catch (final AuthenticationException e) {
                    LOG.info(e.getMessage());
                }
            }

            if (principal instanceof Subject) {
                return (Subject) principal;
            }
        }

        // Secondly try basic authentication
        final String auth = request.getHeader("Authorization");
        if (auth == null && getDefaultUser() != null) {
            return getDefaultUser();
        }
        return getAuthenticator().authenticate(request, response);
    }

    public boolean isInternalOnly() {
        return internalOnly;
    }

    public void setInternalOnly(boolean internalOnly) {
        this.internalOnly = internalOnly;
    }

    private static Subject getDefaultUser() {
        return defaultUser;
    }

    private static void setDefaultUser(Subject defaultUser) {
        defaultUser = defaultUser;
    }

    private static Authenticator getAuthenticator() {
        return authenticator;
    }

    private static void setAuthenticator(Authenticator authenticator) {
        authenticator = authenticator;
    }

    public static String getDefaultPassword() {
        return defaultPassword;
    }

    private static void setDefaultPassword(String defaultPassword) {
        defaultPassword = defaultPassword;
    }

    private static String getDefaultUsername() {
        return defaultUsername;
    }

    private static void setDefaultUsername(String defaultUsername) {
        defaultUsername = defaultUsername;
    }

    public String getContainerEncoding() {
        return containerEncoding;
    }

    private static void setContainerEncoding(String containerEncoding) {
        containerEncoding = containerEncoding;
    }

    public static String getFormEncoding() {
        return formEncoding;
    }

    private static void setFormEncoding(String formEncoding) {
        formEncoding = formEncoding;
    }

    private static BrokerPool getPool() {
        return pool;
    }

    private void setPool(BrokerPool pool) {
        this.pool = pool;
    }
}