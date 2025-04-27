package org.example.model;

import javax.persistence.Embeddable;
import java.time.Instant;

@Embeddable
public class ShortLinkVisit {
    private String browser;
    private String operativeSystem;
    private String ipAddress;
    private Instant time;

    public ShortLinkVisit() {
    }

    public ShortLinkVisit(String browser, String operativeSystem, String ipAddress) {
        this.browser = browser;
        this.operativeSystem = operativeSystem;
        this.ipAddress = ipAddress;
        this.time = Instant.now();
    }

    @Override
    public String toString() {
        return "ShortLinkVisit{" +
                "browser='" + browser + '\'' +
                ", operativeSystem='" + operativeSystem + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", time=" + time +
                '}';
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOperativeSystem() {
        return operativeSystem;
    }

    public void setOperativeSystem(String operativeSystem) {
        this.operativeSystem = operativeSystem;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }
}
