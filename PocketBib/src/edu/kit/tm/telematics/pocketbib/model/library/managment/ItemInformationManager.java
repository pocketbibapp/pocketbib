package edu.kit.tm.telematics.pocketbib.model.library.managment;

import java.util.ArrayList;

import edu.kit.tm.telematics.pocketbib.model.Constants;
import edu.kit.tm.telematics.pocketbib.model.library.Book;
import edu.kit.tm.telematics.pocketbib.model.library.Isbn;
import edu.kit.tm.telematics.pocketbib.model.library.Issn;
import edu.kit.tm.telematics.pocketbib.model.library.Magazine;

/**
 * The class that manages all the information provider.
 */
public class ItemInformationManager implements BookInformationProvider, MagazineInformationProvider {

	/** the list with all the added {@link BookInformationProvider} */
	private ArrayList<BookInformationProvider> bookProviderList;
	
	/** the list with all the added {@link MagazineInformationProvider} */
	private ArrayList<MagazineInformationProvider> magazineProviderList;
	
	/**
	 * Constructs the manager
	 */
	public ItemInformationManager() {
		bookProviderList = new ArrayList<BookInformationProvider>();
		magazineProviderList = new ArrayList<MagazineInformationProvider>();
	}
	
	/**
	 * Adds a book information provider (if it isn't already managed).
	 * 
	 * @param provider
	 *            the provider that is added
	 * @return <code>true</code> if the provider was added, <code>false</code>
	 *         if the provider was already managed beforehand or is <code>null</code>
	 */
	public boolean addBookInformationProvider(BookInformationProvider provider) {
		if (!bookProviderList.contains(provider) || provider == null) {
			bookProviderList.add(provider);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Adds a magazine information provider (if it isn't already managed).
	 * 
	 * @param provider
	 *            the provider that is added
	 * @return <code>true</code> if the provider was added, <code>false</code>
	 *         if the provider was already managed beforehand or is <code>null</code>
	 */
	public boolean addMagazineInformationProvider(MagazineInformationProvider provider) {
		if (!magazineProviderList.contains(provider) || provider == null) {
			magazineProviderList.add(provider);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the book with the given ISBN number.
	 * 
	 * @param isbn
	 *            the ISBN number of the book
	 * @return the book
	 */
	public Book getBook(Isbn isbn) {
		ArrayList<Book> results = new ArrayList<Book>(bookProviderList.size());
		
		// if there are many providers it might be better to do it in parallel
		for (BookInformationProvider provider : bookProviderList) {
			Book result = provider.getBook(isbn);
			if (result != null) {
				results.add(result);
			}
		}
		
		// takes the first non-null value for every attribute and creates a book this way
		Book b = Book.createNew();
		for (Book result : results) {
			if (b.getAuthor() == null && result.getAuthor() != null)
				b.setAuthor(result.getAuthor());
			if (b.getDescription() == null && result.getDescription() != null)
				b.setDescription(result.getDescription());
			if (b.getEdition() == null && result.getEdition() != null)
				b.setEdition(result.getEdition());
			if (b.getIsbn() == null && result.getIsbn() != null)
				b.setIsbn(result.getIsbn());
			if (b.getPageCount() == Constants.NOT_SAVED)
				b.setPageCount(result.getPageCount());
			if (b.getPrice() == Constants.NOT_SAVED)
				b.setPrice(result.getPrice());
			if (b.getDetailUrl() == null && result.getDetailUrl() != null)
				b.setDetailUrl(result.getDetailUrl());
			if (b.getPosition() == null && result.getPosition() != null)
				b.setPosition(result.getPosition());
			if (result.getPublicationYear() != Constants.NOT_SAVED)
				b.setPublicationYear(result.getPublicationYear());
			if (b.getPublisher() == null && result.getPublisher() != null)
				b.setPublisher(result.getPublisher());
			if (b.getTitle() == null && result.getTitle() != null)
				b.setTitle(result.getTitle());
		}
		return b;
	}

	/**
	 * Returns the magazine with the given ISSN number.
	 * 
	 * @param issn
	 *            the ISSN number of the magazine
	 * @return the magazine
	 */
	public Magazine getMagazine(Issn issn) {
		ArrayList<Magazine> results = new ArrayList<Magazine>(magazineProviderList.size());

		for (MagazineInformationProvider provider : magazineProviderList) {
			Magazine result = provider.getMagazine(issn);
			assert result != null;
			results.add(result);
		}

		Magazine m = Magazine.createNew();
		for (Magazine result : results) {
			if (m.getDescription() == null && result.getDescription() != null)
				m.setDescription(result.getDescription());
			if (m.getEdition() == null && result.getEdition() != null)
				m.setEdition(result.getEdition());
			if (m.getIssn() == null && result.getIssn() != null)
				m.setIssn(result.getIssn());
			if (m.getPageCount() == Constants.NOT_SAVED && result.getPageCount() != 0)
				m.setPageCount(result.getPageCount());
			if (m.getPrice() == Constants.NOT_SAVED && result.getPrice() != Constants.NOT_SAVED)
				m.setPrice(result.getPrice());
			if (m.getDetailUrl() == null && result.getDetailUrl() != null)
				m.setDetailUrl(result.getDetailUrl());
			if (m.getPosition() == null && result.getPosition() != null)
				m.setPosition(result.getPosition());
			if (m.getPublicationDate() == null && result.getPublicationDate() != null)
				m.setPublicationDate(result.getPublicationDate());
			if (m.getPublisher() == null && result.getPublisher() != null)
				m.setPublisher(result.getPublisher());
			if (m.getTitle() == null && result.getTitle() != null)
				m.setTitle(result.getTitle());
		}
		return m;
	}

	/**
	 * Deletes a book information provider.
	 * 
	 * @param provider
	 *            the provider that is deleted
	 * @return <code>true</code> if the provider was removed, <code>false</code>
	 *         if the provider wasn't managed beforehand and therefore couldn't
	 *         be deleted
	 */
	public boolean removeBookInformationProvider(BookInformationProvider provider) {
		return bookProviderList.remove(provider);
	}

	/**
	 * Deletes a magazine information provider.
	 * 
	 * @param prover
	 *            the provider that is deleted
	 * @return <code>true</code> if the provider was removed, <code>false</code>
	 *         if the provider wasn't managed beforehand and therefore couldn't
	 *         be deleted
	 */
	public boolean removeMagazineInformationProvider(MagazineInformationProvider provider) {
		return magazineProviderList.remove(provider);
	}

}
