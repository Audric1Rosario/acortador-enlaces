package org.example.templates;

import io.javalin.http.Context;
import org.example.App;
import io.javalin.Javalin;
import org.example.model.Usuario;
import org.example.repository.UsuarioRepo;
import org.example.util.Autentificacion;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class Controller {
    protected final Javalin app;

    protected Controller() {
        this.app = App.getInstance();
    }

    public abstract void applicarRutas();

    protected Autentificacion autenticar(String usuario, String password) {
        Usuario temp = UsuarioRepo.getInstance().findByUsername(usuario);
        if (temp == null) {
            return Autentificacion.BOTH_WRONG;
        }
        return temp.logIn(usuario, password);
    }

    protected Usuario verifyUsuario(@NotNull Context ctx) {
        Usuario usuario = (Usuario) ctx.sessionAttribute("usuario");
        if (usuario == null && ctx.cookie("usuario") != null && ctx.cookie("clave") != null) {
            // Verificar si existen cookies.
            String plainUsuario = ctx.cookie("usuario");
            String plainPassword = ctx.cookie("clave");

            StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
            // Buscar
            List<Usuario> usuarios = UsuarioRepo.getInstance().findAll();
            int i = 0;
            while (usuario == null && i < usuarios.size()) {
                if (passwordEncryptor.checkPassword(usuarios.get(i).getUsuario(), plainUsuario)) {
                    usuario = usuarios.get(i);
                }
                i++;
            }
            // Proceder a evaluar contraseña
            if (usuario != null) {
                if (!passwordEncryptor.checkPassword(usuario.getPassword(), plainPassword)) {
                    usuario = null;
                } else {
                    // si las cookies son correctas, guardar esto en la session.
                    // agregar al session attribute
                    ctx.sessionAttribute("usuario",usuario);
                }
            }
        }
        return usuario;
    }

    protected boolean esAdmin(@NotNull Context ctx) {
        Usuario usuario = verifyUsuario(ctx);
        if (usuario != null) {
            if (usuario.getRol().equalsIgnoreCase("admin")) {
                return true;
            }
        }
        return false;
    }
}
