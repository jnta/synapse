package org.synapse.infrastructure.rest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.synapse.application.dto.CreateNoteCommand;
import org.synapse.application.dto.NoteDTO;
import org.synapse.application.dto.UpdateNoteCommand;
import org.synapse.application.usecase.NoteUseCases;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/api/v1/notes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NoteResource {

    private final NoteUseCases noteUseCases;

    public NoteResource(NoteUseCases noteUseCases) {
        this.noteUseCases = noteUseCases;
    }

    @GET
    public List<NoteDTO> getAllNotes() {
        return noteUseCases.getAllNotes();
    }

    @GET
    @Path("/{id: .+}")
    public NoteDTO getNote(@Encoded @PathParam("id") String id) {
        NoteDTO note = noteUseCases.getNote(decode(id));
        if (note != null && note.content() != null) {
            String snippet = note.content().substring(0, Math.min(note.content().length(), 50)).replace("\n", "\\n");
            System.out.println("[NoteResource] GET content snippet: " + snippet);
        }
        return note;
    }

    @POST
    public Response createNote(CreateNoteCommand command) {
        NoteDTO created = noteUseCases.createNote(command);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id: .+}")
    public NoteDTO updateNote(@Encoded @PathParam("id") String id, UpdateNoteCommand command) {
        if (command != null && command.content() != null) {
            System.out.println("[NoteResource] PUT content length: " + command.content().length());
            System.out.println("[NoteResource] PUT has newlines: " + command.content().contains("\n"));
        }
        return noteUseCases.updateNote(decode(id), command);
    }

    @DELETE
    @Path("/{id: .+}")
    public Response deleteNote(@Encoded @PathParam("id") String id) {
        noteUseCases.deleteNote(decode(id));
        return Response.noContent().build();
    }

    private String decode(String id) {
        return URLDecoder.decode(id, StandardCharsets.UTF_8);
    }

    @POST
    @Path("/folders")
    public Response createFolder(CreateFolderRequest request) {
        noteUseCases.createFolder(request.path());
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/folders")
    public List<String> getAllFolders() {
        return noteUseCases.getAllFolders();
    }

    @POST
    @Path("/move")
    public Response moveNode(MoveRequest request) {
        noteUseCases.moveNode(request.source(), request.target());
        return Response.noContent().build();
    }

    public record CreateFolderRequest(String path) {}
    public record MoveRequest(String source, String target) {}
}
