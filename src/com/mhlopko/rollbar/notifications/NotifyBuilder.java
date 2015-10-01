package com.mhlopko.rollbar.notifications;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.helpers.LogLog;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mhlopko.rollbar.util.AppConfiguration;
import com.mhlopko.rollbar.util.AppConfigurationKey;

public class NotifyBuilder {

    private static final String NOTIFIER_NAME = "Rollbar-maven";
    private static final String NOTIFIER_VERSION = "0.1";
    private static final String NOTIFIER_LANGUAGE = "java";

    private final String accessToken;
    private final String environment;

    private final JSONObject notifierData;
    private final JSONObject serverData;

    NotifyBuilder(String accessToken, String environment) throws UnknownHostException {
        this.accessToken = accessToken;
        this.environment = environment;

        notifierData = getNotifierData();
        serverData = getServerData();
    }

    @SuppressWarnings("unchecked")
    JSONObject build(String level, String message, Throwable throwable, Map<String, Object> context) {

        JSONObject payload = new JSONObject();

        // access token
        payload.put(RollbarParameter.ACCESS_TOKEN.getKey(), this.accessToken);

        // data
        JSONObject data = new JSONObject();

        // general values
        data.put(RollbarParameter.ENVIRONMENT.getKey(), this.environment);
        data.put(RollbarParameter.LEVEL.getKey(), level);
        data.put(
                RollbarParameter.PLATFORM.getKey(),
                getValue(RollbarParameter.PLATFORM.getKey(), context, NOTIFIER_LANGUAGE)
                );
        data.put(
                RollbarParameter.FRAMEWORK.getKey(),
                getValue(RollbarParameter.FRAMEWORK.getKey(), context, NOTIFIER_LANGUAGE)
                );
        data.put(RollbarParameter.LANGUAGE.getKey(), NOTIFIER_LANGUAGE);
        data.put(RollbarParameter.TIMESTAMP.getKey(), System.currentTimeMillis() / 1000);

        // message data
        data.put(RollbarParameter.BODY.getKey(), getBody(message, throwable));

        // request data
        if (context != null) {
            JSONObject requestData = getRequestData(context);
            if (requestData != null && !requestData.isEmpty()) {
                data.put(RollbarParameter.REQUEST.getKey(), requestData);
            }
        }

        // custom data
        JSONObject customData = new JSONObject();
        fillCustomData(customData, context);

        // log message
        if (throwable != null && message != null) {
            customData.put(RollbarParameter.LOG.getKey(), message);
        }

        // logs
        if (context != null) {
            JSONArray logsData = getLogsData(context);
            if (logsData != null) customData.put(RollbarParameter.LOGS.getKey(), logsData);
        }

        if (!customData.isEmpty()) data.put(RollbarParameter.CUSTOM.getKey(), customData);

        // person data
        if (context != null) {
            JSONObject personData = getPersonData(context);
            if (personData != null) data.put(RollbarParameter.PERSON.getKey(), personData);
        }

        // client data
        if (context != null) {
            JSONObject clientData = getClientData(context);
            if (clientData != null) data.put(RollbarParameter.CLIENT.getKey(), clientData);
        }

        // server data
        data.put(RollbarParameter.SERVER.getKey(), serverData);

        // notifier data
        data.put(RollbarParameter.NOTIFIER.getKey(), notifierData);

        payload.put(RollbarParameter.DATA.getKey(), data);

        return payload;
    }

    @SuppressWarnings("unchecked")
    private JSONObject getBody(String message, Throwable original) {
        JSONObject body = new JSONObject();

        Throwable throwable = original;

        if (throwable != null) {
            List<JSONObject> traces = new ArrayList<JSONObject>();
            do {
                traces.add(0, createTrace(throwable));
                throwable = throwable.getCause();
            } while (throwable != null);

            JSONArray tracesArray = new JSONArray();

            for ( JSONObject trace: traces ) {
                tracesArray.add(trace);
            }

            body.put(RollbarParameter.TRACE_CHAIN.getKey(), tracesArray);
        }

        if (original == null && message != null) {
            JSONObject messageBody = new JSONObject();
            messageBody.put(RollbarParameter.BODY.getKey(), message);
            body.put(RollbarParameter.MESSAGE.getKey(), messageBody);
        }

        return body;
    }

    @SuppressWarnings("unchecked")
    private JSONObject getRequestData(Map<String, Object> context) {

        JSONObject requestData = new JSONObject();

        String rqParam = RollbarParameter.REQUEST.getKey();
        HttpServletRequest httpRequest =
                isHttpServletRequest(context, rqParam) ? (HttpServletRequest) context.get(rqParam) : null;

        // url: full URL where this event occurred
        String url = getValue(RollbarParameter.URL.getKey(), context, null);
        if (url == null && httpRequest != null) url = httpRequest.getRequestURI();
        if (url != null) requestData.put(RollbarParameter.URL.getKey(), url);

        // method: the request method
        String method = getValue(RollbarParameter.METHOD.getKey(), context, null);
        if (method == null && httpRequest != null) method = httpRequest.getMethod();
        if (method != null) requestData.put(RollbarParameter.METHOD.getKey(), method);

        // headers
        Map<String, String> headers =
                (Map<String, String>) context.get(RollbarParameter.HEADERS.getKey());
        if (headers == null && httpRequest != null) {
            headers = new HashMap<String, String>();

            Enumeration<String> headerNames = httpRequest.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, httpRequest.getHeader(headerName));
            }
        }
        if (headers != null) {
            JSONObject headersData = new JSONObject();
            for (Entry<String, String> entry : headers.entrySet()) {
                headersData.put(entry.getKey(), entry.getValue());
            }
            if (!headersData.isEmpty()) {
                requestData.put(RollbarParameter.HEADERS.getKey(), headersData);
            }
        }

        // params
        Map<String, String> params =
        (Map<String, String>) context.get(RollbarParameter.PARAMS.getKey());
        if (params == null && httpRequest != null) params = httpRequest.getParameterMap();
        if (params != null) {
            JSONObject paramsData = new JSONObject();
            for (Entry<String, String> entry : params.entrySet()) {
                paramsData.put(entry.getKey(), entry.getValue());
            }
            if (!paramsData.isEmpty()) {
                String key = method != null ?
                        (method.equalsIgnoreCase("post") ? "POST" : "GET") : "parameters";
                        requestData.put(key, paramsData);
            }
        }

        // query string
        String query = (String) context.get(RollbarParameter.QUERY.getKey());
        if (query == null && httpRequest != null) query = httpRequest.getQueryString();
        if (query != null) requestData.put(RollbarParameter.QUERY_STRING.getKey(), query);

        // user ip
        String userIP = (String) context.get(RollbarParameter.USER_IP.getKey());
        if (userIP == null && httpRequest != null) userIP = httpRequest.getRemoteAddr();
        if (userIP != null) requestData.put(RollbarParameter.USER_IP.getKey(), userIP);

        // sessionId
        String sessionId = null;
        Object sessionObj = context.get(RollbarParameter.SESSION.getKey());
        if (sessionObj instanceof HttpSession) {
            sessionId = ((HttpSession) sessionObj).getId();
        } else if (sessionObj instanceof String) {
            sessionId = (String) sessionObj;
        }
        if (sessionId == null && httpRequest != null) {
            HttpSession session = httpRequest.getSession(false);
            if (session != null) sessionId = session.getId();

        }
        if (sessionId != null) requestData.put(RollbarParameter.SESSION.getKey(), sessionId);

        // protocol
        String protocol = (String) context.get(RollbarParameter.PROTOCOL.getKey());
        if (protocol == null && httpRequest != null) protocol = httpRequest.getProtocol();
        if (protocol != null) requestData.put(RollbarParameter.PROTOCOL.getKey(), protocol);

        // requestId
        String requestId = (String) context.get(RollbarParameter.REQUEST_ID.getKey());
        if (requestId != null) requestData.put(RollbarParameter.ID.getKey(), requestId);

        return requestData;
    }

    private boolean isHttpServletRequest(Map<String, Object> context, String rqParam) {
        return context.get(rqParam) != null && context.get(rqParam) instanceof HttpServletRequest;
    }

    @SuppressWarnings("unchecked")
    private JSONObject fillCustomData(JSONObject customData, Map<String, Object> context) {

        if ( context != null ) {
            for (Entry<String, Object> entry : context.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    customData.put(entry.getKey(), value);
                }
                // HTTPSession attributes
                else if (value instanceof HttpSession) {
                    HttpSession session = (HttpSession) value;
                    Enumeration<String> attributes = session.getAttributeNames();
                    while (attributes.hasMoreElements()) {
                        String nameSession = attributes.nextElement();
                        Object valueSession = session.getAttribute(nameSession);
                        if (valueSession instanceof String) {
                            String str = (String) valueSession;
                            customData.put(
                                    RollbarParameter.CUSTOM_DATA_SESSION.getKey() + nameSession,
                                    str
                                    );
                        } else if (valueSession instanceof String[]) {
                            String[] array = (String[]) valueSession;
                            customData.put(
                                    RollbarParameter.CUSTOM_DATA_SESSION.getKey() + nameSession,
                                    Arrays.asList(array)
                                    );
                        }
                    }

                }
                // HttpServletRequest attributes
                else if (value instanceof HttpServletRequest) {
                    HttpServletRequest servletRequest = (HttpServletRequest) value;
                    Enumeration<String> attributes = servletRequest.getAttributeNames();
                    while (attributes.hasMoreElements()) {
                        String nameRequest = attributes.nextElement();
                        Object valueRequest = servletRequest.getAttribute(nameRequest);
                        if (valueRequest instanceof String) {
                            String str = (String) valueRequest;
                            customData.put(
                                    RollbarParameter.CUSTOM_DATA_ATTRIBUTE.getKey() + nameRequest,
                                    str
                                    );
                        } else if (valueRequest instanceof String[]) {
                            String[] array = (String[]) valueRequest;
                            customData.put(
                                    RollbarParameter.CUSTOM_DATA_ATTRIBUTE.getKey() + nameRequest,
                                    Arrays.asList(array)
                                    );
                        }
                    }

                }
            }
        }

        return customData;
    }

    @SuppressWarnings("unchecked")
    private JSONArray getLogsData(Map<String, Object> context) {
        JSONArray logsData = null;

        List<String> lines = (List<String>) context.get(RollbarParameter.LOGS.getKey());
        if (lines != null) {

            logsData = new JSONArray();

            for ( String line: lines ) {
                logsData.add(line);
            }
        }

        return logsData;
    }

    @SuppressWarnings("unchecked")
    private JSONObject getClientData(Map<String, Object> context) {
        JSONObject clientData = null;

        String browser = getValue(RollbarParameter.USER_AGENT.getKey(), context, null);
        if (browser == null) {
            HttpServletRequest request =
                    (HttpServletRequest) context.get(RollbarParameter.REQUEST.getKey());
            if (request != null) {
                browser = request.getHeader(RollbarParameter.USER_AGENT_CAMELCASE.getKey());
            }
        }

        if (browser != null) {
            clientData = new JSONObject();

            JSONObject javascript = new JSONObject();
            javascript.put(RollbarParameter.BROWSER.getKey(), browser);

            clientData.put(RollbarParameter.JAVASCRIPT.getKey(), javascript);
        }
        return clientData;
    }

    @SuppressWarnings("unchecked")
    private JSONObject getPersonData(Map<String, Object> context) {
        JSONObject personData = null;

        String id = getValue(RollbarParameter.USER.getKey(), context, null);
        if (id != null) {
            personData = new JSONObject();

            personData.put(RollbarParameter.ID.getKey(), id);
            setIfNotNull(RollbarParameter.USERNAME.getKey(), personData, context);
            setIfNotNull(RollbarParameter.USER_EMAIL.getKey(), personData, context);
        }
        return personData;
    }

    @SuppressWarnings("unchecked")
    private JSONObject getNotifierData() {
        JSONObject notifier = new JSONObject();
        notifier.put(RollbarParameter.NAME.getKey(), NOTIFIER_NAME);
        notifier.put(RollbarParameter.VERSION.getKey(), NOTIFIER_VERSION);
        return notifier;
    }

    @SuppressWarnings("unchecked")
    private JSONObject getServerData() throws UnknownHostException {

        InetAddress localhost = InetAddress.getLocalHost();

        String host = localhost.getHostName();
        String ip = localhost.getHostAddress();

        JSONObject notifier = new JSONObject();
        notifier.put(RollbarParameter.HOST.getKey(), host);
        notifier.put(RollbarParameter.IP.getKey(), ip);
        return notifier;
    }

    private void setIfNotNull(String jsonKey, JSONObject object, Map<String, Object> context) {
        setIfNotNull(jsonKey, object, jsonKey, context);
    }

    @SuppressWarnings("unchecked")
    private void setIfNotNull(String jsonKey, JSONObject object, String key, Map<String, Object> context) {
        String value = getValue(key, context, null);
        if (value != null) object.put(jsonKey, value);
    }

    private String getValue(String key, Map<String, Object> context, String defaultValue) {
        if (context == null) return defaultValue;
        Object value = context.get(key);
        if (value == null) return defaultValue;
        return value.toString();
    }

    @SuppressWarnings("unchecked")
    private JSONObject createTrace(Throwable throwable) {
        JSONObject trace = new JSONObject();

        JSONArray frames = new JSONArray();

        StackTraceElement[] elements = throwable.getStackTrace();
        for (int i = elements.length - 1; i >= 0; --i) {
            StackTraceElement element = elements[i];

            JSONObject frame = new JSONObject();

            frame.put(RollbarParameter.CLASS_NAME.getKey(), element.getClassName());
            frame.put(RollbarParameter.FILENAME.getKey(), element.getFileName());
            frame.put(RollbarParameter.METHOD.getKey(), element.getMethodName());

            if (element.getLineNumber() > 0) {
                frame.put(RollbarParameter.LINE_NUMBER.getKey(), element.getLineNumber());
            }

            frames.add(frame);
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);

            throwable.printStackTrace(ps);
            ps.close();
            baos.close();

            trace.put(RollbarParameter.RAW.getKey(), baos.toString("UTF-8"));
        } catch (Exception e) {
            LogLog.error("Exception printing stack trace.", e);
        }

        JSONObject exceptionData = new JSONObject();
        exceptionData.put(RollbarParameter.CLASS.getKey(), throwable.getClass().getName());
        exceptionData.put(RollbarParameter.MESSAGE.getKey(), throwable.getMessage());

        trace.put(RollbarParameter.FRAMES.getKey(), frames);
        trace.put(RollbarParameter.EXCEPTION.getKey(), exceptionData);

        return trace;
    }

}
