package org.finos.springbot.teams.history;

import java.util.Map;
import java.util.Optional;

import org.finos.springbot.teams.content.TeamsAddressable;
import org.finos.springbot.workflow.history.PlatformHistory;

/**
 * In teams, data isn't stored in messages, it's kept in a third party store like 
 * Azure Blob Storage or a database.  For that reason, we need a storage method 
 * on this interface to allow data to be written back to the store.
 * 
 * @author rob@kite9.com
 *
 */
public interface TeamsHistory extends PlatformHistory<TeamsAddressable> {
	
	/**
	 * Returns a unique ID that can be used to retrieve/store data.
	 */
	public String createStorageId();

	public void store(String storageId, TeamsAddressable a, Map<String, Object> data);
	
	public <X> Optional<Map<String, Object>> retrieve(String storageId, TeamsAddressable a);
	
}
