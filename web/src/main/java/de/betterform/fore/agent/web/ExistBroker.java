package de.betterform.fore.agent.web;

import de.betterform.fore.xml.dom.DOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exist.EXistException;
import org.exist.dom.DocumentImpl;
import org.exist.http.servlets.Authenticator;
import org.exist.http.servlets.BasicAuthenticator;
import org.exist.http.urlrewrite.XQueryURLRewrite;
import org.exist.security.AuthenticationException;
import org.exist.security.PermissionDeniedException;
import org.exist.security.Subject;
import org.exist.security.XmldbPrincipal;
import org.exist.security.internal.web.HttpAccount;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.lock.Lock;
import org.exist.storage.txn.TransactionManager;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.XPathException;
import org.exist.xquery.value.Sequence;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.Principal;

/**
 * Created by joern on 28.11.14.
 */
public class ExistBroker {
    private static final Log LOG = LogFactory.getLog(ExistBroker.class);
    public static final String ATTR_XQUERY_USER = "xquery.user";
    public static final String ATTR_XQUERY_PASSWORD = "xquery.password";

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final HttpSession session;

    private BrokerPool pool;
    private Subject defaultUser;
    private BasicAuthenticator authenticator;


    public ExistBroker(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        this.request=request;
        this.response=response;
        this.session=session;
    }

    public void init() throws EXistException {
        this.pool = BrokerPool.getInstance();
        this.defaultUser = this.pool.getSecurityManager().getGuestSubject();
        this.authenticator = new BasicAuthenticator(this.pool);
        if(LOG.isDebugEnabled()){
            LOG.debug("Pool: " + pool);
            LOG.debug("defaultUser: " + defaultUser);
            LOG.debug("authenticator: " + authenticator);
        }
    }

    /**
     * returns document located at specified location (path) or null if document does not exist.
     * @param path - an absolute database path to the resource document
     * @return the document as w3c.dom.Document or null if the resource does not exist
     * @throws IOException
     * @throws EXistException
     * @throws PermissionDeniedException
     */
    Document getDocument(String path) throws IOException, EXistException, PermissionDeniedException {
        DocumentImpl resource = null;

        DBBroker dbbroker = getDBBroker();
        final XmldbURI pathUri = XmldbURI.create(URLDecoder.decode(path, "UTF-8"));
        resource = dbbroker.getXMLResource(pathUri, Lock.READ_LOCK);

        if(LOG.isDebugEnabled()){
            LOG.debug("ExistBroker getDocument: " + path);
            DOMUtil.prettyPrintDOM(resource);
        }
        return resource;
    }

    void storeDocument(Document model){
        TransactionManager transactionManager = this.pool.getTransactionManager();
        //todo: implement
    }

    DBBroker getDBBroker() throws IOException, EXistException {
        Subject user = authenticate(request, response);
        if ( user.isAuthenticated() ) {
            return this.pool.get(user);
        }
        throw new EXistException("User: " + user + " is not authenticated");
    }

    protected Subject authenticate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Subject user = getDefaultUser();

        final Object userAttrib = request.getAttribute(ATTR_XQUERY_USER);
        final HttpSession session = request.getSession(false);

        if (userAttrib != null || (session != null && request.isRequestedSessionIdValid())) {
            final Object passwdAttrib = request.getAttribute(ATTR_XQUERY_PASSWORD);
            String username;
            String password;
            if (userAttrib != null) {
                username = getValue(userAttrib);
                password = getValue(passwdAttrib);
            } else {
                username = getSessionAttribute(session, "user");
                password = getSessionAttribute(session, "password");
            }

            //TODO authentication should use authenticate from AbstractExistHttpServlet !!!
            try {
                if (username != null && password != null) {
                    Subject newUser = this.pool.getSecurityManager().authenticate(username, password);
                    if (newUser != null && newUser.isAuthenticated()) {
                        user = newUser;
                    }
                }

            } catch (final AuthenticationException e) {
                LOG.error("User can not be authenticated (" + username + ").");
            }
        }

        if (user == getDefaultUser()) {
            Subject requestUser = HttpAccount.getUserFromServletRequest(request);
            if (requestUser != null) {
                user = requestUser;
            } else {
                requestUser = getAuthenticator().authenticate(request, response, false);
                if (requestUser != null) {
                    user = requestUser;
                }
            }
        }


        return user;
    }


    protected Subject getDefaultUser() {
        return this.pool.getSecurityManager().getGuestSubject();
    }

    protected Authenticator getAuthenticator() {
        return new BasicAuthenticator(this.pool);
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
