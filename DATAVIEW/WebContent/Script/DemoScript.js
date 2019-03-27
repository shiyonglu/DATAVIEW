$(function () {

     $("#LoadDialogButton").off("click");
    $(document).on("click", "#LoadDialogButton", function () {

        var url = "http://localhost:8080/webbench/DialogContentPage.html";
        var divId = " #MainContentDiv";

        var q1 = "?inp1=" + $("#Input1").val();
        var q2 = "&inp2=" + $("#Input2").val();

        url = url + q1 + q2 + divId; //url in the form 'DialogContentPage.aspx?inp1=xx&inp2=yy #MainContentDiv'

        $('<div id=DialogDiv>').dialog("destroy");

        $('<div id=DialogDiv>').dialog({
            dialogClass: 'DynamicDialogStyle',
            modal: true,
            open: function () {
                $(this).load(url);
            },
            close: function (e) {
                $(this).empty();
                $(this).dialog('destroy');
            },
            height: 350,
            width: 540,
            title: 'Dynamic Dialog'

        });
    });


});    //end of main jQuery Ready method