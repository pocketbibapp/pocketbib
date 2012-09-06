#!/usr/bin/env python
# -*- coding: utf-8 -*-

import urlparse, MySQLdb, sys, json, re, random, smtplib, config, hashlib
from email.message import Message

class RequestParser(object):
	def __init__(self, request):
		self.request = request
	def parseJson(self):
		try:
			return json.loads(self.request.body.read())
		except:
			return {}
	@staticmethod
	def emailValid(email):
		return re.match('[a-zA-Z0-9_.-]+@[a-zA-Z0-9_.-]+$', str(email))
	@staticmethod
	def hashValid(hash):
		return re.match('[a-fA-F0-9]{40}', str(hash))
	@staticmethod
	def idValid(id):
		return re.match('[0-9]+$', str(id))
	@staticmethod
	def isbnValid(isbn):
		return re.match('[0-9]{10}|[0-9]{13}$', str(isbn))
	@staticmethod
	def issnValid(issn):
		return re.match('[0-9]{8}$', str(issn))
	@staticmethod
	def saltValid(salt):
		return re.match('[0-9]{16}$', salt)
	@staticmethod
	def dictHasKeys(dict, keys):
		hasKeys = True
		for key in keys:
			if dict.get(key) == None or (dict.get(key) != None and dict[key] == ''):
				hasKeys = False
				break
		return hasKeys
	
class Generator(object):
	@staticmethod
	def __indexFaq(prefix, set, list):
		if len(set) == 1:
			list.append(prefix + str(set[0]))
		else:
			for i in range(len(set)):
				setCopy = set[:]
				del setCopy[i]
				Generator.__indexFaq(prefix + str(set[i]), setCopy, list)

	@staticmethod
	def indexCombination(count):
		list = []
		indexList = []
		for i in range (count):
			indexList.append(i)
		Generator.__indexFaq('', indexList, list)
		return list
		
class Email(object):
	def __init__(self, receiver, subject, message):
		self.__receivers = [receiver]
		self.__message = Message()
		self.__message.set_payload(message)
		self.__message["Subject"] = subject
		self.__message["From"] = config.SMTP_SENDER 
		self.__message["To"] = receiver
	def send(self):
		try:
			smtpObj = smtplib.SMTP(config.SMTP_HOST, config.SMTP_PORT)
			if config.SMTP_TLS:
				smtpObj.starttls()
				smtpObj.ehlo()
			smtpObj.login(config.SMTP_USER, config.SMTP_PASSWORD)
			smtpObj.sendmail(config.SMTP_SENDER, self.__receivers, self.__message.as_string())
			print "Mail send"
		except:
			print "Mail not send - error"

def hashString(raw):
    h = hashlib.sha1()
    h.update(raw)
    return h.hexdigest()