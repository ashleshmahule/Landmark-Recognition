import numpy as np
from keras.preprocessing import image
import tensorflow as tf
from PIL import ImageFile
from keras.models import load_model


ImageFile.LOAD_TRUNCATED_IMAGES = True

tf.config.experimental.list_physical_devices('GPU')
print("Num GPUs Available: ", len(tf.config.experimental.list_physical_devices('GPU')))
physical_devices = tf.config.experimental.list_physical_devices('GPU')
if len(physical_devices) > 0:
    tf.config.experimental.set_memory_growth(physical_devices[0], True)

label_index = {0: 'Achyutaraya Temple', 1: 'Aghorashewara Temple', 2: 'Baharampur', 3: 'Bahubali Gomateshwara Temple',
               4: 'Chandikeshwara Temple',
               5: 'Channakeshava Temple', 6: 'Chennakeshava Temple',7: 'Chennakeshava Temple', 8: 'Chitradurga Fort', 9: 'Hazara Rama Temple',
               10: 'Hoysaleswara Temple',
               11: 'Jor Bangla Temple', 12: "King's Palace", 13: 'Krishna Temple', 14: 'Lakshmi Devi Temple',
               15: 'Lakshmi Narsimha Temple',
               16: 'Lalji Temple', 17: 'Madan Mohan Temple', 18: 'Mahanavmi Dibba', 19: 'Rasmancha',
               20: 'Sasivekalu Ganesha Temple',
               21: 'Shivappa Nayaka Palace', 22: 'Shayamarai Temple', 23: 'Surabheshwara Temple',
               24: 'Uma Maheshwara Temple',
               25: 'Viroopaksha Temple', 26: 'Vishnu Temple', 27: 'Vittala Temple'}


path = 'E:/6th SEM/Lab/ST Lab/Landmark-Recognition/Python Files/DataSet/533.jpg'

img = image.load_img(path, target_size=(150, 150))

x = image.img_to_array(img)
x = np.expand_dims(x, axis=0)
images = np.vstack([x])
print(images)
print(images.shape)
print(type(images))
print(type(images[0][0][0][0]))

model = tf.keras.models.load_model(filepath="savedModel.h5")

classes = model.predict(images, batch_size=5)

# predicting images
img = image.load_img(path, target_size=(150, 150))
x = image.img_to_array(img)
x = np.expand_dims(x, axis=0)

images = np.vstack([x])
classes = model.predict(images, batch_size=5)
print(classes)
print(type(list(classes[0])[0]))
classes=list(classes[0])

print(label_index[classes.index(1.0)])



# converting model to tflite

# model=tf.keras.models.load_model("savedModel.h5")
# converter = tf.lite.TFLiteConverter.from_keras_model(model)
# #converter.experimental_new_converter = True
# tflite_model = converter.convert()
# open("converted_model.tflite", "wb").write(tflite_model)  

# interpreter = tf.lite.Interpreter(model_path="converted_model.tflite")
# interpreter.allocate_tensors()
# input_details = interpreter.get_input_details()
# output_details = interpreter.get_output_details()

# input_shape = input_details[0]['shape']
# input_data = images
# interpreter.set_tensor(input_details[0]['index'], input_data)

# interpreter.invoke()

# output_data = interpreter.get_tensor(output_details[0]['index'])
# output_data=list(output_data[0])
# print(label_index[output_data.index(1.0)])
