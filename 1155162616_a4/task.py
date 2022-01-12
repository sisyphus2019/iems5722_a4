from celery import Celery
from flask import Flask
import requests

def make_celery(app):
    celery = Celery(app.import_name, broker=app.config['CELERY_BROKER_URL'])
    celery.conf.update(app.config)
    TaskBase = celery.Task

    class ContextTask(TaskBase):
        abstract = True

        def __call__(self, *args, **kwargs):
                return TaskBase.__call__(self, *args, **kwargs)
    celery.Task = ContextTask
    return celery

app = Flask(__name__)
app.config.update(
    CELERY_BROKER_URL='amqp://guest@localhost'
)
celery = make_celery(app)

@celery.task()
def pushMessage(chatroom_name,chatroom_id,name,msg,token):
    api_key = "AAAA5bhrZCs:APA91bFxdos5e6xm9bC-X3GPvE_LixUCvericpzEfgWgyLgIb8GW_xUQK-JRybzWkbDsGp-H6EjXLGsNFu02J9d8LD59MVRGw-ZyvQ_nOYGNt-cRPWNoAXMearmJNaXG2yiVNi5RRIcB"
    url = "https://fcm.googleapis.com/fcm/send"
    
    headers = {
        "Content-Type" : "application/json",
        "Authorization": "key="+api_key,
    }

    device_token = token

    payload = {
        "to":device_token,
        "notification":{
            "title" : chatroom_name,
            "tag" : chatroom_id,
            "body" : name+": "+msg
        }
    }

    result = requests.post(url,headers=headers,json=payload)

    if(result.status_code==200):
        print("Request is sent to FCM server successfully!")

