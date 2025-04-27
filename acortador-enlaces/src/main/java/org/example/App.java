package org.example;

import io.javalin.plugin.json.JavalinJackson;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.NullSessionDataStore;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionHandler;
import org.example.model.Usuario;
import org.example.repository.UsuarioRepo;
import org.example.repository.generators.Base64SequenceGenerator;
import org.example.templates.Controller;
import io.javalin.Javalin;
import io.javalin.plugin.rendering.JavalinRenderer;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import org.example.templates.Repositorio;
import org.example.util.CustomDefaultSessionCache;
import org.h2.tools.Server;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;

public class App {
    private static Javalin app = null;

    @NotNull
    private static Supplier<SessionHandler> SessionHandlerSupplier() {
        final SessionHandler sessionHandler = new SessionHandler();
// [..] add persistence DB etc. management here [..]
        SessionCache sessionCache = new CustomDefaultSessionCache(sessionHandler);
        sessionCache.setSessionDataStore(new NullSessionDataStore());
        sessionHandler.setSessionCache(sessionCache);
        sessionHandler.getSessionCookieConfig().setHttpOnly(true);
        sessionHandler.getSessionCookieConfig().setSecure(true);
        sessionHandler.getSessionCookieConfig().setComment("__SAME_SITE_STRICT__");
        return () -> sessionHandler;
    }

    public static Javalin getInstance() {
        if (app == null) {
            app = Javalin.create(config -> {
                config.addStaticFiles("/static");
                config.sessionHandler(SessionHandlerSupplier());
            });
            JavalinJackson.getObjectMapper().findAndRegisterModules();

            //region Para poder hacer que Thymeleaf use UTF-8
            TemplateEngine templateEngine = new TemplateEngine();
            ClassLoaderTemplateResolver bt = new ClassLoaderTemplateResolver();
            bt.setTemplateMode(TemplateMode.HTML);
            bt.setCharacterEncoding(StandardCharsets.UTF_8.name());
            templateEngine.setTemplateResolver(bt);
            JavalinThymeleaf.configure(templateEngine);
            JavalinRenderer.register(JavalinThymeleaf.INSTANCE, ".html");
            //endregion

            //region Identifando controladores y aplicando rutas
            Reflections reflections = new Reflections("org.example.controller");
            Set<Class<? extends Controller>> controladoresClass =
                    reflections.getSubTypesOf(Controller.class);
            List<Class<? extends Controller>> controladores = new ArrayList<>(controladoresClass);
            Collections.sort(controladores, new Comparator<Class<? extends Controller>>() {
                @Override
                public int compare(Class<? extends Controller> o1, Class<? extends Controller> o2) {
                    int vo1 = 0;
                    int vo2 = 0;
                    try {
                        vo1 = o1.getField("prioridad").getInt(null);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    try {
                        vo2 = o2.getField("prioridad").getInt(null);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return vo1 - vo2;
                }
            });
            int i = 0;
            for (Class<? extends Controller> controlador :
                    controladores) {
                try {
                    controlador.getConstructor().newInstance().applicarRutas();
                    System.out.printf("(%d/%d) Se han aplicado las rutas del controlador \"%s\".\n", ++i, controladores.size(), controlador.getSimpleName());
                } catch (InstantiationException | NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

            }
            Repositorio.innit();
            //endregion
        }
        return app;
    }

    public static void main(String[] args) {
        try {
            Server dbH2Server = Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers", "-ifNotExists").start();
            System.out.printf("H2 SQL Server fue iniciado en: %s\n", dbH2Server.getURL());
            Server dbH2WebServer = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
            System.out.printf("H2 Web Server fue iniciado en: %s\n", dbH2WebServer.getURL());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Javalin app = getInstance().start();
        if (UsuarioRepo.getInstance().countAll() == 0) {
            // Guardar el primer usuario (administrador).
            Usuario administrador = new Usuario("admin", "admin", "admin", "admin");
            UsuarioRepo.getInstance().save(administrador);
        }
    }

}
