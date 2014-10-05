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
import java.util.Collection;
import org.apache.commons.io.* ;
import org.apache.commons.io.filefilter.* ;

public class Main {

    private static String HOME = "/Volumes/TOSHIBA-EXT/cfi2" ;
    private static String EXTRACTION_DIR = "run3" ;
    private static final Map<String,String> selectorMap = new HashMap<String, String>() ;
    private static Map<String,String> fieldNames = new HashMap<String, String>() ;

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
        String dataDir ;
        String extractDir ;

        if(args.length > 1) 
            extractDir = args[1] ;
        else
            extractDir = EXTRACTION_DIR ;
        
        if(args.length > 0) 
            dataDir = args[0] ;
        else
            dataDir = HOME ;

        selectorMap.put("State", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(2) > td:nth-child(1)") ;
        selectorMap.put("District Name", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(2) > td:nth-child(2)") ;
        selectorMap.put("School Code", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(3) > td:nth-child(1)") ;
        selectorMap.put("School Name", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(3) > td:nth-child(2)") ;
        selectorMap.put("Village Name", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(5) > td:nth-child(1)") ;
        selectorMap.put("PINCODE", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(6) > td") ;
        selectorMap.put("Grade*", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(2) > td:nth-child(3)") ;
        selectorMap.put("teachers_male", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(7) > td > table:nth-child(2) > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(1) > td:nth-child(5)") ;
        selectorMap.put("teachers_female", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(7) > td > table:nth-child(2) > tbody > tr:nth-child(2) > td > table > tbody > tr:nth-child(1) > td:nth-child(7)") ;
        selectorMap.put("toilets_boys", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(7) > td > table:nth-child(3) > tbody > tr:nth-child(10) > td:nth-child(2)" ) ;
        selectorMap.put("toilets_girls", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(7) > td > table:nth-child(3) > tbody > tr:nth-child(10) > td:nth-child(3)") ;
        selectorMap.put("playground", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(7) > td > table:nth-child(3) > tbody > tr:nth-child(5) > td:nth-child(4)") ;

        selectorMap.put("electricity", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(7) > td > table:nth-child(3) > tbody > tr:nth-child(7) > td:nth-child(4)") ;
        selectorMap.put("computer_lab", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(7) > td > table:nth-child(3) > tbody > tr:nth-child(7) > td:nth-child(6)") ;
        selectorMap.put("no_of_computers", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(7) > td > table:nth-child(3) > tbody > tr:nth-child(8) > td:nth-child(4)") ;
        selectorMap.put("library", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(7) > td > table:nth-child(3) > tbody > tr:nth-child(9) > td:nth-child(5)") ;
        selectorMap.put("books_in_library", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(7) > td > table:nth-child(3) > tbody > tr:nth-child(9) > td:nth-child(7)") ;
        selectorMap.put("drinking_water", "body > div:nth-child(4) > center > table > tbody > tr:nth-child(7) > td > table:nth-child(3) > tbody > tr:nth-child(10) > td:nth-child(5)") ;


        populateFieldMap(fieldNames, selectorMap) ;

        generateDataFiles(dataDir, extractDir) ;

    }

    // convert the key name from selector into field/column names for db. mostly lower case and _
    // key names are used for matching in text, so can't directly change in selector
    private static void populateFieldMap(Map<String,String> fields, Map<String,String> selectos) {
        for(String key : selectos.keySet() ) {
            fields.put(key, key.toLowerCase().replace(" ", "_") ) ;
        }
        fields.put("Grade*", "rating" ) ;
    }

    private static void generateDataFiles(String dir, String extractDir) {
        File mainDir = new File(dir + "/files/" ) ;
        Collection<File> dirList = FileUtils.listFilesAndDirs(mainDir, DirectoryFileFilter.INSTANCE, DirectoryFileFilter.INSTANCE) ;
        for(File subDir: dirList) {
            //System.out.println("The sub dir:" + subDir.getName() ) ;
            long start = System.currentTimeMillis();
            processDirectory(dir, subDir, extractDir) ;
            long end = System.currentTimeMillis() ;
            System.out.println("Time taken for dir [" + subDir.getName() + "] = " + (end - start) + "ms" ) ;

        }
    }

    private static void processDirectory(String dir, File subDir, String extractDir) {
        Collection<File> htmlFiles = FileUtils.listFiles(subDir, new String[]{"html"}, false) ;
        for(File eachFile : htmlFiles) {
            //System.out.println("Processing file:" + eachFile.getName() ) ;
            try {
                Map<String, String> data = processFile2(eachFile);
                if(data.size() != 0) {
                    String dataString = convertMapToString(data) ;
                    String dataFileName = dir + "/" + extractDir + "/" + subDir.getName() + "/" + eachFile.getName().replaceFirst(".html", ".txt") ;
                    //System.out.println("Writing data to file:" + dataFileName) ;
                    FileUtils.writeStringToFile(new File(dataFileName), dataString, "UTF-8" ) ;
                }
            } catch(Exception e) {
                e.printStackTrace() ;
            }
        }
    }

    private static String convertMapToString(Map<String,String> data) {
        StringBuilder sb = new StringBuilder("{") ;
        for(String key: data.keySet() ) {
            sb
            .append(" \"").append(key)
            .append("\" : \"").append(data.get(key)).append("\"")
            .append(" ,") ;
        }
        sb.setLength(sb.length() - 1);
        sb.append(" }") ;

        return sb.toString() ;
    }

    private static Map<String, String> processFile2(File input) {
        Map<String, String> parsedData = new HashMap<String, String>() ;

        try {
            if( input.length() == 0L ){
                System.out.println("File size is zero for file:" + input.getName() ) ;
                return parsedData ;
            }

            Document doc = Jsoup.parse(input, "UTF-8", "http://schoolreportcards.in") ;

            for(String elem : selectorMap.keySet()) {
                String dataFound = null ;
                try {
                    Element node = doc.select(selectorMap.get(elem)).first() ;
                    String textData = htmlSpaceTrim(node.text()) ;
                    if (textData != null && textData.contains(elem)) {
                        dataFound = parseElementText(textData, elem);
                    } else {
                        dataFound = textData ;
                    }
                } catch(Exception e) {
                    System.out.println("Error in getting [" + elem + "] for school:" + input.getName()  + ": error:" +
                        e.getMessage() ) ;
                    dataFound = "" ;
                }
                if("PINCODE".equals(elem) )
                    dataFound = cleanPincode(dataFound) ;
                parsedData.put(fieldNames.get(elem), dataFound) ;
            }
            //System.out.println("parsedData:" + parsedData) ;

        }catch(Exception e) {
            e.printStackTrace();
        }
        return parsedData ;

    }

    private static String cleanPincode(String pin) {
        if(pin == null )
            return null ;
        else
            return pin.substring(1, pin.length() -1) ;
    }
}
