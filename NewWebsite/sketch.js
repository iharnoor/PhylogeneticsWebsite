// A2Z F16
// Daniel Shiffman
// https://github.com/shiffman/A2Z-F16
// http://shiffman.net/a2z

function setup() {
  noCanvas();
  // createFileInput creates a button in the window
  // that can be used to select files
  // The first argument is the callback function
  // The 'multiple' flag allows more than one file to be selected
  var fileSelect = createFileInput(gotFile, 'multiple');

  input = createInput();
  input.position(20, 265);

  button = createButton('Remove Nodes');
  button.position(input.x + input.width, 265);
  button.mousePressed(greet);

  greeting = createElement('h2', 'what is your name?');
  greeting.position(20, 5);

  textAlign(CENTER);
  textSize(50);
}

// file is a p5.File object that has metadata, and the file's contents
function gotFile(file) {
  // Make a div to display info about the file
  var fileDiv = createDiv(file.name + ' ' + file.type + ' ' + file.subtype + ' ' + file.size + ' bytes');
  // Assign a CSS class for styling (see index.html)
  fileDiv.class('file');

  // Hanlde image and text differently
  if (file.type === 'image') {
    var img = createImg(file.data);
    img.class('thumb');
  } else if (file.type === 'text') {
    // Make a paragraph of text
    var par = createP(file.data);
    par.class('text');
    var texts = selectAll('.text');
    // var paraText=  document.getElementsByClassName('text').innerHTML;
    paraText = document.getElementsByClassName('text')[0].innerHTML;


    pushStringToServer(paraText); 
    // push the file to the Server
  }
}
    
function pushStringToServer(triplets) {
  var data = JSON.stringify({
        "text": triplets
  });;

  var request = new XMLHttpRequest();
  request.open("POST", "http://127.0.0.1:80/upload/");
  request.setRequestHeader("Content-Type", "application/json");
  request.addEventListener("readystatechange", processRequest, false);
  request.send(data);
    function processRequest(e){
    // document.write("This is Working <p>");
      if(request.readyState === 4 && request.status === 200){
        var response = request.responseText;
//        document.write(response);
        // Convert Base64 to Image
        var img = createImg();
        img.class('thumb');
        document.getElementsByClassName('thumb')[0]
        .setAttribute(
        'src', 'data:image/png;base64,'+response);

      }
      else if (request.readyState === 4){
        document.write("<p>Error : " + request.status + "," + request.statusText);
      }
    }
}


function greet() {
  var name = input.value();
  greeting.html('hello '+name+'!');
  pushLeavesToServer(name)
  // input.value('');

  // for (var i=0; i<200; i++) {
  //   push();
  //   fill(random(255), 255, 255);
  //   translate(random(width), random(height));
  //   rotate(random(2*PI));
  //   text(name, 0, 0);
  //   pop();
  // }
}

function pushLeavesToServer(leaves) {
  var data = JSON.stringify({
        "text": leaves
  });;

  var request = new XMLHttpRequest();
  request.open("POST", "http://127.0.0.1:80/uploadLeaves/");
  request.setRequestHeader("Content-Type", "application/json");
  request.addEventListener("readystatechange", processRequest, false);
  request.send(data);
    function processRequest(e){
    // document.write("This is Working <p>");
      if(request.readyState === 4 && request.status === 200){
        var response = request.responseText;
//        document.write(response);
        // Convert Base64 to Image
        var img = createImg();
        img.class('thumb');
        document.getElementsByClassName('thumb')[0]
        .setAttribute(
        'src', 'data:image/png;base64,'+response);

      }
      else if (request.readyState ==4){
        document.write("<p>Error : " + request.status + "," + request.statusText);
      }
    }
}
