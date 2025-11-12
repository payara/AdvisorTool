package fish.payara.advisor.config.files;

import fish.payara.advisor.AdvisorBean;
import fish.payara.advisor.AdvisorType;
import fish.payara.advisor.Analyzer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class JSFEventsConfigXml implements Analyzer<List<AdvisorBean>> {

    private List<String> keyPatterns = new ArrayList<>();
    
    public JSFEventsConfigXml() {
        keyPatterns.add("jakarta-faces-remove-event-postconstruct-customscope-event-xml");
        keyPatterns.add("jakarta-faces-remove-event-predestroy-customscope-event-xml");
    }

    public JSFEventsConfigXml(List<String> keyPatterns) {
        this.keyPatterns = keyPatterns;
    }
    
    @Override
    public List<AdvisorBean> analise(java.io.File file) {
        List<AdvisorBean> advisors = new ArrayList<>();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = null;
        try {
            saxParser = factory.newSAXParser();
            JSFEventsConfigHandler handler = new JSFEventsConfigHandler();
            saxParser.parse(file, handler);
            ArrayList<JSFConstantEvent> constantEvents = handler.getConstantEvents();
            for (JSFConstantEvent constantEvent : constantEvents) {
                String keyPattern;
                if (constantEvent.value().equals("jakarta.faces.event.PostConstructCustomScopeEvent")) {
                    keyPattern = keyPatterns.get(0);
                } else if (constantEvent.value().equals("jakarta.faces.event.PreDestroyCustomScopeEvent")) {
                    keyPattern = keyPatterns.get(1);
                } else {
                    keyPattern = "";
                }

                AdvisorBean advisorFileBean = new AdvisorBean.
                        AdvisorBeanBuilder(keyPattern, constantEvent.value()).
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

    private static class JSFEventsConfigHandler extends DefaultHandler{
        private ArrayList<JSFConstantEvent> constantEvents = new ArrayList<>();
        private JSFConstantEvent currentEvent = new JSFConstantEvent(null, null);
        private boolean inTargetTag = false;
        private StringBuilder currentContent;

        public JSFEventsConfigHandler() {
            
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inTargetTag) {
                currentContent.append(new String(ch, start, length));
            }
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("system-event-class".equals(qName)) {
                inTargetTag = true;
                currentContent = new StringBuilder();
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("system-event-class".equals(qName)) {
                inTargetTag = false;
                currentEvent = new JSFConstantEvent("system-event-class", currentContent.toString());
                constantEvents.add(currentEvent);
            }
        }

        public ArrayList<JSFConstantEvent> getConstantEvents() {
            return constantEvents;
        }
        
    }
    
    record JSFConstantEvent(String name, String value){}
}
