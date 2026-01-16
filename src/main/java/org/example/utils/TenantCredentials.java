package org.example.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.utils.SharedData.*;

public class TenantCredentials {
    private final String name;
    private final boolean critical;
    private final String url;
    private final String userName;
    private String accessToken;
    private String tokenExpirationDateTime;
    private Map<String, String> cookies = new HashMap<>();
    private Map<String, String> csrfTokens = new HashMap<>();

    public TenantCredentials(String name, boolean critical, String url, String userName, String accessToken, String tokenExpirationDateTime) {
        this.name = name;
        this.critical = critical;
        this.url = url;
        this.userName = userName;
        this.accessToken = accessToken;
        this.tokenExpirationDateTime = tokenExpirationDateTime;
    }

    public TenantCredentials(String name, boolean critical, String url, String userName) {
        this.name = name;
        this.critical = critical;
        this.url = url;
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public boolean isCritical() {
        return critical;
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getAccessToken() {
        return accessToken;
    }


    public String getCsrfToken(String endpoint) {
        if (csrfTokens.get(endpoint) != null && ! csrfTokens.get(endpoint).isEmpty() ){
            return csrfTokens.get(endpoint);
        }
        else {
            return "";
        }
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setCsrfToken(String endpoint, String token) {
        this.csrfTokens.put(endpoint, token);
    }

    public String getTokenExpirationDateTime() {
        return tokenExpirationDateTime;
    }

    public void setTokenExpirationDateTime(String tokenExpirationDateTime) {
        this.tokenExpirationDateTime = tokenExpirationDateTime;
    }

    public LocalDateTime convertToLocalDateTime(String dateTimeStr) {
        String dateTimeStrWithoutUTC = dateTimeStr.replace(" UTC", "");
        return LocalDateTime.parse(dateTimeStrWithoutUTC, DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER_PATTERN));
    }

    public boolean isTokenValid() {
        if (accessToken == null || tokenExpirationDateTime == null) {
            return false;
        }

        return LocalDateTime.now(ZoneId.of("UTC")).isBefore(convertToLocalDateTime(tokenExpirationDateTime).minusMinutes(5));
    }

    public static void setTenantCredentialsList(List<TenantCredentials> newTenantCredentialsList) {
        tenantCredentialsList = newTenantCredentialsList;
    }

    public static void addTenantCredentials(TenantCredentials tenant) {
        tenantCredentialsList.add(tenant);
    }

    public static void updateExistingTenantCredentials(TenantCredentials oldTenant, TenantCredentials newTenant) {
        if (oldTenant.isTokenValid() && !newTenant.isTokenValid()) {
            newTenant.setAccessToken(oldTenant.getAccessToken());
            newTenant.setTokenExpirationDateTime(oldTenant.getTokenExpirationDateTime());
        }

        int index = tenantCredentialsList.indexOf(oldTenant);
        tenantCredentialsList.remove(oldTenant);

        if (index >= 0) {
            tenantCredentialsList.add(index, newTenant);
        } else {
            tenantCredentialsList.add(newTenant);
        }
    }

    public static void deleteTenantCredentials(TenantCredentials tenant) {
        tenantCredentialsList.remove(tenant);
    }


    public static TenantCredentials getTenantObjectByCredentials(String url, String clientid) {
        return tenantCredentialsList.stream()
                .filter(tenant -> tenant.getUrl().equals(url) &&
                        tenant.getUserName().equals(clientid))
                .findFirst()
                .orElseGet(() -> {
                    TenantCredentials newTenant = new TenantCredentials(url, false, url, clientid, null, null);
                    tenantCredentialsList.add(newTenant);
                    return newTenant;
                });
    }

    public String getCookie(String endpoint) {
        if (cookies.get(endpoint) != null &&! cookies.get(endpoint).isEmpty() ){
            return cookies.get(endpoint);
        }
        else {
            return "";
        }
    }

    public void setCookie(String endpoint, String cookie) {
        this.cookies.put(endpoint, cookie);
    }
}
