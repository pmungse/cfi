package org.cfi;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.util.*;

import org.apache.commons.io.* ;
import org.apache.commons.io.filefilter.* ;

public class Main {

    private static String HOME = "/Volumes/TOSHIBA-EXT/cfi" ;
    private static String EXTRACTION_DIR = "/Users/pritam_mungse/Documents/workspace/code-for-india/extracted/run5" ;
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
    private static void populateFieldMap(Map<String,String> fields, Map<String,String> selectors) {
        for(String key : selectors.keySet() ) {
            fields.put(key, key.toLowerCase().replace(" ", "_") ) ;
        }
        fields.put("Grade*", "rating" ) ;
        fields.put("School Code", "school_id" ) ;
    }

    private static void generateDataFiles(String dir, String extractDir) {
        File mainDir = new File(dir + "/files/" ) ;
        Collection<File> dirList = FileUtils.listFilesAndDirs(mainDir, DirectoryFileFilter.INSTANCE, DirectoryFileFilter.INSTANCE) ;
        for(File subDir: dirList) {
            //System.out.println("The sub dir:" + subDir.getName() ) ;
            long start = System.currentTimeMillis();
            long filesProcessed = processDirectory(subDir, extractDir) ;
            long end = System.currentTimeMillis() ;
            System.out.println("Time taken for dir [" + subDir.getName() + "], files = " + filesProcessed
		+ ", time = " + (end - start) + "ms" ) ;

        }
    }

    private static long processDirectory(File subDir, String extractDir) {
        long counter = 0L ;
        try {
            Collection<File> htmlFiles = FileUtils.listFiles(subDir, new String[]{"html"}, false);
            String toJsonFile = extractDir + File.separator + subDir.getName() + ".data.json";
            Collection<String> jsonStrings = new ArrayList<String>();
            for (File eachFile : htmlFiles) {
                counter++;
                try {
                    Map<String, String> data = processFile2(eachFile);
                    if (data.size() != 0) {
                        String dataString = convertMapToString(data);
                        jsonStrings.add(dataString);
                    }
                } catch (Exception e) {
                    System.out.println("Exception while processing file: " + eachFile.getAbsolutePath() ) ;
                    e.printStackTrace();
                }
            }
            FileUtils.writeLines(new File(toJsonFile), "UTF-8", jsonStrings) ;
        }catch(Exception e) {
            System.out.println("Exception while processing subDir: " + subDir.getAbsolutePath() ) ;
            e.printStackTrace();
        }
        return counter ;
    }

    private static String convertMapToString(Map<String,String> data) {
        JSONObject obj = new JSONObject();
        obj.putAll(data) ;
        return obj.toJSONString() ;
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
