#coding=utf-8
from flask import Flask,request,make_response
from task import pushMessage
import mysql.connector
import math
import json

app = Flask(__name__)

db = mysql.connector.connect(
    host = "localhost",
    user = "dbuser",
    passwd = "password",
    database = "iems5722",
    autocommit = True
)

@app.route('/hello/')
def welcome():
    return "liuruchen"

@app.route('/api/a3/get_chatrooms/')
def getChatrooms():
    try:
        #cursor 光标
        cursor = db.cursor()
        sqlQuery = 'select * from chatrooms'
        cursor.execute(sqlQuery)
        receive = cursor.fetchall()
        chatroomsData = []
        for result in receive:
            eachchatroomData = {'id':result[0],'name':result[1]}
            chatroomsData.append(eachchatroomData)
            #清空 再接收
            eachchatroomData = {}

        chatrooms = {'data':chatroomsData,'status':'OK'}
        response = make_response(json.dumps(chatrooms))
    except:
        errorResponse = {'status':'ERROR'}
        response = make_response(json.dumps(errorResponse))

    return response

@app.route('/api/a3/get_messages/')
def getMessages():
    try:
        #获得get参数
        chatroom_id = request.args.get('chatroom_id')
        page = request.args.get('page')
        page = int(page)

        #从数据库中获取该chatroom保存的messages
        cursor = db.cursor()
        sqlQuery = 'select message,name,message_time,user_id from messages where chatroom_id='+chatroom_id+" order by message_time desc"
        cursor.execute(sqlQuery)
        #fetchall得到的是由多个元组组成的列表
        receive = cursor.fetchall()

        total_pages = math.ceil(len(receive)/5)

        #如果输入page错误 返回错误信息
        if(page > total_pages):
            errorResponse = {'message':'The query page does not exist!','status':'ERROR'}
            response = make_response(json.dumps(errorResponse))
            return response

        #将所获得数据库列表分page并存储在allpages列表中
        allPages = []
        eachPage = []
        for result in range(0,len(receive),5):
            eachPage = receive[result:result+5]
            allPages.append(eachPage)

        #返回对应页面的信息
        messagesData = []
        for result in allPages[page-1]:
            eachmessageData = {'message':result[0],'name':result[1],'message_time':str(result[2]),'user_id':result[3]}
            messagesData.append(eachmessageData)
            #清空 再接收
            eachmessageData = {}

        pageMessages = {'data':{'current_page':page,'messages':messagesData,'total_pages':total_pages},'status':'OK'}
        response = make_response(json.dumps(pageMessages))
    except:
        errorResponse = {'status':'ERROR'}
        response = make_response(json.dumps(errorResponse))

    return response

@app.route('/api/a3/send_message/',methods=['POST'])
def uploadMessage():
    try:
        #获取post信息
        chatroom_id = request.form.get('chatroom_id')
        user_id = request.form.get('user_id')
        name = request.form.get('name')
        message = request.form.get('message')

        #差错处理
        if(len(name)>20):
            name = name[0:19]
        if(len(message)>200):
            message = message[0:199]

        #插入数据库
        cursor = db.cursor()
        sqlQuery = 'insert into messages (chatroom_id,user_id,name,message) values (%s,%s,"%s","%s")' % (chatroom_id,user_id,name,message)
        cursor.execute(sqlQuery)

        #获得chatroom_name
        sqlQuery = "select name from chatrooms where id ="+chatroom_id
        cursor.execute(sqlQuery)
        result = cursor.fetchone()
        print(result[0])

        chatroom_name = result[0]


        #发送给数据库中存储的每一台机器
        sqlQuery = "SELECT * from push_tokens"
        cursor.execute(sqlQuery)
        #fetchall得到的是由多个元组组成的列表
        token_json = cursor.fetchall()
        #发送给每一台机子
        for token in token_json:
            print(token[2])
            #发送异步信息
            pushMessage.delay(chatroom_name,chatroom_id,name,message,token[2])

        successResponse = {'status':'OK'}
        response = make_response(json.dumps(successResponse))
    except:
        errorResponse = {'status':'ERROR'}
        response = make_response(json.dumps(errorResponse))

    return response

@app.route('/api/a4/submit_push_token/',methods=['POST'])
def getTokens():
    try:
        #获取post信息
        user_id = request.form.get('user_id')
        token = request.form.get('token')

        #插入数据库
        cursor = db.cursor()
        sqlQuery = 'insert into push_tokens (user_id,token) values (%s,"%s")' % (user_id,token)
        cursor.execute(sqlQuery)

        successResponse = {'status':'OK'}
        response = make_response(json.dumps(successResponse))
    except:
        errorResponse = {'status':'ERROR'}
        response = make_response(json.dumps(errorResponse))

    return response

if __name__ == '__main__':
    app.run(host="0.0.0.0",port=5000)