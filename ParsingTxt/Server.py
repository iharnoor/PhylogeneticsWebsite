import ServerAction
from flask import Flask, request, jsonify

import base64

app = Flask(__name__)


# root
@app.route("/")
def index():
    """
    this is a root dir of my server
    :return: str
    """
    return "This is root!!!!"


# GET
@app.route('/users/<user>')
def hello_user(user):
    """
    this serves as a demo purpose
    :param user:
    :return: str
    """
    return "Hello %s!" % user


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


@app.route('/api/postData/<lang>', methods=['POST'])
def get_text_prediction(lang):
    """
    predicts requested text whether it is ham or spam
    :return: json
    """
    json = request.get_json()
    print(json)
    if len(json['text']) == 0:
        return 'error invalid input'

    image_binary = base64.b64decode(json['text'])
    with open('image.jpg', 'wb') as f:
        f.write(image_binary)

    dict = imageToText('image.jpg', lang)
    return dict
    # return json['text']


# # Flask
# from flask import request
# import json
# from froala_editor import File
# from froala_editor import FlaskAdapter
#
#
# @app.route('/upload_file', methods=['POST'])
# def upload_file():
#     try:
#         response = File.upload(FlaskAdapter(request), '/public/')
#     except Exception:
#         response = {'error': str(sys.exc_info()[1])}
#     return json.dumps(response)


if __name__ == '__main__':
    # app.run(host='0.0.0.0', port=5000)
    app.run(host='127.0.0.1', port=5000)

# reduce network needs a file with the nodes you want to keep
#
