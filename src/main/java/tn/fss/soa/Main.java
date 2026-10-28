package tn.fss.soa;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String XML_FILE = "/cinema.xml";
    private static final String XSD_FILE = "/cinema.xsd";
    public static void main(String[] args) {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        System.out.println("==============================================");
        System.out.println("  FONCTIONNALITÉ 1 : VALIDATION XML / XSD");
        System.out.println("==============================================");
        boolean valide = validerXML(XML_FILE, XSD_FILE);

        if (!valide) {
            System.out.println("\nLe document est invalide : interrogation XPath annulée.");
            return;
        }

        System.out.println("\n==============================================");
        System.out.println("  FONCTIONNALITÉ 2 : INTERROGATION XPATH");
        System.out.println("==============================================");
        interrogerXPath(XML_FILE);
    }

    public static boolean validerXML(String xmlPath, String xsdPath) {
        try (InputStream xsd = Main.class.getResourceAsStream(xsdPath);
             InputStream xml = Main.class.getResourceAsStream(xmlPath)) {

            if (xsd == null || xml == null) {
                System.err.println("Fichier XML ou XSD introuvable dans les resources.");
                return false;
            }

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsd));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));

            System.out.println("✔ Le document " + xmlPath + " est VALIDE par rapport à " + xsdPath);
            return true;

        } catch (SAXParseException e) {
            System.out.println("✘ Document INVALIDE !");
            System.out.println("  Ligne " + e.getLineNumber() + ", colonne " + e.getColumnNumber());
            System.out.println("  Erreur : " + e.getMessage());
            return false;
        } catch (SAXException e) {
            System.out.println("✘ Erreur SAX : " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("✘ Erreur : " + e.getMessage());
            return false;
        }
    }

    public static void interrogerXPath(String xmlPath) {
        try (InputStream xml = Main.class.getResourceAsStream(xmlPath)) {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(xml);

            XPath xpath = XPathFactory.newInstance().newXPath();

            afficherListe("Q1 - Tous les titres de films",
                    "//film/titre", doc, xpath);

            afficherListe("Q2 - Utilisateurs actifs",
                    "//utilisateur[@actif='true']/nom", doc, xpath);

            afficherListe("Q3 - Films en promotion",
                    "//film[@promotion='true']/titre", doc, xpath);


            afficherListe("Q4 - Films sortis après 2010",
                    "//film[annee > 2010]/titre", doc, xpath);

            afficherListe("Q5 - Films dont la note est >= 8.6",
                    "//film[note >= 8.6]/titre", doc, xpath);

            afficherListe("Q6 - Films réalisés par Christopher Nolan",
                    "//film[realisateur='Christopher Nolan']/titre", doc, xpath);

            afficherListe("Q7 - Films réservés par l'utilisateur U1",
                    "//film[@id=//reservation[@utilisateurRef='U1']/@filmRef]/titre", doc, xpath);

            XPathExpression exprCount = xpath.compile("count(//film)");
            double nbFilms = (Double) exprCount.evaluate(doc, XPathConstants.NUMBER);
            System.out.println("\nQ8 - Nombre total de films : " + (int) nbFilms);

            XPathExpression exprMoyenne = xpath.compile("sum(//film/prix) div count(//film)");
            double moyenne = (Double) exprMoyenne.evaluate(doc, XPathConstants.NUMBER);
            System.out.printf("Q9 - Prix moyen d'un billet : %.2f DT%n", moyenne);

            XPathExpression exprNom = xpath.compile("/cinema/utilisateurs/utilisateur[1]/nom");
            String premier = (String) exprNom.evaluate(doc, XPathConstants.STRING);
            System.out.println("Q10 - Premier utilisateur : " + premier);

        } catch (Exception e) {
            System.err.println("Erreur XPath : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void afficherListe(String titre, String expression, Document doc, XPath xpath) throws Exception {
        XPathExpression expr = xpath.compile(expression);
        NodeList noeuds = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        System.out.println("\n" + titre);
        System.out.println("   Expression : " + expression);
        if (noeuds.getLength() == 0) {
            System.out.println("   (aucun résultat)");
            return;
        }
        List<String> resultats = new ArrayList<>();
        for (int i = 0; i < noeuds.getLength(); i++) {
            Node n = noeuds.item(i);
            resultats.add(n.getTextContent().trim());
        }
        for (String r : resultats) {
            System.out.println("   - " + r);
        }
    }
}