from Launcher import ServerAction
from flask import Flask, request, Response
from flask_cors import CORS, cross_origin

import base64

app = Flask(__name__)
# app = Flask(__name__)
cors = CORS(app, resources={r"/api/*": {"origins": "*"}})


# # POST
# @app.route('/upload/<flag>', methods=['POST'])
# def uploadTriplets(flag):
#     """
#     predicts requested text whether it is ham or spam
#     :return: json
#     """
#     json = request.get_json()
#     print(json)
#     if len(json['text']) == 0:
#         return 'error invalid input'
#
#     triplets = json['text']
#
#     print(triplets)
#     with open('retrievedTriplets.txt', 'w+') as newickField:
#         newickField.write(triplets)
#
#     ServerAction.tripletsToDot('retrievedTriplets.txt')
#     # ServerAction.convertDotToPNG('cExample1.dot')
#     print('flag=', flag)
#     if len(flag) > 0:
#         ServerAction.convertDotToPNGJulia('cExample1.dot', flag)
#     else:
#         ServerAction.convertDotToPNGJulia('cExample1.dot')
#
#     with open("net.png", "rb") as image_file:
#         encoded_string = base64.b64encode(image_file.read())
#     return encoded_string

# POST
@app.route('/uploadLeaves/', methods=['POST'])
@cross_origin()
def uploadLeavesToBeRemoved():
    """
    predicts requested text whether it is ham or spam
    :return: json
    """
    json = request.get_json()
    print(json)
    if len(json['text']) == 0:
        return 'error invalid input'

    leaves = json['text']

    print(leaves)

    # ServerAction.removeLeaves(leaves + '')
    # convertDotToPNG('cExample1.dot')
    ServerAction.removeNodes(leaves)

    # with open("net.png", "rb") as image_file:
    #     encoded_string = base64.b64encode(image_file.read())
    # print(encoded_string)
    return "working"


@app.route('/changeRoot/<root>', methods=['POST'])
def uploadRootToBeChanged(root):
    print('root= ', root)
    json = request.get_json()
    print(json)
    if len(json['text']) == 0:
        return 'error invalid input'

    flag = json['text']

    print(flag)

    ServerAction.changeRoot(flag, root)
    with open("net.png", "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read())
    print(encoded_string)
    return encoded_string


# POST
@app.route('/uploadParenthetical/<flag>', methods=['POST'])
def uploadParentheticalFormat(flag):
    json = request.get_json()
    print(json)
    if len(json['text']) == 0:
        return 'error invalid input'

    parentheticalFormat = json['text']

    print(parentheticalFormat)

    ServerAction.parentheticalFormatToPNG(parentheticalFormat, flag)

    with open("net.png", "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read())
    print(encoded_string)
    return encoded_string


@app.route('/getParenthetical/')
def getParenthetical():
    """
    this serves as a demo purpose
    :param user:
    :return: str
    """
    parenthetical = ServerAction.returnParentheticalFormat('cExample.dot')
    print(parenthetical)
    return "Hello parenthetical is here" + parenthetical


# POST
@app.route('/uploadHyde/<thresh>', methods=['POST'])
@cross_origin()
def uploadHyde(thresh):
    json = request.get_json()
    print(json)
    if len(json['text']) == 0:
        return 'error invalid input'

    hyde = json['text']

    print('Threshold =', thresh)

    print(hyde)
    with open('HydeInput.txt', 'w+') as f:
        f.write(hyde)

    ServerAction.parseHydeToTriplets("HydeInput.txt", float(thresh))
    # TODO: uncomment the following done
    ServerAction.tripletsToDot('HydeToTriplets.txt')
    # ServerAction.tripletsToDot('hydetotriplets.out')
    dotFile = ServerAction.returnReducedDotFile('cExample1.dot')

    with open('upload.dot', 'w+') as f:
        f.write(dotFile)

    print(dotFile)
    return "work in progress"


# POST
@app.route('/uploadNewick/', methods=['POST'])
@cross_origin()
def uploadParentheticalAndReturnDot():
    """
    predicts requested text whether it is ham or spam
    :return: json
    """
    json = request.get_json()
    print(json)
    if len(json['text']) == 0:
        return 'error invalid input'

    parenthetical = json['text']

    print(parenthetical)

    ServerAction.newickToDot(parenthetical)

    # ServerAction.convertDotToPNG('cExample1.dot')
    # print('flag=', flag)
    # if len(flag) > 0:
    #     ServerAction.convertDotToPNGJulia('cExample1.dot', flag)
    # else:
    #     ServerAction.convertDotToPNGJulia('cExample1.dot')
    return "work in progress"


# POST
@app.route('/upload/', methods=['POST'])
@cross_origin()
def uploadTripletsAndReturnDot():
    """
    predicts requested text whether it is ham or spam
    :return: json
    """
    json = request.get_json()
    print(json)
    if len(json['text']) == 0:
        return 'error invalid input'

    triplets = json['text']

    print(triplets)
    with open('retrievedTriplets.txt', 'w+') as f:
        f.write(triplets)

    ServerAction.tripletsToDot('retrievedTriplets.txt')
    dotFile = ServerAction.returnReducedDotFile('cExample1.dot')

    with open('upload.dot', 'w+') as f:
        f.write(dotFile)

    # ServerAction.convertDotToPNG('cExample1.dot')
    # print('flag=', flag)
    # if len(flag) > 0:
    #     ServerAction.convertDotToPNGJulia('cExample1.dot', flag)
    # else:
    #     ServerAction.convertDotToPNGJulia('cExample1.dot')
    print(dotFile)
    return "work in progress"


# GET
@app.route('/readDot')
@cross_origin()
def receiveDot():
    print('Sending Dot')
    # TODO change done
    # dotFile = returnReducedDotFile('cExample1.dot')

    # with open('upload.dot', 'w+') as newickField:
    #     newickField.write(dotFile)

    # with open("upload1.dot", "r") as newickField:
    with open("upload.dot", "r") as f:
        return Response(f.read(), mimetype='text/plain')

    # return "working"


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5001)
