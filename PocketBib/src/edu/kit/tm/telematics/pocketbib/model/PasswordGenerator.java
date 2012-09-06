package edu.kit.tm.telematics.pocketbib.model;

import java.security.SecureRandom;

/**
 * A generator for random passwords.
 */
public class PasswordGenerator {

	/** usable characters for the password. Hard distinguishable symbols like l, I, 0 and O are excluded. */
	private final static char[] CHARACTERS = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'o',
			'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
			'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7',
			'8', '9' };

	/**
	 * Generates a random password.
	 * 
	 * @param minLength
	 *            the minimal length (inclusive)
	 * @param maxLength
	 *            the maximum length (inclusive)
	 * @return a random password
	 */
	public static String generatePassword(int minLength, int maxLength) {
		SecureRandom rand = new SecureRandom();
		int length = minLength + rand.nextInt(maxLength - minLength + 1);
		
		StringBuilder builder = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			builder.append(CHARACTERS[rand.nextInt(CHARACTERS.length)]);
		}
		
		return builder.toString();
	}
	
	/*
	 * No constructor in utility classes!
	 */
	private PasswordGenerator() {
		throw new UnsupportedOperationException();
	}

}
