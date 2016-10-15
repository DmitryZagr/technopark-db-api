package ru.mail.park.util;

/**
 * Created by admin on 14.10.16.
 */
public class MyJsonUtils {
    public static String replaceOneQuoteTwoQuotes(String json) {
        json = json.replace('\'', '\"');
        json = json.replaceAll("None", "\"null\"") ;
        json = json.replaceAll("True", "\"true\"");
        json = json.replaceAll("False", "\"false\"");
        return json;
    }

}
