package se.kry.codetest.model;

import io.vertx.core.json.JsonObject;

public class Service {
    public static final String ALIAS = "alias";
    public static final String URL = "url";
    public static final String MODIFY_DATE = "modifyDate";
    public static final String STATUS = "status";
    private String alias;
    private String status;
    private String url;
    private Long modifyDate;

    public Service(final String alias, final String status, final String url, final Long modifyDate) {
        this.alias = alias;
        this.status = status;
        this.url = url;
        this.modifyDate = modifyDate;
    }

    public static Service from(final JsonObject jsonBody) {
        return new Service(jsonBody.getString(ALIAS), jsonBody.getString(STATUS), jsonBody.getString(URL), jsonBody.getLong(MODIFY_DATE));
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(final String alias) {
        this.alias = alias;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public Long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(final Long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final ServiceStatus status) {
        this.status = status.name();
    }

    public enum ServiceStatus {
        UNKNOWN("UNKNOWN"), OK("OK"), FAIL("FAIL");
        private String value;

        ServiceStatus(String value) {
            this.value = value;
        }
    }
}
