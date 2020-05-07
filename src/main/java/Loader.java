import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.HashMap;

public class Loader
{
    private static HashMap<Integer, WorkTime> voteStationWorkTimes = new HashMap<>();
    private static String query = "INSERT INTO voter_count(name, birthDate) VALUES(?, ?)";

    public static void main(String[] args) throws Exception
    {
        long parsingTime = System.currentTimeMillis();
        long usage = getMemoryUsage();

        System.out.println("Initial memory: " + usage);

        //SAX parsing
        String fileName = "res/data-0.2M.xml";
        parseFileBySAX(fileName);

        System.out.println("Memory after SAX parsing: " + (getMemoryUsage() - usage));
        System.out.println("Parsing time: " + (System.currentTimeMillis() - parsingTime) + " ms");
    }

    private static long getMemoryUsage() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    private static void parseFileBySAX(String fileName) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLHandler handler = new XMLHandler();
        parser.parse(new File(fileName), handler);

        // Executing by Statemenet
//        DBConnection.executeMultiinsert();

        // Executing by PreparedStatement
        DBConnection.execute();

        DBConnection.printVoterCounts();
//        printVoteStationsWorkTime();
    }

    public static HashMap<Integer, WorkTime> getVoteStationWorkTimes() {
        return voteStationWorkTimes;
    }

    public static void addVoteStationWorkTimes(Integer number, WorkTime station) {
        voteStationWorkTimes.put(number, station);
    }

    public static void printVoteStationsWorkTime() {
        System.out.println("Voting station work times: ");
        for(Integer votingStation : voteStationWorkTimes.keySet())
        {
            WorkTime workTime = voteStationWorkTimes.get(votingStation);
            System.out.println("\t" + votingStation + " - " + workTime);
        }
    }
}