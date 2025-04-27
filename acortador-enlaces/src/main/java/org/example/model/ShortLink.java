package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.repository.ShortLinkRepo;
import org.example.util.Propiedades;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
public class ShortLink {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "slb64")
    @GenericGenerator(name = "slb64", strategy = "org.example.repository.generators.Base64SequenceGenerator", parameters = {
            @Parameter(name = "initial_value", value = "0"),
            @Parameter(name = "reserved_list_filename", value = "reserved"),
            @Parameter(name = "reserved_case_sensitive", value = "true")
    })
    private String id;
    private String titulo;
    private String url;
    private Instant createDateTime;

    @JsonIgnore
    @ManyToOne(optional = true)
    Usuario usuario;

    @JsonIgnore
    @ElementCollection(fetch=FetchType.EAGER)
    private List<ShortLinkVisit> visits = new ArrayList<>();

    @Transient
    private int visitsCount;
    @Transient
    private String username = "";
    @Transient
    private int iduser = 0;
    @Transient
    private String userrole = "";

    public ShortLink(String titulo, String url) {
        this.titulo = titulo;
        this.url = url;
        this.createDateTime = Instant.now();
        this.visitsCount = 0;
    }

    public ShortLink(String url) {
        this.url = url;
        this.createDateTime = Instant.now();
        this.visitsCount = 0;
    }

    public ShortLink() {
        this.visitsCount = 0;
    }

    public int getVisitsCount() {
        return visitsCount;
    }

    public void updateVisitsCount() {
        if (usuario == null) {
            this.username = "Desconocido";
            this.iduser = -1;
            this.userrole = "none";
        } else {
            this.username = usuario.getUsuario();
            this.iduser = usuario.getId();
            this.userrole = usuario.getRol();
        }
        this.visitsCount = ShortLinkRepo.getInstance().findById(this.id).getVisits().size();
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Instant getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(Instant createDateTime) {
        this.createDateTime = createDateTime;
    }

    public List<ShortLinkVisit> getVisits() {
        return visits;
    }

    public void setVisits(List<ShortLinkVisit> visits) {
        this.visits = visits;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonInclude
    public String getShortLink() {
        return Propiedades.get("domain")+id;
    }

    @JsonInclude
    public String getUsername() {
        return this.username;
    }

    @JsonInclude
    public int getUserId() {
        return this.iduser;
    }

    @JsonInclude
    public String getUserrole() {
        return this.userrole;
    }
}
