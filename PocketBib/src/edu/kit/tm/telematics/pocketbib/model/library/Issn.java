package edu.kit.tm.telematics.pocketbib.model.library;

import junit.framework.Assert;

/**
 * International Standard Serial Number (ISSN) is a unique eight-digit number
 * used to identify a print or electronic periodical publication.
 * 
 * @see Magazine
 */
public class Issn {

	/**
	 * The ISSN.
	 */
	private final String issn;

	/**
	 * The length for a valid ISSN.
	 */
	private static final int ISSN_LENGTH = 8;

	/**
	 * Calculates the checksum for the given valid ISSN.
	 * 
	 * @param issn
	 *            the ISSN (without hyphens and whitespace) *
	 * @return the digit of the checksum
	 * @throws NumberFormatException
	 *             if string cannot be parsed as an integer value
	 */
	public static int calculateChecksum(String issn) {
		Assert.assertNotNull(issn);
		Assert.assertTrue(issn.length() == ISSN_LENGTH - 1 || issn.length() == ISSN_LENGTH);

		int sum = 0;
		for (int digitPos = 0; digitPos < ISSN_LENGTH - 1; digitPos++) {
			int n = Integer.parseInt("" + issn.charAt(digitPos));
			int weight = ISSN_LENGTH - digitPos;
			sum += n * weight;
		}
		int checksum = sum % 11;
		return checksum == 0 ? checksum : 11 - checksum;
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
	 * Returns if this ISSN is of a valid format.
	 * 
	 * @return <code>true</code> if the ISSN is 9 characters long and the
	 *         checksum is valid; <code>false</code> otherwise
	 */
	public static boolean validIssn(String issn) {
		if (issn != null && issn.length() == ISSN_LENGTH) {
			char checksum = convertDigitToChar(calculateChecksum(issn));
			return checksum == issn.charAt(ISSN_LENGTH - 1);
		} else {
			return false;
		}
	}

	/**
	 * Creates an ISSN from the given <code>string</code>. Use
	 * {@link #stripText(String)} to strip unwanted characters from the text
	 * before using this constructor.
	 * 
	 * @param issn
	 *            the ISSN
	 * @throws IllegalArgumentException
	 *             thrown if the ISSN has the wrong length, format or checksum.
	 */
	public Issn(String issn) {
		if (validIssn(issn)) {
			this.issn = issn;
		} else {
			throw new IllegalArgumentException("Invalid issn: " + issn);
		}
	}
	
	/**
	 * Creates an issn object if possible from the string.
	 * @param rawString the string that is checked
	 * @return the issn if it is valid (even with hyphens); <code>null</code> otherwise
	 */
	public static Issn initIssn(String rawString) {
		Issn issn = null;
		String stripped = stripText(rawString);
		if (Issn.validIssn(stripped)) {
			issn = new Issn(stripped);
		}
		return issn;
	}


	@Override
	public boolean equals(Object o) {
		if (o instanceof Issn) {
			Issn i = (Issn) o;
			return issn.equals(i.issn);
		} else {
			return false;
		}
	}

	/**
	 * Returns the ISSN.
	 * 
	 * @return the ISSN
	 */
	public String getIssn() {
		return issn;
	}

	/**
	 * Returns the ISSN in a more human readable format (e.g "0317-8471").
	 * 
	 * @return ISSN in more human readable format
	 */
	public String getReadableIssn() {
		return String.format("%s-%s", issn.substring(0, 4), issn.substring(4));
	}

	@Override
	public int hashCode() {
		return Integer.parseInt(issn);
	}
	
	@Override
	public String toString() {
		return getReadableIssn();
	}
}
