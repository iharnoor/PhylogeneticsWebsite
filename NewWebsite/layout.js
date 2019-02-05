document.ready(function unhide(){
    var x = document.getElementById("fileType");
    print("hello");
    if(x.value == "trip"){
        x = document.getElementById("chooser");
        x.setAttribute("id", "visible");
    } 
})