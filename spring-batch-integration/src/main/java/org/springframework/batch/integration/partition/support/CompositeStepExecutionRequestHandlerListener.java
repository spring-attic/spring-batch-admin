package org.springframework.batch.integration.partition.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.integration.partition.StepExecutionRequestHandlerListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener composed of a set of other listeners.
 *
 * @author Sebastien Gerard
 * @author Stephane Nicoll
 */
public class CompositeStepExecutionRequestHandlerListener implements StepExecutionRequestHandlerListener {

    private static final Log logger = LogFactory.getLog(CompositeStepExecutionRequestHandlerListener.class);

    private final List<StepExecutionRequestHandlerListener> handlerListeners;

    /**
     * Creates a new instance with an empty listener set.
     */
    public CompositeStepExecutionRequestHandlerListener() {
        this(new ArrayList<StepExecutionRequestHandlerListener>());
    }

    /**
     * Creates a new instance with the specified listener set.
     *
     * @param handlerListeners the listener set
     */
    public CompositeStepExecutionRequestHandlerListener(List<StepExecutionRequestHandlerListener> handlerListeners) {
        this.handlerListeners = handlerListeners;
    }

    public void beforeHandle(StepExecution stepExecution) {
        for (StepExecutionRequestHandlerListener listener : handlerListeners) {
            try {
                listener.beforeHandle(stepExecution);
            } catch (Exception e) {
                logger.warn("Unexpected exception in a request handler listener", e);
            }
        }
    }

    public void onDeniedException(StepExecution stepExecution) {
        for (StepExecutionRequestHandlerListener listener : handlerListeners) {
            try {
                listener.onDeniedException(stepExecution);
            } catch (Exception e) {
                logger.warn("Unexpected exception in a request handler listener", e);
            }
        }
    }

    public void afterHandle(StepExecution stepExecution) {
        for (StepExecutionRequestHandlerListener listener : handlerListeners) {
            try {
                listener.afterHandle(stepExecution);
            } catch (Exception e) {
                logger.warn("Unexpected exception in a request handler listener", e);
            }
        }
    }

}

