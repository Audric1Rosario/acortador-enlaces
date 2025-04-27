package org.example.templates;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.reflections.Reflections;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Set;

public abstract class Repositorio<T> {
    private Class<T> entityClass;
    private static SessionFactory sessionFactory = null;

    public static void innit() {
        Configuration config = new Configuration();
        Reflections reflections = new Reflections("org.example.model");
        System.out.println("Detectando las entidades para Hibernate.");
        Set<Class<?>> entities =
                reflections.getTypesAnnotatedWith(Entity.class);
        if(entities.size()==0)
            System.out.println("No se detecto alguna entidad.");
        for (Class<?> entity :
                entities) {
            System.out.printf("Se detecto el @Entity: %s\n", entity.getCanonicalName());
            config.addAnnotatedClass(entity);
        }
        System.out.println("Detectando los integrables para Hibernate.");
        Set<Class<?>> embeddables =
                reflections.getTypesAnnotatedWith(Embeddable.class);
        if(embeddables.size()==0)
            System.out.println("No se detecto algún integrable.");
        for (Class<?> embeddable :
                embeddables) {
            System.out.printf("Se detecto el @Embeddable: %s\n", embeddable.getCanonicalName());
            config.addAnnotatedClass(embeddable);
        }
        sessionFactory = config.buildSessionFactory();
    }

    public Repositorio(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected Session getSession() {
        Session session = sessionFactory.getCurrentSession();
        session.setHibernateFlushMode(FlushMode.ALWAYS);
        return session;
    }

    public T save(T entity) {
        Session session = getSession();
        session.beginTransaction();
        session.saveOrUpdate(entity);
        session.getTransaction().commit();
        return entity;
    }

    public void remove(T entity) {
        Session session = getSession();
        session.beginTransaction();
        session.remove(entity);
        session.getTransaction().commit();
    }

    public void removeById(Object id) {
        T entity = findById(id);
        remove(entity);
    }


    public T findById(Object id) {
        Session session = getSession();
        session.beginTransaction();
        T target = session.find(entityClass, id);
        session.getTransaction().commit();
        return target;
    }

    public T refresh(T entity) {
        Session session = getSession();
        session.beginTransaction();
        session.refresh(entity);
        session.getTransaction().commit();
        return entity;
    }

    public List<T> findAll() {
        Session session = getSession();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = criteriaBuilder.createQuery(entityClass);
        criteria.from(entityClass);
        List<T> lista = session.createQuery(criteria).getResultList();
        session.getTransaction().commit();
        return lista;
    }

    public void commit() {
        getSession().getTransaction().commit();
    }

    public Long countAll() {
        Session session = getSession();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = criteriaBuilder.createQuery(Long.class);
        Root<T> root = criteria.from(entityClass);
        criteria.select(criteriaBuilder.count(root));
        long count = session.createQuery(criteria).getSingleResult();
        session.getTransaction().commit();
        return count;
    }
}
