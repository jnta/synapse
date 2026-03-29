package org.synapse.infrastructure.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.synapse.domain.model.VaultInfo;
import org.synapse.infrastructure.persistence.LocalFileNoteRepository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Path("/api/v1/vault")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class VaultResource {

    @Inject
    LocalFileNoteRepository noteRepository;


    private final List<VaultInfo> recentVaults = new CopyOnWriteArrayList<>();

    @GET
    public VaultInfo getCurrentVault() {
        java.nio.file.Path path = noteRepository.getVaultPath();
        return new VaultInfo(
            path.toString(),
            path.getFileName() != null ? path.getFileName().toString() : "Vault",
            System.currentTimeMillis()
        );
    }

    @POST
    @Path("/open")
    public Response openVault(VaultInfo vault) {
        noteRepository.setVaultPath(java.nio.file.Path.of(vault.path()));
        
        // Add to recents if not exists
        recentVaults.removeIf(v -> v.path().equals(vault.path()));
        recentVaults.add(0, new VaultInfo(
            vault.path(),
            vault.name() != null ? vault.name() : java.nio.file.Path.of(vault.path()).getFileName().toString(),
            System.currentTimeMillis()
        ));
        
        return Response.ok(getCurrentVault()).build();
    }

    @GET
    @Path("/recent")
    public List<VaultInfo> getRecentVaults() {
        return recentVaults;
    }
}
