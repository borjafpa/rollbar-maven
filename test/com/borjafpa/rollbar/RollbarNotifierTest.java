package com.borjafpa.rollbar;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.borjafpa.rollbar.RollbarNotifier;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RollbarNotifier.class})
public class RollbarNotifierTest {
    
    private String message = "message";
    private Exception throwable = new Exception();
    private Map<String, Object> context = new HashMap<String, Object>();
    private RollbarNotifier.Level level = RollbarNotifier.Level.WARNING;
    
    @Test
    public void testInit() throws UnknownHostException {
        /*
         * - With valid URL 
         */
        
        initNotifier();
        
        assertNotNull("It set the URL to notify", RollbarNotifier.getUrl());
        assertNotNull("It set the builder to notify", RollbarNotifier.getBuilder());
    }

    @Test
    public void testNotifyString() throws Exception {
        mockNotifier();
        initNotifier();
        
        RollbarNotifier.notify(message);
        
        checkNotifier(RollbarNotifier.Level.INFO, message, null, null);
    }

    @Test
    public void testNotifyStringMapOfStringObject() throws Exception {
        mockNotifier();
        initNotifier();
        
        RollbarNotifier.notify(message, context);
        
        checkNotifier(RollbarNotifier.Level.INFO, message, null, context);
    }

    @Test
    public void testNotifyLevelString() throws Exception {
        mockNotifier();
        initNotifier();
        
        RollbarNotifier.notify(level, message);
        
        checkNotifier(level, message, null, null);
    }

    @Test
    public void testNotifyLevelStringMapOfStringObject() throws Exception {
        mockNotifier();
        initNotifier();
        
        RollbarNotifier.notify(level, message, context);
        
        checkNotifier(level, message, null, context);
    }

    @Test
    public void testNotifyErrorThrowable() throws Exception {
        mockNotifier();
        initNotifier();
        
        RollbarNotifier.notifyError(throwable);
        
        checkNotifier(RollbarNotifier.Level.ERROR, null, throwable, null);
    }

    @Test
    public void testNotifyErrorThrowableMapOfStringObject() throws Exception {
        mockNotifier();
        initNotifier();
        
        RollbarNotifier.notifyError(throwable, context);
        
        checkNotifier(RollbarNotifier.Level.ERROR, null, throwable, context);
    }

    @Test
    public void testNotifyErrorStringThrowable() throws Exception {
        mockNotifier();
        initNotifier();
        
        RollbarNotifier.notifyError(message, throwable);
        
        checkNotifier(RollbarNotifier.Level.ERROR, message, throwable, null);
    }

    @Test
    public void testNotifyErrorStringThrowableMapOfStringObject() throws Exception {
        mockNotifier();
        initNotifier();
        
        RollbarNotifier.notifyError(message, throwable, context);
        
        checkNotifier(RollbarNotifier.Level.ERROR, message, throwable, context);
    }

    @Test
    public void testNotifyInLevelLevelThrowable() throws Exception {
        mockNotifier();
        initNotifier();
        
        RollbarNotifier.notifyInLevel(level, throwable);
        
        checkNotifier(level, null, throwable, null);
    }
    
    @Test
    public void testNotifyInLevelLevelThrowableMapOfStringObject() throws Exception {
        mockNotifier();
        initNotifier();
        
        RollbarNotifier.notifyInLevel(level, throwable, context);
        
        checkNotifier(level, null, throwable, context);
    }
    
    private void initNotifier() throws UnknownHostException {
        String url = "http://www.google.com";
        String apiKey = "apiKey";
        String environment = "Test";
        
        RollbarNotifier.init(url, apiKey, environment);
    }

    private void mockNotifier() throws Exception {
        PowerMockito.mockStatic(RollbarNotifier.class);
        
        PowerMockito.when(
                RollbarNotifier.class, "init", 
                anyString(), anyString(), anyString()
                ).thenCallRealMethod();
        
        PowerMockito.when(RollbarNotifier.class, "notify", anyString()).thenCallRealMethod();
        PowerMockito.when(RollbarNotifier.class, "notify", anyString(), anyMap()).thenCallRealMethod();
        PowerMockito.when(RollbarNotifier.class, "notify", anyObject(), anyString()).thenCallRealMethod();
        PowerMockito.when(RollbarNotifier.class, "notify", anyObject(), anyString(), anyMap()).thenCallRealMethod();
        
        PowerMockito.when(RollbarNotifier.class, "notifyError", anyObject()).thenCallRealMethod();
        PowerMockito.when(RollbarNotifier.class, "notifyError", anyObject(), anyMap()).thenCallRealMethod();
        PowerMockito.when(RollbarNotifier.class, "notifyError", anyString(), anyObject()).thenCallRealMethod();
        PowerMockito.when(RollbarNotifier.class, "notifyError", anyString(), anyObject(), anyMap()).thenCallRealMethod();
        
        PowerMockito.when(RollbarNotifier.class, "notifyInLevel", anyObject(), anyObject()).thenCallRealMethod();
        PowerMockito.when(RollbarNotifier.class, "notifyInLevel", anyObject(), anyObject(), anyMap()).thenCallRealMethod();
        
        PowerMockito.doNothing().when(
                            RollbarNotifier.class, "notify", 
                            anyObject(), anyString(), anyObject(), anyObject()
                        );
    }
    
    private void checkNotifier(RollbarNotifier.Level level, String message, Throwable throwable, 
            Map<String, Object> context) throws Exception {
        
        PowerMockito.verifyPrivate(
                RollbarNotifier.class, Mockito.times(1)
                ).invoke(
                            "notify", 
                            eq(level), eq(message), eq(throwable), eq(context)
                        );
        
    }
}
