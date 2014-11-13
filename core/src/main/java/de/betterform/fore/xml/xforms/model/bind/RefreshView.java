/*
 * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
 * Licensed under the terms of BSD License
 */

package de.betterform.fore.xml.xforms.model.bind;

import java.util.List;

/**
 * Refresh Viewport to model items. Provides access to changes of a model
 * item's properties.
 *
 */
public interface RefreshView {

    void setValueChangedMarker();
    void setReadonlyMarker();
    void setReadWriteMarker();
    void setEnabledMarker();
    void setDisabledMarker();
    void setOptionalMarker();
    void setRequiredMarker();
    void setValidMarker();
    void setInvalidMarker();
    void setInvalids(List<Constraint> invalids);
    List<Constraint> getInvalids();

    void reset();

    boolean isValueChangedMarked();
    boolean isValidMarked();
    boolean isInvalidMarked();
    boolean isReadonlyMarked();
    boolean isReadwriteMarked();
    boolean isRequiredMarked();
    boolean isOptionalMarked();
    boolean isEnabledMarked();
    boolean isDisabledMarked();
}
