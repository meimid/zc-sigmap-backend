package com.sigmap.common.dyna_docs_model;

import com.sigmap.common.dyna_docs_model.request.DynaDocsModelRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface DynaDocsModelService {
    /**
     * Récupère tous les modèles de documents dynamiques
     * @return Liste des modèles de documents dynamiques
     */
    List<DynaDocsModel> findAll();

    /**
     * Récupère une page de modèles de documents dynamiques avec pagination et filtrage
     * @param spec Spécifications pour le filtrage
     * @param pageable Paramètres de pagination
     * @return Page de modèles de documents dynamiques
     */
    Page<DynaDocsModel> findAll(Specification<DynaDocsModel> spec, Pageable pageable);

    /**
     * Recherche un modèle de document dynamique par son identifiant
     * @param id Identifiant du modèle
     * @return Le modèle trouvé ou un Optional vide
     */
    Optional<DynaDocsModel> findById(Integer id);

    /**
     * Enregistre un nouveau modèle de document dynamique
     * @param dynaDocsModel Le modèle à enregistrer
     * @return Le modèle enregistré
     */
    DynaDocsModel save(DynaDocsModelRequest dynaDocsModel);

    /**
     * Met à jour un modèle de document dynamique existant
     * @param id Identifiant du modèle à mettre à jour
     * @param request Les nouvelles données du modèle
     * @return Le modèle mis à jour
     */
    DynaDocsModel update(Integer id, DynaDocsModelRequest request);

    /**
     * Supprime un modèle de document dynamique
     * @param id Identifiant du modèle à supprimer
     */
    void delete(Integer id);

    /**
     * Récupère la liste des modèles de documents dynamiques liés aux documents dynamiques
     * ayant le même type de marché et mode de passation que la réalisation de l'appel d'offre.
     * @param appelOffreId Identifiant de l'appel d'offre
     * @return Liste des modèles de documents dynamiques correspondants
     */
    List<DynaDocsModel> findByAppelOffre(Integer appelOffreId);
}
