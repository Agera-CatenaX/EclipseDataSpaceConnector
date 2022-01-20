#main.py

# Import the Flask module that has been installed.
from flask import Flask, jsonify
from flask import request
import requests

# Creating a new "app" by using the Flask constructor. Passes __name__ as a parameter.
app = Flask(__name__)

# Annotation that allows the function to be hit at the specific URL.
@app.route("/health", methods=["GET"])
def index():
    return "healthy"

@app.route('/negotiation', methods = ['POST'])
def new_user():
    contractOffer = request.get_json() #
    # add here the code to create the user
    print(contractOffer)
    response = requests.post('http://localhost:9191/api/negotiation?connectorAddress=http://localhost:8181/api/ids/multipart', json = contractOffer,
                             headers={"Content-Type": "application/json"})

    return jsonify(response)


if __name__ == '__main__':
    app.run()