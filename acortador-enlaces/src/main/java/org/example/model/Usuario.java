package org.example.model;

import org.example.util.Autentificacion;
import org.example.util.Roles;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
public class Usuario {
    // Variables
    @Id
    @GeneratedValue
    private int id;
    private String usuario;
    private String nombre;
    private String password;
    private String rol;

    // Lista de shortlinks que tiene mi usuario
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ShortLink> misAcortadores;

    @Transient
    private Set<Roles> rolesSet;
    // Nota: rolesSet, se actualizara cuando se llene la lista de usuarios

    // Constructores
    public Usuario() {
    }

    public Usuario(String usuario, String password) {
        this.usuario = usuario;
        this.password = password;
    }

    public Usuario(String usuario, String nombre, String password) {
        this.usuario = usuario;
        this.nombre = nombre;
        this.password = password;
    }

    public Usuario(String usuario, String nombre, String password, Set<Roles> rolesSet) {
        this.usuario = usuario;
        this.nombre = nombre;
        this.password = password;
        this.rolesSet = rolesSet;
    }

    public Usuario(String usuario, String nombre, String password, String rol) {
        this.usuario = usuario;
        this.nombre = nombre;
        this.password = password;
        this.rol = rol;
    }

    public Usuario(String usuario, String nombre, String password, String rol, Set<Roles> rolesSet) {
        this.usuario = usuario;
        this.nombre = nombre;
        this.password = password;
        this.rol = rol;
        this.rolesSet = rolesSet;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Autentificacion setPassword(@NotNull String oldPassword, String password) {
        if (oldPassword.compareTo(this.password) == 0) {
            this.password = password;
            return Autentificacion.SUCCESSFUL;
        }
        return Autentificacion.WRONG_PASSWORD;
    }

    public String getPassword() {
        return password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Set<Roles> getRolesSet() {
        return rolesSet;
    }

    public void setRolesSet(Set<Roles> rolesSet) {
        this.rolesSet = rolesSet;
    }

    public List<ShortLink> getMisAcortadores() {
        return misAcortadores;
    }

    // Método para autenticarse (Iniciar sesión)
    public Autentificacion logIn(@NotNull String usuario, String password) {
        if (usuario.compareTo(this.usuario) == 0) {
            return checkPassword(password);
        }
        return Autentificacion.WRONG_USERNAME;
    }

    // Comprobar que la contraseña sea correcta.
    public Autentificacion checkPassword(@NotNull String probar) {
        if (probar.compareTo(this.password) == 0) {
            return Autentificacion.SUCCESSFUL;
        }
        return Autentificacion.WRONG_PASSWORD;
    }

    // Cambiar todo
    public Autentificacion cambiarCredenciales (@NotNull Usuario nuevasCredenciales) {
        this.nombre = nuevasCredenciales.getNombre();
        this.password = nuevasCredenciales.getPassword();
        return Autentificacion.SUCCESSFUL;
    }
}
