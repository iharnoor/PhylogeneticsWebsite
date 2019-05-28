var nodeNum = 0;
var nodeVal = [];
var nodesCheckedStr = "";
var threshold;
var paraText = "";
var reg1000P = new RegExp("internal([0-9]{4})");
var regHash = new RegExp("Hash[A-Za-z]+");

var removeNodeBool = false;

// i hid textarea box
// var c = document.getElementById("textareabox");
// c.style.visibility = "hidden";

// var btnRefresh = document.getElementById("remove");
// btnRefresh.style.visibility = "hidden";

var btnUpdateNet = document.getElementById("updateNetwork");
// btnUpdateNet.style.visibility = "hidden";
btnUpdateNet.disabled = true;

var e = document.getElementById("textThreshold");
e.style.visibility = "hidden";

var newickField = document.getElementById("parenthetical");
newickField.style.visibility = "hidden";

var createNetworkSelector = document.getElementById("createNetwork");
// createNetworkSelector.style.visibility = "hidden";
createNetworkSelector.disabled = true;
var parentheticalSelector = document.getElementById("parenthetical");
parentheticalSelector.style.visibility = "hidden";

var selectbutton = document.getElementById("transfer_reason");

var refresher = document.getElementById("refresh");
// refresher.style.visibility="hidden";
refresher.disabled = true;
var globalhydeFileData = "";

function formdata() {
    var firstname1 = document.getElementById("rNodes").value;

    // document.writeln(firstname1 + "<br>");
    alert(firstname1);
}

// var lineD3 = d3.line();
// .curve(d3.curveCatmullRom.alpha(0.5));

var svg = d3.select("svg"),
    width = +svg.attr("width"),
    height = +svg.attr("height");

var colorD3 = d3.scaleOrdinal(d3.schemeCategory20);

var simulation = d3.forceSimulation()
    .force("link", d3.forceLink().distance(100).id(function (d) {
        return d.id;
    }))
    .force("charge", d3.forceManyBody().strength(-10))
    .force("collide", d3.forceCollide(50))
    .force("center", d3.forceCenter(width / 2, height / 2));


function createD3Graph(nodesRemoveArr, isRemoveSelected) {
    removeGraph();
    // d3.dot("cExample.dot", function (graph) {
    document.getElementById("loader").style.visibility = "hidden";
    // refresher.style.visibility = "visible";
    // selectbutton.style.visibility = "hidden";
    selectbutton.disabled = true;
    // b.style.visibility = "visible";
    refresher.style.visibility = "visible";
    refresher.disabled = false;

    // selectbutton.style.visibility="hidden";
    selectbutton.disabled = true;
    // b.style.visibility = "visible";
    // btnRemoveNodes.disabled= false;
    // createboxes();
    d3.dot("http://localhost:5001/readDot", function (graph) {
            // d3.dot("cExample.dot", function (graph) {

            // d3.select("svg").remove();

            //if (error) throw error;
            // build the arrow.
            svg.append("svg:defs").selectAll("marker")
                .data(["end"])      // Different link/path types can be defined here
                .enter().append("svg:marker")    // This section adds in the arrows
                .attr("id", String)
                .attr("viewBox", "10 10 10 10")
                .attr("refX", 15)
                .attr("refY", -0.5)
                .attr("markerWidth", 6)
                .attr("markerHeight", 6)
                .attr("orient", "auto")
                .append("svg:path")
                .attr("d", "M0,-5L10,0L0,5");

            if (isRemoveSelected) {
                for (let i = 0; i < graph.nodes.length; i++)
                    for (let j = 0; j < nodesRemoveArr.length; j++) {
                        if (graph.nodes[i].id === nodesRemoveArr[j]) {
                            graph.nodes.splice(i, 1); //remove 1 item at index i
                        }
                    }

                for (let i = 0; i < graph.links.length; i++) {
                    for (let j = 0; j < nodesRemoveArr.length; j++) {
                        if (graph.links[i].target === nodesRemoveArr[j]) {
                            graph.links.splice(i, 1); //remove 1 item at index i
                        }
                    }
                }
            }

            // add the links and the arrows
            var path = svg.append("svg:g").attr("class", "links").selectAll("path")
                .data(graph.links)
                .enter().append("svg:path")
                //    .attr("class", function(d) { return "link " + d.type; })
                .attr("marker-end", "url(#end)");
            // alert(Object.values(graph.nodes.name));
            // alert(graph.nodes);
            nodeNum = graph.nodes.length;


            var node = svg.append("g")
                .selectAll("circle")
                .data(graph.nodes)
                .enter().append("g");

            node
                .append("circle")
                // .attr("r", 5)
                .attr("r", function (d) {
                    var valueOfNode = d.id.toString();
                    if (!reg1000P.test(valueOfNode))
                    // if (valueOfNode < 1000)
                        nodeVal.push(valueOfNode);
                    // return (parseInt(d.id.toString()) <= 1000) ? 10 : 5;
                    return (!reg1000P.test(valueOfNode) || valueOfNode === "internal1000") ? 10 : 5;
                })

                .style("fill", function (d) {
                    if (removeNodeBool === false) {
                        createboxes();
                        removeNodeBool = true;
                    }
                    // return (parseInt(d.id.toString()) < 1000) ? "blue" : (parseInt(d.id.toString()) === 1000) ? "red" : "gray";
                    var valueOfNode = d.id.toString();
                    return (regHash.test(valueOfNode) ? "green" : (valueOfNode === "internal1000" ? "red" : reg1000P.test(valueOfNode) ? "gray" : "blue"));
                })
                //.attr("fill", function(d) { return color(d.group); })
                .call(d3.drag()
                    .on("start", dragstarted)
                    .on("drag", dragged)
                    .on("end", dragended));

            // alert(nodeVal);
            // add the text
            node.append("text")
                .attr("x", 12)
                .attr("dy", ".35em")
                .text(function (d) {
                    // return d.id;
                    var valueOfNode = d.id.toString();
                    // return (!reg1000P.test(valueOfNode)) ? d.id : "";
                    // return (parseInt(d.id.toString()) < 1000) ? d.id : "";
                    return d.id;
                });

            simulation
                .nodes(graph.nodes)
                .on("tick", ticked);

            simulation.force("link")
                .links(graph.links);

            let linkGen = d3.linkVertical().x(function (d) {
                return d.x;
            })
                .y(function (d) {
                    return d.y;
                });


            var linkRad = d3.linkRadial()
                .angle(function (d) {
                    return d.x;
                })
                .radius(function (d) {
                    return d.y;
                });


            function ticked() {
                path.attr("d", function (d) {
                    return "M" + d.source.x + "," + d.source.y + "L" + d.target.x + "," + d.target.y;
                });
                node
                    .attr("transform", function (d) {
                        return "translate(" + d.x + "," + d.y + ")";
                    });
            }
        }
    )
    ;

// document.elementFromPoint(x, y).click();///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

function dragstarted(d) {
    if (!d3.event.active) simulation.alphaTarget(0.3).restart();
    d.fx = d.x;
    d.fy = d.y;
}

function dragged(d) {
    d.fx = d3.event.x;
    d.fy = d3.event.y;
}

function dragended(d) {
    if (!d3.event.active) simulation.alphaTarget(0);
    d.fx = null;
    d.fy = null;
}

//End of D3 code

var bool = 0;
var selectedDropDown = "Triplets";

function setup() {
    noCanvas();
    // createFileInput creates a button in the window
    // that can be used to select files
    // The first argument is the callback function
    // The 'multiple' flag allows more than one file to be selected
    inp = createFileInput(gotFile, 'multiple');
    inp.position(400, 100);
    inp.addClass("chooseButton");
    inp.attribute("id", "file");

    // inpputForHyde = createFileInput(gotHydeFile, 'multiple');

    textAlign(CENTER);
    textSize(50);
}

function selectInputType(val) {
    selectedDropDown = val;

    // location.reload();
    if (val === 'HYDE format') {
        removeGraph();
        // document.getElementById("parenthetical").style.visibility ="hidden";
        // location.reload(true);
        // d.style.visibility = "visible";

        createNetworkSelector.style.visibility = "visible";
        createNetworkSelector.disabled = false;
        e.style.visibility = "visible";
        newickField.style.visibility = "hidden";
        let x = document.getElementById("file");
        if (x.style.display === "none") {
            x.style.display = "block";
        } else {
            x.style.visibility = "visible";
        }
    } else if (val === 'Triplets') {
        removeGraph();
        // document.getElementById("parenthetical").style.visibility ="hidden";
        // location.reload(true);
        // var e = document.getElementById("textThreshold");
        newickField.style.visibility = "hidden";
        e.style.visibility = "hidden";
        let x = document.getElementById("file");
        // createNetworkSelector.style.visibility = "visible";
        createNetworkSelector.disabled = false;
        if (x.style.display === "none") {
            x.style.display = "block";
        } else {
            x.style.visibility = "visible";
        }
    } else if (val === 'pf') {
        removeGraph();
        e.style.visibility = "hidden";
        // location.reload(true);
        newickField.style.visibility = "visible";
        parentheticalSelector.style.visibility = "visible";
        createNetworkSelector.style.visibility = "visible";
        let x = document.getElementById("file");
        x.style.visibility = "visible";
        // createNetworkSelector.style.visibility = "visible";
        createNetworkSelector.disabled = false;
    }
}

function simulateClick(x, y) {
    jQuery(document.elementFromPoint(315, 205)).click();
    alert("clicking" + x + " " + y);
}

function createboxes() {

    // b.style.visibility= "hidden";
    // btnRemoveNodes.disabled= true;
    createNetworkSelector.disabled = true;
    // createNetworkSelector.style.visibility = "hidden";
    // c.style.visibility = "visible";
    // btnUpdateNet.style.visibility = "visible";


    btnUpdateNet.disabled = false;
    nodeVal.forEach(function (element) {
        linebreak = document.createElement("hr");
        var x = document.createElement("INPUT");
        x.checked = true;
        x.setAttribute("type", "checkbox");
        x.setAttribute("id", element);
        x.onclick = checking;
        var y = document.createElement("p");
        var text = document.createTextNode("" + element);
        y.appendChild(text);
        document.getElementById("placeholder").appendChild(x);
        document.getElementById("placeholder").appendChild(y);
        document.getElementById("placeholder").appendChild(linebreak);
        // document.body.appendChild(y);
        console.log(x.id);
    });
}


function checking() {
    nodesCheckedStr += this.id.toString() + ",";
    // alert(nodesCheckedStr);
}

function onClickCreateNetwork() {
    createNetworkSelector.disabled = true;
    document.getElementById("loader").style.visibility = "visible";
    if (selectedDropDown === "pf") {
        var parentheticalText = document.getElementById("parenthetical").value;
        if (parentheticalText === "") {
            paraText = document.getElementsByClassName('text')[0].innerHTML;
            pushParentheticalToServer(paraText);
        } else {
            pushParentheticalToServer(parentheticalText);
        }
    } else if (selectedDropDown === "HYDE format") {
        threshold = document.getElementById("textThreshold").value;
        pushHydeToServer(globalhydeFileData, threshold);
    } else if (selectedDropDown == "Triplets") {
        paraText = document.getElementsByClassName('text')[0].innerHTML;
        pushStringToServer(paraText);
        // document.getElementById("graph").click();
        // alert("hello");
        document.getElementById("graph").dispatchEvent(new MouseEvent("click"));
        // var img = document.getElementById("graph");
        // var rect = img.getBoundingClientRect();
        // document.elementFromPoint(rect.left, rect.top).click();
    }
    createboxes();
    // simulateClick();
    // simulateClick(309, 203);
    // var coordinates = document.getElementById("graph").getBoundingClientRect();
    // simulateClick(coordinates.left, coordinates.top);
}


function changeRootAction() {
    var flag = inputForFlag.value();
    var tripRoot = input2.value();

    pushChangeRootToServer(flag, tripRoot)
    // greeting.html('Flag=  '+ flag +' Root='+ tripRoot);
}

function removeNodesAction() {
    // var leaves = document.getElementById("textareabox").value;
    nodesCheckedStr = nodesCheckedStr.substr(0, nodesCheckedStr.length - 1);

    console.log(nodesCheckedStr);

    var strArr = nodesCheckedStr.split(',');

    createD3Graph(strArr, true)
    // pushLeavesToServer(nodesCheckedStr)
}

function removeGraph() {
    $(".hi").empty();
}

function refreshGraph() {
    // svg.selectAll("svg").remove();
    createD3Graph();

}

// function downloadImage() {
//     var url = document.getElementsByClassName('thumb')[0].getAttribute('src');//response;//img.src.replace(/^data:image\/[^;]+/, 'data:application/octet-stream');
//     url = document.getElementsByClassName('thumb')[0].src;//url.replace(/^data:image\/[^;]+/, 'data:application/octet-stream');
//     url = url.replace(/^data:image\/[^;]+/, 'data:application/octet-stream');
//     window.open(url, 'image.png');
// //  window.location= "buf/image.png";
// }

// file is a p5.File object that has metadata, and the file's contents
function gotFile(file) {
    // document.getElementById("loader").style.visibility = "visible";
    // refresher.style.visibility = "visible";
    refresher.disabled = false;
    // selectbutton.style.visibility = "hidden";
    selectbutton.disabled = true;

    // refresher.style.visibility="visible";
    refresher.disabled = false;
    // selectbutton.style.visibility="hidden";
    // Make a div to display info about the file
    var fileDiv = createDiv(file.name + ' ' + file.type + ' ' + file.subtype + ' ' + file.size + ' bytes');
    fileDiv.style.visibility = "hidden";
    // fileDiv.addClass("boxText1");

    // Assign a CSS class for styling (see index.html)
    fileDiv.class('files');
    var hideinfo = document.getElementsByClassName("files");

    // Hanlde image and text differently
    if (file.type === 'image') {
        alert('image not accepted')
    } else if (file.type === 'text') {

        if (selectedDropDown === "HYDE format") {//HYDE Selected
            var par = createP(file.data);
            par.class('text');
            var texts = selectAll('.text');
            // var paraText=  document.getElementsByClassName('text').innerHTML;
            paraText = document.getElementsByClassName('text')[0].innerHTML;

            globalhydeFileData = paraText;

            // if (threshold == "") {
            //     alert("Enter Threshold First")
            // } else {
            //     pushHydeToServer(paraText, threshold);
            // }
        } else if (selectedDropDown == 'Triplets') {
            // Make a paragraph of text

            var par = createP(file.data);
            par.class('text');
            var texts = selectAll('.text');
            var paraText = document.getElementsByClassName('text').innerHTML;
            // paraText = document.getElementsByClassName('text')[0].innerHTML;

            // pushStringToServer(paraText);
        } else if (selectedDropDown == 'pf') {
            // Make a paragraph of text

            var par = createP(file.data);
            par.class('text');
            var texts = selectAll('.text');
            var paraText = document.getElementsByClassName('text').innerHTML;
            // paraText = document.getElementsByClassName('text')[0].innerHTML;

            // pushStringToServer(paraText);
        }
    }
    var boxnums = document.getElementsByClassName("boxText1");
    // boxnums.style.visibility ="hidden";
}

function pushHydeToServer(hydeInput, thresh) {
    var data = JSON.stringify({
        "text": hydeInput
    });

    var request = new XMLHttpRequest();
    request.open("POST", "http://127.0.0.1:5001/uploadHyde/" + thresh);
    request.setRequestHeader("Content-Type", "application/json");
    request.addEventListener("readystatechange", processRequest, false);
    request.send(data);

    function processRequest(e) {
        // document.write("This is Working <p>");
        if (request.readyState === 4 && request.status === 200) {
            createD3Graph();

        } else if (request.readyState === 4) {
            // document.write("<p>Error : " + request.status + "," + request.statusText);
        }
    }
}

function pushParentheticalToServer(parenthetical) {
    var data = JSON.stringify({
        "text": parenthetical
    });

    var request = new XMLHttpRequest();
    request.open("POST", "http://127.0.0.1:5001/uploadNewick/");
    request.setRequestHeader("Content-Type", "application/json");
    request.addEventListener("readystatechange", processRequest, false);
    request.send(data);

    function processRequest(e) {
        // document.write("This is Working <p>");
        if (request.readyState === 4 && request.status === 200) {
//         var response = request.responseText;
// //        document.write(response);
//         // Convert Base64 to Image
//         var img = createImg();
//         img.class('thumb');
//         img = document.getElementsByClassName('thumb')[0]
//         .setAttribute(
//         'src', 'data:image/png;base64,'+response);
            createD3Graph();

        } else if (request.readyState === 4) {
            // document.write("<p>Error : " + request.status + "," + request.statusText);
        }

    }
}


function pushStringToServer(triplets) {
    var data = JSON.stringify({
        "text": triplets
    });

    var request = new XMLHttpRequest();
    request.open("POST", "http://127.0.0.1:5001/upload/");
    request.setRequestHeader("Content-Type", "application/json");
    request.addEventListener("readystatechange", processRequest, false);
    request.send(data);

    function processRequest(e) {
        // document.write("This is Working <p>");
        if (request.readyState === 4 && request.status === 200) {
//         var response = request.responseText;
// //        document.write(response);
//         // Convert Base64 to Image
//         var img = createImg();
//         img.class('thumb');
//         img = document.getElementsByClassName('thumb')[0]
//         .setAttribute(
//         'src', 'data:image/png;base64,'+response);
            createD3Graph();

        } else if (request.readyState === 4) {
            // document.write("<p>Error : " + request.status + "," + request.statusText);
        }

    }
}

function pushLeavesToServer(leaves) {
    var data = JSON.stringify({
        "text": leaves
    });

    var request = new XMLHttpRequest();
    request.open("POST", "http://127.0.0.1:5001/uploadLeaves/");
    request.setRequestHeader("Content-Type", "application/json");
    request.addEventListener("readystatechange", processRequest, false);
    request.send(data);

    function processRequest(e) {
        // document.write("This is Working <p>");
        if (request.readyState === 4 && request.status === 200) {
//             var response = request.responseText;
// //        document.write(response);
//             // Convert Base64 to Image
//             var img = createImg();
//             img.class('thumb');
//             document.getElementsByClassName('thumb')[0]
//                 .setAttribute(
//                     'src', 'data:image/png;base64,' + response);
            createD3Graph();

        } else if (request.readyState == 4) {
            // document.write("<p>Error : " + request.status + "," + request.statusText);
        }
    }
}

function pushChangeRootToServer(flag, tripRoot) {
    var data = JSON.stringify({
        "text": flag
    });
    ;

    var request = new XMLHttpRequest();
    request.open("POST", "http://127.0.0.1:5000/changeRoot/" + tripRoot);
    request.setRequestHeader("Content-Type", "application/json");
    request.addEventListener("readystatechange", processRequest, false);
    request.send(data);

    function processRequest(e) {
        // document.write("This is Working <p>");
        if (request.readyState === 4 && request.status === 200) {
            var response = request.responseText;
//        document.write(response);
            // Convert Base64 to Image
            var img = createImg();
            img.class('thumb');
            document.getElementsByClassName('thumb')[0]
                .setAttribute(
                    'src', 'data:image/png;base64,' + response);

        } else if (request.readyState === 4) {
            // document.write("<p>Error : " + request.status + "," + request.statusText);
        }
    }
}