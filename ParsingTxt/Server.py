import os
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
def uploadText():
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
    ServerAction.convertDotToPNG('cExample1.dot')

    with open("cExample1.png", "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read())

    return encoded_string


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
    app.run(host='127.0.0.1', port=80)
