from tensorflow.keras.models import load_model
from tensorflow import keras
loaded_model = keras.models.load_model('./spam_model.h5')

dir_output = dir(loaded_model)


if 'predict' in dir_output:
    print("found")
else:
    print("not found")

print(loaded_model.predict("Hello"))