package edu.kit.tm.telematics.pocketbib.model.library;

import junit.framework.Assert;

/**
 * International Standard Book Number (ISBN) is a unique numeric commercial book
 * identifier.
 * 
 * @see Book
 */
public class Isbn {

	/** the raw, valid ISBN-13, e.g. 9783836218023 */
	private final String isbn13;

	/**
	 * The length of a valid ISBN-10.
	 */
	private static final int ISBN10_LENGTH = 10;

	/**
	 * The length of a valid ISBN-13.
	 */
	private static final int ISBN13_LENGTH = 13;

	@SuppressWarnings("unused")
	/** Tag for debugging purposes. */
	private static final String TAG = "Isbn";
	
	/**
	 * Calculates the checksum for the given raw, valid ISBN-10.
	 * 
	 * @param rawIsbn10
	 *            the ISBN-10 (without hyphens and whitespace)
	 * @return the digit of the checksum
	 * @throws NumberFormatException
	 *             if string cannot be parsed as an integer value
	 */
	public static int calculateIsbn10Checksum(String rawIsbn10) {
		Assert.assertTrue("rawIsbn10 is null", rawIsbn10 != null);
		Assert.assertTrue(rawIsbn10 + " is of invalid length", rawIsbn10.length() == ISBN10_LENGTH - 1 || rawIsbn10.length() == ISBN10_LENGTH);
//		Log.v(TAG, "Calculating checksum for " + rawIsbn10);
		int sum = 0;
		for (int digitPos = 0; digitPos < ISBN10_LENGTH - 1; digitPos++) {
			char c = rawIsbn10.charAt(digitPos);
			Assert.assertTrue("invalid character: " + c, Character.isDigit(c));
			int n = Integer.parseInt("" + c);
			int weight = digitPos + 1;
			sum += n * weight;
		}
		return sum % 11;
	}

	/**
	 * Calculates the checksum for the given valid ISBN-13.
	 * 
	 * @param rawIsbn13
	 *            the ISBN-13 (without hyphens and whitespace)
	 * @return the digit of the checksum
	 * @throws NumberFormatException
	 *             if string cannot be parsed as an integer value
	 */
	public static int calculateIsbn13Checksum(String rawIsbn13) {
		Assert.assertTrue("rawIsbn13 is null", rawIsbn13 != null);
		Assert.assertTrue(rawIsbn13 + " is of invalid length", rawIsbn13.length() == ISBN13_LENGTH - 1 || rawIsbn13.length() == ISBN13_LENGTH);

		int sum = 0;
		for (int digitPos = 0; digitPos < ISBN13_LENGTH - 1; digitPos++) {
			char c = rawIsbn13.charAt(digitPos);
			Assert.assertTrue(Character.isDigit(c));
			int n = Integer.parseInt("" + c);
			int weight = (digitPos % 2 == 0) ? 1 : 3;
			sum += n * weight;
		}
		int checksum = sum % 10;
		return checksum == 0 ? checksum : 10 - checksum;
	}

	/**
	 * Returns the checksum digit as a <code>char</code>.
	 * 
	 * @return <code>'X'</code> if the checksum digit is 10; the
	 *         <code>char</code> representation of the digit otherwise
	 */
	protected static char convertDigitToChar(int checksum) {
		assert checksum >= 0 && checksum <= 10;
		return checksum == 10 ? 'X' : Character.forDigit(checksum, 10);
	}

	/**
	 * Converts a valid ISBN-10 (e.g. 383621802X) to the corresponding ISBN-13.
	 * 
	 * @param rawIsbn10
	 *            the ISBN-10 (without hyphens and spaces)
	 * @return the ISBN-13 if it exits; <code>null</code> otherwise
	 */
	protected static String convertIsbn10To13(String rawIsbn10) {
		if (validIsbn10(rawIsbn10)) {
			String isbn13 = "978" + rawIsbn10.substring(0, ISBN10_LENGTH - 1);
			isbn13 += calculateIsbn13Checksum(isbn13);
			return isbn13;
		} else {
			return null;
		}

	}

	/**
	 * Converts a valid ISBN-13 (e.g. 9780306406157) to the corresponding
	 * ISBN-10.
	 * 
	 * @param rawIsbn13
	 *            the ISBN-10 (without hyphens and whitespace)
	 * @return the ISBN-10 if it exits; <code>null</code> otherwise
	 */
	protected static String convertIsbn13To10(String rawIsbn13) {
		if (validIsbn13(rawIsbn13) && rawIsbn13.matches("^978[0-9]{9}[0-9X]{1}$")) {
			String isbn10 = rawIsbn13.substring(3, ISBN13_LENGTH - 1);
			isbn10 += calculateIsbn10Checksum(isbn10);
			return isbn10;
		} else {
			return null;
		}

	}

	/**
	 * Strips all whitespace characters and hyphens from the text
	 * 
	 * @param text
	 *            the original text
	 * @return the stripped text; <code>null</code> if the original text is
	 *         <code>null</code>
	 */
	public static String stripText(String text) {
		if (text != null) {
			return text.replaceAll("-|\\s", "").toUpperCase();
		} else {
			return null;
		}
	}

	/**
	 * Validates the format of the ISBN-10.
	 * 
	 * @param rawIsbn
	 *            the ISBN-10 (without hyphens and whitespace)
	 * @return <code>true</code> if the ISBN-10 is valid; <code>false</code>
	 *         otherwise
	 */
	public static boolean validIsbn10(final String rawIsbn) {
		if (rawIsbn != null && rawIsbn.length() == ISBN10_LENGTH && rawIsbn.matches("^[0-9]{9}[0-9X]{1}$")) {
			char checksum = convertDigitToChar(calculateIsbn10Checksum(rawIsbn));
			return checksum == rawIsbn.charAt(ISBN10_LENGTH - 1);
		} else {
			return false;
		}
	}

	/**
	 * Validates the format of the ISBN-13
	 * 
	 * @param rawIsbn
	 *            the ISBN-13 (without hyphens and whitespace)
	 * @return <code>true</code> if the ISBN-13 is valid; <code>false</code>
	 *         otherwise
	 */
	public static boolean validIsbn13(final String rawIsbn) {
		if (rawIsbn != null && rawIsbn.length() == ISBN13_LENGTH && rawIsbn.matches("^(978|979)[0-9]{9}[0-9X]{1}$")) {
			char checksum = convertDigitToChar(calculateIsbn13Checksum(rawIsbn));
			return checksum == rawIsbn.charAt(ISBN13_LENGTH - 1);
		} else {
			return false;
		}
	}

	/**
	 * Creates an ISBN from the given ISBN-10 or ISBN-13 string. Use
	 * {@link #stripText(String)} to strip unwanted characters from the text
	 * before using this constructor.
	 * 
	 * @param isbn
	 *            the ISBN-10 or ISBN-13
	 * @throws IllegalArgumentException
	 *             thrown if the ISBN has the wrong length, format or checksum.
	 */
	public Isbn(String inputIsbn) {
		if (!validIsbn10(inputIsbn) && !validIsbn13(inputIsbn)) {
			throw new IllegalArgumentException("Invalid ISBN format: " + inputIsbn);
		} else if (inputIsbn.length() == ISBN10_LENGTH) {
			isbn13 = convertIsbn10To13(inputIsbn);
			Assert.assertTrue("isbn13 is null", isbn13 != null);
		} else {
			isbn13 = inputIsbn;
		}
	}
	
	/**
	 * Creates an isbn object if possible from the string.
	 * @param rawString the string that is checked
	 * @return the isbn if it is a valid ISBN-10 or ISBN-13 (even with hyphens); <code>null</code> otherwise
	 */
	public static Isbn initIsbn(String rawString) {
		Isbn isbn = null;
		String stripped = stripText(rawString);
		if (Isbn.validIsbn10(stripped)) {
			stripped = Isbn.convertIsbn10To13(stripped);
		}
		if (Isbn.validIsbn13(stripped)) {
			isbn = new Isbn(stripped);
		}
		return isbn;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Isbn) {
			Isbn i = (Isbn) o;
			return isbn13.equals(i.isbn13);
		} else {
			return false;
		}
	}

	/**
	 * Returns the unformatted ISBN-10.
	 * 
	 * @return the unformatted ISBN-10; <code>null</code> if no ISBN-10 exists
	 */
	public String getIsbn10() {
		return convertIsbn13To10(isbn13);
	}

	/**
	 * Returns the unformatted ISBN-13.
	 * 
	 * @return the unformatted ISBN-13.
	 */
	public String getIsbn13() {
		return isbn13;
	}

	/**
	 * Returns a more human readable format of this ISBN-10.
	 * 
	 * @return a more human readable format of this ISBN-10; <code>null</code>
	 *         if no ISBN-10 exists
	 */
	public String getReadableIsbn10() {
		String isbn10 = convertIsbn13To10(isbn13);
		if (isbn10 == null)
			return "";
		
		return isbn10.substring(0, 2)				// Group
				+ "-" + isbn10.substring(2, 6)		// Publisher
				+ "-" + isbn10.substring(6, 9)		// Title
				+ "-" + isbn10.substring(9);		// Check digit
	}

	/**
	 * Returns a human readable format of this ISBN-13, e.g. 978-3836218023.
	 * 
	 * @return a human readable format of this ISBN-13.
	 */
	public String getReadableIsbn13() {
		return isbn13.substring(0, 3)				// EAN
				+ "-" + isbn13.substring(3, 4)		// Group
				+ "-" + isbn13.substring(4, 6)		// Publisher
				+ "-" + isbn13.substring(6, 12)		// Title
				+ "-" + isbn13.substring(12);		// Check digit
	}

	@Override
	public int hashCode() {
		return Integer.parseInt(isbn13);
	}

	@Override
	public String toString() {
		return getReadableIsbn13();
	}

}