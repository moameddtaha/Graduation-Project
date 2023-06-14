import requests

url = 'http://127.0.0.1:5000/sms'

message1 = "Free entry in 2 a wkly comp to win FA Cup final tkts 21st May 2005. Text FA to 87121 to receive entry question(std txt rate)T&C's apply 08452810075over18's"
message2 = "Ok lar... Joking wif u oni..."
msg = "Hell0 Ilddm http://ssdff.com sdfdfsdfsdf"
payload = {'message': message1}

try:
    response = requests.post(url, json=payload)
    if response.status_code == 200:
        print("Connection successful")
        print("Server message:", response.text)
    else:
        print("Connection failed")
except requests.exceptions.RequestException as e:
    print("Connection error:", e)

