#!/usr/bin/env python2
# -*- coding: utf-8 -*-

import MySQLdb

connection = MySQLdb.connect(host = "localhost", user= "root", passwd = "test", db = "bib.tm.kit.edu")
cursor = connection.cursor()
#sqllist = ("UPDATE Items SET type = 'OtherItem'", "UPDATE Items SET isbn = NULL WHERE isbn = 'NULL' OR isbn = ''",
#	"UPDATE Items SET issn = NULL WHERE issn = 'NULL' OR issn = ''", "UPDATE Items SET type = 'Magazine' WHERE issn IS NOT NULL",
#	 "UPDATE Items SET type = 'Book' WHERE isbn IS NOT NULL")
#for sql in sqllist:
#	cursor.execute(sql)
cursor.execute("SELECT biblionumber FROM Items")
for row in cursor.fetchall():
	cursor.execute("INSERT INTO ItemCopies (item_id, is_active, lend_id, is_deleted) VALUES (%s, 1, -1, 0)", row[0])
connection.close()