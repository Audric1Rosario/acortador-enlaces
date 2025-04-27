package org.example.controller;

import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.example.model.ShortLink;
import org.example.model.ShortLinkVisit;
import org.example.model.Usuario;
import org.example.repository.ShortLinkRepo;
import org.example.repository.UsuarioRepo;
import org.example.service.ShortLinkService;
import org.example.util.CustomBase64;
import org.example.templates.Controller;
import org.example.util.exception.CustomBase64DecodingException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShortLinkController extends Controller {
    private final static Object[] NULO = new Object[]{null};

    public static final int prioridad = 2;

    @Override
    public void applicarRutas() {
        ShortLinkRepo shortLinkRepo = ShortLinkRepo.getInstance();
        ShortLinkService shortLinkService = ShortLinkService.getInstance();
        UserAgentAnalyzer uaa = UserAgentAnalyzer
                .newBuilder()
                .hideMatcherLoadStats()
                .withCache(10000)
                .build();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/YYYY HH:mm:ss").withZone(ZoneId.from(ZoneOffset.UTC));

        app.get("/:id", ctx -> {
            String id = ctx.pathParam("id");
            try {
                /* preview el titulo del link y su descripción,
                ver algún link de bit.ly y ponerle un +
                Fuentes:
                https://support.bitly.com/hc/en-us/articles/360039553192-How-to-verify-a-Bitly-link-s-destination
                https://support.bitly.com/hc/en-us/articles/230650447-Can-I-preview-a-link-before-clicking-on-it-
                */
                if (id.endsWith("+")) {
                    id = id.substring(0, id.length() - 1);
                    Map<String, Object> model = new HashMap<>();
                    ShortLink shortLink = shortLinkRepo.findById(id);
                    Usuario usuario = verifyUsuario(ctx);
                    model.put("inicio", usuario);
                    model.put("shortlink", shortLink);
                    ctx.render("/templates/linkplus.html", model);
                } else {
                    //el redirect
                    ShortLink shortLink = shortLinkRepo.findById(id);
                    UserAgent userAgent = uaa.parse(ctx.userAgent());
                    String os = userAgent.getValue(UserAgent.OPERATING_SYSTEM_NAME_VERSION_MAJOR);
                    String browser = userAgent.getValue(UserAgent.AGENT_NAME);
                    String ip = ctx.header("X-Forwarded-For");
                    if (ip == null)
                        ip = ctx.ip();
                    shortLink.getVisits().add(new ShortLinkVisit(browser, os, ip));
                    shortLinkRepo.save(shortLink);
                    ctx.redirect(shortLink.getUrl());
                }
            } catch (NumberFormatException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                throw new InternalServerErrorResponse(sw.toString());
            } catch (NullPointerException e) {
                //esto pasa si el que se intenta decodar es inválido.
                ctx.status(404);
                ctx.res.setCharacterEncoding(StandardCharsets.UTF_8.name());
                ctx.result(new String("Link inválida.".getBytes(), StandardCharsets.UTF_8));
            }
        });

        app.post("/data/add", ctx -> {
            Usuario usuario = verifyUsuario(ctx);
            String link = ctx.formParam("link");
            if (!link.toLowerCase().startsWith("http://") && !link.toLowerCase().startsWith("https://"))
                link = "http://" + link;
            ShortLink shortLink = shortLinkService.registrar(link);
            if (usuario != null) {
                usuario.getMisAcortadores().add(shortLink);
                UsuarioRepo.getInstance().save(usuario);
                shortLink.setUsuario(usuario);
                ShortLinkRepo.getInstance().save(shortLink);
                // Luego cambiarlo en shortlinks y usuario
                ctx.sessionAttribute("usuario", usuario);
                ctx.sessionAttribute("shortlinks", usuario.getMisAcortadores());
                ctx.redirect("/");
            } else {
                if (shortLink != null) {
                    ((List<ShortLink>) ctx.sessionAttribute("shortlinks")).add(shortLink);
                    ctx.redirect("/");
                } else
                    ctx.redirect("/?error=1");
            }
        });
    }

    public static void parsearShortlinks(Context ctx, Usuario usuario) {
        // asignarle el usuario al shortlink y guardarlos todos.
        List<ShortLink> acortadores = (List<ShortLink>) ctx.sessionAttribute("shortlinks");
        for (int i = 0; i < acortadores.size(); i++) {
            // agregar usuario
            acortadores.get(i).setUsuario(usuario);
            // guardar en la base de datos
            ShortLinkRepo.getInstance().save(acortadores.get(i));
            usuario.getMisAcortadores().add(acortadores.get(i));
        }
        // guardar usuario + sus acortadores.
        UsuarioRepo.getInstance().save(usuario);
    }
}
