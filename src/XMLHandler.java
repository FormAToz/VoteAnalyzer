import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.text.SimpleDateFormat;
import java.util.Date;


public class XMLHandler extends DefaultHandler {

    private static SimpleDateFormat birthDayFormat = new SimpleDateFormat("yyyy.MM.dd");
    private static SimpleDateFormat visitDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    private Voter voter;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            if (qName.equals("voter") && voter == null) {
                Date birthDate = birthDayFormat.parse(attributes.getValue("birthDay"));
                voter = new Voter(attributes.getValue("name"), birthDate);

            } else if (qName.equals("visit") && voter != null) {
                int count = Loader.getVoterCounts().getOrDefault(voter, 0);
                Loader.addVoter(voter, ++count);

                Integer station = Integer.parseInt(attributes.getValue("station"));
                Date time = visitDateFormat.parse(attributes.getValue("time"));
                WorkTime workTime = Loader.getVoteStationWorkTimes().get(station);
                if(workTime == null) {
                    workTime = new WorkTime();
                    Loader.addVoteStationWorkTimes(station, workTime);
                }
                workTime.addVisitTime(time.getTime());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("voter")) {
            voter = null;
        }
    }
}
