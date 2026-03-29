package org.synapse.domain.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class SlugGenerator {

    public String generateSlug(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\-/]", "-") // Allow forward slash
                .replaceAll("-+", "-")
                .replaceAll("/+", "/")
                .replaceAll("^-|-$|^/|/$", "");
        
        if (slug.isEmpty()) {
            slug = "untitled-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return slug;
    }
}
