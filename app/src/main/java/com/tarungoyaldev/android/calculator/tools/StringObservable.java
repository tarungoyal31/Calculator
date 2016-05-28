package com.tarungoyaldev.android.calculator.tools;

import java.util.Observable;

/**
 * String Observable to notify whenever there is a change in string data.
 */
public class StringObservable extends Observable {
    private String observedString;

    public StringObservable(String str) {
        this.observedString = str;
    }

    /**
     * Updates the string and notifies the observers according to boolean notify.
     * @param str String to be updated
     * @param notify whether to notify observers
     */
    public void updateString(String str, boolean notify) {
        observedString = str;
        if (notify) {
            setChanged();
            notifyObservers(observedString);
        }
    }

    /**
     * Updates the string and notifies the observers.
     * @param str String to be updated
     */
    public void updateString(String str) {
        updateString(str,true);
    }

    /**
     * Concatinates the string to current observed string and notifies the observers.
     * @param str String to be updated
     */
    public void concat(String str) {
        observedString = observedString.concat(str);
        setChanged();
        notifyObservers(observedString);
    }

    public String getObservedString() {
        return observedString;
    }
}
