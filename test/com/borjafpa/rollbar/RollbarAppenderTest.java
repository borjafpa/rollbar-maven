package com.borjafpa.rollbar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

import java.util.Hashtable;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.Priority;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.borjafpa.rollbar.RollbarAppender;
import com.borjafpa.rollbar.RollbarNotifier;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RollbarNotifier.class, MDC.class})
public class RollbarAppenderTest {
    
    @Test
    public void testAppendLoggingEventFirstCase() throws Exception {
        /*
         * - Not enabled
         */
        mockNotifier();
        
        String fqnOfCategoryClass = RollbarAppenderTest.class.getName();
        Category logger = Logger.getLogger(RollbarAppenderTest.class);
        Priority level = Priority.DEBUG;
        String message = "message";
        Throwable throwable = new Exception();
        
        LoggingEvent event = new LoggingEvent(fqnOfCategoryClass, logger, level, message, throwable);
        
        RollbarAppender appender = new RollbarAppender();
        appender.setEnabled(false);
        appender.append(event);
        
        checkNotifier(0, 0);
    }
    
    @Test
    public void testAppendLoggingEventSecondCase() throws Exception {
        /*
         * - Enable
         * - Does not have to notify
         */
        mockNotifier();
        
        String fqnOfCategoryClass = RollbarAppenderTest.class.getName();
        Category logger = Logger.getLogger(RollbarAppenderTest.class);
        Priority level = Priority.DEBUG;
        String message = "message";
        Throwable throwable = new Exception();
        
        LoggingEvent event = new LoggingEvent(fqnOfCategoryClass, logger, level, message, throwable);
        
        RollbarAppender appender = new RollbarAppender();
        appender.setLayout(new SimpleLayout());
        appender.setEnabled(true);
        appender.append(event);
        
        checkNotifier(0, 0);
    }
    
    @Test
    public void testAppendLoggingEventThirdCase() throws Exception {
        /*
         * - Enabled
         * - Have to notify
         * - Only throwable and is not a throwable
         */
        mockNotifier();
        
        String fqnOfCategoryClass = RollbarAppenderTest.class.getName();
        Category logger = Logger.getLogger(RollbarAppenderTest.class);
        Priority level = Priority.ERROR;
        String message = "message";
        Throwable throwable = null;
        
        LoggingEvent event = new LoggingEvent(fqnOfCategoryClass, logger, level, message, throwable);
        
        RollbarAppender appender = new RollbarAppender();
        appender.setLayout(new SimpleLayout());
        appender.setEnabled(true);
        appender.setOnlyThrowable(true);
        appender.append(event);
        
        checkNotifier(0, 0);
    }
    
    @Test
    public void testAppendLoggingEventFourthCase() throws Exception {
        /*
         * - Enabled
         * - Have to notify
         * - Not only throwable
         * - Is throwable
         */
        mockNotifier();
        mockMDC();
        
        String fqnOfCategoryClass = RollbarAppenderTest.class.getName();
        Category logger = Logger.getLogger(RollbarAppenderTest.class);
        Priority level = Priority.ERROR;
        String message = "message";
        Throwable throwable = new Exception();
        
        LoggingEvent event = new LoggingEvent(fqnOfCategoryClass, logger, level, message, throwable);
        
        RollbarAppender appender = new RollbarAppender();
        appender.setLayout(new SimpleLayout());
        appender.setEnabled(true);
        appender.setOnlyThrowable(false);
        appender.append(event);
        
        checkNotifier(0, 1);
    }
    
    @Test
    public void testAppendLoggingEventFifthCase() throws Exception {
        /*
         * - Enabled
         * - Have to notify
         * - Not only throwable
         * - Is not throwable
         */
        mockNotifier();
        mockMDC();
        
        String fqnOfCategoryClass = RollbarAppenderTest.class.getName();
        Category logger = Logger.getLogger(RollbarAppenderTest.class);
        Priority level = Priority.ERROR;
        String message = "message";
        Throwable throwable = null;
        
        LoggingEvent event = new LoggingEvent(fqnOfCategoryClass, logger, level, message, throwable);
        
        RollbarAppender appender = new RollbarAppender();
        appender.setLayout(new SimpleLayout());
        appender.setEnabled(true);
        appender.setOnlyThrowable(false);
        appender.append(event);
        
        checkNotifier(1, 0);
    }

    @Test
    public void testHasToNotifyFirstCase() {
        /*
         * - Has to notify 
         */
        
        RollbarAppender appender = new RollbarAppender();
        appender.setLevel(Level.DEBUG.toString());
        
        assertTrue("It returns that it has to notify", appender.hasToNotify(Priority.WARN));
    }
    
    @Test
    public void testHasToNotifySecondCase() {
        /*
         * - Does not have to notify 
         */
        
        RollbarAppender appender = new RollbarAppender();
        appender.setLevel(Level.ERROR.toString());
        
        assertFalse("It returns that it has not to notify", appender.hasToNotify(Priority.WARN));
    }

    @Test
    public void testRequiresLayout() {
        RollbarAppender appender = new RollbarAppender();
        
        assertTrue("It returns that requires layout", appender.requiresLayout());
    }
    
    private void mockNotifier() throws Exception {
        PowerMockito.mockStatic(RollbarNotifier.class);
        PowerMockito.doNothing().when(RollbarNotifier.class, "notify", anyString(), anyMap());
        PowerMockito.doNothing().when(RollbarNotifier.class, "notifyError", anyString(), anyObject(), anyMap());
    }
    
    private void mockMDC() throws Exception {
        Hashtable<String, Object> context = new Hashtable<String, Object>();
        
        PowerMockito.mockStatic(MDC.class);
        PowerMockito.when(MDC.class, "getContext").thenReturn(context);
    }
    
    private void checkNotifier(int timesNotify, int timesNotifyError) throws Exception {
        
        PowerMockito.verifyStatic(Mockito.times(timesNotify));
        RollbarNotifier.notify(anyString(), anyMap());
        
        PowerMockito.verifyStatic(Mockito.times(timesNotifyError));
        RollbarNotifier.notifyError(anyString(), (Throwable)anyObject(), anyMap());
        
    }

}
