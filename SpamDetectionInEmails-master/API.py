import json
import re
import pickle
from flask import Flask, request, jsonify
import string
import numpy as np
from tensorflow.keras.models import load_model
from keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences

## some config values 
embed_size = 100 # how big is each word vector
max_feature = 50000 # how many unique words to use (i.e num rows in embedding vector)
max_len = 2000 # max number of words in a question to use

# load the tokenizer from a file
with open('tokenizer.pickle', 'rb') as handle:
    tokenizer = pickle.load(handle)

loaded_model = load_model('./spam_model.h5')

def remove_hyperlink(word):
    return  re.sub(r"http\S+", "", word)

def to_lower(word):
    result = word.lower()
    return result

def remove_number(word):
    result = re.sub(r'\d+', '', word)
    return result

def remove_punctuation(word):
    result = word.translate(str.maketrans(dict.fromkeys(string.punctuation)))
    return result

def remove_whitespace(word):
    result = word.strip()
    return result

def replace_newline(word):
    return word.replace('\n','')

def clean_up_pipeline(sentence):
    cleaning_utils = [remove_hyperlink,
                      replace_newline,
                      to_lower,
                      remove_number,
                      remove_punctuation,remove_whitespace]
    for o in cleaning_utils:
        sentence = o(sentence)
    return sentence

def preprocess_test_data(sentence):
    sentence_cleaned = clean_up_pipeline(sentence)
    sentence_sequence = np.array(tokenizer.texts_to_sequences([sentence_cleaned]))
    sentence_padded = pad_sequences(sentence_sequence, maxlen=max_len)
    return sentence_padded

def paragraph_prediction(sentence):
    preprocessed_sentence = preprocess_test_data(sentence)
    prediction = loaded_model.predict(preprocessed_sentence)
    print(prediction)
    if prediction > 0.5:
        return "spam"
    else:
        return "not spam"
    
app = Flask(__name__)

# Define the API endpoint for receiving SMS messages
@app.route('/sms', methods=['POST'])
def detect_spam():
    # Get the SMS message from the request
    message = request.json['message']
    message_str = json.dumps(message)
    # Make a prediction using the machine learning model
    prediction = paragraph_prediction(message_str)

    # Return the prediction as a JSON response
    response = {'is_spam': prediction}
    return jsonify(response)

if __name__ == '__main__':
    app.run()
