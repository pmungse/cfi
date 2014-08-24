package org.cfi;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {

    private static final String HOME = "." ;
    private static final Set<String> lookupSet = new HashSet<String>() ;
    private static final Map<String,String> selectorMap = new HashMap<String, String>() ;

    /*
        private static String getLocalFileName(String title) {
            return HOME + File.separator + "data" + File.separator + title ;
        }

            private static void processUrl() {
                try {
                    // url processing

                    // save file to local disk

                    // get url data and save locally
                    Document doc = Jsoup.connect("http://schoolreportcards.in/seeschoolreportdetail13.asp?cmbschool=28010101201&cmbyear=1950&cmbstate=28&cmbdistrict=All%20Districts").get();
                    String title = doc.title() ;
                    String uri = doc.baseUri() ;

                    File dataFile = new File( getLocalFileName(uri)) ;
                    FileUtils.writeStringToFile(dataFile, doc.html() );


                    // get html and parse data into n=v pair in text file
                }catch(Exception e) {
                    e.printStackTrace();
                }

            }

            private static void processFile(String fileName) {
                try {
                    Map<String, String> parsedData = new HashMap<String, String>() ;
                    File input = new File(fileName) ;
                    Document doc = Jsoup.parse(input, "UTF-8", "http://schoolreportcards.in") ;

                    Elements listOfTD = doc.getElementsByTag("TD") ;
                    for(Element td : listOfTD) {
                        String text = td.text() ;
                        //System.out.println("td:" + text ) ;
                        String lookupDataFound = findLookupData(text) ;
                        if(lookupDataFound != null) {
                            String dataFound = findDataPoint(td,lookupDataFound) ;
                            if(dataFound != null ) {
                                parsedData.put(lookupDataFound, dataFound);
                                System.out.println("found:" + lookupDataFound + "=" + dataFound);
                            }
                        }
                    }

                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
            private static String findDataPoint(Element elem, String lookupDataFound) {

                String dataFound= null ;
                // check if the name and value are in same td / tr, if row has more columns
                if(elem.children().size() > 1) {
                    // skip the tables and rows, the TD will come again in list of internal TDs
        //            for(Element child : elem.children()) {
        //                if(child.tagName().equals("TR"))
        //                    break ; // internal rows skip, it's columns will be processed for TD tags
        //
        //                dataFound = findDataPoint(child, lookupDataFound) ;
        //                if(dataFound != null ) {
        //                    break;
        //                }
        //            }
                } else if (elem.children().size() ==1 && (
                        elem.child(0).tagName().equalsIgnoreCase("TR") || elem.child(0).tagName().equalsIgnoreCase("TABLE")
                        || elem.child(0).tagName().equalsIgnoreCase("TBODY") )) {
                } else {
                    // only one column element
                    // trim column before testing any name/value
                    String textData = elem.text().trim();
                    if (textData != null && textData.contains(lookupDataFound)) {
                        // if column has only name, check in the next column for value
                        if (textData.equalsIgnoreCase(lookupDataFound)) {
                            // value is in next column
                            dataFound = elem.nextElementSibling().text().trim();
                        } else {
                            dataFound = parseElementText(textData, lookupDataFound);
                        }

                    }
                }
                return dataFound;
            }
            private static String findLookupData(String text) {
                for(String str : lookupSet) {
                    if(text.contains(str))
                        return str ;
                }
                return null;
            }
        */

    private static String parseElementText(String textData, String lookupDataFound) {
        int keyLen = lookupDataFound.length() ;
        int matchStart = textData.indexOf(lookupDataFound) ;
        String data = textData.substring(matchStart+keyLen).trim() ;
        // html no breaking space
        data = htmlSpaceTrim(data) ;

        return data;
    }
    private static String htmlSpaceTrim(String input) {
       return input.replace( (char) 160, ' ').trim() ;
    }


    public static void main(String[] args) {
        lookupSet.add("School Code") ;
        lookupSet.add("School Name") ;
        lookupSet.add("Year of Establishment") ;
        lookupSet.add("PINCODE") ;
        lookupSet.add("Teacher(s) Male") ;
        lookupSet.add("Teacher(s) Female") ;

        selectorMap.put("School Code", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(3) > td:nth-child(1)") ;
        selectorMap.put("School Name", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(3) > td:nth-child(2)") ;
        selectorMap.put("Village Name", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(5) > td:nth-child(1)") ;
        selectorMap.put("Teacher(s) Male", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(7) > td > table:nth-child(2) > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(1) > td:nth-child(5)") ;
        selectorMap.put("Teacher(s) Female", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(7) > td > table:nth-child(2) > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(1) > td:nth-child(7)") ;
        selectorMap.put("Toilets boys", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(7) > td > table:nth-child(3) > tbody > tr:nth-child(10) > td:nth-child(2)" ) ;
        selectorMap.put("Toilets girls", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(7) > td > table:nth-child(3) > tbody > tr:nth-child(10) > td:nth-child(3)") ;

        processFile2(HOME + File.separator + "testData" + File.separator + "test.html");
    }

    private static void processFile2(String fileName) {
        try {
            Map<String, String> parsedData = new HashMap<String, String>() ;
            File input = new File(fileName) ;
            Document doc = Jsoup.parse(input, "UTF-8", "http://schoolreportcards.in") ;

            for(String elem : selectorMap.keySet()) {
                String dataFound = null ;
                Element node = doc.select(selectorMap.get(elem)).first() ;
                String textData = htmlSpaceTrim(node.text()) ;
                if (textData != null && textData.contains(elem)) {
                    dataFound = parseElementText(textData, elem);
                } else {
                    dataFound = textData ;
                }
                parsedData.put(elem, dataFound) ;
            }
            System.out.println("parsedData:" + parsedData) ;

        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
