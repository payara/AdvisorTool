package fish.payara.advisor.config.files;

import fish.payara.advisor.AdvisorBean;
import fish.payara.advisor.AdvisorType;
import fish.payara.advisor.Analyzer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class JSFParamConfigXml implements Analyzer<List<AdvisorBean>> {

    private List<String> keyPatterns = new ArrayList<>();

    public JSFParamConfigXml() {
        keyPatterns.add("jakarta-faces-remove-property-fss1-xml");
        keyPatterns.add("jakarta-faces-remove-property-fss2-xml");
    }

    public JSFParamConfigXml(List<String> keyPatterns) {
        this.keyPatterns = keyPatterns;
    }


    @Override
    public List<AdvisorBean> analise(File file) {
        List<AdvisorBean> advisors = new ArrayList<>();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = null;
        try {
            saxParser = factory.newSAXParser();
            JSFContextPropertiesHandler handler = new JSFContextPropertiesHandler();
            saxParser.parse(file, handler);
            ArrayList<JSFProperty> jsfProperties = handler.getJsfProperties();
            for (JSFProperty jsfProperty : jsfProperties) {
                int idKeyPattern = 0;
                if(jsfProperty.value().equals("jakarta.faces.FULL_STATE_SAVING_VIEW_IDS_PARAM_NAME")) {
                    idKeyPattern = 1;
                }

                AdvisorBean advisorFileBean = new AdvisorBean.
                        AdvisorBeanBuilder(keyPatterns.get(idKeyPattern), jsfProperty.value()).
                        setFile(file).
                        setLine("0").
                        setType(AdvisorType.WARN).
                        setMethodDeclaration("Constant deprecated").build();
                advisors.add(advisorFileBean);
            }
            
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

        return advisors;
    }

    static class JSFContextPropertiesHandler extends DefaultHandler {
        private final ArrayList<JSFProperty> jsfProperties = new ArrayList<>();
        private JSFProperty currentProperty = new JSFProperty(null, null);
        private boolean inTargetTag = false;
        private StringBuilder currentContent;

        public JSFContextPropertiesHandler() {

        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inTargetTag) {
                currentContent.append(new String(ch, start, length));
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("param-name".equals(qName)) {
                inTargetTag = true;
                currentContent = new StringBuilder();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("param-name".equals(qName)) {
                inTargetTag = false;
                currentProperty = new JSFProperty("param-name", currentContent.toString());
                jsfProperties.add(currentProperty);
            }
        }

        public ArrayList<JSFProperty> getJsfProperties() {
            return jsfProperties;
        }
    }

    record JSFProperty(String name, String value) {
    }
}
