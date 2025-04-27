package org.example.controller;

import org.example.model.ShortLink;
import org.example.model.Usuario;
import org.example.repository.ShortLinkRepo;
import org.example.repository.UsuarioRepo;
import org.example.templates.Controller;
import org.example.util.Autentificacion;
import org.example.util.Roles;
import org.jasypt.util.password.StrongPasswordEncryptor;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class Acciones extends Controller {
    public static final int prioridad = 1;
    @Override
    public void applicarRutas() {
        app.post("/autenticar", ctx -> {
            //Obteniendo la informacion de la petion. Pendiente validar los parametros.
            String nombreUsuario = ctx.formParam("usuario");
            String password = ctx.formParam("password");
            String recordar = ctx.formParam("remember");

            // Verificar si es correcto.
            if (autenticar(nombreUsuario,password) == Autentificacion.SUCCESSFUL) {
                // Buscar usuario para guardar.
                Usuario initUsuario = UsuarioRepo.getInstance().findByUsername(nombreUsuario);
                if (initUsuario != null) {
                    if (initUsuario.getRol().equalsIgnoreCase("admin")) {
                        initUsuario.setRolesSet(Set.of(Roles.ROLE_ADMIN, Roles.AUTENTICADO));
                    } else {
                        initUsuario.setRolesSet(Set.of(Roles.ROLE_USUARIO));
                    }
                    // Parsear acortadores.
                    ShortLinkController.parsearShortlinks(ctx, initUsuario);
                    initUsuario = UsuarioRepo.getInstance().findByUsername(nombreUsuario);
                    // agregar al session attribute
                    ctx.sessionAttribute("usuario",initUsuario);
                    // Si recordar fue marcado.
                    if (recordar != null) {
                        // Encriptar
                        StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
                        String plainPassword = passwordEncryptor.encryptPassword(initUsuario.getPassword());
                        String plainUsername = passwordEncryptor.encryptPassword(initUsuario.getUsuario());
                        // Guardar en cookies.
                        ctx.cookie("usuario", plainUsername, 604800);
                        ctx.cookie("clave", plainPassword, 604800);
                    }
                    ctx.redirect("/");
                } else {
                    ctx.redirect("/login");
                }
            } else {
                ctx.redirect("/login");
            }
            return;
        });

        app.post("/registrarusuario", ctx -> {
            String nombreUsuario = ctx.formParam("usuario");
            String password = ctx.formParam("claveA");
            String nombreReal = ctx.formParam("nombre");
            Usuario usuario = new Usuario(nombreUsuario, nombreReal, password, "usuario");
            // Guardar usuario.
            if (UsuarioRepo.getInstance().findByUsername(nombreUsuario) == null) {
                UsuarioRepo.getInstance().save(usuario);
                ctx.html("Usuario registrado, volver a: <a href=\"/login\">Login</a>.");
            } else {
                ctx.html("El usuario ya existe, volver a: <a href=\"/registrar\">Registrar</a>.");
            }
            return;
        });

        app.post("/specialregister", ctx -> {
            // Si no es admin.
            if (!esAdmin(ctx)) {
                ctx.redirect("/");
                return;
            }
            String nombreUsuario = ctx.formParam("username");
            String nombreReal = ctx.formParam("nombre");
            String password = ctx.formParam("pass1");
            String rol = ctx.formParam("rol");
            // Buscar usuario.
            if (UsuarioRepo.getInstance().findByUsername(nombreUsuario) == null) {
                // Solo registrar si no existe.
                UsuarioRepo.getInstance().save(new Usuario(nombreUsuario,nombreReal,password, rol));
            }
            ctx.redirect("/admin");
            return;
        });

        app.post("/eliminarusuario", ctx -> {
            // Si no es admin.
            int id = Integer.parseInt(ctx.formParam("idusuario"));
            Usuario usuario = UsuarioRepo.getInstance().findById(id);
            Usuario miUsuario = verifyUsuario(ctx);
            if (!adminNoAdmin(ctx, usuario)) {
                ctx.redirect("/");
                return;
            }
            // Si no, entonces proceder a eliminar.
            // Primero hay que eliminar todos los shortlink que tiene.
            for (ShortLink link:
                 usuario.getMisAcortadores()) {
                ShortLinkRepo.getInstance().remove(link);
            }
            usuario.getMisAcortadores().removeAll(usuario.getMisAcortadores());
            // Luego eliminar el usuario.
            UsuarioRepo.getInstance().remove(usuario);
            ctx.redirect("/admin");
        });

        app.post("/actualizarshort", ctx -> {
            // Si no es admin.
            int id = Integer.parseInt(ctx.formParam("idusuario"));
            String rol = ctx.formParam("rol");
            Usuario usuario = UsuarioRepo.getInstance().findById(id);
            // Se modifica el rol del usuario.
            usuario.setRol(rol);
            UsuarioRepo.getInstance().save(usuario);
            ctx.redirect("/admin");
        });

        app.post("/actualizarperfil", ctx -> {
            int id = -1;
            // En caso de recibir una actualización no deseada.
            try {
                id = Integer.parseInt(ctx.formParam("idusuario"));
            } catch (NumberFormatException e) {
                ctx.redirect("/");
                return;
            }
            // recoger todos los parámetros
            //String usuario = ctx.formParam("username");
            String nombre = ctx.formParam("nombre");
            String rol = ctx.formParam("rol");
            String password = ctx.formParam("pass1");
            // Buscar usuario a modificar.
            Usuario aModificar = UsuarioRepo.getInstance().findById(id);
            // Si las contraseñas son iguales
            if (aModificar.getPassword().compareTo(password) == 0) {
                // proceder a modificar si coinciden.
                aModificar.setRol(rol);
                aModificar.setNombre(nombre);
                UsuarioRepo.getInstance().save(aModificar);
            }
            ctx.redirect("/usuario/"+id);
            return; // volver al mismo lugar
        });

        app.post("/cambiarclave", ctx -> {
            int id = -1;
            // En caso de recibir una actualización no deseada.
            try {
                id = Integer.parseInt(ctx.formParam("idusuario"));
            } catch (NumberFormatException e) {
                ctx.redirect("/");
                return;
            }
            // recoger todos los parámetros
            String anterior = ctx.formParam("pass1");
            String nueva = ctx.formParam("pass2");
            String confirmacion = ctx.formParam("pass3");
            // Buscar usuario.
            Usuario aModificar = UsuarioRepo.getInstance().findById(id);
            // Si obtuve una contraseña anterior que no es
            // o no coinciden la nueva con la confirmación
            if (aModificar.setPassword(anterior, nueva) == Autentificacion.SUCCESSFUL &&
                nueva.compareTo(confirmacion) == 0) {
                // Proceder a cambiar la contraseña.
                UsuarioRepo.getInstance().save(aModificar);
            }
            // Volver al mismo lugar.
            ctx.redirect("/usuario/" + id);
            return;
        });

        app.post("/perfileliminaracortador", ctx-> {
            eliminarAcortador(ctx, true);
        });

        app.post("/eliminaracortador", ctx -> {
            eliminarAcortador(ctx, false);
        });

        app.post("/eliminartodo", ctx -> {
            // Este eliminar, es para acortadores anonimos
            String idacortador = ctx.formParam("idacortador");
            if(!esAdmin(ctx) || idacortador == null) {
               ctx.redirect("/");
               return;
            }
            ShortLink shortLink = ShortLinkRepo.getInstance().findById(idacortador);
            if (shortLink == null) {
                ctx.redirect("/");
                return;
            }
            // Comprobar que no tenga usuario
            if (shortLink.getUsuario() != null) {
                ctx.redirect("/");
                return;
            }
            // Proceder a eliminar.
            ShortLinkRepo.getInstance().remove(shortLink);
            ctx.redirect("/todo");
        });

        app.get("/salir", ctx -> {
            if (ctx.cookie("usuario") != null) {
                // quitar las cookies si sale de la sesión
                ctx.removeCookie("usuario");
                ctx.removeCookie("clave");
            }
            //invalidando la sesion.
            ctx.req.getSession().invalidate();
            ctx.redirect("/login");
            return;
        });
    }

    private boolean adminNoAdmin(Context ctx, Usuario modificar) {
        // Si no es un admin
        if (!esAdmin(ctx) || modificar == null) { // O si modificar es null
            return false;
        }
        Usuario actual = verifyUsuario(ctx);
        // Comprobar que un admin secundario no pueda eliminar a otro admin secundario.
        if (!actual.getUsuario().equalsIgnoreCase("admin") &&
                modificar.getRol().equalsIgnoreCase("admin")) {
            return false;
        }
        return true;
    }

    private void eliminarAcortador(@NotNull Context ctx, boolean admin) {
        String idshort = ctx.formParam("idacortador");
        int idusuario = -1;
        try {
            idusuario = Integer.parseInt(ctx.formParam("idusuario"));
        } catch (NumberFormatException e) {
            ctx.redirect("/");
            return;
        }
        ShortLink aEliminar = ShortLinkRepo.getInstance().findById(idshort);
        if (aEliminar == null) {
            ctx.redirect("/usuario/" + idusuario);
            return;
        }
        Usuario usuario = UsuarioRepo.getInstance().findById(idusuario);
        Usuario actual = verifyUsuario(ctx);
        int index = -1;
        for (int i = 0; i < usuario.getMisAcortadores().size() && index == -1; i++) {
            if (usuario.getMisAcortadores().get(i).getId().compareTo(aEliminar.getId()) == 0) {
                index = i;
            }
        }
        usuario.getMisAcortadores().remove(index);
        aEliminar.setUsuario(null);
        // Eliminar shortlink y  salvar usuario
        UsuarioRepo.getInstance().save(usuario);
        ShortLinkRepo.getInstance().remove(aEliminar);

        if (!admin) {
            ctx.sessionAttribute("usuario", usuario);
            ctx.sessionAttribute("shortlinks", usuario.getMisAcortadores());
            ctx.redirect("/");
        } else {
            // si el usuario actual y el modificar son iguales.
            if (actual.getId() == usuario.getId()) {
                // volver a guardar este parámetro.
                ctx.sessionAttribute("usuario", usuario);
            }
            ctx.redirect("/usuario/"+idusuario);
        }
    }
}
