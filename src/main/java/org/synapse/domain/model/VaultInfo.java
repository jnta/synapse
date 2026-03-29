package org.synapse.domain.model;


public record VaultInfo(
    String path,
    String name,
    long lastAccessed
) {}
