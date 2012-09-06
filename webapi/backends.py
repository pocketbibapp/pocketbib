#!/usr/bin/env python2
# -*- coding: utf-8 -*-

import MySQLdb, util, re, config

from resources import *

class MysqlError(Exception):
	def __init__(self, value):
		self.value = value
	def __str__(self):
		return repr(self.value)

class Mysql(object):
	def __init__(self):
		self.__connection = None
		self.__cursor = None
		
		self.__openConnection()
		
		self.__itemMapping = {
			'id' : ('biblionumber', 0),
			'title' : ('title', 2),
			'author' : ('author', 1),
			'publisher' : ('publisher', 7),
			'publication_year' : ('publicationyear', 6),
			'description' : ('description', 23),
			'isbn13' : ('isbn', 4),
			'issn' : ('issn', 5),
			'creation_time' : ('timestamp', 13),
			'is_active' : ('is_active', 18),
			'num_pages' : ('num_pages', 19),
			'edition' : ('edition', 8),
			'position' : ('place', 15),
			'sum_rating' : ('sum_rating', 20),
			'num_rating' : ('num_rating', 21),
			'type' : ('type', 22),
			'publication_date' : ('publication_date', 23),
			'is_deleted' : ('is_deleted', 24),
		}
		
		self.__userMapping = {
			'id' : ('UID', 0),
			'authKey' : ('authKey', 22),
			'email' : ('email', 10),
			'salt' : ('salt', 16),
			'passwordhash' : ('password', 17),
			'firstname' : ('firstname', 3),
			'lastname' : ('lastname', 4),
			'building' : ('housenr', 6),
			'roomnr' : ('roomnr', 21),
			'note' : ('note', 18),
			'telephone' : ('tel', 11),
			'is_admin' :  ('is_admin', 19),
			'is_active' : ('is_active', 20),
			'is_deleted' : ('is_deleted', 23),
		}
		
		self.__lendMapping = {
			'id' : ('auto', 0),
			'user_id' : ('UserID', 1),
			'copy_id' : ('biblionumber', 2),
			'borrow_time' : ('date', 3),
			'return_time' : ('return_time', 5),
		}
		
		self.__itemCopyMapping = {
			'id' : ('id', 0),
			'item_id' : ('item_id', 1),
			'lend_id' : ('lend_id', 2),
			'creation_time' : ('creation_time', 3),
			'is_active' : ('is_active', 4),
			'is_deleted' : ('is_deleted', 5),
		}
		
		self.__ratingMapping = {
			'id' : ('id', 0),
			'user_id' : ('user_id', 1),
			'item_id' : ('item_id', 2),
			'rating' : ('rating', 3),
			'creation_time' : ('time', 4),
			'comment' : ('comment', 5),
			'is_deleted' : ('is_deleted', 6),
		}
		
	def __openConnection(self):
		print "MysqlBackend -> OpenConnection"
		try:
			self.__connection = MySQLdb.connect(host = config.DB_HOST, user= config.DB_USER, passwd = config.DB_PASSWORD, db = config.DB_NAME, charset="utf8", use_unicode=True)
			self.__cursor = self.__connection.cursor()
		except MySQLdb.OperationalError:
			self.__connection = None
			self.__cursor = None
			print "MysqlBackend -> Can't connect. Mysql server down."
		
	def __closeConnection(self):
		print "MysqlBackend -> Close Connection"
		try:
			self.__connection.close()
		except (MySQLdb.ProgrammingError, AttributeError):
			print "No connection to close"
		
	def __execute(self, sql, values = None):
		try:
			self.__cursor.execute(sql, values)
		except (MySQLdb.OperationalError, AttributeError):
			print "MysqlBackend -> MysqlOperationalError"
			self.__closeConnection()
			self.__openConnection()
			if self.__connection != None:
				self.__cursor.execute(sql, values)
			else:
				raise MysqlError("Mysqlserver nicht erreichbar")
				
	def __fetchone(self):
		if self.__cursor != None:
			return self.__cursor.fetchone()
		else:
			raise MysqlError("Mysqlserver nicht erreichbar")
		
	def __fetchall(self):
		if self.__cursor != None:
			return self.__cursor.fetchall()
		else:
			raise MysqlError("Mysqlserver nicht erreichbar")
		
	def getItemMapping(self):
		return self.__itemMapping.copy()
		
	def getUserMapping(self):
		return self.__userMapping.copy()
		
	def getLendMapping(self):
		return self.__lendMapping.copy()
		
	def getRatingMapping(self):
		return self.__ratingMapping.copy()
		
	def getItemCopyMapping(self):
		return self.__itemCopyMapping.copy()
		
	def __unicodeList(self, list):
		unicodeList = []
		for item in list:
			if item != None and isinstance(item, str):
				item = unicode(item, "utf_8")
			unicodeList.append(item)
		return list
		
	def __getRows(self, query, values = None):
		if values == None or len(values) == 0:
			self.__execute(query)
		else:
			self.__execute(query, values)
		rowlist = self.__fetchall()
		if not query.startswith(('SELECT * FROM Ratings', 'SELECT * FROM Lendbooks', 'SELECT * FROM ItemCopies')):
			#print self.__cursor._executed
			pass
		unicodeList = []
		for row in rowlist:
			unicodeList.append(self.__unicodeList(row))
		return unicodeList
	def __getRow(self, query, values = None):
		if values == None or (isinstance(values, list) and len(values) == 0):
			self.__execute(query)
		else:
			self.__execute(query, values)
		row = self.__fetchone()
		if not query.startswith(('SELECT * FROM Ratings', 'SELECT * FROM Lendbooks', 'SELECT * FROM ItemCopies')):
			#print self.__cursor._executed
			pass
		if row != None:
			return self.__unicodeList(row)
			
	def __insertRow(self, table, dict, mapping):
		del dict['id']
		columnString = ''
		valuesList = []
		sqlValuePlaceholder = ''
		for key in dict:
			if dict[key] == None or mapping.get(key) == None:
				continue
			columnString += ", " + mapping[key][0]
			valuesList.append(dict[key])
			sqlValuePlaceholder += "%s, "
		columnString = columnString.replace(", ", "", 1)
		sql = "INSERT INTO %s (%s) VALUES (" %(table, columnString)
		sql += sqlValuePlaceholder[:-2] + ")"
		self.__execute(sql , valuesList)
		#print self.__cursor._executed
		return self.__connection.insert_id()
		
	def __updateRow(self, table, dict, mapping, keyValue):
		value = dict[keyValue]
		del dict['id']
		setString = ''
		valuesList = []
		for key in mapping:
			if dict.get(key) != None:
				setString += ", " + mapping[key][0] + " = %s "
				valuesList.append(dict[key])
		setString = setString.replace(", ", "", 1)
		sql = "UPDATE %s SET %sWHERE %s='%s'" %(table, setString, mapping[keyValue][0], value)
		self.__execute(sql, valuesList)
		#print self.__cursor._executed
		
	def __regexEscape(self, pattern):
		escapeList = (".", "^", "$", "*", "+", "?", "{", "}", "|", "(", ")")
		for escape in escapeList:
			pattern = pattern.replace(escape, "[" + escape + "]")
		return pattern
		
	def __regexUnescape(self, pattern):
		unescapeList = ("[", "]")
		for unescape in unescapeList:
			pattern = pattern.replace(unescape, "")
		return pattern
		
	def __rowToDict(self, row, mapping):
		for key in mapping:
			if mapping[key] != '':
				mapping[key] = (None if row == None else row[mapping[key][1]])
		return mapping
	
	def __mapBooleanToDB(self, dict):
		if dict.get('is_active') != None:
			dict['is_active'] = (1 if dict['is_active'] else 0)
		if dict.get('is_deleted') != None:
			dict['is_deleted'] = (1 if dict['is_deleted'] else 0)
		return dict
	
	def __mapBooleanFromDB(self, dict):
		if dict.get('is_active') != None:
			dict['is_active'] = (True if dict['is_active'] == 1 else False)
		if dict.get('is_deleted') != None:
			dict['is_deleted'] = (True if dict['is_deleted'] == 1 else False)
		return dict

	def __search(self, table, dict, mapping, showDisabled = False):
		conditionString = ''
		regexpList = []
		for key in dict:
			if mapping.get(key) != None and dict[key] != None and dict[key] != '':
				conditionString += " AND " + mapping[key][0]
				escapedSearch = self.__regexEscape(str(dict[key]))
				exact = True
				if escapedSearch.startswith("%") and escapedSearch.endswith("%"):
					exact = False
					escapedSearch = escapedSearch[1:-1]
				searchList = escapedSearch.split(" ")
				cleanList = []
				for pattern in searchList:
					if pattern != '' and pattern != None:
						cleanList.append(pattern)
				if exact:
					conditionString += " = %s"
					regexpList.append(str(dict[key]))
				else:
					conditionString += " REGEXP %s"
					regextmp = "^"
					combinations = util.Generator.indexCombination(len(cleanList))
					for combi in combinations:
						regextmp += "|((.)*"
						searchListCombination = []
						for index in combi:
							searchListCombination.append(cleanList[int(index)])
						for part in searchListCombination:
							if self.__regexUnescape(part) == "?" or self.__regexUnescape(part) == "!" \
								or self.__regexUnescape(part) == ".":
								regextmp += "([a-zA-Z0-9]| )" + part
								#len(searchListCombination) == 1 Aufhebung der Beschränkung bei mehreren Wörtern
							elif (len(self.__regexUnescape(part)) < 3)and table != 'User':
								regextmp += "([^a-zA-Z0-9]" + part + " |[^a-zA-Z0-9]" + part + " -)"
							else:
								regextmp += part
							regextmp += "(.)*"
						regextmp += ")"
					regexpList.append(regextmp.replace("^|", "^") + "$")
		conditionString = conditionString.replace(" AND ", "", 1)
		#print conditionString
		sql = "SELECT * FROM %s WHERE %s " % (table, conditionString) +  "AND " + self.getItemMapping()['is_deleted'][0] + " = '0'"
		if showDisabled == False:
			sql += " AND " + self.getItemMapping()['is_active'][0] + " = '1' "
		return (self.__getRows(sql, regexpList) if len(regexpList) > 0 else None)
		
	def __searchItemsFulltext(self, query, orderField = "rank", orderDirection = "DESC", limit = False, offset = 0, showDisabled = False):
		columnList = "(" + self.getItemMapping()['title'][0] + ", " + self.getItemMapping()['author'][0] + ", " + self.getItemMapping()['publisher'][0] + ", " + self.getItemMapping()['description'][0] \
			+ ", " + self.getItemMapping()['isbn13'][0] + ", " + self.getItemMapping()['issn'][0] + ")"
		subquery = "(SELECT sub." + self.getItemMapping()["sum_rating"][0] + " / sub." + self.getItemMapping()["num_rating"][0] + " FROM Items AS sub" + " WHERE sub." + self.getItemMapping()["id"][0] + " = i." + self.getItemMapping()["id"][0] + ") AS rating"
		sql = "SELECT *, " + subquery + ", MATCH " + columnList + " AGAINST (%s) AS rank FROM Items As i WHERE MATCH " + columnList + " AGAINST (%s) AND " + self.getItemMapping()['is_deleted'][0] + " = '0' "
		if showDisabled == False:
			sql += "AND " + self.getItemMapping()['is_active'][0] + " = '1' "
		if orderField == "rating"  or orderField == "rank" or self.getItemMapping().get(orderField) != None:
			if orderField == "rating":
				sql += "ORDER BY rating"
			elif orderField == "rank":
				sql += "ORDER BY rank"
			else:
				sql += "ORDER BY " + self.getItemMapping()[orderField][0]
			if orderDirection == "ASC":
				sql += " ASC"
			elif orderDirection == "DESC":
				sql += " DESC"
		if limit != False:
			sql += " LIMIT %s, %s"
			self.__execute(sql , (query, query, offset, limit))
		else:
			self.__execute(sql , (query, query))
		#print self.__cursor._executed
		rowlist = self.__fetchall()
		unicodeList = []
		for row in rowlist:
			unicodeList.append(self.__unicodeList(row))
		return unicodeList
		
	def __searchUsersFulltext(self, query, showDisabled = False):
		columnList = "(" + self.getUserMapping()['firstname'][0] + ", " + self.getUserMapping()['lastname'][0] + ", " + self.getUserMapping()['email'][0] + ")"
		sql = "SELECT *, MATCH " + columnList + " AGAINST (%s) AS rank FROM User WHERE MATCH " + columnList + " AGAINST (%s) AND " + self.getUserMapping()['is_deleted'][0] + " = '0' ORDER BY rank DESC"
		self.__execute(sql , (query, query))
		rowlist = self.__fetchall()
		unicodeList = []
		for row in rowlist:
			unicodeList.append(self.__unicodeList(row))
		return unicodeList
		
	def searchItem(self, dict):
		results = None
		if dict.get("query") != None:
			if util.RequestParser.dictHasKeys(dict, ('orderField', 'orderDirection')):
				if dict.get('limit') != None:
					results = self.__searchItemsFulltext(dict["query"], dict['orderField'], dict['orderDirection'], dict['limit'], (dict['offset'] if dict.get('offset') != None else 0), showDisabled = (False if dict.get("showDisabled") == None else dict.get("showDisabled")))
				else:
					results = self.__searchItemsFulltext(dict["query"], dict['orderField'], dict['orderDirection'], showDisabled = (False if dict.get("showDisabled") == None else dict.get("showDisabled")))
			elif dict.get('limit') != None:
				if util.RequestParser.dictHasKeys(dict, ('orderField', 'orderDirection')):
					results = self.__searchItemsFulltext(dict["query"], dict['orderField'], dict['orderDirection'], dict['limit'], (dict['offset'] if dict.get('offset') != None else 0), showDisabled = (False if dict.get("showDisabled") == None else dict.get("showDisabled")))
				else:
					results = self.__searchItemsFulltext(dict["query"], limit =  dict['limit'], offset = (dict['offset'] if dict.get('offset') != None else 0), showDisabled = (False if dict.get("showDisabled") == None else dict.get("showDisabled")))	
			else:
				results = self.__searchItemsFulltext(dict["query"], showDisabled = (False if dict.get("showDisabled") == None else dict.get("showDisabled")))
		elif dict.get("title") != None or dict.get("author") != None or dict.get("publisher") != None or dict.get("description") != None or dict.get("isbn") != None or dict.get("issn") != None:
			results = self.__search('Items', dict, self.getItemMapping(), showDisabled = (False if dict.get("showDisabled") == None else dict.get("showDisabled")))
		if results == None:
			return None
		itemList = []
		for item in results:
			itemList.append(self.__getCorrectItem(self.__rowToDict(item, self.getItemMapping())))
		return itemList
		
	def searchUser(self, dict):
		results = None
		if dict.get("query") != None:
			results = self.__searchUsersFulltext(dict["query"])
		elif dict.get("firstname") != None or dict.get("lastname") != None or dict.get("email") != None:
			results = self.__search('User', dict, self.getUserMapping())
		if results == None:
			return None
		userList = []
		for row in results:
			dict = self.__rowToDict(row, self.getUserMapping())
			user = (Admin(dict) if dict['is_admin'] == 1 else User(dict))
			user.is_active = (True if user.is_active == 1 else False)
			userList.append(user)
		return userList
    
	def __getCorrectItem(self, dict):
		dict = self.__mapBooleanFromDB(dict)
		if dict['type'] == 'Book':
			return Book(dict)
		elif dict['type'] == 'Magazine':
			return Magazine(dict)
		else:
			return OtherItem(dict)
	
	def getItem(self, id):
		if isinstance(id, int) or isinstance(id, long):
			row = self.__getRow("SELECT * FROM Items WHERE " + self.getItemMapping()['id'][0] + "= %s AND " + self.getItemMapping()['is_deleted'][0] + " = %s", (id, 0))
		else:
			row = None
		if row != None:
			return self.__getCorrectItem(self.__rowToDict(row, self.getItemMapping()))
		else:
			return None
			
	def getItemByIsbn(self, isbn):
		if util.RequestParser.isbnValid(isbn):
			row = self.__getRow("SELECT * FROM Items WHERE " + self.getItemMapping()['isbn13'][0] + "= %s AND " + self.getItemMapping()['is_deleted'][0] + " = %s", (isbn, 0))
		else:
			row = None
		if row != None:
			return self.__getCorrectItem(self.__rowToDict(row, self.getItemMapping()))
			
	def getItemByIssn(self, issn):
		if util.RequestParser.issnValid(issn):
			row = self.__getRow("SELECT * FROM Items WHERE " + self.getItemMapping()['issn'][0] + "= %s AND " + self.getItemMapping()['is_deleted'][0] + " = %s", (issn, 0))
		else:
			row = None
		if row != None:
			return self.__getCorrectItem(self.__rowToDict(row, self.getItemMapping()))
		
	def getItemCopy(self, copy_id):
		row = self.__getRow("SELECT * FROM ItemCopies WHERE " + self.getItemCopyMapping()['id'][0] + "= %s AND " + self.getItemCopyMapping()['is_deleted'][0] + " = %s", (copy_id, 0))
		if row != None:
			dict = self.__mapBooleanFromDB(self.__rowToDict(row, self.getItemCopyMapping()))
			dict['item'] = self.getItem(dict['item_id'])
			copy = ItemCopy(dict)
			return copy
		
	def getItemCopies(self, item):
		rows = self.__getRows("SELECT * FROM ItemCopies WHERE " + self.getItemCopyMapping()['item_id'][0] + "= %s AND " + self.getItemCopyMapping()['is_deleted'][0] + " = %s", (item.id, 0))
		itemList = []
		for row in rows:
			dict = self.__mapBooleanFromDB(self.__rowToDict(row, self.getItemCopyMapping()))
			dict['item'] = item
			itemList.append(ItemCopy(dict))
		return itemList
		
	def getUser(self, authKeyOrEmailOrID):
		query =  "SELECT * FROM User WHERE "
		valid = False
		if util.RequestParser.emailValid(authKeyOrEmailOrID):
			query += self.getUserMapping()['email'][0]
			valid = True
		elif util.RequestParser.hashValid(authKeyOrEmailOrID):
			query += self.getUserMapping()['authKey'][0]
			valid = True
		elif util.RequestParser.idValid(authKeyOrEmailOrID):
			query += self.getUserMapping()['id'][0]
			valid = True
		if valid:
			query += "=%s AND " + self.getUserMapping()['is_deleted'][0] + " = %s"
			row = self.__getRow(query, (authKeyOrEmailOrID, 0))
			if row != None:
				dict = self.__mapBooleanFromDB(self.__rowToDict(row, self.getUserMapping()))
				user = (Admin(dict) if dict['is_admin'] == 1 else User(dict))
				return user
		else:
			return None
		
	def getUsers(self):
		rows = self.__getRows("SELECT * FROM User WHERE " + self.getUserMapping()['is_deleted'][0] + " = '0'")
		userlist = []
		for user in rows:
			dict = self.__mapBooleanFromDB(self.__rowToDict(user, self.getUserMapping()))
			u = (Admin(dict) if dict['is_admin'] == 1 else User(dict))
			userlist.append(u.asJsonable())
		return userlist
	
	def getRating(self, user, item):
		row = self.__getRow("SELECT * FROM Ratings WHERE " + self.getRatingMapping()['user_id'][0]
				+ '="' + str(user.id) + '" AND ' + self.getRatingMapping()['item_id'][0] + '="' + str(item.id) + '"'
				+ ' AND ' + self.getRatingMapping()['is_deleted'][0] + " = '0'")
		if row != None:
			dict = self.__rowToDict(row, self.getRatingMapping())
			dict['user'] = user
			dict['item'] = item
			if dict['user'] and dict['item']:
				return Rating(dict)
		
	def getRatingById(self, ratingId):
		row = self.__getRow("SELECT * FROM Ratings WHERE " + self.getRatingMapping()['id'][0] + ' = %s AND '
		+ self.getRatingMapping()['is_deleted'][0] + " = '0'", ratingId)
		if row != None:
			dict = self.__rowToDict(row, self.getRatingMapping())
			dict['user'] = self.getUser(dict['user_id'])
			dict['item'] = self.getItem(dict['item_id'])
			if dict['user'] and dict['item']:
				return Rating(dict)
		
	def getRatings(self, item):
		rows = self.__getRows("SELECT * FROM Ratings WHERE " + self.getRatingMapping()['item_id'][0] + '="' + str(item.id) + '"'
			+ ' AND ' + self.getRatingMapping()['is_deleted'][0] + " = '0'")	
		ratingList = []
		for row in rows:
			dict = self.__rowToDict(row, self.getRatingMapping())
			dict['user'] = self.getUser(dict['user_id'])
			dict['item'] = item
			if dict['user'] and dict['item']:
				ratingList.append(Rating(dict))
		return ratingList
		
	def getLend(self, copy):
		row = self.__getRow("SELECT * FROM Lendbooks WHERE " + self.getLendMapping()['copy_id'][0] + '="' + str(copy.id) + '"'
				+ ' AND ' + self.getLendMapping()['return_time'][0] + ' IS NULL')
		if row != None:
			dict = self.__rowToDict(row, self.getLendMapping())
			dict['user'] = self.getUser(dict['user_id'])
			dict['copy'] = copy
			if dict['user'] and dict['copy']:
				return Lend(dict)
		
	def getBorrowedCopies(self, user):
		rows = self.__getRows("SELECT * FROM Lendbooks WHERE " + self.getLendMapping()['user_id'][0] + '="' + str(user.id) + '"'
			+ ' AND ' + self.getLendMapping()['return_time'][0] + ' IS NULL')
		copyList = []
		for row in rows:
			dict = self.__rowToDict(row, self.getLendMapping())
			if self.getItemCopy(dict['copy_id']) != None:
				copyList.append(self.getItemCopy(dict['copy_id']))
		return copyList
	
	def createUser(self, user):
		dict = self.__mapBooleanToDB(user.asSerialised())
		dict['is_admin'] = (1 if isinstance(user, Admin) else 0)
		return self.__insertRow('User', dict, self.getUserMapping())
			
	def modifyUser(self, user):
		dict = self.__mapBooleanToDB(user.asSerialised())
		dict['is_admin'] = (1 if isinstance(user, Admin) else 0)
		self.__updateRow('User', dict, self.getUserMapping(), 'id')
		return user.id

	def createItem(self, item):
		return self.__insertRow('Items', self.__mapBooleanToDB(item.asSerialised()), self.getItemMapping())
		
	def modifyItem(self, item):
		self.__updateRow('Items', self.__mapBooleanToDB(item.asSerialised()), self.getItemMapping(), 'id')
		return item.id
		
	def createLend(self, lend):
		dict = lend.asSerialised()
		return self.__insertRow('Lendbooks', dict, self.getLendMapping())
		
	def modifyLend(self, lend):
		self.__updateRow('Lendbooks', lend.asSerialised(), self.getLendMapping(), 'id')
		return lend.id
		
	def createRating(self, rating):
		return self.__insertRow('Ratings', rating.asSerialised(), self.getRatingMapping())
		
	def modifyRating(self, rating):
		dict = rating.asSerialised()
		del dict['creation_time']
		self.__updateRow('Ratings', dict, self.getRatingMapping(), 'id')
		return rating.id
		
	def createItemCopy(self, itemCopy):
		return self.__insertRow('ItemCopies', self.__mapBooleanToDB(itemCopy.asSerialised()), self.getItemCopyMapping())
		
	def modifyItemCopy(self, itemCopy):
		dict = itemCopy.asSerialised()
		del dict['creation_time']
		self.__updateRow('ItemCopies', self.__mapBooleanToDB(dict), self.getItemCopyMapping(), 'id')
		return itemCopy.id
	
	def __del__(self):
		self.__closeConnection()