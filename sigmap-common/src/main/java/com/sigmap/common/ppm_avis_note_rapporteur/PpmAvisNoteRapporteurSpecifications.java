package com.sigmap.common.ppm_avis_note_rapporteur;

import org.springframework.data.jpa.domain.Specification;

public class PpmAvisNoteRapporteurSpecifications {

    private static Specification<PpmAvisNoteRapporteur> notDeleted() {
        return (root, query, cb) ->
                cb.or(
                        cb.isNull(root.get("isDeleted")),
                        cb.isFalse(root.get("isDeleted"))
                );
    }

    public static Specification<PpmAvisNoteRapporteur> id(Integer id) {
        return (root, query, cb) -> {
            if (id == null) {
                return cb.conjunction();
            }
            return cb.and(
                    cb.equal(root.get("id"), id),
                    notDeleted().toPredicate(root, query, cb)
            );
        };
    }

    public static Specification<PpmAvisNoteRapporteur> processInstanceId(String processInstanceId) {
        return (root, query, cb) -> {
            if (processInstanceId == null || processInstanceId.isBlank()) {
                return cb.conjunction();
            }
            return cb.and(
                    cb.equal(root.get("processInstanceId"), processInstanceId),
                    notDeleted().toPredicate(root, query, cb)
            );
        };
    }

    public static Specification<PpmAvisNoteRapporteur> taskId(String taskId) {
        return (root, query, cb) -> {
            if (taskId == null || taskId.isBlank()) {
                return cb.conjunction();
            }
            return cb.and(
                    cb.equal(root.get("taskId"), taskId),
                    notDeleted().toPredicate(root, query, cb)
            );
        };
    }

    public static Specification<PpmAvisNoteRapporteur> ppmId(Integer ppmId) {
        return (root, query, cb) -> {
            if (ppmId == null) {
                return cb.conjunction();
            }
            return cb.and(
                    cb.equal(root.get("ppmId"), ppmId),
                    notDeleted().toPredicate(root, query, cb)
            );
        };
    }

    public static Specification<PpmAvisNoteRapporteur> ppmVer(Integer ppmVer) {
        return (root, query, cb) -> {
            if (ppmVer == null) {
                return cb.conjunction();
            }
            return cb.and(
                    cb.equal(root.get("ppmVer"), ppmVer),
                    notDeleted().toPredicate(root, query, cb)
            );
        };
    }

    public static Specification<PpmAvisNoteRapporteur> ppmIdAndVer(Integer ppmId, Integer ppmVer) {
        return (root, query, cb) -> {
            if (ppmId == null || ppmVer == null) {
                return cb.conjunction();
            }
            return cb.and(
                    cb.equal(root.get("ppmId"), ppmId),
                    cb.equal(root.get("ppmVer"), ppmVer),
                    notDeleted().toPredicate(root, query, cb)
            );
        };
    }

    public static Specification<PpmAvisNoteRapporteur> rapporteurUserId(String userId) {
        return (root, query, cb) -> {
            if (userId == null || userId.isBlank()) {
                return cb.conjunction();
            }
            return cb.and(
                    cb.equal(root.get("rapporteurUserId"), userId),
                    notDeleted().toPredicate(root, query, cb)
            );
        };
    }

    public static Specification<PpmAvisNoteRapporteur> docIdNote(Integer docIdNote) {
        return (root, query, cb) -> {
            if (docIdNote == null) {
                return cb.conjunction();
            }
            return cb.and(
                    cb.equal(root.get("docIdNote"), docIdNote),
                    notDeleted().toPredicate(root, query, cb)
            );
        };
    }

    public static Specification<PpmAvisNoteRapporteur> avisContains(String avis) {
        return (root, query, cb) -> {
            if (avis == null || avis.isBlank()) {
                return cb.conjunction();
            }
            return cb.and(
                    cb.like(cb.lower(root.get("avis")), "%" + avis.toLowerCase() + "%"),
                    notDeleted().toPredicate(root, query, cb)
            );
        };
    }

    public static Specification<PpmAvisNoteRapporteur> hasAvis() {
        return (root, query, cb) ->
                cb.and(
                        cb.isNotNull(root.get("avis")),
                        notDeleted().toPredicate(root, query, cb)
                );
    }

}