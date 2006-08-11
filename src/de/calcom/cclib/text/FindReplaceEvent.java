package de.calcom.cclib.text;

import java.util.EventObject;

/**
 * This is simply overriding EventObject by storing a FindReplaceDialog
 * into the source field of the superclass.
 *
 * <p>Added i18n support for application SimplyHTML in version 1.5</p>
 *
 * @author Ulrich Hilger
 * @author CalCom
 * @author <a href="http://www.calcom.de">http://www.calcom.de</a>
 * @author <a href="mailto:info@calcom.de">info@calcom.de</a>
 * @version 1.5, April 27, 2003
 */

public class FindReplaceEvent extends EventObject {

  public FindReplaceEvent(FindReplaceDialog source) {
    super(source);
  }
}
