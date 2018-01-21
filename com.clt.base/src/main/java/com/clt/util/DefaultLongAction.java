package com.clt.util;

import com.clt.event.ProgressListener;

/**
 * @author dabo
 */
public abstract class DefaultLongAction extends AbstractLongAction {

    private String description;

    /**
     * Constructor. Takes a description as argument.
     *
     * @param description Description of the executed action
     */
    public DefaultLongAction(String description) {

        this.description = description;
    }

    @Override
    protected abstract void run(ProgressListener l) throws Exception;

    @Override
    public String getDescription() {

        return this.description;
    }

    public static <T> LongCallable<T> asCallable(final LongAction action) {

        return new AbstractLongCallable<T>() {

            @Override
            public void cancel() {

                if (action instanceof Cancellable) {
                    ((Cancellable) action).cancel();
                }
            }

            @Override
            public boolean canCancel() {

                if (action instanceof Cancellable) {
                    return ((Cancellable) action).canCancel();
                } else {
                    return false;
                }
            }

            @Override
            protected T call(ProgressListener l)
                    throws Exception {

                action.addProgressListener(l);
                action.run();
                action.removeProgressListener(l);
                return null;
            }

            @Override
            public String getDescription() {

                return action.getDescription();
            }
        };
    }
}
