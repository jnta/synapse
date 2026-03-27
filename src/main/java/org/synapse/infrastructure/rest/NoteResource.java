package org.synapse.infrastructure.rest;

import org.synapse.application.dto.CreateNoteCommand;
import org.synapse.application.dto.NoteDTO;
import org.synapse.application.dto.UpdateNoteCommand;
import org.synapse.application.usecase.NoteUseCases;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

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
    @Path("/{id}")
    public NoteDTO getNote(@PathParam("id") String id) {
        return noteUseCases.getNote(id);
    }

    @POST
    public Response createNote(CreateNoteCommand command) {
        NoteDTO created = noteUseCases.createNote(command);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public NoteDTO updateNote(@PathParam("id") String id, UpdateNoteCommand command) {
        return noteUseCases.updateNote(id, command);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteNote(@PathParam("id") String id) {
        noteUseCases.deleteNote(id);
        return Response.noContent().build();
    }
}
