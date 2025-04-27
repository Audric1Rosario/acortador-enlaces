package org.example.repository;

import org.example.model.ShortLink;
import org.example.model.ShortLinkVisit;
import org.example.templates.Repositorio;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ShortLinkRepo extends Repositorio<ShortLink> {
    private static final ShortLinkRepo instance = new ShortLinkRepo();

    public static ShortLinkRepo getInstance() {
        return instance;
    }

    private ShortLinkRepo() {
        super(ShortLink.class);
    }

    public Map<String, Long> countVisitsByIdGroupByBrowser(String id) {
        return countVisitsByIdGroupByX(id, "v.browser");
    }

    public Map<String, Long> countVisitsByIdGroupByOperativeSystem(String id) {
        return countVisitsByIdGroupByX(id, "v.operativeSystem");
    }

    public Map<String, Long> countVisitsByIdGroupByIpAddress(String id) {
        return countVisitsByIdGroupByX(id, "v.ipAddress");
    }

    public Map<ZonedDateTime, Long> countVisitsByIdGroupByDateHour(String id) {
        Map<ZonedDateTime, Long> result = new HashMap<>();
        Session session = getSession();
        session.beginTransaction();
        Query query = session.createQuery("SELECT extract(year FROM v.time), extract(month FROM v.time), extract(day FROM v.time), extract(hour FROM v.time), count(*) FROM ShortLink a LEFT JOIN a.visits v WHERE a.id=:id GROUP BY extract(year FROM v.time), extract(month FROM v.time), extract(day FROM v.time), extract(hour FROM v.time)");
        query.setParameter("id", id);
        Iterator resultado = query.getResultList().iterator();
        while (resultado.hasNext()) {
            Object[] row = (Object[]) resultado.next();
            if(row[0] == null)
                continue;
            ZonedDateTime datetime = ZonedDateTime.of((int) row[0],(int) row[1],(int) row[2],(int) row[3],0,0,0, ZoneId.of("UTC"));
            result.put(datetime, (Long) row[row.length-1]);
        }
        session.getTransaction().commit();
        return result;
    }

    private Map<String, Long> countVisitsByIdGroupByX(String id, String x) {
        Map<String, Long> results = new HashMap<>();
        Session session = getSession();
        session.beginTransaction();
        Query query = session.createQuery(String.format("SELECT %s, count(*) FROM ShortLink a LEFT JOIN a.visits v WHERE a.id=:id GROUP BY %s", x, x));
        query.setParameter("id", id);
        Iterator resultado = query.getResultList().iterator();
        while (resultado.hasNext()) {
            Object[] row = (Object[]) resultado.next();
            if (row[0] == null)
                continue;
            results.put((String) row[0], (long) row[1]);
        }
        session.getTransaction().commit();
        return results;
    }
}
