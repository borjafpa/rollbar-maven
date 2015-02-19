package com.borjafpa.rollbar;

public enum RollbarParameter {
    // Default parameters
    ACCESS_TOKEN("access_token"),
    ENVIRONMENT("environment"),
    LEVEL("level"),
    PLATFORM("platform"),
    FRAMEWORK("framework"),
    LANGUAGE("language"),
    TIMESTAMP("timestamp"),
    
    // Body parameters
    DATA("data"),
    TRACE_CHAIN("trace_chain"),
    BODY("body"),
    MESSAGE("message"),
    
    // Request parameters
    URL("url"),
    METHOD("method"),
    HEADERS("headers"),
    PARAMS("params"),
    USER_AGENT("user_agent"),
    USER_AGENT_CAMELCASE("User-Agent"),
    QUERY("query"),
    QUERY_STRING("query_string"),
    REQUEST("request"),
    SESSION("session"),
    PROTOCOL("protocol"),
    REQUEST_ID("requestId"),
    ID("id"),
    
    // Trace parameters
    RAW("raw"),
    FRAMES("frames"),
    EXCEPTION("exception"),
    CLASS_NAME("class_name"),
    FILENAME("filename"),
    LINE_NUMBER("lineno"),
    CLASS("class"),
    
    
    // Log parameter
    LOG("log"),
    
    // Logs parameter
    LOGS("logs"),
    
    // Custom parameters
    CUSTOM("custom"),
    CUSTOM_DATA_SESSION("session."),
    CUSTOM_DATA_ATTRIBUTE("attribute."),
    
    // Person parameters
    USER("user"),
    PERSON("person"),
    USER_IP("user_ip"),
    USERNAME("username"),
    USER_EMAIL("email"),
    
    // Client parameters
    CLIENT("client"),
    BROWSER("browser"),
    JAVASCRIPT("javascript"),
    
    // Server parameters
    SERVER("server"),
    HOST("host"),
    IP("ip"),
    
    // Notifier parameters
    NOTIFIER("notifier"),
    NAME("name"),
    VERSION("version");
    
    private String key;
    
    RollbarParameter(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
