import ServerAction
from flask import Flask, request, jsonify

import base64

app = Flask(__name__)


# POST
@app.route('/upload/', methods=['POST'])
def uploadTriplets():
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
    # ServerAction.convertDotToPNG('cExample1.dot')
    ServerAction.convertDotToPNGJulia('cExample1.dot')

    with open("net.png", "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read())
    return encoded_string


# POST
@app.route('/uploadLeaves/', methods=['POST'])
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
    ServerAction.removeLeaves(leaves)

    with open("net.png", "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read())
    print(encoded_string)
    return encoded_string


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


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5000)
