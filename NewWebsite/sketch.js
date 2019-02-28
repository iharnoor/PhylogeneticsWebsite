var nodeNum= 0;
function formdata()
{
    var firstname1= document.getElementById("rNodes").value;

    document.writeln(firstname1 + "<br>");
    alert(firstname1);
}
var lineD3 = d3.line()
    .curve(d3.curveCatmullRom.alpha(0.5));

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


function createD3Graph() {
    // d3.dot("cExample.dot", function (graph) {
    d3.dot("http://localhost:5001/readDot", function (graph) {
        // d3.dot("cExample.dot", function (graph) {
        //if (error) throw error;

        // build the arrow.
        svg.append("svg:defs").selectAll("marker")
            .data(["end"])      // Different link/path types can be defined here
            .enter().append("svg:marker")    // This section adds in the arrows
            .attr("id", String)
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", 15)
            .attr("refY", -0.5)
            .attr("markerWidth", 6)
            .attr("markerHeight", 6)
            .attr("orient", "auto")
            .append("svg:path")
            .attr("d", "M0,-5L10,0L0,5");

        // add the links and the arrows
        var path = svg.append("svg:g").attr("class", "links").selectAll("path")
            .data(graph.links)
            .enter().append("svg:path")
            //    .attr("class", function(d) { return "link " + d.type; })
            .attr("marker-end", "url(#end)");
        nodeNum= graph.nodes.length;
        var node = svg.append("g")
            .selectAll("circle")
            .data(graph.nodes)
            .enter().append("g");

        node
            .append("circle")
            // .attr("r", 5)
            .attr("r", function (d) {
                return (parseInt(d.id.toString()) <= 1000) ? 10 : 5;
            })
            .style("fill", function (d) {
                return (parseInt(d.id.toString()) < 1000) ? "red" : "green";
            })
            //.attr("fill", function(d) { return color(d.group); })
            .call(d3.drag()
                .on("start", dragstarted)
                .on("drag", dragged)
                .on("end", dragended));

        // add the text
        node.append("text")
            .attr("x", 12)
            .attr("dy", ".35em")
            .text(function (d) {
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
                var dx = d.target.x - d.source.x,
                    dy = d.target.y - d.source.y,
                    dr = Math.sqrt(dx * dx + dy * dy);
                return "M" +
                    d.source.x + "," +
                    d.source.y + "A" +
                    dr + "," + dr + " 0 0,1 " +
                    d.target.x + "," +
                    d.target.y;
            });
            node
                .attr("transform", function (d) {
                    return "translate(" + d.x + "," + d.y + ")";
                });
        }
    });
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



var bool= 0;
function setup() {
    noCanvas();
    // createFileInput creates a button in the window
    // that can be used to select files
    // The first argument is the callback function
    // The 'multiple' flag allows more than one file to be selected
    inp = createFileInput(gotFile, 'multiple');
    inp.position(400, 100);
    inp.addClass("chooseButton");


    // inpputForHyde = createFileInput(gotHydeFile, 'multiple');

    textAlign(CENTER);
    textSize(50);
}

function selectInputType(val){
    if (val==='HYDE format'){
        bool= 1;
    }
}

function createboxes() {
    // alert("Hello");
    // print(nodeNum);
    // let loc = 400;
    for (let i = 0; i < nodeNum; i++) {
        // alert("Here in the loop");
        var x = document.createElement("INPUT");
        x.setAttribute("type", "checkbox");
        // x.position(loc, 400);
        // loc+= 10;
        document.body.appendChild(x);
    }
}



function onClickCreateNetwork() {
    var parentheticalText = inputForParenthetical.value();
    // var tripRoot = input2.value();

    // pushChangeRootToServer(flag, tripRoot)
    greeting.html('Flag=' + parentheticalText);
}


function changeRootAction() {
    var flag = inputForFlag.value();
    var tripRoot = input2.value();

    pushChangeRootToServer(flag, tripRoot)
    // greeting.html('Flag=  '+ flag +' Root='+ tripRoot);
}

function removeNodesAction() {
    var name = input.value();
    greeting.html('hello ' + name + '!');
    pushLeavesToServer(name)
    // input.value('');
}

function ajaxTest() {
    jQuery.support.cors = true;
    $.ajax({
        type: "GET",
        url: "http://localhost:5001/readDot",
        // success: function (data) {
        //     $("#test").html(data);
        //     alert(data);
        // }
        // data:{q:idiom},
        async: true,
        dataType: "text",
        crossDomain: true,
        success: function (data) {
            alert(data);
            // alert(xhr.getResponseHeader('Location'));
        },
        error: function (jqXHR, textStatus, ex) {
            alert(textStatus + "," + ex + "," + jqXHR.responseText);
        }
    });
}

function downloadImage() {
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
        // pushTripletsToServer(paraText);
        // push the file to the Server
    }
}


// function pushTripletsToServer(triplets) {
//     var data = JSON.stringify({
//         "text": triplets
//     });
//
//     jQuery.support.cors = true;
//     $.ajax({
//         type: "POST",
//         url: "http://localhost:5000/upload/",
//         data: {json: JSON.stringify({text: triplets})},
//         // async: true,
//         contentType: "application/json; charset=utf-8",
//         dataType: "json",
//         crossDomain: true,
//         success: function (data) {
//             alert(data);
//         },
//         failure: function (errMsg) {
//             alert(errMsg);
//         }
//     });

    // var request = new XMLHttpRequest();
    // request.open("POST", "http://127.0.0.1:5000/upload/");
    // request.setRequestHeader("Content-Type", "application/json");
    // request.addEventListener("readystatechange", processRequest, false);
    // request.send(data);
    //
    // function processRequest(e) {
    //     // document.write("This is Working <p>");
    //     if (request.readyState === 4 && request.status === 200) {
    //         createD3Graph();
    //
    // }



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
            document.write("<p>Error : " + request.status + "," + request.statusText);
        }
    }
}

function pushLeavesToServer(leaves) {
    var data = JSON.stringify({
        "text": leaves
    });
    ;

    var request = new XMLHttpRequest();
    request.open("POST", "http://127.0.0.1:5000/uploadLeaves/");
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

        } else if (request.readyState == 4) {
            document.write("<p>Error : " + request.status + "," + request.statusText);
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
            document.write("<p>Error : " + request.status + "," + request.statusText);
        }
    }
}