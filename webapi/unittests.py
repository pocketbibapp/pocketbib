#!/usr/bin/env python2
# -*- coding: utf-8 -*-

import unittest
import urlparse, sys, json, re, random, string
import util, backends
from resources import *
import httplib, urllib

connection = Model().connection

class UserAdministration(unittest.TestCase):
	def setUp(self):
		self.jsonuser = '{"email" : ""}'
		self.jens = connection.getUser(110)
		
	def test_user_attribute(self):
		print "Test: User Attribute"
		self.failUnlessEqual(self.jens.firstname, "Jens")
		self.failUnlessEqual(self.jens.lastname, "Horneber")
		self.failUnlessEqual(self.jens.is_active, True)
		self.failUnlessEqual(self.jens.telephone, "953")
		self.failUnlessEqual(isinstance(self.jens, Admin), False)
		self.failUnlessEqual(self.jens.id, 110)
		self.failUnlessEqual(self.jens.note, "")
		self.failUnlessEqual(self.jens.building, "85.5")
		self.failUnlessEqual(self.jens.is_deleted, False)
		self.failUnlessEqual(self.jens.email, "horneber@kit.edu")
		
		self.jens.firstname = "Hans"
		self.jens.lastname = "Maurer"
		self.jens.is_active = False
		self.jens.telephone = "+49839393239"
		self.jens.note = "Ich bin der König!"
		self.jens.building = "89.30"
		self.jens.is_deleted = True
		self.jens.email = "alex@bananenmail.com"
		
		self.failUnlessEqual(self.jens.id, self.jens.save())
		
		new_jens = connection.getUser(110)
		self.failUnlessEqual(new_jens, None) #is deleted 
		
		self.jens.is_deleted = False
		self.jens.save()
		
		self.jens = connection.getUser(110)
		
		self.failUnlessEqual(self.jens.id, 110)
		self.failUnlessEqual(self.jens.firstname, "Hans")
		self.failUnlessEqual(self.jens.lastname, "Maurer")
		self.failUnlessEqual(self.jens.is_active, False)
		self.failUnlessEqual(self.jens.telephone, "+49839393239")
		self.failUnlessEqual(self.jens.note, u"Ich bin der König!")
		self.failUnlessEqual(self.jens.building, "89.30")
		self.failUnlessEqual(self.jens.email, "alex@bananenmail.com")
		
		self.jens.firstname = "Jens"
		self.jens.lastname = "Horneber"
		self.jens.is_active = True
		self.jens.telephone = "953"
		self.jens.note = ""
		self.jens.building = "85.5"
		self.jens.is_deleted = False
		self.jens.email = "horneber@kit.edu"
		
		self.jens.save()
		
		self.failUnlessEqual(self.jens.firstname, "Jens")
		self.failUnlessEqual(self.jens.lastname, "Horneber")
		self.failUnlessEqual(self.jens.is_active, True)
		self.failUnlessEqual(self.jens.telephone, "953")
		self.failUnlessEqual(isinstance(self.jens, Admin), False)
		self.failUnlessEqual(self.jens.id, 110)
		self.failUnlessEqual(self.jens.note, "")
		self.failUnlessEqual(self.jens.building, "85.5")
		self.failUnlessEqual(self.jens.is_deleted, False)
		self.failUnlessEqual(self.jens.email, "horneber@kit.edu")
		
		self.failUnlessEqual(connection.getUser(106).lastname, u"Müller")
	
	def test_user_wrong_input(self):
		pass#self.failUnlessRaises(ValueError, fak.fak, -1)
		
class UtilTests(unittest.TestCase):
    def testSha1(self):
        print "Test: Hash Sha1"
        self.failUnlessEqual(util.hashString("test"), "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3")
        
class WebApiBlackboxTest(unittest.TestCase):
	def setUp(self):		
		self.headers = {"Content-type": "application/x-www-form-urlencoded",
				"Accept": "text/plain"}
		self.conn = None
		self.userList = None
		self.adminAuthKey = "d961df474611d9410c3b8f93f4290b267255315d"
		
	def request(self, method, url, params = ""):
		if self.conn:
			self.conn.close()
		self.conn = httplib.HTTPConnection("fpieper.de", 8080)
		self.conn.request(method, url, params, self.headers)
		return self.conn.getresponse()
	
	def getAuthKey(self, user_id, reload = False):
		if (not self.userList) or reload:
			response = self.request("GET", "/%s/users" % self.adminAuthKey)
			self.failUnlessEqual(response.status, 200)
			self.userList = json.loads(response.read())
		for user in self.userList:
			if user_id == user['id']:
				return user['authKey']
			
	def testCacheAuthKey(self):
		self.failUnlessEqual(self.getAuthKey(99), self.adminAuthKey)

	def test_getAuthKey(self):
		#Correct Login
		params = '{"email" : "horst@all.com", "passwordhash" : "86f7e437faa5a7fce15d1ddcb9eaeaea377667b8"}'
		response = self.request("POST", "/getAuthKey", params)
		self.failUnlessEqual(response.status, 200)
		self.failUnlessEqual(response.read(), '{"authKey": "d961df474611d9410c3b8f93f4290b267255315d"}')
		#Wrong fields
		params = '{"email" : "l195712@rtrtr.com", "password_hash" : "86f7e437faa5a7fce15d1ddcb9eaeaea377667b8"}'
		response = self.request("POST", "/getAuthKey", params)
		self.failUnlessEqual(response.status, 400)
		#Disabled user
		params = '{"email" : "fritz@all.com", "passwordhash" : "e9d71f5ee7c92d6dc9e92ffdad17b8bd49418f98"}'
		response = self.request("POST", "/getAuthKey", params)
		self.failUnlessEqual(response.status, 418)
		
	def test_currentBorrowed(self):
		#Success
		response = self.request("GET", "/%s/user/currentBorrowed" % self.getAuthKey(110))
		self.failUnlessEqual(response.status, 200)
		self.failUnlessEqual(json.loads(response.read())[0]['id'], 1235)
		#Empty
		response = self.request("GET", "/%s/user/currentBorrowed" % self.getAuthKey(90))
		self.failUnlessEqual(response.status, 200)
		self.failUnlessEqual(json.loads(response.read()), [])
		#NoUser
		response = self.request("GET", "/d4d735c18c6d395f52e3720d5074dfc4de104161/user/currentBorrowed")
		self.failUnlessEqual(response.status, 401)
		#OtherUser
		response = self.request("GET", "/%s/user/94/currentBorrowed" % self.getAuthKey(90))
		self.failUnlessEqual(response.status, 200)
		self.failUnlessEqual(json.loads(response.read()), [])
		
	def test_user(self):
		response = self.request("GET", "/%s/user" % self.getAuthKey(90))
		self.failUnlessEqual(response.status, 200)
		self.failUnlessEqual(response.read(), '{"firstname": "kluas", "lastname": "rata", "is_active": true, "telephone": null, "roomnr": null, "is_admin": true, "id": 90, "building": null, "note": "", "is_deleted": false, "authKey": "24d735c18c6d395f52e3720d5074dfc4de104161", "email": "hannelore@kohl.de"}')
		response = self.request("GET", "/user/93")
		self.failUnlessEqual(response.status, 200)
		self.failUnlessEqual(response.read(), '{"firstname": "Max", "lastname": "Mauthe", "is_active": true, "telephone": null, "roomnr": null, "is_admin": true, "id": 93, "building": null, "note": null, "is_deleted": false, "email": "max@kllop.dde"}')
		
	def test_createDeleteUser(self):
		params = {
			"firstname" : "Waldraud",
			"lastname" : "Hafer",
			"email" : "pferd@gaulhausen.de",
			"passwordhash" : "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3",
			"is_active" : True,
			"is_admin" : True
		}

		#User darf nicht erstellen
		response = self.request("PUT", "/%s/user" % self.getAuthKey(109), json.dumps(params))
		self.failUnlessEqual(response.status, 403)
		
		#Admin kann erstellen
		response = self.request("PUT", "/%s/user" % self.getAuthKey(107), json.dumps(params))
		self.failUnlessEqual(response.status, 200)
		createdUserId = json.loads(response.read()).get('id')
		self.failUnless(createdUserId, int)
		
		#eigenes Ändern
		response = self.request("PUT", "/%s/user" % self.getAuthKey(110), '{"email" : "horneber@kit.edu", "roomnr" : "Hallo"}')
		self.failUnlessEqual(response.status, 200)
		self.failUnless(isinstance(json.loads(response.read()).get('id'), int))
		
		#Emailfunktion manuell getestet
		
		response = self.request("DELETE", "/%s/user/%i" % (self.adminAuthKey, createdUserId))
		self.failUnlessEqual(response.status, 200)
		
	def test_getAllUsers(self):
		response = self.request("GET", "/%s/users" % self.adminAuthKey)
		self.failUnlessEqual(response.status, 200)
		self.failIfEqual(response.read(), '[]')
		
	def test_CreateAndDeleteCopy(self):
		response = self.request("PUT", "/%s/item/1600" % self.adminAuthKey)
		self.failUnlessEqual(response.status, 200)
		createdCopyId = json.loads(response.read()).get('id')
		response = self.request("DELETE", "/%s/itemCopy/%i" % (self.adminAuthKey, createdCopyId))
		self.failUnlessEqual(response.status, 200)
		response = self.request("DELETE", "/%s/itemCopy/%i" % (self.adminAuthKey, createdCopyId))
		self.failUnlessEqual(response.status, 400)
		
	def test_CreateAndDeleteRating(self):
		params = {
			"rating" : 4,
			"comment" : u"schönes Debuggen :)",
		}
		#Disabled User
		response = self.request("PUT", "/%s/item/1600/rate" % self.getAuthKey(111), json.dumps(params))
		self.failUnlessEqual(response.status, 418)
		#Normal comment
		response = self.request("PUT", "/%s/item/1600/rate" % self.getAuthKey(110), json.dumps(params))
		createdRatingId = json.loads(response.read()).get('id')
		self.failUnlessEqual(response.status, 200)
		
		#Wrong Rating ID
		response = self.request("PUT", "/%s/item/100000/rate" % self.getAuthKey(110), json.dumps(params))
		self.failUnlessEqual(response.status, 404)
		
		#Get Item check
		response = self.request("GET", "/item/1600")
		self.failUnlessEqual(response.status, 200)
		itemInfo = json.loads(response.read())
		found = False
		for rating in itemInfo['ratings']:
			if rating['id'] == createdRatingId and rating['rating'] == params['rating'] and rating['comment'] == params['comment']:
				found = True
		self.failUnless(found)
				
		response = self.request("DELETE", "/%s/rating/%i" % (self.adminAuthKey, createdRatingId))
		
	def test_SearchUser(self):
		params = {
			"query" : "Frodo",
		}
		#Disabled
		response = self.request("POST", "/%s/user/search" % self.getAuthKey(111), json.dumps(params))
		self.failUnlessEqual(response.status, 418)
		
		#Normal User
		response = self.request("POST", "/%s/user/search" % self.getAuthKey(110), json.dumps(params))
		self.failUnlessEqual(response.status, 403)
		
		#Normal User
		response = self.request("POST", "/%s/user/search" % self.adminAuthKey, json.dumps(params))
		self.failUnlessEqual(response.status, 200)
		self.failIfEqual(response.read(), '[]')
		
	def test_SearchItem(self):
		params = {
			"query" : "java",
		}
		response = self.request("POST", "/item/search", json.dumps(params))
		self.failUnlessEqual(response.status, 200)
		self.failIfEqual(response.read(), '[]')
		params = {
			"query" : "jkaenfalfmaeöfeamföamoefpeafnmoaenfiaefonraflnarofinarof",
		}
		response = self.request("POST", "/item/search", json.dumps(params))
		self.failUnlessEqual(response.status, 200)
		self.failUnlessEqual(response.read(), '[]')
		
	def test_GetBook(self):
		#Get Item By ISBN check
		response = self.request("GET", "/book/9783440115084")
		self.failUnlessEqual(response.status, 200)
		self.failUnlessEqual(json.loads(response.read())['author'], "Peter Norman")
		
	def test_GetMagazine(self):
		#Get Item By ISBN check
		response = self.request("GET", "/magazine/15308669")
		self.failUnlessEqual(response.status, 200)
		self.failUnlessEqual(json.loads(response.read())['publisher'], "Wiley")
		
	def test_BorrowReturn(self):
		response = self.request("GET", "/%s/item/1600/borrow" % self.getAuthKey(110))
		self.failUnlessEqual(response.status, 200)
		
		response = self.request("GET", "/%s/item/1600/borrow" % self.getAuthKey(110))
		self.failUnlessEqual(response.status, 405)
		
		response = self.request("PUT", "/%s/item/1600" % self.adminAuthKey)
		self.failUnlessEqual(response.status, 200)
		createdCopyId = json.loads(response.read()).get('id')
		
		response = self.request("GET", "/%s/item/1600/borrow" % self.getAuthKey(110))
		self.failUnlessEqual(response.status, 405)
		
		response = self.request("DELETE", "/%s/itemCopy/%i" % (self.adminAuthKey, createdCopyId))
		self.failUnlessEqual(response.status, 200)
		
		response = self.request("GET", "/%s/item/1600/return" % self.getAuthKey(110))
		self.failUnlessEqual(response.status, 200)
		
	def test_createDeleteItem(self):
		params = {
			"type" : u"Book",
			"title" : u"Hans auf großer Fahrt",
			"author" : u"Fritz Adelbert",
			"isbn13" : u"978-3765459115",
			"publisher" : u"Hans Georg"
		}
		response = self.request("PUT", "/%s/item" % self.getAuthKey(110), json.dumps(params))
		self.failUnlessEqual(response.status, 403)
		
		response = self.request("PUT", "/%s/item" % self.adminAuthKey, json.dumps(params))
		self.failUnlessEqual(response.status, 200)
		createdItemId = json.loads(response.read()).get('id')
		
		#Get Item check
		response = self.request("GET", "/item/%s" % createdItemId)
		self.failUnlessEqual(response.status, 200)
		itemInfo = json.loads(response.read())
		self.failUnlessEqual(itemInfo['title'], params['title'])
		
		params['title'] = "Moritz auf hoher See"
		params['id'] = createdItemId
		response = self.request("PUT", "/%s/item" % self.adminAuthKey, json.dumps(params))
		self.failUnlessEqual(response.status, 200)
		moddedItemId = json.loads(response.read()).get('id')
		self.failUnlessEqual(moddedItemId, createdItemId)
		
		#Get Item check
		response = self.request("GET", "/item/%s" % createdItemId)
		self.failUnlessEqual(response.status, 200)
		itemInfo = json.loads(response.read())
		self.failUnlessEqual(itemInfo['title'], params['title'])
		
		#put Copy
		response = self.request("PUT", "/%s/item/%i" % (self.adminAuthKey, createdItemId))
		self.failUnlessEqual(response.status, 200)
		createdCopyId = json.loads(response.read()).get('id')
		
		#borrow
		response = self.request("GET", "/%s/item/%i/borrow" % (self.getAuthKey(110), createdItemId))
		self.failUnlessEqual(response.status, 200)
		
		response = self.request("DELETE", "/%s/item/%i" % (self.adminAuthKey, createdItemId), json.dumps(params))
		self.failUnlessEqual(response.status, 200)
		
		response = self.request("GET", "/%s/user/currentBorrowed" % self.getAuthKey(110))
		self.failUnlessEqual(response.status, 200)
		self.failIfEqual(json.loads(response.read()), [])
		
	def tearDown(self):
		if self.conn:
			self.conn.close()

if __name__ == "__main__": 
	unittest.main()