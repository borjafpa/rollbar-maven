package com.mhlopko.rollbar.notifications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mhlopko.rollbar.notifications.NotifyBuilder;
import com.mhlopko.rollbar.notifications.RollbarNotifier;
import com.mhlopko.rollbar.notifications.RollbarParameter;

public class NotifyBuilderTest {

    private static final String accessToken = "accessToken" + System.currentTimeMillis();
    private static final String environment = "test" + System.currentTimeMillis();
    private static final String level = RollbarNotifier.Level.INFO.toString();
    private static NotifyBuilder builder = null;

    @BeforeClass
    public static void init() throws UnknownHostException {
        builder = new NotifyBuilder(accessToken, environment);
    }

    @Test
    public void testBuildFirstCase() {
        /*
         * - Checking default values
         *  - access token
         *  - data
         *  - general values
         *  - message data
         *  - server data
         *  - notifier data
         *
         * - Exception null
         * - Context null
         */
        JSONObject json = builder.build(level, null, null, null);
        JSONObject data = (JSONObject) json.get(RollbarParameter.DATA.getKey());

        assertEquals(
                "The json result includes the access token",
                accessToken, json.get(RollbarParameter.ACCESS_TOKEN.getKey())
                );

        assertTrue(
                "The default data is into data",
                data != null && data.containsKey(RollbarParameter.ENVIRONMENT.getKey())
                        && data.containsKey(RollbarParameter.LEVEL.getKey())
                        && data.containsKey(RollbarParameter.PLATFORM.getKey())
                        && data.containsKey(RollbarParameter.FRAMEWORK.getKey())
                        && data.containsKey(RollbarParameter.LANGUAGE.getKey())
                        && data.containsKey(RollbarParameter.TIMESTAMP.getKey())
                        && data.containsKey(RollbarParameter.BODY.getKey())
                        && data.containsKey(RollbarParameter.SERVER.getKey())
                        && data.containsKey(RollbarParameter.NOTIFIER.getKey())
                );
    }

    @Test
    public void testBuildSecondCase() {
        /*
         * - Checking default values
         *  - access token
         *  - data
         *  - general values
         *  - message data
         *  - server data
         *  - notifier data
         *
         * - With exception not null
         */

        String message = "Message " + System.currentTimeMillis();

        Exception exception = new Exception("Exception message");
        JSONObject json = builder.build(level, message, exception, null);
        JSONObject data = (JSONObject) json.get(RollbarParameter.DATA.getKey());
        JSONObject body = (JSONObject) data.get(RollbarParameter.BODY.getKey());
        JSONObject custom = (JSONObject) data.get(RollbarParameter.CUSTOM.getKey());

        assertTrue(
                "The json result includes parameters related to the Exception",
                body.containsKey(RollbarParameter.TRACE_CHAIN.getKey())
                );

        assertEquals(
                "The parameter log data is into the custom parameters",
                message,
                custom.get(RollbarParameter.LOG.getKey())
                );
    }

    @Test
    public void testBuildThirdCase() {
        /*
         * - Checking default values
         *  - access token
         *  - data
         *  - general values
         *  - message data
         *  - server data
         *  - notifier data
         *
         * - With not empty context
         */
        String parameter1 = "p1";
        String parameter2 = "p2";
        String value1 = "v1";
        String value2 = "v2";

        Map<String, Object> context = new HashMap<String, Object>();
        context.put(parameter1, value1);
        context.put(parameter2, value2);

        JSONObject json = builder.build(level, null, null, context);
        JSONObject data = (JSONObject) json.get(RollbarParameter.DATA.getKey());
        JSONObject custom = (JSONObject) data.get(RollbarParameter.CUSTOM.getKey());

        assertFalse(
                "The json result includes the custom data into the data parameters",
                custom.isEmpty()
                );
        assertEquals(
                "The custom parameter includes the key " + parameter1,
                custom.get(parameter1), value1
                );
        assertEquals(
                "The custom parameter includes the key " + parameter2,
                custom.get(parameter2), value2
                );
    }

    @Test
    public void testBuildFourthCase() {
        /*
         * - With request data
         */
        String url = "http://url.com";
        String method = "GET";
        String userIp = "127.0.0.1";
        String session = "session";
        String protocol = "http";
        String requestId = "request.id";

        String headerKey1 = "hp1";
        String headerKey2 = "hp2";
        String headerValue1 = "hv1";
        String headerValue2 = "hv2";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(headerKey1, headerValue1);
        headers.put(headerKey2, headerValue2);

        String paramKey1 = "hp1";
        String paramValue1 = "hv1";
        Map<String, String> params = new HashMap<String, String>();
        params.put(paramKey1, paramValue1);

        String query = "query";

        Map<String, Object> context = new HashMap<String, Object>();

        context.put(RollbarParameter.URL.getKey(), url);
        context.put(RollbarParameter.METHOD.getKey(), method);
        context.put(RollbarParameter.HEADERS.getKey(), headers);
        context.put(RollbarParameter.PARAMS.getKey(), params);
        context.put(RollbarParameter.QUERY.getKey(), query);
        context.put(RollbarParameter.USER_IP.getKey(), userIp);
        context.put(RollbarParameter.SESSION.getKey(), session);
        context.put(RollbarParameter.PROTOCOL.getKey(), protocol);
        context.put(RollbarParameter.REQUEST_ID.getKey(), requestId);

        JSONObject json = builder.build(level, null, null, context);
        JSONObject data = (JSONObject) json.get(RollbarParameter.DATA.getKey());
        JSONObject request = (JSONObject) data.get(RollbarParameter.REQUEST.getKey());
        JSONObject requestParams = (JSONObject) request.get(method);

        assertFalse(
                "The json result includes the parameter request into the data",
                request.isEmpty()
                );

        assertEquals(
                "The url data is into the request parameters",
                request.get(RollbarParameter.URL.getKey()), url
                );

        assertEquals(
                "The method data is into the request parameters",
                request.get(RollbarParameter.METHOD.getKey()), method
                );

        assertTrue(
                "The headers data is into the request parameters",
                request.containsKey(RollbarParameter.HEADERS.getKey())
                );

        assertEquals(
                "The params data is into the request parameters",
                requestParams.get(paramKey1), paramValue1
                );

        assertEquals(
                "The query is into the request parameters",
                request.get(RollbarParameter.QUERY_STRING.getKey()),
                query
                );

        assertEquals(
                "The user IP is into the request parameters",
                request.get(RollbarParameter.USER_IP.getKey()),
                userIp
                );

        assertEquals(
                "The session is into the request parameters",
                request.get(RollbarParameter.SESSION.getKey()),
                session
                );

        assertEquals(
                "The protocol is into the request parameters",
                request.get(RollbarParameter.PROTOCOL.getKey()),
                protocol
                );

        assertEquals(
                "The request ID is into the request parameters",
                request.get(RollbarParameter.ID.getKey()),
                requestId
                );
    }

    @Test
    public void testBuildCase() {
        /*
         * - With logs
         */
        Map<String, Object> context = new HashMap<String, Object>();
        List<String> logs = new ArrayList<String>();

        logs.add("l1");
        logs.add("l2");
        logs.add("l3");

        context.put(RollbarParameter.LOGS.getKey(), logs);

        JSONObject json = builder.build(level, null, null, context);
        JSONObject data = (JSONObject) json.get(RollbarParameter.DATA.getKey());
        JSONObject custom = (JSONObject) data.get(RollbarParameter.CUSTOM.getKey());

        assertTrue(
                "The json result includes the logs inside the custom parameters",
                custom.containsKey(RollbarParameter.LOGS.getKey())
                );

        JSONArray logsArray = (JSONArray) custom.get(RollbarParameter.LOGS.getKey());

        assertEquals(
                "The result includes all the logs into the parameters",
                logsArray.size(), logs.size()
                );
    }

    @Test
    public void testBuildSixthCase() {
        /*
         * - With person data
         */
        String userId = "userId";
        String username = "username";
        String userEmail = "user@mail.com";

        Map<String, Object> context = new HashMap<String, Object>();

        context.put(RollbarParameter.USER.getKey(), userId);
        context.put(RollbarParameter.USERNAME.getKey(), username);
        context.put(RollbarParameter.USER_EMAIL.getKey(), userEmail);

        JSONObject json = builder.build(level, null, null, context);
        JSONObject data = (JSONObject) json.get(RollbarParameter.DATA.getKey());

        assertTrue(
                "The person info is into the data parameters",
                data.containsKey(RollbarParameter.PERSON.getKey())
                );

        JSONObject person = (JSONObject) data.get(RollbarParameter.PERSON.getKey());

        assertEquals(
                "The user data includes the user ID",
                person.get(RollbarParameter.ID.getKey()), userId
                );

        assertEquals(
                "The user data includes the username",
                person.get(RollbarParameter.USERNAME.getKey()), username
                );

        assertEquals(
                "The user data includes the user email",
                person.get(RollbarParameter.USER_EMAIL.getKey()), userEmail
                );

    }

    @Test
    public void testBuildSeventhCase() {
        /*
         * - With client data
         */

        String userAgent = "Mozilla/5.0%20(Windows%20NT%206.3;%20WOW64;%20rv:35.0)"
                + "%20Gecko/20100101%20Firefox/35.0";

        Map<String, Object> context = new HashMap<String, Object>();
        context.put(RollbarParameter.USER_AGENT.getKey(), userAgent);

        JSONObject json = builder.build(level, null, null, context);
        JSONObject data = (JSONObject) json.get(RollbarParameter.DATA.getKey());

        assertTrue(
                "The client info is into the data parameters",
                data.containsKey(RollbarParameter.CLIENT.getKey())
                );

        JSONObject client = (JSONObject) data.get(RollbarParameter.CLIENT.getKey());

        assertTrue(
                "The javascript info is into the client parameters",
                client.containsKey(RollbarParameter.JAVASCRIPT.getKey())
                );

        JSONObject jsClient = (JSONObject) client.get(RollbarParameter.JAVASCRIPT.getKey());

        assertEquals(
                "The browser information is into the javascript client parameter",
                jsClient.get(RollbarParameter.BROWSER.getKey()),
                userAgent
                );
    }
}
