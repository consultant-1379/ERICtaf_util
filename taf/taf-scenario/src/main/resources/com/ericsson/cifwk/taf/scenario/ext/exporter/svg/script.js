function showTooltip(id) {
    var tooltips = document.getElementsByClassName('tooltip');
    for (var i = 0; i < tooltips.length; i++) {
        tooltips[i].style.visibility = 'hidden';
    }
    show('tooltip'+id);
}

function show(id) {
    document.getElementById(id).style.visibility = 'visible';
    document.getElementById(id).style.resize = 'both';
}

function hide(id) {
    document.getElementById(id).style.visibility = 'hidden';
    document.getElementById(id).style.resize = 'initial';
}

