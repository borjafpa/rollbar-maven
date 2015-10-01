package com.mhlopko.rollbar.notifications;
import java.net.UnknownHostException;
import java.sql.Timestamp;


public class Rollbar {
    public static void main(String[] args) {
        // server-side - FirstProject : 73d292f5fb724b3a952a35c87ead255e
        // client-side - FirstProject: 74580056b2174585adba6edff4d8b55a

        String urlString = "https://api.rollbar.com/api/1/item/";
        String apiKey = "73d292f5fb724b3a952a35c87ead255e";
        String env = "test";

        try {
            RollbarNotifier.init(urlString, apiKey, env);

            RollbarNotifier.notify("test 1 " + new Timestamp(System.currentTimeMillis()));

            System.out.println("b1");

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("b2");
    }
}
