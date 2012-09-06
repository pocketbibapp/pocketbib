package edu.kit.tm.telematics.pocketbib.model.library.managment;

import edu.kit.tm.telematics.pocketbib.model.library.Issn;

import edu.kit.tm.telematics.pocketbib.model.library.Magazine;

/**
 * The Provider class for magazine informations.
 *
 */
public interface MagazineInformationProvider {
	/**
	 * Returns the Information for the magazine with the ISSN number.
	 * @param issn ISSN number of the specific magazine
	 * @return the magazine information
	 */
	public Magazine getMagazine(Issn issn);
}
