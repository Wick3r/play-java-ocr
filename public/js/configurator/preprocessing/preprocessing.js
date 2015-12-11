/**
 * Created by Benedikt Linke on 08.12.2015.
 */

var caman = Caman('#canvas');

var rotation = 0;

$(function() {
    $('.slider').each(function() {
        var op = $(this).attr('id');

        $('#' + op).slider({
            min: $(this).data('min'),
            max: $(this).data('max'),
            val: $(this).data('val'),
            change: function(e, ui) {
                $('#v-'+op ).html(ui.value);
                $(this).data('val', ui.value);

                if(e.originalEvent === undefined) {
                    return;
                }

                applyFilters();
                caman.render();
            }
        });
    });

    $('#rotate-left').click(function() {
        rotateLeft();
    });

    $('#rotate-right').click(function() {
        rotateRight();
    });

    $('.preset').click(function() {
        resetFilters();
        var preset = $(this).data('preset');
        caman.revert(true);
        caman[preset]();
        caman.render();
    });

    $('#reset').click(function() {
        caman.reset();
        caman.render();
        resetFilters();
    });

    $('#save').click(function() {
        window.open(caman.toBase64());
    });

});
function applyFilters() {
    caman.revert(false);

    $('.slider').each(function() {
        var op = $(this).attr('id');
        var value = $(this).data('val');

        if (value === 0) {
            return;
        }

        caman[op](value);
    });
}

function resetFilters() {
    $('.slider').each(function() {
        var op = $(this).attr('id');

        $('#' + op).slider('option', 'value', $(this).attr('data-val'));
    });

    caman.reset();
    caman.render();
}

function rotateRight () {
    rotation += 90;
    caman.rotate(90);
    applyFilters();
    caman.render();
};

function rotateLeft () {
    rotation -= 90;
    caman.rotate(-90);
    applyFilters();
    caman.render();
};