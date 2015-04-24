/*!
 * Fore HTML5 form enhancer
 * author: joern turner 2015
 */
'use strict';

$(document).ready(function() {


    $("[data-repeat-insert]").on("click",function(){
        var targetRepeat = $(this).attr("data-repeat-insert");
        console.log("adding repeat-item....target repeat: ",targetRepeat);

        var repeat = $("[data-repeat-item=" + targetRepeat + "]:last");
        console.log("repeat item: ",repeat)

        var itemClone = repeat.clone();
        $(repeat).after(itemClone);
    });

    $(document.body).on("click",".repeat-delete", function(){
        var repeat = $(this).closest("[data-repeat-item]");
        console.log("repeat item: ",repeat)

        //check, if the repeat item is the last of its name and if so don't delete but empty controls
        var repeatName = $(repeat).attr("data-repeat-item");
        console.log("repeat item name: ",repeatName)

        var repeatEntries = $("[data-repeat-item=" + repeatName + "]");
        if(repeatEntries.length == 1){
            //todo: use html <template> tag instead of just clearing controls on last entry
            //clear controls
            console.log("clearing controls....");
            resetFields($("form"));
        }else{
            $(repeat).remove();
        }
        return false;
    });

    function resetFields(form) {
        $(':input', form).each(function() {
            var type = this.type;
            var tag = this.tagName.toLowerCase(); // normalize case
            // to reset the value attr of text inputs,
            // password inputs, fileUpload and textareas
            if (type == 'text' || type == 'password' || tag == 'textarea' || type=='file')
                this.value = "";
            // checkboxes and radios need to have their checked state cleared
            else if (type == 'checkbox' || type == 'radio')
                this.checked = false;
            // select elements need to have their 'selectedIndex' property set to -1
            // (this works for both single and multiple select elements)
            else if (tag == 'select')
                this.selectedIndex = 0;
        });
    }

    $( "form" ).on( "submit", function( event ) {
        event.preventDefault();

        var names = $("body *[name]");
        var result = "";
        names.each(function(i,val){
            console.log("value: ", val);

            var formValue = $(val).val();
            if(formValue.indexOf(formValue,'.')){

            } else{
                result += $(val).attr("name") + ":" + formValue + ","
            }

        });

        console.log("data: ", result);


    });

});
