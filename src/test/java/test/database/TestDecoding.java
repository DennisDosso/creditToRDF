package test.database;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class TestDecoding {

    public static void main(String[] args) {

        try {
            String url = "https%3A%2F%2Fmywebsite%2Fdocs%2Fenglish%2Fsite%2Fmybook.do%3Frequest_type";
            String result = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8.name());
            System.out.println(result);
        } catch (UnsupportedEncodingException e) {

        }
    }
}
