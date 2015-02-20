package com.borjafpa.rollbar.notifications;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.helpers.LogLog;
import org.json.simple.JSONObject;

import com.borjafpa.rollbar.http.HttpRequest;

public class RollbarNotifier {

    public static final int MAX_RETRIES = 5;

    private static NotifyBuilder builder;
    private static URL url;

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(
            2, 
            new ThreadFactory() {
                public Thread newThread(Runnable runnable) {
                    Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                    // thread.setDaemon(true);
                    thread.setName("RollbarNotifier-" + new Random().nextInt(100));
                    return thread;
                }
            }
            );

    public enum Level {
        DEBUG, INFO, WARNING, ERROR
    }

    public static void init(String urlString, String apiKey, String env) throws UnknownHostException {
        url = getURL(urlString);
        builder = new NotifyBuilder(apiKey, env);
    }

    public static void notify(String message) {
        notify(Level.INFO, message, null, null);
    }

    public static void notify(String message, Map<String, Object> context) {
        notify(Level.INFO, message, null, context);
    }

    public static void notify(Level level, String message) {
        notify(level, message, null, null);
    }

    public static void notify(Level level, String message, Map<String, Object> context) {
        notify(level, message, null, context);
    }

    public static void notifyError(Throwable throwable) {
        notify(Level.ERROR, null, throwable, null);
    }

    public static void notifyError(Throwable throwable, Map<String, Object> context) {
        notify(Level.ERROR, null, throwable, context);
    }

    public static void notifyError(String message, Throwable throwable) {
        notify(Level.ERROR, message, throwable, null);
    }

    public static void notifyError(String message, Throwable throwable, Map<String, Object> context) {
        notify(Level.ERROR, message, throwable, context);
    }

    public static void notifyInLevel(Level level, Throwable throwable) {
        notify(level, null, throwable, null);
    }

    public static void notifyInLevel(Level level, Throwable throwable, Map<String, Object> context) {
        notify(level, null, throwable, context);
    }
    
    public static NotifyBuilder getBuilder() {
        return builder;
    }

    public static URL getUrl() {
        return url;
    }

    private static void notify(final Level level, final String message, final Throwable throwable, 
            final Map<String, Object> context) {

        EXECUTOR.execute(new Runnable() {

            public void run() {
                try {
                    JSONObject payload = builder.build(level.toString(), message, throwable, context);
                    postJson(payload);
                } catch (Throwable e) {
                    LogLog.error("There was an error notifying the error.", e);
                }
            }

        });

    }

    private static void postJson(JSONObject json) {
        HttpRequest request = new HttpRequest(url, json.toString());

        boolean success = request.execute();
        if (!success && request.getAttemptNumber() < MAX_RETRIES) {
            retryRequest(request);
        }
    }

    private static void retryRequest(final HttpRequest request) {
        EXECUTOR.schedule(new Runnable() {
            public void run() {
                request.execute();
            }
        }, request.getAttemptNumber(), TimeUnit.SECONDS);
    }

    private static URL getURL(String urlString) {
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            LogLog.error("Error parsing the notifiying URL", e);
            throw new IllegalArgumentException();
        }
        return url;
    }
}
