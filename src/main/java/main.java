import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Scanner;
import java.util.stream.*;

public class main {
    public static void main(String args[]){
        try {
            // connect to url that provides the data and is filtered to the year 2014
            URL url = new URL("https://www.healthit.gov/data/open-api?source=Meaningful-Use-Acceleration-Scorecard.csv&period=2014");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            // check that its working
            int responsecode = conn.getResponseCode();
            if (responsecode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responsecode);
            } else {
                // use a scanner to fill in the data to a string
                String data = "";
                Scanner scanner = new Scanner(url.openStream());
                while (scanner.hasNext()) {
                    data += scanner.nextLine();
                }
                scanner.close();
                conn.disconnect();

                // at this point, we have the result from the api saved as a string
                // cleanse it
                String cleanData = data.replaceAll("\\{", "")
                        .replaceAll("\\}", "")
                        .replaceAll("\\[", "")
                        .replaceAll("\\]", "")
                        .replaceAll("\"", "")
                        .replaceAll("[0-9]:", "");

                /* We can store the region-percent data in a hashmap as each region should be unique.
                   First we take each line and break in half based on the colon.
                   If it's a region, search further in the lines for the first case of 'pct_hospitals_mu'
                   Then put the region value and pct_hosp value as a pair in the hashmap.
		        */
                Map<String, Float> myMap = new LinkedHashMap<String, Float>();
                String[] lines = cleanData.split(",");
                for (int i = 0; i < lines.length; i++) {
                    String[] keyValue = lines[i].split(": ");
                    if (keyValue[0].contains("region") && !keyValue[0].contains("code")){
                        while(i < lines.length){
                            String[] secondKeyValue = lines[i].split(": ");
                            if (secondKeyValue[0].contains("pct_hospitals_mu") && secondKeyValue[0].contains("aiu")){
                                myMap.put(keyValue[1], Float.valueOf(secondKeyValue[1]));
                                break;
                            }
                            i++;
                        }
                    }
                }

                // now we need to sort it by descending value
                // this code gathers the entries, sorts them using a comparator, and then will organize them back into a new linkedhashmap
                Map<String, Float> sortedInDescending = myMap
                        .entrySet()
                        .stream()
                        .sorted(Collections.reverseOrder(Entry.comparingByValue()))
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (entry1, entry2) -> entry2, LinkedHashMap::new));

                // we now have our sorted list, ready to print
                System.out.println("-- Printing list in descending order --");
                for(Map.Entry<String, Float> entry : sortedInDescending.entrySet()){
                    System.out.println(entry);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}