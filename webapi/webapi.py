#!/usr/bin/env python2
# -*- coding: utf-8 -*-
from bottle import route, run, request, put, post, get, delete, abort, default_app
import urlparse, sys, json, hashlib, re, random, string
import util, backends
from resources import *

connection = Model().connection

errors = {
	'noUser' : 'Your authenfication key is not linked to any account.',
	'wrongUser' : 'You are not allowed to view this page.',
	'missingDetails' : 'Missing or invalid details.',
	'searchUserForbidden' : 'You are not allowed to search users.',
	'disabled' : 'Disabled User',
}

def checkRights(authKeyEmailOrId, haveToBeAdmin):
	try:
		user = connection.getUser(authKeyEmailOrId)
	except MysqlError, e:
		abort(501, e.value)
	if user == None:
		abort(401, errors['noUser'])
	elif user.is_active == False:
		abort(418, errors['disabled'])
	if haveToBeAdmin and (not isinstance(user, Admin)):
		abort(403, errors['wrongUser'])
	return user

@post('/getAuthKey')
def getAuthKey():
	givenCredentials = util.RequestParser(request).parseJson()
	if not (util.RequestParser.dictHasKeys(givenCredentials, ('email', 'passwordhash')) and util.RequestParser.hashValid(givenCredentials.get('passwordhash'))):
		abort(400, errors['missingDetails'])
	user = checkRights(givenCredentials['email'], False)
	calcSaltHash = util.hashString(givenCredentials['passwordhash'] + str(user.salt))
	authDict = {
		'authKey' : user.authKey,
	}
	return (json.dumps(authDict) if calcSaltHash == user.passwordhash else abort(401, errors['wrongUser']))

@route('/<authKey>/user/currentBorrowed')
def currentBorrowed(authKey):
	currentUser = checkRights(authKey, False)
	return json.dumps(currentUser.listAsSerialised(currentUser.getBorrowedItems()))

@route('/<authKey>/user/<id:int>/currentBorrowed')
def currentBorrowed(authKey, id):
	currentUser = checkRights(authKey, False)
	if isinstance (currentUser, Admin) or currentUser.id == id:
		user = connection.getUser(id)
		return json.dumps(user.listAsSerialised(user.getBorrowedItems()))
	else:
		abort(403, error['wrongUser'])
	
@route('/<authKey>/user')
def getUser(authKey):
	user = checkRights(authKey, False)
	return (json.dumps(None) if user == None else user.asJsonable())

@route('/user/<id:int>')
def getOtherUser(id):
	try:
		u = connection.getUser(id)
	except backends.MysqlError, e:
		abort(500, e.value)
	if u == None:
		abort(400, errors['missingDetails'])
	return json.dumps(u.asSafeJsonable())
	
@put('/<authKey>/user')
def createModifyUser(authKey):
	currentUser = checkRights(authKey, False)
	givenUser = util.RequestParser(request).parseJson()
	if isinstance(currentUser, Admin):
		user = (connection.getUser(givenUser.get('id')) if givenUser.get('id') != None else connection.getUser(givenUser.get('email')))
		if user == None and util.RequestParser.dictHasKeys(givenUser, ('email', 'firstname', 
				'lastname', 'is_admin', 'is_active')) and (givenUser.get('password') or givenUser.get('passwordhash')):
			user = (Admin() if givenUser['is_admin'] == True else User())
			user.salt = random.randint(1000000000000000, 9999999999999999)	
		elif user == None:
			abort(400, errors['missingDetails'])
		elif givenUser.get('is_admin') != None:
			dict = user.asSerialised()
			if givenUser['is_admin'] == True and isinstance(user, User):
				dict['is_admin'] = True
				user = Admin(dict)
			elif givenUser['is_admin'] == False and isinstance(user, Admin):
				dict['is_admin'] = False
				user = User(dict)
		user.email = givenUser.get('email')
		if givenUser.get('password'):
			print "sendmail"
			"""emailtext =  u"Hallo " + user.firstname + ,
				vielen Dank, dass du dich entschieden hast unsere tolle [awesome] App zu verwenden.
				Dein Passwort lautet: + givenUser['password'] + u
				Wir empfehlen dir eindringlich das Passwort nach der Erstanmeldung zu aendern.
				Viel Spaß mit unserer atemberaubenden Applikation.
				King Max Porno"""
				#max.vogler@gmail.com
			emailtext = "Benutzername: " + givenUser['email'] +  "\n\nPasswort: " + givenUser['password']
			util.Email(givenUser['email'], "PocketBib Passwort erhalten", emailtext).send()
			user.passwordhash = util.hashString(givenUser['password'])
		else:
			user.passwordhash = givenUser.get('passwordhash')
		user.firstname = givenUser.get('firstname')
		user.lastname = givenUser.get('lastname')
		user.building = givenUser.get('building')
		user.roomnr = givenUser.get('roomnr')
		user.note = givenUser.get('note')
		user.telephone = givenUser.get('telephone')
		user.is_active = givenUser.get('is_active')
		return json.dumps({"id" : user.save()})
	elif givenUser.get('email') == currentUser.email: #nur eigenes Konto bearbeiten
		currentUser.passwordhash = givenUser.get('passwordhash')
		currentUser.building = givenUser.get('building')
		currentUser.roomnr = givenUser.get('roomnr')
		currentUser.telephone = givenUser.get('telephone')
		currentUser.note = givenUser.get('note')
		return json.dumps({"id" : currentUser.save()})
	else:
		abort(403, errors["wrongUser"])
			
@route('/<authKey>/users')
def listUsers(authKey):
	currentUser = checkRights(authKey, True)
	users = connection.getUsers()
	return json.dumps(users)

@put('/<authKey>/item')
def createModifyItem(authKey):
	currentUser = checkRights(authKey, True)
	givenItem = util.RequestParser(request).parseJson()
	item = connection.getItem(givenItem.get('id'))
	if item == None and util.RequestParser.dictHasKeys(givenItem, ('title', 'type')):
		if givenItem['type'] == 'Book':
			if util.RequestParser.dictHasKeys(givenItem, ('isbn13', 'publisher', 'author')):
				item = Book()
			else:
				abort(400, errors['missingDetails'])
		elif givenItem['type'] == 'Magazine':
			if util.RequestParser.dictHasKeys(givenItem, ('issn', 'publisher')):
				item = Magazine()
			else:
				abort(400, errors['missingDetails'])
		else:
			item = OtherItem()
	elif item == None:
		abort(400, errors['missingDetails'])
	item.title = givenItem.get('title')
	item.author = givenItem.get('author')
	item.publisher = givenItem.get('publisher')
	item.publication_year = givenItem.get('publication_year')
	item.publication_date = givenItem.get('publication_date')
	item.description = givenItem.get('description')
	item.isbn13 = givenItem.get('isbn13')
	item.issn = givenItem.get('issn')
	item.creation_time = givenItem.get('creation_time')
	item.is_active = givenItem.get('is_active')
	item.num_pages = givenItem.get('num_pages')
	item.edition = givenItem.get('edition')
	item.position = givenItem.get('position')
	item.type = givenItem.get('type')
	savedItem = connection.getItem(item.save())
	if isinstance(givenItem.get('availableCopies'), int):
		if item.availableCopies() < givenItem.get('availableCopies'):
			for i in range(givenItem['availableCopies'] - item.availableCopies()):
				savedItem.addCopy()
	return json.dumps({"id" : savedItem.id})
		
@delete('/<authKey>/item/<id:int>')
def deleteItem(authKey, id):
	currentUser = checkRights(authKey, True)
	givenDict = util.RequestParser(request).parseJson()
	item = connection.getItem(id)
	if item != None:
		#force = (givenDict.get('force') if isinstance(givenDict.get('force'), bool) else False)
		removedAllCopies = item.removeAllCopies(True)
		if removedAllCopies:
			item.is_deleted = True
			item.save()
			abort(200, "Removed item")
		else:
			abort(409, "Can't remove item")
	else:
		abort(400, errors['missingDetails'])
		
@delete('/<authKey>/user/<id:int>')
def deleteUser(authKey, id):
	currentUser = checkRights(authKey, True)
	user = connection.getUser(id)
	if user != None:
		user.returnAllCopies()
		user.is_deleted = True
		user.save()
		abort(200, "Removed user")
	else:
		abort(400, errors['missingDetails'])
		
@delete('/<authKey>/rating/<id:int>')
def deleteRating(authKey, id):
	currentUser = checkRights(authKey, False)	
	rating = connection.getRatingById(id)
	
	if not rating:
		abort(404, "Rating not found")
	   
	if not isinstance(currentUser, Admin) and rating.user.id != currentUser.id:
		abort(403, errors['wrongUser'])
	else:
		rating.is_deleted = True
		rating.save()
		abort(200, "Removed rating")

@post('/<authKey>/user/search')
def searchUsers(authKey):
	currentUser = checkRights(authKey, True)
	givenUser = util.RequestParser(request).parseJson()
	results = connection.searchUser(givenUser)
	if results == None:
		abort(404, errors['missingDetails'])
	return json.dumps(Model().listAsSerialised(results))
	
@post('/item/search')
def searchItems():
	givenPattern = util.RequestParser(request).parseJson()
	try:
		results = connection.searchItem(givenPattern)
	except backends.MysqlError, e:
		abort(500, e.value)
	if results == None:
		abort(404, errors['missingDetails'])
	return json.dumps(Model().listAsSerialised(results))
	
@put('/<authKey>/item/<id:int>/rate')
def rateItem(authKey, id):
	currentUser = checkRights(authKey, False)
	item = connection.getItem(id)
	if item != None:
		givenRating = util.RequestParser(request).parseJson()
		if givenRating.get('rating') != None:
			rating = connection.getRating(currentUser, item)
			if rating == None:
				mapping = {
					'user' : currentUser,
					'item' : item,
					'rating' : givenRating['rating'],
					'comment' : givenRating.get('comment'),
				}
				rating = Rating(mapping)
			else:
				rating.rating = givenRating['rating']
				rating.comment = givenRating.get('comment')
			return json.dumps({"id" :  rating.save()})
		abort(400, errors['missingDetails'])
	abort(404, "Item not found.")

@route('/item/<id:int>')
def itemInfo(id):
	try:
		item = connection.getItem(id)
	except backends.MysqlError, e:
		abort(500, e.value)
	return (abort(404, "Item not found.") if item == None else item.asJsonable())
	
@route('/book/<isbn:int>')
def bookInfo(isbn):
	try:
		item = connection.getItemByIsbn(isbn)
	except backends.MysqlError, e:
		abort(500, e.value)
	return (abort(404, "Book not found.") if item == None else item.asJsonable())
	
@route('/magazine/<issn:int>')
def magazineInfo(issn):
	try:
		item = connection.getItemByIssn(issn)
	except backends.MysqlError, e:
		abort(500, e.value)
	return (abort(404, "Magazine not found.") if item == None else item.asJsonable())

@put('/<authKey>/item/<id:int>')
def addCopy(authKey, id):
	checkRights(authKey, True)
	return json.dumps({"id":connection.getItem(id).addCopy()})

@delete('/<authKey>/itemCopy/<id:int>')
def deleteCopy(authKey, id):
	checkRights(authKey, True)
	givenDict = util.RequestParser(request).parseJson()
	copy = connection.getItemCopy(id)
	if copy != None:
		force = (givenDict.get('force') if isinstance(givenDict.get('force'), bool) else True)
		removed = connection.getItem(copy.item.id).removeCopy(copy, force)
		if removed:
			abort(200, "Removed Item")
		else:
			abort(409, "Can't Delete Copy")
	else:
		abort(400, errors['missingDetails'])
		
@route('/<authKey>/item/<id:int>/borrow')
def borrowItem(authKey, id):
	currentUser = checkRights(authKey, False)
	item = connection.getItem(id)
	if item != None and item.availableCopies() > 0 and item.is_active:
		freeCopy = item.getFreeCopy()
		if currentUser.hasLendItem(item) == None and freeCopy != None:
			lend = Lend({'user' : currentUser, 'copy' : freeCopy})
			lend.save()
			abort(200, "Borrowed item successful.")
		abort(405, "You lend the item yet.")
	abort(404, "Item currently not available.")	

@route('/<authKey>/item/<id:int>/return')
def returnItem(authKey, id):
	currentUser = checkRights(authKey, False)
	item = connection.getItem(id)
	if item != None:
		copy = currentUser.hasLendItem(item)
		if copy != None:
			lend = connection.getLend(copy)
			lend.returnItem()
			lend.save()
			abort(200, "Returned item successful.")
		else:
			abort(405, "You did not lend the item yet.")
	else:
		abort(404, "Item not found.")

#run(host='0.0.0.0', port=8080)