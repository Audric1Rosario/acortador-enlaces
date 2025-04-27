package org.example.service;


import org.apache.commons.validator.routines.UrlValidator;
import org.example.model.ShortLink;
import org.example.model.ShortLinkVisit;
import org.example.repository.ShortLinkRepo;
import org.example.util.CustomBase64;

public class ShortLinkService {
    private static final ShortLinkService instance = new ShortLinkService();

    public static ShortLinkService getInstance() {
        return instance;
    }

    private UrlValidator urlValidator;
    private ShortLinkRepo shortLinkRepo;
    private ShortLinkService() {
        this.urlValidator = new UrlValidator();
        this.shortLinkRepo = ShortLinkRepo.getInstance();
    }

    public ShortLink registrar(String url) {
        ShortLink shortLink = null;
        if(!urlValidator.isValid(url)) {
            return shortLink;
        }
        int startHost = url.indexOf("://")+3;
        String protocol = url.substring(0, startHost).toLowerCase();
        String host = url.substring(startHost);
        int endHost = host.indexOf("/");
        if(endHost==-1)
            endHost = host.length();
        host = host.substring(0, endHost).toLowerCase();
        shortLink = new ShortLink(protocol+host+url.substring(endHost+startHost));
        shortLinkRepo.save(shortLink);
        return shortLink;
    }
}
