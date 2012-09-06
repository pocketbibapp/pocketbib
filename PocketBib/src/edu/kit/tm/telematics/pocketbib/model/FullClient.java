package edu.kit.tm.telematics.pocketbib.model;

import edu.kit.tm.telematics.pocketbib.model.library.managment.BookInformationProvider;
import edu.kit.tm.telematics.pocketbib.model.library.managment.LibraryManager;
import edu.kit.tm.telematics.pocketbib.model.library.managment.MagazineInformationProvider;
import edu.kit.tm.telematics.pocketbib.model.user.RegistrationProvider;

public interface FullClient extends LibraryManager, RegistrationProvider, BookInformationProvider, MagazineInformationProvider {

}
