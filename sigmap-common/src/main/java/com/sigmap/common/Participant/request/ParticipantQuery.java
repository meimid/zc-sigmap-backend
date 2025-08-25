package com.sigmap.common.Participant.request;

import com.sigmap.common.Participant.Participant;
import com.sigmap.common.Participant.ParticipantSpecifications;
import com.sigmap.common.config.ParticipantType;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class ParticipantQuery {
    private String search;
    private Integer reunionId;
    private String userId;
    private ParticipantType type;
    private Boolean present;
    private boolean deleted;

    /**
     * Creates a {@link Specification} from the properties of the current object.
     *
     * @return a {@link Specification} that can be used to query the database.
     */
    public Specification<Participant> toSpecification() {
        // Start with the mandatory deleted status filter.
        Specification<Participant> spec = Specification.where(ParticipantSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(ParticipantSpecifications.search(search));
        }

        if (reunionId != null) {
            spec = spec.and(ParticipantSpecifications.reunionId(reunionId));
        }

        if (userId != null && !userId.isBlank()) {
            spec = spec.and(ParticipantSpecifications.userId(userId));
        }

        if (type != null) {
            spec = spec.and(ParticipantSpecifications.type(type));
        }

        if (present != null) {
            spec = spec.and(ParticipantSpecifications.present(present));
        }

        return spec;
    }
}