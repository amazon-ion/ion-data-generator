package com.amazon.ion.datagenerator.schema.constraints;

import com.amazon.ion.IonList;
import com.amazon.ion.IonValue;

/**
 * Process the constraint 'Contains' which indicates that the list or S-expression is expected to contain all of specified
 * values, in no particular order.
 */
public class Contains implements ReparsedConstraint {
    private final IonList expectedContainedValues;

    /**
     * Initializing the newly created Object.
     * @param expectedContainedValue represents the value of constraint 'contains'.
     */
    private Contains(IonValue expectedContainedValue) {
        this.expectedContainedValues = (IonList) expectedContainedValue;
    }

    /**
     * Helping to access the private attribute expectedContainedValue.
     * @return IonList which represents the value of constraint 'contains'.
     */
    public IonList getExpectedContainedValues() {
        return this.expectedContainedValues;
    }

    /**
     * Parsing the provided value into Contains.
     * @param field represent the value of constraint 'contains'.
     * @return an object of Contains.
     */
    public static Contains of(IonValue field) {
        return new Contains(field);
    }
}
