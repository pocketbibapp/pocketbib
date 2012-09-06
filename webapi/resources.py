#!/usr/bin/env python2
# -*- coding: utf-8 -*-

import hashlib, json, util, datetime, backends, re

class Model(object):
	connection = None
	def __init__(self):
		if Model.connection == None:
			Model.connection = backends.Mysql()
	
	def asJsonable(self):
		return self.asSerialised()
		
	def listAsSerialised(self, list):
		newList = []
		for item in list:
			newList.append(item.asJsonable())
		return newList

class Item(Model):
	def __init__(self, dict = {}):
		Model.__init__(self)
		self.__id = (None if dict.get('id') == None else int(dict.get('id')))
		self.__title = dict.get('title')
		self.__publisher = dict.get('publisher')
		self.__description = dict.get('description')
		self.__creation_time = dict.get('creation_time')
		self.__is_active = dict.get('is_active')
		self.__num_pages = (None if dict.get('sum_rating') == None else int(dict.get('num_pages')))
		self.__edition = dict.get('edition')
		self.__position = dict.get('position')
		self.__sum_rating = (None if dict.get('sum_rating') == None else int(dict.get('sum_rating')))
		self.__num_rating = (None if dict.get('num_rating') == None else int(dict.get('num_rating')))
		self.__is_deleted = dict.get('is_deleted')
		self.__type = (unicode(self.__class__.__name__) if dict.get('type') == None else dict.get('type'))
		self.__copies = None
		self.__ratings = None

	def asSerialised(self):
		return {
			'id' : self.id,
			'title' : self.title,
			'publisher' : self.publisher,
			'description' : self.description,
			'creation_time' : (None if self.__creation_time == None else unicode(self.__creation_time)),
			'is_active' : self.is_active,
			'is_deleted' : self.is_deleted,
			'num_pages' : self.num_pages,
			'edition' : self.edition,
			'position' : self.position,
			'sum_rating' : self.sum_rating,
			'num_rating' : self.num_rating,
			'availableCopies' : self.availableCopies(),
			'type' : self.__type,
		}
		
	def asJsonable(self):
		dict = self.asSerialised()
		dict['ratings'] = self.listAsSerialised(self.ratings)
		dict['item_copies'] = self.listAsSerialised(self.copies)
		del dict['is_deleted']
		return dict
		
	def getId(self):
		return self.__id
			
	def getTitle(self):
		return self.__title
		
	def setTitle(self, title):
		if title != None and title != '':
			self.__title = title			
	def getPublisher(self):
		return self.__publisher
		
	def setPublisher(self, publisher):
		if publisher != None and publisher != '':
			self.__publisher = publisher
			
	def getDescription(self):
		return self.__description
		
	def setDescription(self, description):
		if description != None and description != '':
			self.__description = description
			
	def getCreationTime(self):
		return self.__creation_time
		
	def setCreationTime(self, time):
		if time != None:
			self.__creation_time = time
			
	def getActive(self):
		return self.__is_active
		
	def setActive(self, boolean):
		if boolean != None and isinstance(boolean, bool):
			self.__is_active = boolean
			
	def getDeleted(self):
		return self.__is_deleted
	
	def setDeleted(self, boolean):
		if boolean != None and isinstance(boolean, bool):
			self.__is_deleted = boolean
			
	def getNumPages(self):
		return self.__num_pages
		
	def setNumPages(self, num):
		if num != None and isinstance(num, int):
			self.__num_pages = num
			
	def getEdition(self):
		return self.__edition
		
	def setEdition(self, edition):
		if edition != None:
			self.__edition = edition
			
	def getPosition(self):
		return self.__position
		
	def setPosition(self, position):
		if position != None:
			self.__position = position
			
	def getSumRating(self, reload = False):
		if self.__sum_rating == None or reload:
			self.__sum_rating = 0
			for rating in self.ratings:
				self.__sum_rating += rating.rating
		return self.__sum_rating
				
	def getNumRating(self, reload = False):
		if self.__num_rating == None or reload:
			self.__num_rating = len(self.__ratings)
		return self.__num_rating
		
	def getType(self):
		return self.__type
		
	def setType(self, type):
		if type != None and type != '':
			self.__type = type
		
	def getCopies(self, reload = False):
		if self.__copies == None or reload:
			self.__copies = Model.connection.getItemCopies(self)	
		return self.__copies
		
	def getRatings(self, reload = False):
		if self.__ratings == None or reload:
			self.__ratings = Model.connection.getRatings(self)
			self.getNumRating(True)
			self.getSumRating(True)
		return self.__ratings
		
	def addCopy(self):
		copy = ItemCopy({'item': self})
		return copy.save()
		
	def removeCopy(self, copy, force = False):
		if copy.lend != None:
			if force:
				copy.lend.returnItem()
				copy.lend.save()
			else:
				return False
		copy.is_deleted = True
		copy.save()
		return True
			
	def removeAllCopies(self, force = False):
		if len(self.copies) > 0:
			cannotdelete = False
			if force:
				for copy in self.copies:
					if not self.removeCopy(copy, force):
						cannotdelete = True
			else:
				cannotdelete = True
			return (False if cannotdelete else True)
		else:
			return True
	
	def availableCopies(self):
		lendCopies = 0
		for copy in self.copies:
			if copy.lend != None:
				lendCopies += 1
		return self.count_copies - lendCopies
		
	def getCountCopies(self):
		return len(self.copies)
		
	def getFreeCopy(self):
		freeCopy = None
		for copy in self.copies:
			if copy.lend == None and copy.is_active:
				freeCopy = copy
				break
		return freeCopy
		
	def save(self):
		if self.id == None:
			return Model.connection.createItem(self)
		elif Item.connection.getItem(self.id) != None:
			self.getRatings(True)
			return Model.connection.modifyItem(self)
			
	id = property(getId)
	title = property(getTitle, setTitle)
	publisher = property(getPublisher, setPublisher)
	description = property(getDescription, setDescription)
	creation_time = property(getCreationTime, setCreationTime)
	is_active = property(getActive, setActive)
	is_deleted = property(getDeleted, setDeleted)
	num_pages = property(getNumPages, setNumPages)
	edition = property(getEdition, setEdition)
	position = property(getPosition, setPosition)
	sum_rating = property(getSumRating)
	num_rating = property(getNumRating)
	copies = property(getCopies)
	count_copies = property(getCountCopies)
	ratings = property(getRatings)
	type = property(getType, setType)
	
class Book(Item):
	def __init__(self, dict = {}):
		Item.__init__(self, dict)
		self.__author = dict.get('author')
		self.__publication_year = (int(dict.get('publication_year')) if dict.get('publication_year') != None else None)
		self.__isbn13 = dict.get('isbn13')
	def asSerialised(self):
		itemDict = super(Book, self).asSerialised()
		itemDict['author'] = self.author
		itemDict['isbn13'] = self.isbn13
		itemDict['publication_year'] = self.publication_year
		return itemDict
	def asJsonable(self):
		return super(Book, self).asJsonable()
	def getAuthor(self):
		return self.__author
	def setAuthor(self, author):
		if author != None and author != '':
			self.__author = author
	def getPublicationYear(self):
		return self.__publication_year
	def setPublicationYear(self, year):
		if year != None and year != '':
			self.__publication_year = year
	def getIsbn13(self):
		return self.__isbn13
	def setIsbn13(self, isbn):
		if isbn != None:
			self.__isbn13 = re.sub("[^0-9]", "", isbn)
	author = property(getAuthor, setAuthor)
	publication_year = property(getPublicationYear, setPublicationYear)
	isbn13 = property(getIsbn13, setIsbn13)
	
class Magazine(Item):
	def __init__(self, dict = {}):
		Item.__init__(self, dict)
		self.__issn = dict.get('issn')
		self.__publication_date = dict.get('publication_date')
	def asSerialised(self):
		itemDict = super(Magazine, self).asSerialised()
		itemDict['issn'] = self.issn
		itemDict['publication_date'] = self.publication_date
		return itemDict
	def asJsonable(self):
		return super(Magazine, self).asJsonable()
	def getIssn(self):
		return self.__issn
	def setIssn(self, issn):
		if issn != None:
			self.__issn = re.sub("[^0-9]", "", issn)
	def getPublicationDate(self):
		self.__publication_date
	def setPublicationDate(self, publication_date):
		if publication_date != None:
			self.__publication_date = publication_date
	issn = property(getIssn, setIssn)
	publication_date = property(getPublicationDate, setPublicationDate)
	
class OtherItem(Book, Magazine):
	def __init__(self, dict = {}):
		Book.__init__(self, dict)
		Magazine.__init__(self, dict)
		
class User(Model):	
	def __init__(self, dict = {}):
		Model.__init__(self)
		self.__id = dict.get('id')
		self.__authKey = dict.get('authKey')
		self.__email = dict.get('email')
		self.__salt = dict.get('salt')
		self.__passwordhash = dict.get('passwordhash')
		self.__firstname = dict.get('firstname')
		self.__lastname = dict.get('lastname')
		self.__building = dict.get('building')
		self.__roomnr = dict.get('roomnr')
		self.__note = dict.get('note')
		self.__telephone = dict.get('telephone')
		self.__is_active = dict.get('is_active')
		self.__is_deleted = dict.get('is_deleted')
		self.__borrowedCopies = None
		
	def asSerialised(self):
		return {
			'id' : self.id,
			'authKey' : self.authKey,
			'email' : self.email,
			'salt' : self.salt,
			'passwordhash' : self.passwordhash,
			'firstname' : self.firstname,
			'lastname' : self.lastname,
			'building' : self.building,
			'roomnr' : self.roomnr,
			'note' : self.note,
			'telephone' : self.telephone,
			'is_admin' : (True if isinstance(self, Admin) else False),
			'is_active' : self.is_active,
			'is_deleted' : self.is_deleted,
		}
		
	def asJsonable(self):
		jsonabledict = self.asSerialised()
		del jsonabledict['salt']
		del jsonabledict['passwordhash']
		return jsonabledict
		
	def asSafeJsonable(self):
		jsonabledict = self.asJsonable()
		del jsonabledict['authKey']
		return jsonabledict
		
	def getId(self):
		return self.__id
		
	def getAuthKey(self):
		return self.__authKey

	def __updateAuthKey(self):
		self.__authKey = util.hashString(self.passwordhash + self.email + str(isinstance(self, Admin)) + str(self.is_active))
		
	def getEmail(self):
		return self.__email
	
	def setEmail(self, email):
		if email != None and util.RequestParser.emailValid(email):
			self.__email = email
	
	def getSalt(self):
		return self.__salt
	
	def setSalt(self, salt):
		if salt != None and util.RequestParser.saltValid(str(salt)):
			self.__salt = salt

	def getPasswordHash(self):
		return self.__passwordhash			
		
	def setPasswordHash(self, passwordhash):
		if passwordhash != None and util.RequestParser.hashValid(passwordhash):
			self.__passwordhash = util.hashString(passwordhash + str(self.__salt))

	def getFirstname(self):
		return self.__firstname
		
	def setFirstname(self, firstname):
		if firstname != None and firstname != '':
			self.__firstname = firstname
		
	def getLastname(self):
		return self.__lastname
		
	def setLastname(self, lastname):
		if lastname != None and lastname != '':
			self.__lastname = lastname
		
	def getBuilding(self):
		return self.__building
		
	def setBuilding(self, building):
		if building != None and len(building) <= 30 and len(building) > 0:
			self.__building = building
		
	def getRoomnr(self):
		return self.__roomnr
	
	def setRoomnr(self, roomnr):
		if roomnr != None and len(roomnr) <= 30 and len(roomnr) > 0:
			self.__roomnr = roomnr
	
	def getNote(self):
		return self.__note
		
	def setNote(self, note):
		if note != None:
			self.__note = note
		
	def getTelephone(self):
		return self.__telephone
		
	def setTelephone(self, telephone):
		if telephone != None and len(telephone) <= 30 and len(telephone) > 0:
			self.__telephone = telephone
			
	def getActive(self):
		return self.__is_active
		
	def setActive(self, boolean):
		if boolean != None and isinstance(boolean, bool):
			self.__is_active = boolean
			
	def getDeleted(self):
		return self.__is_deleted
		
	def setDeleted(self, boolean):
		if boolean != None and isinstance(boolean, bool):
			self.__is_deleted = boolean
			
	def getBorrowedCopies(self):
		if self.__borrowedCopies == None:
			self.__borrowedCopies = Model.connection.getBorrowedCopies(self)
		return self.__borrowedCopies
	
	def returnAllCopies(self):
		for copy in self.getBorrowedCopies():
			copy.lend.returnItem()
	
	def getBorrowedItems(self):
		itemList = []
		for copy in self.borrowedCopies:
			itemList.append(copy.item)
		return itemList
	
	def hasLendItem(self, item):
		copy = None
		for borrowedCopy in self.borrowedCopies:
			if borrowedCopy.item.id == item.id:
				copy = borrowedCopy
				break
		return copy
	
	def save(self):
		self.__updateAuthKey()
		if User.connection.getUser(self.email) == None and User.connection.getUser(self.id) == None:
			return Model.connection.createUser(self)
		else:
			return Model.connection.modifyUser(self)
			
	id = property(getId)
	authKey = property(getAuthKey)
	email = property(getEmail, setEmail)
	salt = property(getSalt, setSalt)
	passwordhash = property(getPasswordHash, setPasswordHash)
	firstname = property(getFirstname, setFirstname)
	lastname = property(getLastname, setLastname)
	building = property(getBuilding, setBuilding)
	roomnr = property(getRoomnr, setRoomnr)
	note = property(getNote, setNote)
	telephone = property(getTelephone, setTelephone)
	is_active = property(getActive, setActive)
	is_deleted = property(getDeleted, setDeleted)
	borrowedCopies = property(getBorrowedCopies)
	borrowedItems = property(getBorrowedItems)
	
class Admin(User):
	def __init__(self, dict = {}):
		User.__init__(self, dict)
		
class Lend(Model):
	def __init__(self, dict):
		Model.__init__(self)
		self.__id = dict.get('id')
		self.__user = dict['user']
		self.__copy = dict['copy']
		self.__borrow_time = dict.get('borrow_time')
		self.__return_time = dict.get('return_time')
		
	def asSerialised(self):
		return {
			'id': self.__id,
			'user_id' : self.__user.id,
			'copy_id' : self.__copy.id,
			'borrow_time' : (None if self.__borrow_time == None else unicode(self.__borrow_time)),
			'return_time' : (None if self.__return_time == None else unicode(self.__return_time)),
		}
		
	def getId(self):
		return self.__id
		
	def returnItem(self):
		if self.__return_time == None:
			self.__return_time = datetime.datetime.now()
		
	def save(self):
		if self.__id == None:
			Model.connection.createLend(self)
			createdLend = Model.connection.getLend(self.__copy)
			createdLend.__copy.lend = createdLend
			return createdLend.__copy.save()
		else:
			Model.connection.modifyLend(self)
			self.__copy.lend = None
			self.__copy.save()
	id = property(getId)

class Rating(Model):
	def __init__(self, dict):
		Model.__init__(self)
		self.__id = dict.get('id')
		self.__user = dict['user']
		self.__item = dict['item']
		self.__rating = None
		self.__rating = int(dict['rating'])
		self.__comment = dict.get('comment')
		self.__creation_time = dict.get('creation_time')
		self.__is_deleted = dict.get('is_deleted')

	def asSerialised(self):
		return {
			'id': self.id,
			'user_id' : self.user.id,
			'item_id' : self.item.id,
			'rating' : self.rating,
			'comment' : self.comment,
			'creation_time' : (None if self.creation_time == None else unicode(self.creation_time)),
			'is_deleted' : self.is_deleted
		}
		
	def getId(self):
		return self.__id
		
	def getUser(self):
		return self.__user
		
	def getItem(self):
		return self.__item
		
	def getRating(self):
		return self.__rating
	def setRating(self, rating):
		if rating != None and rating >= 0 and rating <= 10:
			self.__rating = rating
		
	def getComment(self):
		return self.__comment
	def setComment(self, comment):
		if comment != None:
			self.__comment = comment
			
	def getDeleted(self):
		return self.__is_deleted
		
	def setDeleted(self, boolean):
		if boolean != None and isinstance(boolean, bool):
			self.__is_deleted = boolean

	def getCreationTime(self):
		return self.__creation_time
		
	def save(self):
		ratingId = None
		if self.__id == None:
			ratingId = Model.connection.createRating(self)
		else:
			ratingId = Model.connection.modifyRating(self)
		self.__item.save()
		return ratingId
	
	id = property(getId)
	user = property(getUser)
	item = property(getItem)
	rating = property(getRating, setRating)
	comment = property(getComment, setComment)
	creation_time = property(getCreationTime)
	is_deleted = property(getDeleted, setDeleted)

class ItemCopy(Model):
	def __init__(self, dict):
		Model.__init__(self)
		self.__id = dict.get('id')
		self.__item = dict['item']
		self.__lend = None
		self.__creation_time = dict.get('creation_time')
		self.__is_active = (True if dict.get('is_active') == None else dict.get('is_active'))
		self.__is_deleted = dict.get('is_deleted')

	def asSerialised(self):	
		return {
			'id': self.id,
			'item_id' : self.item.id,
			'lend_id' : (-1 if self.lend == None else self.lend.id),
			'creation_time' : (None if self.creation_time == None else unicode(self.creation_time)),
			'is_active' : self.is_active,
			'is_deleted' : self.is_deleted,
		}
		
	def asJsonable(self):
		dict = self.asSerialised()
		dict['lend'] = (None if self.lend == None else self.lend.asJsonable())
		return dict
		
	def getId(self):
		return self.__id
		
	def getItem(self):
		return self.__item
		
	def getLend(self, reload = False):
		if self.__lend == None or reload:
			self.__lend = Model.connection.getLend(self)
		return self.__lend
	
	def setLend(self, lend):
		self.__lend = lend
		
	def getCreationTime(self):
		return self.__creation_time
		
	def getActive(self):
		return self.__is_active
		
	def setActive(self, active):
		if isinstance(active, bool):
			self.__is_active = active
			
	def getDeleted(self):
		return self.__is_deleted
		
	def setDeleted(self, boolean):
		if boolean != None and isinstance(boolean, bool):
			self.__is_deleted = boolean
			
	def cmpLend(self, copy1, copy2):
		if copy1.lend == None and copy2.lend != None:
			return -1 - copy2.lend.id
		elif copy1.lend != None and copy2.lend == None:
			return copy1.lend.id + 1
		elif copy1.lend == None and copy2.lend == None:
			return 0
		else:
			return copy1.lend.id - copy2.lend.id
		
	def save(self):
		if self.__id == None:
			return Model.connection.createItemCopy(self)
		else:
			return Model.connection.modifyItemCopy(self)
	
	id = property(getId)
	lend = property(getLend, setLend)
	item = property(getItem)
	creation_time = property(getCreationTime)
	is_active = property(getActive, setActive)
	is_deleted = property(getDeleted, setDeleted)