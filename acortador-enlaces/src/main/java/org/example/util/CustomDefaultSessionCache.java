package org.example.util;

import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.Session;
import org.eclipse.jetty.server.session.SessionData;
import org.eclipse.jetty.server.session.SessionHandler;
import org.example.model.ShortLink;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Set;

public class CustomDefaultSessionCache extends DefaultSessionCache {
    /**
     * @param manager The SessionHandler related to this SessionCache
     */
    public CustomDefaultSessionCache(SessionHandler manager) {
        super(manager);
    }

    //pregenerar algunos atributos
    private void defaultAttributes(SessionData data) {
        data.setAttribute("shortlinks", new ArrayList<ShortLink>());
        data.setAttribute("usuario",null);
    }

    @Override
    public Session newSession(HttpServletRequest request, SessionData data)
    {
        defaultAttributes(data);
        return super.newSession(request, data);
    }

//    @Override
//    public void add(String id, Session session) throws Exception {
//        System.out.println(id);
//        super.add(id,session);
//    }

    @Override
    public Session newSession(SessionData data)
    {
        defaultAttributes(data);
        return super.newSession(data);
    }
}
