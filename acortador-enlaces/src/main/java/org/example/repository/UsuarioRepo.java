package org.example.repository;

import org.example.model.Usuario;
import org.example.templates.Repositorio;
import org.hibernate.Session;

import javax.persistence.Query;
import java.util.List;

public class UsuarioRepo extends Repositorio<Usuario> {
    private static final UsuarioRepo instance = new UsuarioRepo();

    public static UsuarioRepo getInstance() {
        return instance;
    }

    private UsuarioRepo() {
        super(Usuario.class);
    }

    // Consultas
    public List<Usuario> findAllByNombre(String nombre){
        Session em = getSession();
        em.beginTransaction();
        Query query = em.createQuery("select e from Usuario e where e.nombre like :nombre");
        query.setParameter("nombre", nombre+"%");
        List<Usuario> lista = query.getResultList();
        em.getTransaction().commit();
        return lista;
    }

    public List<Usuario> findAllByUsuario(String usuario){
        Session em = getSession();
        em.beginTransaction();
        Query query = em.createQuery("select e from Usuario e where e.usuario like :usuario");
        query.setParameter("usuario", usuario+"%");
        List<Usuario> lista = query.getResultList();
        em.getTransaction().commit();
        return lista;
    }

    public Usuario findByUsername(String username) {
        Session em = getSession();
        em.beginTransaction();
        Query query = em.createQuery("select e from Usuario e where e.usuario = :username");
        query.setParameter("username", username);
        Usuario usuario = ((List<Usuario>)query.getResultList())
                .stream().filter(e -> e.getUsuario().equalsIgnoreCase(username))
                .findFirst().orElse( null);
        em.getTransaction().commit();
        return usuario;
    }
}

