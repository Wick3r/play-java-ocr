/**
 * Created by Sebastian Lauterkorn on 15.12.2015.
 */
var areas;
var currId;
var currArea;

function setImageSource(data, callback, job){
    var image = $('#canvas');
    image.remove();

    $("#image-area").html('<img id="canvas" src="" style="width:100%;" />');

    $('#canvas').attr("src", data);

    preProcessing.setCaman(Caman('#canvas', function () {
        this.render();

        callback(job);
    }));

    /*
     $('#canvas').attr("src", data);

     preProcessing.caman.reset();
     */

    /*
     var ctx = $('#canvas')[0].getContext("2d");
     var img = new Image();
     img.src = data;
     img.onload = function() {
     ctx.drawImage(img, 0, 0);
     };*/
    /*
     $.get(data,
     function(rs){
     var ctx = $('#canvas').getContext("2d");
     ctx.drawImage(rs, 0, 0);
     });*/
}



function getValuesOfSelectedArea(areas){
    this.areas = areas;
}


function getValuesForInput(area){

    if(area != null)
    {
        $("#editMetaDataType").prop('disabled', true);

        var xHeight = parseInt(area.height);
        var yWidth = parseInt(area.width);
        var type = area.type;

        $('#editHeigth').val(xHeight);
        $('#editWidth').val(yWidth);

        if(type == "meta" )
        {
            $("#editMetaDataType").prop('disabled', false);
        }
    }

}

function reset(){
    document.getElementById('templating').contentWindow.resetAreas();
}

function getAreas(){
    //return document.getElementById('templating').contentWindow.getValues();
    return this.areas;
}

function createArea(options){
    document.getElementById('templating').contentWindow.createArea(options);
}

$('#deleteAreas').click(function(){
    reset();
})

$('#metadata').click(function(){
    document.getElementById('templating').contentWindow.createNewArea('meta');
});
$('#img').click(function(){
    document.getElementById('templating').contentWindow.createNewArea('img');
});
$('#text').click(function(){
    document.getElementById('templating').contentWindow.createNewArea('text');
});

// Load preprocessed Image in second step
$('#next').click(function(){
    console.log("data-step: " + $(this).attr('data-step'));
    if($(this).attr('data-step') != "complete") {
        console.log("next method");
        var image = preProcessing.saveImage();
        var height = preProcessing.getCanvasHeight();
        var width = preProcessing.getCanvasWidth();
        var iframe = document.getElementById('templating');
        iframe.height = height;
        document.getElementById('templating').contentWindow.loadImageForSecondStep(image, height, width);
    }
});



$(':button').click(function (){
    if($(this).attr('data-step') == "complete"){
        saveData();
    }
});

// Callback Function for multistepmodal
$('#modal-sample-1').modalSteps({
    callbacks: {
        '1': function(){
        },
        '2': function(){

            console.log("Vor der Abfrage im Callback");
            var temp = $('#templateName').val();
            var string;
            if(!temp)
            {
                console.log("temp = null: " + temp);
                $('#next').attr("disabled", true);
            }
            else
            {
                console.log("temp != null: " + temp);
                $('#next').attr("disabled", false);
            }

        }
    }
});

/*$('#templateName').change(function() {

    var name =  $( this ).val();
    console.log("Dein Metatyp: " + name);
    if(name != null)
    {
        $('#next').attr("disabled", false);
    }
    else
    {
        $('#next').attr("disabled", false);
    }


});*/


// If Input is empty. User can't complete the Modal
$('#templateName').change(function(){

    var temp = $(this).val();
    if(!temp)
    {
        console.log("ONE Change Function == null: " + temp);
        //Setting the complete button not klickable
        $('#next').attr("disabled", true);

    }

})
;

//Get metaDataType for some stuff !--TODO
$('#editMetaDataType').change(function() {

    var metatype =  $( this ).val();
    console.log("Dein Metatyp: " + metatype);


});

//If user types one char the complete button is klickable
$('#templateName').keypress(function(){
    var name =  $( this ).val();
   // console.log("String Length: " + name.length);

    if(name.length >= 0)
    {
        $('#next').attr("disabled", false);
    }


});


// Unused but works
/*$('#editHeigth').keypress(validateNumber);
//$('#editWidth').keypress(validateNumber);



function validateNumber(event) {
    var $id = $(event.target).next("span");
    //if the letter is not digit then display error and don't type anything
    if (event.which != 8 && event.which != 0 && (event.which < 48 || event.which > 57)) {
        //display error message
        $id.html("Bitte nur Zahlen eingeben").show().fadeOut("slow");
        return false;
    }

}*/

// Does not work yet maby later
/*$('#editHeigth').change(function() {

    var height =  $( this ).val();
    console.log("ich change");
    currArea =  document.getElementById('templating').contentWindow.setSizeHeight(currId, height);
    currId = 0;

});*/


// With the element initially shown, we can hide it slowly:
/*
$( "#metadata" ).click(function() {
    $( "#meta" ).hide( "slow", function() {
        alert( "Animation complete." );
    });
});
*/

