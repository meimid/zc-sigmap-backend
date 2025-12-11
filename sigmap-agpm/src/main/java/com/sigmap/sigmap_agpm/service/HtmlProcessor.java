package com.sigmap.sigmap_agpm.service;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
@Slf4j
public class HtmlProcessor {
    public static class ProcessedContent {
        public String cleanedHtml;
        public Map<String, java.io.InputStream> images;
        public int imageCount;  // Utile pour debug

        public ProcessedContent() {
            images = new HashMap<>();
            imageCount = 0;
        }
    }

    public static ProcessedContent processHtml(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            ProcessedContent empty = new ProcessedContent();
            empty.cleanedHtml = "";
            return empty;
        }

        Document doc = Jsoup.parse(htmlContent);
        ProcessedContent result = new ProcessedContent();

        // 1. Nettoyage CSS : Transforme classes en styles inline
        //cleanCssStyles(doc);
        convertCssToHtmlTags(doc);

        // 2. Extraction des images base64
        int imgId = 0;
        for (Element img : doc.select("img[src^=data:image]")) {
            String src = img.attr("src");
            if (src.startsWith("data:image/")) {
                try {
                    // Extraire base64 (après la virgule)
                    String base64 = src.split(",", 2)[1];  // Split sur première virgule seulement
                    byte[] imgBytes = Base64.getDecoder().decode(base64.trim());
                    String placeholder = "image" + imgId;
                    result.images.put(placeholder, new ByteArrayInputStream(imgBytes));

                    // Remplacer src par placeholder (et garder les autres attrs comme class/alt)
                    img.attr("src", placeholder);
                    imgId++;
                    result.imageCount++;
                   log.info("Image extraite : {} taille: {} bytes" + placeholder , imgBytes.length);
                } catch (IllegalArgumentException e) {
                    log.error("Erreur décodage base64 pour img {} : {}" , imgId , e.getMessage());
                    // Option : Supprime l'img ou garde le src original
                    img.remove();
                }
            }
        }

        result.cleanedHtml = doc.html();  // HTML nettoyé, prêt pour Jasper markup
        log.info("Traitement terminé : {} images extraites." , result.imageCount );
        return result;
    }

    private static void convertCssToHtmlTags(Document doc) {
        // Alignement center : Enveloppe dans <div style="text-align: center;">
        for (Element el : doc.select(".ql-align-center")) {
            el.wrap("<div style=\"text-align: center;\"></div>");
            el.removeAttr("class");
        }

        // h1 → <p><font size="18"><b>TEXT</b></font></p>
        for (Element h1 : doc.select("h1")) {
            String text = h1.html();  // Contenu original
            Element bold = new Element("b").html(text);
            Element font = new Element("font").attr("size", "18").attr("color", "black");
            font.appendChild(bold);  // Correction : appendChild pour ajouter l'élément enfant
            h1.html(font.outerHtml());  // Remplace le contenu de h1
            h1.tagName("p");  // Change en p pour meilleur rendu Jasper
            h1.attr("style", "text-align: center;");  // Align si besoin
        }

        // h2 → <p><font size="14"><b>TEXT</b></font></p>
        for (Element h2 : doc.select("h2")) {
            String text = h2.html();
            Element bold = new Element("b").html(text);
            Element font = new Element("font").attr("size", "14");
            font.appendChild(bold);  // Correction ici aussi
            h2.html(font.outerHtml());
            h2.tagName("p");
        }

        log.info("Conversion CSS → balises HTML terminée.");
    }

    // Méthode helper pour nettoyer les CSS (ajoute/ajuste styles inline)
    private static void cleanCssStyles(Document doc) {
        // Pour ql-align-center → text-align: center
        for (Element el : doc.select(".ql-align-center")) {
            String currentStyle = el.attr("style");
            el.attr("style", (currentStyle.isEmpty() ? "" : currentStyle + "; ") + "text-align: center;");
            el.removeAttr("class");  // Optionnel : enlève la classe une fois inline
        }

        // Pour <h1> : Ajoute style bold + size (si pas déjà présent)
        for (Element h1 : doc.select("h1")) {
            String currentStyle = h1.attr("style");
            String newStyle = "font-size: 18pt; font-weight: bold;";
            if (!currentStyle.contains("font-weight: bold")) {
                h1.attr("style", (currentStyle.isEmpty() ? "" : currentStyle + "; ") + newStyle);
            }
        }

        // Ajoute pour <h2> (exemple)
        for (Element h2 : doc.select("h2")) {
            String currentStyle = h2.attr("style");
            h2.attr("style", (currentStyle.isEmpty() ? "" : currentStyle + "; ") + "font-size: 14pt; font-weight: bold;");
        }

        // Ajoute d'autres règles ici si besoin (ex. : .ql-indent-1 → padding-left: 40px;)
        log.info("CSS nettoyé : classes converties en inline styles.");
    }
}
