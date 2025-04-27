package org.example.controller;

import io.javalin.http.Context;
import org.example.model.ShortLink;
import org.example.model.ShortLinkVisit;
import org.example.model.Usuario;
import org.example.repository.ShortLinkRepo;
import org.example.repository.UsuarioRepo;
import org.example.templates.Controller;
import org.example.util.Propiedades;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.jetbrains.annotations.NotNull;

import javax.swing.plaf.synth.ColorType;
import java.time.ZonedDateTime;
import java.util.*;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Vistas extends Controller {
    public static final int prioridad = 1;

    @Override
    public void applicarRutas() {
        // Página principal
        app.get("/", ctx -> {
            Map<String, Object> model = new HashMap<>();
            model.put("titulo", "Inicio");
            Usuario usuario = verifyUsuario(ctx);
            if (usuario != null) {
                model.put("inicio", usuario);
                // cuando ya el usuario inició sesión, darle los acortadores que tiene por defectos
                ctx.sessionAttribute("shortlinks", usuario.getMisAcortadores());
            }
            // Para los shortlink
            List<ShortLink> acortadores = null;
            if (ctx.sessionAttribute("shortlinks") != null) {
                acortadores = (List<ShortLink>) ctx.sessionAttribute("shortlinks");
                acortadores.stream().forEach(e -> e.updateVisitsCount());
            }
            if (acortadores != null) {
                model.put("shortlinks", acortadores);
            } else {
                model.put("shortlinks", ctx.sessionAttribute("shortlinks"));
            }

            model.put("error", ctx.queryParam("error"));
            ctx.render("/templates/main.html", model);
        });

        // Login
        app.get("/login", ctx -> {
            // Verificar que el usuario este dentro...
            if (verifyUsuario(ctx) != null) {
                ctx.redirect("/"); // Si hay un usuario, devolver a la página principal.
                return;
            }
            Map<String, Object> model = new HashMap<>();
            model.put("titulo", "Inicio de sesion");
            ctx.render("/templates/login.html", model);
        });

        // Registro de usuarios
        app.get("/registrar", ctx -> {
            // Verificar que el usuario este dentro...
            if (verifyUsuario(ctx) != null) {
                ctx.redirect("/"); // Si hay un usuario, devolver a la página principal.
                return;
            }
            Map<String, Object> model = new HashMap<>();
            model.put("titulo", "Registrarse");
            ctx.render("/templates/registrar.html", model);
        });

        // Administración
        app.get("/admin", ctx -> {
            // Verificar Administrador.
            if (!esAdmin(ctx)) {
                ctx.redirect("/"); // Si hay un usuario, devolver a la página principal.
                return;
            }

            Map<String, Object> model = new HashMap<>();
            Usuario usuario = verifyUsuario(ctx);
            if (usuario != null) {
                model.put("inicio", usuario);
                model.put("probar", usuario.getUsuario().equalsIgnoreCase("admin") ? "admin" : "secadmin");
                // Preparar todos los usuarios.
                List<Usuario> misUsuarios = UsuarioRepo.getInstance().findAll();
                // No enviar contraseña.
                misUsuarios.stream().forEach(e -> e.setPassword(e.getPassword(), ""));
                model.put("misUsuarios", misUsuarios);
            }
            ctx.render("/templates/admin.html", model);
        });

        // Todos los acortadores
        app.get("/acortadores", ctx -> {
            // Verificar Administrador.
            if (!esAdmin(ctx)) {
                ctx.redirect("/"); // Si hay un usuario, devolver a la página principal.
                return;
            }
            // ...
        });

        // Usuario
        app.get("/usuario/:id", ctx -> { // Tambien es como ver "mi cuenta"
            Usuario miUsuario = verifyUsuario(ctx);
            // Verificar que el usuario este dentro...
            String idString = ctx.pathParam("id");
            if (miUsuario == null || idString == null) {
                ctx.redirect("/"); // Si no hay un usuario, devolver a la página principal.
                return;
            }
            int id = -1;
            try {
                id = Integer.parseInt(idString);
            } catch (NumberFormatException e) {
                ctx.redirect("/");
                return; // Porque no me enviaron un entero.
            }

            Usuario comprobar = UsuarioRepo.getInstance().findById(id);
            // Si es null o si tiene un id diferente del usuario actual (que no es admin).
            if ((comprobar == null || miUsuario.getId() != comprobar.getId())
                    && !miUsuario.getRol().equalsIgnoreCase("admin")) { // solo el admin, puede modificar y ver los otros usuarios
                ctx.redirect("/");
                return; // Porque no me enviaron un entero.
            }

            if (comprobar.getRol().equalsIgnoreCase("admin") &&
                    miUsuario.getId() != comprobar.getId() && !miUsuario.getUsuario().equalsIgnoreCase("admin")) {
                // un admin no puede modificar a otro, a menos que sea el admin principal.
                ctx.redirect("/");
                return;
            }
            // modelo
            Map<String, Object> model = new HashMap<>();

            // Para los shortlink
            List<ShortLink> acortadores = comprobar.getMisAcortadores();
            if (acortadores != null) {
                acortadores.stream().forEach(e -> e.updateVisitsCount());
                model.put("shortlinks", acortadores);
            }

            // Enviar usuario en sesión.
            model.put("inicio", miUsuario);
            model.put("perfil", comprobar);
            model.put("infouser", "(" + comprobar.getId() + ") " + comprobar.getUsuario() + " - " + comprobar.getNombre() + ".");
            model.put("dominio", Propiedades.get("domain"));
            // Renderizar.
            ctx.render("/templates/usuario.html", model);
        });

        // resumen.
        app.get("/resumen/:idacortador/:idusuario", ctx -> {
            Usuario actual = verifyUsuario(ctx);
            String idAcortador = ctx.pathParam("idacortador");
            String idUsuario = ctx.pathParam("idusuario");
            int id = -1;
            try {
                id = Integer.parseInt(idUsuario);
            } catch (NumberFormatException e) {
                ctx.redirect("/");
                return;
            }
            // Buscar cada uno.
            Usuario usuario = UsuarioRepo.getInstance().findById(id);
            ShortLink shortLink = ShortLinkRepo.getInstance().findById(idAcortador);

            // si no encontro el usuario, si no encontro el link
            // o si el actual es un usuario nulo.
            if (usuario == null || shortLink == null || actual == null) {
                ctx.redirect("/");
                return;
            }

            // si es un usuario diferente que no tenga permisos de administrador
            // o si es un administrador, tratando de ver el resumen de otro administrador.
            if (actual.getId() != usuario.getId() && !actual.getRol().equalsIgnoreCase("admin")
                    || (actual.getRol().equalsIgnoreCase("admin") &&
                    usuario.getRol().equalsIgnoreCase("admin") &&
                    usuario.getId() != actual.getId() &&
                    !actual.getUsuario().equalsIgnoreCase("admin"))) {
                ctx.redirect("/");
                return;
            }

            Map<String, Object> model = new HashMap<>();
            // Si actual existe, significa que estamos trabajando con un admin.
            if (actual.getId() != usuario.getId()) {
                model.put("actual", actual);     // Usuario actual (por si es un admin).
            }
            shortLink.updateVisitsCount();
            model.put("inicio", usuario);    // Usuario del que se busca la información
            model.put("titulo", "Resumen de: " + shortLink.getId() + "-" + usuario.getId());
            if (shortLink.getVisits().size() == 0) {
                model.put("direcciona", Propiedades.get("domain") + shortLink.getId());
            } else {
                model.put("direcciona", Propiedades.get("domain") + "grafico/"+ shortLink.getId());
            }
            model.put("direccionb", Propiedades.get("domain") + shortLink.getId());
            // Arreglo para obtener los datos de las visitas
            if (shortLink.getVisits().size() > 0) {
                model.put("ultima", shortLink.getVisits().get(shortLink.getVisits().size() - 1));
                // Obtener arreglo de las visitas.
                List<ShortLinkVisit> misVisitas = new ArrayList<>();
                // Tomar últimas 5 visitas.
                for (int i = shortLink.getVisits().size() - 1,  j = 0;
                i >= 0 && j < 5; i--, j++) {
                    misVisitas.add(shortLink.getVisits().get(i));
                }
                model.put("visitas", misVisitas);
            }
            // Información básica
            model.put("link", shortLink.getUrl());
            model.put("acortador", shortLink.getShortLink());
            model.put("cantidadVisitas", shortLink.getVisitsCount());
            // Gráfico
            model.put("graphsource",String.format("/grafico/%s",idAcortador));
            model.put("graphdata", getGraphData(idAcortador));
            // Proceder a la página de estadistica.
            ctx.render("/templates/resumen.html", model);
        });

        // Todos los acortadores.
        app.get("/todo", ctx -> {
            if (!esAdmin(ctx)) {
                ctx.redirect("/");
                return;
            }
            Map<String, Object> model = new HashMap<>();
            model.put("titulo", "Todos los acortadores");
            /*Nota: si puedes poner paginación aquí, sería bueno */
            Usuario usuario = verifyUsuario(ctx);
            if (usuario != null) {
                model.put("inicio", usuario);
                // Enviar todos los acortadores.
                ctx.sessionAttribute("shortlinks", ShortLinkRepo.getInstance().findAll());
            }
            // Para los shortlink
            List<ShortLink> acortadores = null;
            if (ctx.sessionAttribute("shortlinks") != null) {
                acortadores = (List<ShortLink>) ctx.sessionAttribute("shortlinks");
                acortadores.stream().forEach(e -> e.updateVisitsCount());
            }
            if (acortadores != null) {
                model.put("shortlinks", acortadores);
            } else {
                model.put("shortlinks", ctx.sessionAttribute("shortlinks"));
            }

            model.put("error", ctx.queryParam("error"));
            ctx.render("/templates/todo.html", model);
        });

        // En esta parte, redireccionar al gráfico.

        app.get("/grafico/:idacortador", ctx -> {
            Map<String, Object> model = new HashMap<>();
            String idAcortador = ctx.pathParam("idacortador");

            ShortLink shortLink = ShortLinkRepo.getInstance().findById(idAcortador);
            if (shortLink == null) {
                ctx.redirect("/");
                return;
            }
            // No existen datos para hacer un gráfico
            if (shortLink.getVisits().size() == 0) {
                ctx.redirect("/");
                return;
            }
            model.put("graphdata", getGraphData(idAcortador));
            model.put("titulo", "Estado de: " + idAcortador);
            model.put("mensaje", idAcortador);
            model.put("url", shortLink.getShortLink());
            ctx.render("/templates/grafico.html", model);
        });
    }

    private Map<ZonedDateTime, Long> getGraphData(String idacortador) {
        Map<ZonedDateTime, Long> data = ShortLinkRepo.getInstance().countVisitsByIdGroupByDateHour(idacortador);
        Map<ZonedDateTime, Long> result = new HashMap<>();
        for (ZonedDateTime time :
                data.keySet()) {
            result.put(time.plusHours(1), 0L);
            result.put(time.minusHours(1), 0L);
        }
        result.putAll(data);
        return result;
    }
}
