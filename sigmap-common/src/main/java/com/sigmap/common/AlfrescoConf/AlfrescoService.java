//package com.sigmap.common.AlfrescoConf;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
//import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
//
//import java.io.IOException;
//import java.security.Principal;
//import java.util.*;
//
//@Service
//public class AlfrescoService {
//    @Value("${alfresco.url}")
//    private String alfrescoUrl;
//    private final AlfrescoTokenHolder tokenHolder;
//    private final RestTemplate restTemplate;
//
//    public AlfrescoService(AlfrescoTokenHolder tokenHolder, RestTemplate restTemplate) {
//        this.tokenHolder = tokenHolder;
//        this.restTemplate = restTemplate;
//    }
//
//
//    public String uploadToExistingFolder(MultipartFile file, String folderId) throws IOException {
//        try {
//            // Create a modified version of alfrescoService.uploadDocument that takes folderId directly
//            // instead of folderPath, thus skipping the getOrCreateFolderByPath step
//
//            // Construct the URL for file upload to Alfresco
//            String url = alfrescoUrl + "/api/-default-/public/alfresco/versions/1/nodes/"
//                    + folderId + "/children"
//                    + "?majorVersion=true&alf_ticket=" + tokenHolder.getTicket();
//
//            // Create HTTP headers for the request
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//            // Convert the file to ByteArrayResource
//            ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()) {
//                @Override
//                public String getFilename() {
//                    return file.getOriginalFilename();
//                }
//            };
//
//            // Create multipart request body
//            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//            body.add("filedata", fileAsResource);
//            body.add("name", file.getOriginalFilename());
//            body.add("nodeType", "cm:content");
//
//            // Create request entity with headers and body
//            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
//
//            // Send POST request to upload the file
//            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
//
//            // Parse response to get document ID
//            String responseBody = response.getBody();
//            JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
//            String nodeId = jsonNode.path("entry").path("id").asText();
//            String versionLabel = jsonNode.path("entry").path("versionLabel").asText();
//
//            // Update document properties if needed
//           updateProperties(nodeId);
//
//            // Return document ID and version
//            return nodeId + ";" + versionLabel;
//        } catch (Exception e) {
//            throw new IOException("Error uploading document to existing folder: " + e.getMessage(), e);
//        }
//    }
//
//
//
//    /**
//     * Télécharge un document dans un dossier Alfresco spécifique.
//     * Si le dossier n'existe pas, il est créé avant l'upload.
//     *
//     * @param file Le fichier à télécharger
//     * @param folderPath Le chemin du dossier dans lequel le fichier doit être téléchargé
//     * @return L'ID du document téléchargé et sa version sous forme "nodeId;versionLabel"
//     * @throws Exception Si une erreur se produit lors de l'upload ou de la mise à jour des propriétés
//     */
//    public String uploadDocument(MultipartFile file, String folderPath) throws Exception {
//        // 1. Récupère ou crée le dossier où le fichier sera téléchargé
//        String folderId = getOrCreateFolderByPath(folderPath);
//
//        // 2. Construit l'URL pour l'upload du fichier dans Alfresco
//        String url = alfrescoUrl + "/api/-default-/public/alfresco/versions/1/nodes/"
//                + folderId + "/children"
//                + "?majorVersion=true&alf_ticket=" + tokenHolder.getTicket(); // Ajoute le ticket d'authentification
//
//        // 3. Crée les en-têtes HTTP pour la requête (type de contenu multipart pour l'upload)
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//        // 4. Convertit le fichier en un objet de type ByteArrayResource pour l'inclure dans la requête
//        ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()) {
//            @Override
//            public String getFilename() {
//                return file.getOriginalFilename(); // Renvoie le nom original du fichier
//            }
//        };
//
//        // 5. Crée le corps de la requête multipart (fichier à télécharger, nom, type de noeud)
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("filedata", fileAsResource); // Ajoute le fichier dans le corps
//        body.add("name", file.getOriginalFilename()); // Ajoute le nom du fichier
//        body.add("nodeType", "cm:content"); // Type de noeud : contenu
//
//        // 6. Crée l'entité de la requête avec les en-têtes et le corps
//        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
//
//        // 7. Envoie la requête POST pour télécharger le fichier dans le dossier Alfresco
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
//
//        // 8. Récupère la réponse JSON contenant les informations du fichier téléchargé
//        String responseBody = response.getBody();
//        JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
//        String nodeId = jsonNode.path("entry").path("id").asText(); // ID du document
//        String versionLabel = jsonNode.path("entry").path("versionLabel").asText(); // Version du document
//
//        // 9. Met à jour les propriétés du fichier téléchargé
//        updateProperties(nodeId);
//
//        // 10. Retourne l'ID et la version du fichier sous forme de chaîne "nodeId;versionLabel"
//        return nodeId + ";" + versionLabel;
//    }
//
//    /**
//     * Met à jour les propriétés d'un document dans Alfresco.
//     * Cette méthode met à jour les propriétés par défaut comme l'auteur du document.
//     *
//     * @param nodeId L'ID du document à mettre à jour
//     * @throws Exception Si une erreur se produit lors de la mise à jour des propriétés
//     */
//    public void updateProperties(String nodeId) throws Exception {
//        // 1. Construit l'URL pour mettre à jour le document dans Alfresco
//        String url = alfrescoUrl + "/api/-default-/public/alfresco/versions/1/nodes/" + nodeId + "?alf_ticket=" + tokenHolder.getTicket();
//
//        // 2. Prépare les propriétés à mettre à jour (ici, l'auteur du document)
//        Map<String, Object> props = new HashMap<>();
//        props.put("cm:author", Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
//                .map(Principal::getName)); // On récupère l'auteur à partir de l'authentification
//
//        // 3. Prépare le corps de la requête avec les nouvelles propriétés
//        Map<String, Object> updateBody = new HashMap<>();
//        updateBody.put("properties", props);
//
//        // 4. Crée les en-têtes pour la requête PUT (type JSON)
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        // 5. Crée l'entité de la requête avec les en-têtes et le corps
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(updateBody, headers);
//
//        // 6. Envoie la requête PUT pour mettre à jour les propriétés du document
//        restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
//    }
//
//
//
//
//    /**
//     * Récupère ou crée un dossier dans Alfresco en fonction du chemin fourni.
//     * La méthode parcourt chaque dossier dans le chemin donné et, si le dossier n'existe pas, il est créé.
//     *
//     * @param path Le chemin du dossier dans Alfresco, sous forme de chaîne de caractères (par exemple "folder1/folder2").
//     * @return L'ID du dernier dossier dans le chemin (soit un dossier existant, soit un dossier créé).
//     * @throws Exception Si une erreur se produit lors de l'appel à l'API Alfresco
//     */
//    public String getOrCreateFolderByPath(String path) throws Exception {
//        // Sépare le chemin du dossier en parties individuelles (dossiers séparés par "/")
//        String[] parts = path.split("/");
//
//        // Initialisation de l'ID parent à "-root-" pour le premier dossier (le dossier racine)
//        String parentId = "-root-"; // ID du dossier racine dans Alfresco
//
//        // Parcours chaque dossier dans le chemin donné
//        for (String folderName : parts) {
//            // Ignore les parties vides ou nulles (ex : si le chemin commence ou finit par "/")
//            if (folderName == null || folderName.trim().isEmpty()) continue;
//
//            // 1. Récupérer les enfants du dossier actuel (on recherche seulement des dossiers)
//            String url = alfrescoUrl +
//                    "/api/-default-/public/alfresco/versions/1/nodes/" + parentId +
//                    "/children?where=(nodeType='cm:folder')" +  // Filtre pour ne récupérer que des dossiers
//                    "&alf_ticket=" + tokenHolder.getTicket(); // Paramètre pour l'authentification
//
//            // Création des headers d'authentification pour la requête
//            HttpHeaders headers = new HttpHeaders();
//            HttpEntity<Void> request = new HttpEntity<>(headers);
//
//            // Envoi de la requête pour récupérer les enfants du dossier actuel
//            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
//
//            // Extraction de la liste des dossiers enfants depuis la réponse
//            List<Map<String, Object>> entries = (List<Map<String, Object>>)
//                    ((Map<String, Object>) response.getBody().get("list")).get("entries");
//
//            boolean found = false; // Variable pour savoir si le dossier a été trouvé
//
//            // Parcours les enfants pour vérifier si le dossier actuel existe déjà
//            if (entries != null) {
//                for (Map<String, Object> entryMap : entries) {
//                    Map<String, Object> entry = (Map<String, Object>) entryMap.get("entry");
//                    // Si le nom du dossier correspond, met à jour l'ID parent et marque comme trouvé
//                    if (entry.get("name").equals(folderName)) {
//                        parentId = (String) entry.get("id"); // Met à jour l'ID parent avec celui du dossier trouvé
//                        found = true;
//                        break; // Si trouvé, on quitte la boucle
//                    }
//                }
//            }
//
//            // 2. Si le dossier n'a pas été trouvé, on doit le créer
//            if (!found) {
//                try {
//                    // URL pour créer un nouveau dossier sous le dossier parent
//                    String createUrl = alfrescoUrl +
//                            "/api/-default-/public/alfresco/versions/1/nodes/" + parentId +
//                            "/children?alf_ticket=" + tokenHolder.getTicket(); // Paramètre pour l'authentification
//
//                    // Headers pour la requête de création de dossier (avec Content-Type JSON)
//                    HttpHeaders createHeaders = new HttpHeaders();
//                    createHeaders.setContentType(MediaType.APPLICATION_JSON);
//
//                    // Corps de la requête de création avec le nom du dossier et son type
//                    Map<String, Object> folderBody = new HashMap<>();
//                    folderBody.put("name", folderName); // Nom du dossier
//                    folderBody.put("nodeType", "cm:folder"); // Type de noeud : cm:folder (dossier)
//
//                    // Envoi de la requête POST pour créer le dossier
//                    HttpEntity<Map<String, Object>> createRequest = new HttpEntity<>(folderBody, createHeaders);
//                    ResponseEntity<Map> createResponse = restTemplate.exchange(createUrl, HttpMethod.POST, createRequest, Map.class);
//
//                    // Extraction de l'ID du dossier créé et mise à jour de l'ID parent
//                    Map<String, Object> createdEntry = (Map<String, Object>) createResponse.getBody().get("entry");
//                    parentId = (String) createdEntry.get("id");
//                } catch (HttpClientErrorException.Conflict conflictEx) {
//                    // Handle the case where the folder already exists (409 conflict)
//                    // Try to fetch the folder ID again since it might have been created by another thread
//                    response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
//                    entries = (List<Map<String, Object>>)
//                            ((Map<String, Object>) response.getBody().get("list")).get("entries");
//
//                    if (entries != null) {
//                        for (Map<String, Object> entryMap : entries) {
//                            Map<String, Object> entry = (Map<String, Object>) entryMap.get("entry");
//                            if (entry.get("name").equals(folderName)) {
//                                parentId = (String) entry.get("id");
//                                found = true;
//                                break;
//                            }
//                        }
//                    }
//
//                    if (!found) {
//                        // If we still can't find it, throw the original exception
//                        throw conflictEx;
//                    }
//                }
//            }
//        }
//
//        // Retourne l'ID du dernier dossier dans le chemin
//        return parentId; // L'ID du dossier final (existant ou créé)
//    }
//
//    // Consultation de document
//    /**
//     * Récupère les informations d'un document depuis Alfresco en utilisant son ID et sa version.
//     * Cette méthode envoie une requête à l'API d'Alfresco pour obtenir les détails du document.
//     *
//     * @param alfrescoId L'ID du document dans le format "nodeId;version"
//     * @return La réponse de l'API Alfresco contenant les informations du document en format JSON
//     * @throws IllegalArgumentException Si l'ID du document est mal formé
//     * @throws Exception Si une erreur se produit lors de l'appel à l'API
//     */
//    public String getDocumentInformation(String alfrescoId) {
//        // Décompose l'ID du document en deux parties : nodeId et version
//        String[] parts = alfrescoId.split(";");
//
//        // Vérifie que l'ID est valide et contient bien deux parties : le nodeId et la version
//        if (parts.length != 2) {
//            // Si l'ID est invalide, une exception est levée
//            throw new IllegalArgumentException("L'ID de document est invalide. Il doit contenir à la fois un ID et une version.");
//        }
//
//        // Assigne les valeurs extraites de l'alfrescoId
//        String nodeId = parts[0];  // ID du document
//        String version = parts[1];  // Version du document (non utilisé ici, mais peut être utile pour des appels futurs)
//
//        // Construction de l'URL pour récupérer les informations du document
//        // L'URL inclut le nodeId et un ticket d'authentification
//        String url = alfrescoUrl + "/api/-default-/public/alfresco/versions/1/nodes/" + nodeId
//                + "?alf_ticket=" + tokenHolder.getTicket(); // Paramètre de ticket pour l'authentification
//
//        // Récupération des headers d'authentification depuis le tokenHolder (qui gère les informations d'authentification)
//        HttpHeaders headers = new HttpHeaders(); // Si d'autres headers sont nécessaires, on peut les ajouter ici
//
//        // Création d'une entité HTTP vide (pas de corps de requête ici) mais avec les headers d'authentification
//        HttpEntity<Void> entity = new HttpEntity<>(headers);
//
//        // Envoi de la requête GET à l'API Alfresco pour récupérer les informations du document
//        // La réponse sera au format JSON et sera stockée sous forme de chaîne de caractères
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//
//        // Retourne la réponse du serveur Alfresco, contenant les informations du document au format JSON
//        return response.getBody();
//    }
//
//
//    /**
//     * Récupère le contenu d'un document spécifique dans Alfresco basé sur son ID de nœud et sa version.
//     *
//     * Le format attendu pour l'identifiant est : "nodeId;version" (ex: "abc123;1.0").
//     *
//     * Étapes réalisées :
//     *  - Valide et découpe l'identifiant fourni.
//     *  - Récupère la liste des versions du document depuis Alfresco.
//     *  - Recherche la version spécifique demandée.
//     *  - Télécharge le contenu binaire associé à la version.
//     *
//     * Retourne un objet DocumentResponseDTO contenant :
//     *  - le fichier sous forme de tableau de bytes (`file`)
//     *  - un indicateur d'erreur (`erreur`)
//     *  - un message d'erreur si une exception survient (`msg`)
//     *
//     * En cas d'erreur (ex: ID invalide, version introuvable, problème de communication),
//     * le champ `erreur` sera à true et `msg` contiendra la description de l'erreur.
//     *
//     * @param alfrescoId identifiant composite du document (format "nodeId;version")
//     * @return DocumentResponseDTO contenant le fichier ou l'erreur
//     */
//    public DocumentResponseDTO getDocumentById(String alfrescoId) {
//        DocumentResponseDTO responseDTO = new DocumentResponseDTO();
//
//        try {
//            // Vérification de l'ID reçu
//            String[] parts = alfrescoId.split(";");
//            if (parts.length != 2) {
//                throw new IllegalArgumentException("L'ID de document est invalide. Il doit contenir à la fois un ID et une version.");
//            }
//
//            String nodeId = parts[0];
//            String version = parts[1];
//
//            // Construction de l'URL pour récupérer les versions
//            String url = alfrescoUrl + "/api/-default-/public/alfresco/versions/1/nodes/"
//                    + nodeId + "/versions?alf_ticket=" + tokenHolder.getTicket();
//
//            // Préparation des headers
//            HttpHeaders headers = new HttpHeaders();
//            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//
//            // Appel pour récupérer les versions du document
//            ResponseEntity<String> response = restTemplate.exchange(
//                    url,
//                    HttpMethod.GET,
//                    entity,
//                    String.class
//            );
//
//            // Parsing de la réponse JSON
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode root = objectMapper.readTree(response.getBody());
//
//            JsonNode versions = root.path("list").path("entries");
//
//            String versionId = null;
//            if (versions.isArray()) {
//                for (JsonNode entry : versions) {
//                    JsonNode versionEntry = entry.path("entry");
//                    if (versionEntry.has("id") && versionEntry.get("id").asText().equals(version)) {
//                        versionId = versionEntry.get("id").asText();
//                        break;
//                    }
//                }
//            }
//
//            // Vérification si la version demandée existe
//            if (versionId == null) {
//                throw new IllegalArgumentException("La version " + version + " du document " + nodeId + " n'existe pas.");
//            }
//
//            // Construction de l'URL pour télécharger le fichier
//            String contentUrl = alfrescoUrl + "/api/-default-/public/alfresco/versions/1/nodes/"
//                    + nodeId + "/versions/" + versionId + "/content?alf_ticket=" + tokenHolder.getTicket();
//
//            // Téléchargement du fichier
//            ResponseEntity<byte[]> contentResponse = restTemplate.exchange(
//                    contentUrl,
//                    HttpMethod.GET,
//                    entity,
//                    byte[].class
//            );
//
//            // Retourner la réponse sans erreur
//            responseDTO.setFile(contentResponse.getBody());
//            responseDTO.setErreur(false);
//            responseDTO.setMsg("");
//
//        } catch (Exception e) {
//            // En cas d'erreur, capturer l'exception
//            responseDTO.setErreur(true);
//            responseDTO.setMsg(e.getMessage());
//            responseDTO.setFile(null);
//        }
//
//        return responseDTO;
//    }
//
//
//
//
//}
package com.sigmap.common.AlfrescoConf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriUtils;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;

@Service
@Slf4j
public class AlfrescoService {
    @Value("${alfresco.url}")
    private String alfrescoUrl;
    private final AlfrescoTokenHolder tokenHolder;
    private final RestTemplate restTemplate;

    public AlfrescoService(AlfrescoTokenHolder tokenHolder, RestTemplate restTemplate) {
        this.tokenHolder = tokenHolder;
        this.restTemplate = restTemplate;
    }




    /**
     * Télécharge un document dans un dossier Alfresco spécifique.
     * Si le dossier n'existe pas, il est créé avant l'upload.
     *
     * @param file Le fichier à télécharger
     * @param folderPath Le chemin du dossier dans lequel le fichier doit être téléchargé
     * @return L'ID du document téléchargé et sa version sous forme "nodeId;versionLabel"
     * @throws Exception Si une erreur se produit lors de l'upload ou de la mise à jour des propriétés
     */
    public String uploadDocument(MultipartFile file, String folderPath) throws Exception {
        // 1. Récupère ou crée le dossier où le fichier sera téléchargé
        String folderId = getOrCreateFolderByPath(folderPath);

        // 2. Construit l'URL pour l'upload du fichier dans Alfresco
        String url = alfrescoUrl + "/api/-default-/public/alfresco/versions/1/nodes/"
                + folderId + "/children"
                + "?majorVersion=true&alf_ticket=" + tokenHolder.getTicket(); // Ajoute le ticket d'authentification

        // 3. Crée les en-têtes HTTP pour la requête (type de contenu multipart pour l'upload)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 4. Convertit le fichier en un objet de type ByteArrayResource pour l'inclure dans la requête
        ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename(); // Renvoie le nom original du fichier
            }
        };

        // 5. Crée le corps de la requête multipart (fichier à télécharger, nom, type de noeud)
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("filedata", fileAsResource); // Ajoute le fichier dans le corps
        body.add("name", file.getOriginalFilename()); // Ajoute le nom du fichier
        body.add("nodeType", "cm:content"); // Type de noeud : contenu

        // 6. Crée l'entité de la requête avec les en-têtes et le corps
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        // 7. Envoie la requête POST pour télécharger le fichier dans le dossier Alfresco
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        // 8. Récupère la réponse JSON contenant les informations du fichier téléchargé
        String responseBody = response.getBody();
        JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
        String nodeId = jsonNode.path("entry").path("id").asText(); // ID du document
        String versionLabel = jsonNode.path("entry").path("properties").path("cm:versionLabel").asText(); // Version du document
        System.out.println("------------------------------------------------"+versionLabel);
        // 9. Met à jour les propriétés du fichier téléchargé
        updateProperties(nodeId);

        // 10. Retourne l'ID et la version du fichier sous forme de chaîne "nodeId;versionLabel"
        return nodeId + ";" + versionLabel;
    }

    /**
     * Met à jour les propriétés d'un document dans Alfresco.
     * Cette méthode met à jour les propriétés par défaut comme l'auteur du document.
     *
     * @param nodeId L'ID du document à mettre à jour
     * @throws Exception Si une erreur se produit lors de la mise à jour des propriétés
     */
    public void updateProperties(String nodeId) throws Exception {
        // 1. Construit l'URL pour mettre à jour le document dans Alfresco
        String url = alfrescoUrl + "/api/-default-/public/alfresco/versions/1/nodes/" + nodeId + "?alf_ticket=" + tokenHolder.getTicket();

        // 2. Prépare les propriétés à mettre à jour (ici, l'auteur du document)
        Map<String, Object> props = new HashMap<>();
        props.put("cm:author", Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Principal::getName)); // On récupère l'auteur à partir de l'authentification

        // 3. Prépare le corps de la requête avec les nouvelles propriétés
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("properties", props);

        // 4. Crée les en-têtes pour la requête PUT (type JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 5. Crée l'entité de la requête avec les en-têtes et le corps
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(updateBody, headers);

        // 6. Envoie la requête PUT pour mettre à jour les propriétés du document
        restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
    }




    /**
     * Récupère ou crée un dossier dans Alfresco en fonction du chemin fourni.
     * La méthode parcourt chaque dossier dans le chemin donné et, si le dossier n'existe pas, il est créé.
     *
     * @param path Le chemin du dossier dans Alfresco, sous forme de chaîne de caractères (par exemple "folder1/folder2").
     * @return L'ID du dernier dossier dans le chemin (soit un dossier existant, soit un dossier créé).
     * @throws Exception Si une erreur se produit lors de l'appel à l'API Alfresco
     */
 /*   public String getOrCreateFolderByPath(String path) throws Exception {
        // Sépare le chemin du dossier en parties individuelles (dossiers séparés par "/")
        String[] parts = path.split("/");

        // Initialisation de l'ID parent à "-root-" pour le premier dossier (le dossier racine)
        String parentId = "-root-"; // ID du dossier racine dans Alfresco

        // Parcours chaque dossier dans le chemin donné
        for (String folderName : parts) {
            // Ignore les parties vides ou nulles (ex : si le chemin commence ou finit par "/")
            if (folderName == null || folderName.trim().isEmpty()) continue;

            // 1. Récupérer les enfants du dossier actuel (on recherche seulement des dossiers)
            String url = alfrescoUrl +
                    "/api/-default-/public/alfresco/versions/1/nodes/" + parentId +
                    "/children?where=(nodeType='cm:folder')" +  // Filtre pour ne récupérer que des dossiers
                    "&alf_ticket=" + tokenHolder.getTicket(); // Paramètre pour l'authentification

            // Création des headers d'authentification pour la requête
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // Envoi de la requête pour récupérer les enfants du dossier actuel
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            // Extraction de la liste des dossiers enfants depuis la réponse
            List<Map<String, Object>> entries = (List<Map<String, Object>>)
                    ((Map<String, Object>) response.getBody().get("list")).get("entries");

            boolean found = false; // Variable pour savoir si le dossier a été trouvé

            // Parcours les enfants pour vérifier si le dossier actuel existe déjà
            if (entries != null) {
                for (Map<String, Object> entryMap : entries) {
                    Map<String, Object> entry = (Map<String, Object>) entryMap.get("entry");
                    // Si le nom du dossier correspond, met à jour l'ID parent et marque comme trouvé
                    if (entry.get("name").equals(folderName)) {
                        parentId = (String) entry.get("id"); // Met à jour l'ID parent avec celui du dossier trouvé
                        found = true;
                        break; // Si trouvé, on quitte la boucle
                    }
                }
            }

            // 2. Si le dossier n'a pas été trouvé, on doit le créer
            if (!found) {
                // URL pour créer un nouveau dossier sous le dossier parent
                String createUrl = alfrescoUrl +
                        "/api/-default-/public/alfresco/versions/1/nodes/" + parentId +
                        "/children?alf_ticket=" + tokenHolder.getTicket(); // Paramètre pour l'authentification

                // Headers pour la requête de création de dossier (avec Content-Type JSON)
                HttpHeaders createHeaders = new HttpHeaders();
                createHeaders.setContentType(MediaType.APPLICATION_JSON);

                // Corps de la requête de création avec le nom du dossier et son type
                Map<String, Object> folderBody = new HashMap<>();
                folderBody.put("name", folderName); // Nom du dossier
                folderBody.put("nodeType", "cm:folder"); // Type de noeud : cm:folder (dossier)

                // Envoi de la requête POST pour créer le dossier
                HttpEntity<Map<String, Object>> createRequest = new HttpEntity<>(folderBody, createHeaders);
                ResponseEntity<Map> createResponse = restTemplate.exchange(createUrl, HttpMethod.POST, createRequest, Map.class);

                // Extraction de l'ID du dossier créé et mise à jour de l'ID parent
                Map<String, Object> createdEntry = (Map<String, Object>) createResponse.getBody().get("entry");
                parentId = (String) createdEntry.get("id");
            }
        }

        // Retourne l'ID du dernier dossier dans le chemin
        return parentId; // L'ID du dossier final (existant ou créé)
    }*/


    // Consultation de document
    /**
     * Récupère les informations d'un document depuis Alfresco en utilisant son ID et sa version.
     * Cette méthode envoie une requête à l'API d'Alfresco pour obtenir les détails du document.
     *
     * @param alfrescoId L'ID du document dans le format "nodeId;version"
     * @return La réponse de l'API Alfresco contenant les informations du document en format JSON
     * @throws IllegalArgumentException Si l'ID du document est mal formé
     * @throws Exception Si une erreur se produit lors de l'appel à l'API
     */
    public String getDocumentInformation(String alfrescoId) {
        // Décompose l'ID du document en deux parties : nodeId et version
        String[] parts = alfrescoId.split(";");

        // Vérifie que l'ID est valide et contient bien deux parties : le nodeId et la version
        if (parts.length != 2) {
            // Si l'ID est invalide, une exception est levée
            throw new IllegalArgumentException("L'ID de document est invalide. Il doit contenir à la fois un ID et une version.");
        }

        // Assigne les valeurs extraites de l'alfrescoId
        String nodeId = parts[0];  // ID du document
        String version = parts[1];  // Version du document (non utilisé ici, mais peut être utile pour des appels futurs)

        // Construction de l'URL pour récupérer les informations du document
        // L'URL inclut le nodeId et un ticket d'authentification
        String url = alfrescoUrl + "/api/-default-/public/alfresco/versions/1/nodes/" + nodeId
                + "?alf_ticket=" + tokenHolder.getTicket(); // Paramètre de ticket pour l'authentification

        // Récupération des headers d'authentification depuis le tokenHolder (qui gère les informations d'authentification)
        HttpHeaders headers = new HttpHeaders(); // Si d'autres headers sont nécessaires, on peut les ajouter ici

        // Création d'une entité HTTP vide (pas de corps de requête ici) mais avec les headers d'authentification
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Envoi de la requête GET à l'API Alfresco pour récupérer les informations du document
        // La réponse sera au format JSON et sera stockée sous forme de chaîne de caractères
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // Retourne la réponse du serveur Alfresco, contenant les informations du document au format JSON
        return response.getBody();
    }


    /**
     * Récupère le contenu d'un document spécifique dans Alfresco basé sur son ID de nœud et sa version.
     *
     * Le format attendu pour l'identifiant est : "nodeId;version" (ex: "abc123;1.0").
     *
     * Étapes réalisées :
     *  - Valide et découpe l'identifiant fourni.
     *  - Récupère la liste des versions du document depuis Alfresco.
     *  - Recherche la version spécifique demandée.
     *  - Télécharge le contenu binaire associé à la version.
     *
     * Retourne un objet DocumentResponseDTO contenant :
     *  - le fichier sous forme de tableau de bytes (`file`)
     *  - un indicateur d'erreur (`erreur`)
     *  - un message d'erreur si une exception survient (`msg`)
     *
     * En cas d'erreur (ex: ID invalide, version introuvable, problème de communication),
     * le champ `erreur` sera à true et `msg` contiendra la description de l'erreur.
     *
     * @param alfrescoId identifiant composite du document (format "nodeId;version")
     * @return DocumentResponseDTO contenant le fichier ou l'erreur
     */
    public DocumentResponseDTO getDocumentById(String alfrescoId) {
        DocumentResponseDTO responseDTO = new DocumentResponseDTO();

        try {
            // Vérification de l'ID reçu
            String[] parts = alfrescoId.split(";");
            if (parts.length != 2) {
                throw new IllegalArgumentException("L'ID de document est invalide. Il doit contenir à la fois un ID et une version.");
            }

            String nodeId = parts[0];
            String version = parts[1];

            // Construction de l'URL pour récupérer les versions
            String url = alfrescoUrl + "/api/-default-/public/alfresco/versions/1/nodes/"
                    + nodeId + "/versions?alf_ticket=" + tokenHolder.getTicket();

            // Préparation des headers
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Appel pour récupérer les versions du document
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // Parsing de la réponse JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.getBody());

            JsonNode versions = root.path("list").path("entries");

            String versionId = null;
            if (versions.isArray()) {
                for (JsonNode entry : versions) {
                    JsonNode versionEntry = entry.path("entry");
                    if (versionEntry.has("id") && versionEntry.get("id").asText().equals(version)) {
                        versionId = versionEntry.get("id").asText();
                        break;
                    }
                }
            }

            // Vérification si la version demandée existe
            if (versionId == null) {
                throw new IllegalArgumentException("La version " + version + " du document " + nodeId + " n'existe pas.");
            }

            // Construction de l'URL pour télécharger le fichier
            String contentUrl = alfrescoUrl + "/api/-default-/public/alfresco/versions/1/nodes/"
                    + nodeId + "/versions/" + versionId + "/content?alf_ticket=" + tokenHolder.getTicket();

            // Téléchargement du fichier
            ResponseEntity<byte[]> contentResponse = restTemplate.exchange(
                    contentUrl,
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            // Retourner la réponse sans erreur
            responseDTO.setFile(contentResponse.getBody());
            responseDTO.setErreur(false);
            responseDTO.setMsg("");

        } catch (Exception e) {
            // En cas d'erreur, capturer l'exception
            responseDTO.setErreur(true);
            responseDTO.setMsg(e.getMessage());
            responseDTO.setFile(null);
        }

        return responseDTO;
    }


    public String uploadToExistingFolder(MultipartFile file, String folderId) throws IOException {
        try {
            // Create a modified version of alfrescoService.uploadDocument that takes folderId directly
            // instead of folderPath, thus skipping the getOrCreateFolderByPath step

            // Construct the URL for file upload to Alfresco
            String url = alfrescoUrl + "/api/-default-/public/alfresco/versions/1/nodes/"
                    + folderId + "/children"
                    + "?majorVersion=true&alf_ticket=" + tokenHolder.getTicket();

            // Create HTTP headers for the request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Convert the file to ByteArrayResource
            ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            // Create multipart request body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("filedata", fileAsResource);
            body.add("name", file.getOriginalFilename());
            body.add("nodeType", "cm:content");

            // Create request entity with headers and body
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // Send POST request to upload the file
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            // Parse response to get document ID
            String responseBody = response.getBody();
            JsonNode jsonNode = new ObjectMapper().readTree(responseBody);
            String nodeId = jsonNode.path("entry").path("id").asText();
            String versionLabel = jsonNode.path("entry").path("versionLabel").asText();

            // Update document properties if needed
           updateProperties(nodeId);

            // Return document ID and version
            return nodeId + ";" + versionLabel;
        } catch (Exception e) {
            throw new IOException("Error uploading document to existing folder: " + e.getMessage(), e);
        }
    }

    public String getOrCreateFolderByPath(String path) throws Exception {
        // Split the folder path into individual parts (folders separated by "/")
        String[] parts = path.split("/");

        // Initialize parent ID to "-root-" for the first folder (root folder)
        String parentId = "-root-"; // Root folder ID in Alfresco

        // Process each folder in the given path
        for (String folderName : parts) {
            // Ignore empty or null parts (e.g., if path starts or ends with "/")
            if (folderName == null || folderName.trim().isEmpty()) continue;

            // 1. Get children of current folder (search only for folders)
            String url = alfrescoUrl +
                    "/api/-default-/public/alfresco/versions/1/nodes/" + parentId +
                    "/children?where=(nodeType='cm:folder')" +  // Filter to get only folders
                    "&alf_ticket=" + tokenHolder.getTicket(); // Authentication parameter

            // Create authentication headers for the request
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // Send request to get children of current folder
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            // Extract list of child folders from response
            List<Map<String, Object>> entries = (List<Map<String, Object>>)
                    ((Map<String, Object>) response.getBody().get("list")).get("entries");

            boolean found = false; // Variable to know if folder was found

            // Check children to see if current folder already exists
            if (entries != null) {
                for (Map<String, Object> entryMap : entries) {
                    Map<String, Object> entry = (Map<String, Object>) entryMap.get("entry");
                    // If folder name matches, update parent ID and mark as found
                    if (entry.get("name").equals(folderName)) {
                        parentId = (String) entry.get("id"); // Update parent ID with found folder
                        found = true;
                        log.info("Found existing folder: {} with ID: {}", folderName, parentId); // ✅ Added logging
                        break; // If found, exit the loop
                    }
                }
            }

            // 2. If folder was not found, create it
            if (!found) {
                log.info("Creating new folder: {} under parent: {}", folderName, parentId); // ✅ Added logging

                // URL to create new folder under parent folder
                String createUrl = alfrescoUrl +
                        "/api/-default-/public/alfresco/versions/1/nodes/" + parentId +
                        "/children?alf_ticket=" + tokenHolder.getTicket(); // Authentication parameter

                // Headers for folder creation request (with JSON Content-Type)
                HttpHeaders createHeaders = new HttpHeaders();
                createHeaders.setContentType(MediaType.APPLICATION_JSON);

                // Request body for creation with folder name and type
                Map<String, Object> folderBody = new HashMap<>();
                folderBody.put("name", folderName); // Folder name
                folderBody.put("nodeType", "cm:folder"); // Node type: cm:folder (folder)

                // Send POST request to create folder
                HttpEntity<Map<String, Object>> createRequest = new HttpEntity<>(folderBody, createHeaders);

                try {
                    ResponseEntity<Map> createResponse = restTemplate.exchange(createUrl, HttpMethod.POST, createRequest, Map.class);

                    // Extract ID of created folder and update parent ID
                    Map<String, Object> createdEntry = (Map<String, Object>) createResponse.getBody().get("entry");
                    parentId = (String) createdEntry.get("id");
                    log.info("Successfully created folder: {} with ID: {}", folderName, parentId); // ✅ Added logging

                } catch (HttpClientErrorException.Conflict e) {
                    // ✅ Handle race condition - folder was created by another thread
                    log.warn("Folder {} already exists (race condition), fetching existing folder ID", folderName);

                    // Re-fetch the folder list to get the existing folder ID
                    ResponseEntity<Map> retryResponse = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
                    List<Map<String, Object>> retryEntries = (List<Map<String, Object>>)
                            ((Map<String, Object>) retryResponse.getBody().get("list")).get("entries");

                    if (retryEntries != null) {
                        for (Map<String, Object> entryMap : retryEntries) {
                            Map<String, Object> entry = (Map<String, Object>) entryMap.get("entry");
                            if (entry.get("name").equals(folderName)) {
                                parentId = (String) entry.get("id");
                                log.info("Found existing folder after conflict: {} with ID: {}", folderName, parentId);
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Return ID of the last folder in the path
        return parentId; // ID of final folder (existing or created)
    }

}