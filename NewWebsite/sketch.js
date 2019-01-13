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

  input = createElement('textarea', 'Enter Leaves here');

  input.position(20, 325);

  inputForParenthetical = createElement('textarea', 'Enter Parenthetical Format here');
  inputForParenthetical.position(20,255);

  btnUseParen = createButton('Create Network');
  btnUseParen.position(180, 255);
  btnUseParen.mousePressed(onClickCreateNetwork);

  btnRemoveNodes = createButton('Remove Nodes');
  btnRemoveNodes.position(180, 325);
  btnRemoveNodes.mousePressed(removeNodesAction);

  greeting = createElement('h3', 'Enter Leaves To be kept');
  greeting.position(20, 279);

  greeting = createElement('h3', 'Enter Flag and Root');
  greeting.position(20, 420);

  textAlign(CENTER);
  textSize(50);

  inputForFlag = createInput();
  inputForFlag.position(20, 465);
  
  input2 = createInput();
  input2.position(200, 465);

  button = createButton('Change Root');
  button.position(input2.x + input.width, 465);
  button.mousePressed(changeRootAction);

  button = createButton('Download as PNG');
  button.position(500, 465);
  button.mousePressed(downloadImage);

  textAlign(CENTER);
  textSize(50);
}

function onClickCreateNetwork() {
  var parentheticalText = inputForParenthetical.value();
  // var tripRoot = input2.value();

  pushParentheticalFormatToServer(parentheticalText)

  // greeting.html('Flag='+parentheticalText);
}


function changeRootAction() {
  var flag = inputForFlag.value();
  var tripRoot = input2.value();

  pushChangeRootToServer(flag, tripRoot)
  // greeting.html('Flag=  '+ flag +' Root='+ tripRoot);
}

function removeNodesAction() {
  var name = input.value();
  greeting.html('hello '+name+'!');
  pushLeavesToServer(name)
  // input.value('');
}

function downloadImage(){
  var url = document.getElementsByClassName('thumb')[0].getAttribute('src');//response;//img.src.replace(/^data:image\/[^;]+/, 'data:application/octet-stream');
  url = document.getElementsByClassName('thumb')[0].src;//url.replace(/^data:image\/[^;]+/, 'data:application/octet-stream');
  url = url.replace(/^data:image\/[^;]+/, 'data:application/octet-stream');
  window.open(url, 'image.png');
//  window.location= "buf/image.png";
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
  request.open("POST", "http://127.0.0.1:5000/upload/");
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
        img = document.getElementsByClassName('thumb')[0]
        .setAttribute(
        'src', 'data:image/png;base64,'+response);
		

      }
      else if (request.readyState === 4){
        document.write("<p>Error : " + request.status + "," + request.statusText);
      }
    }
}

function pushLeavesToServer(leaves) {
  var data = JSON.stringify({
        "text": leaves
  });;

  var request = new XMLHttpRequest();
  request.open("POST", "http://127.0.0.1:5000/uploadLeaves/");
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


function pushChangeRootToServer(flag, tripRoot) {
  var data = JSON.stringify({
        "text": flag
  });;

  var request = new XMLHttpRequest();
  request.open("POST", "http://127.0.0.1:5000/changeRoot/"+tripRoot);
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

function pushParentheticalFormatToServer(parenthetical) {
  var data = JSON.stringify({
        "text": parenthetical
  });;

  var request = new XMLHttpRequest();
  request.open("POST", "http://127.0.0.1:5000/uploadParenthetical/");
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

