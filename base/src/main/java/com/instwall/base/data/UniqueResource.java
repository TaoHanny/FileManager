package com.instwall.base.data;

public abstract class UniqueResource {
    public final String uri;
    public final String version;

    protected UniqueResource(String uri, String version) {
        this.uri = uri;
        this.version = version;
    }
}
