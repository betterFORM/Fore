define(["dojo/dom", "dojo/dom-class", "dojo/behavior", "dojo/_base/xhr","bf/XFControl"],
    function(dom, domClass, behavior, xhr, XFControl) {

    /*
    * Copyright (c) 2012. betterFORM Project - http://www.betterform.de
    * Licensed under the terms of BSD License
    */

    return {

        /*
         ###########################################################################################
         matching all elements with .xfControl and instanciate a XFControl instance for each of them.
         Order is important here - all XFControl should be instanciated before their respective widget childs are
         created. Thus this behavior must be loaded before any other control behavior file.
         */
        '.xfControl':function(n) {
            console.debug("ControlBehaviour: XFControl found: ",n);
            var controlId = dojo.attr(n,"id");
            new XFControl({
                id:controlId,
                controlType:"generic"
            }, n);
        }
    }
});




