
package org.wargamer2010.signshop.timing;

import java.util.Map;

/**
 * Interface for operations that expire after a set time.
 */
public interface IExpirable {
    /**
     * Gets the name of the Expirable
     * @return
     */
    String getName();

    /**
     * Initialise Expirable based on string
     * @param entry entry to parse
     * @return whether parsing succeeded
     */
    boolean parseEntry(Map<String, String> entry);

    /**
     * Returns all data belonging to the IExpirable in a Map
     * which can be written to file
     * @return all values of the IExpirable that need to be persisted
     */
    Map<String, String> getEntry();
}
