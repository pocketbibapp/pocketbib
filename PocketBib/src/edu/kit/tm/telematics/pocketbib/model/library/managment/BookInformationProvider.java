package edu.kit.tm.telematics.pocketbib.model.library.managment;

import edu.kit.tm.telematics.pocketbib.model.library.Book;
import edu.kit.tm.telematics.pocketbib.model.library.Isbn;
/**
 * Provider class for book informations.
 * 
 */
public interface BookInformationProvider {
/**
 * Returns the Information for the book with the ISBN number.
 * @param isbn ISBN number of the specific book.
 * @return the book information
 */
	public Book getBook(Isbn isbn);
}