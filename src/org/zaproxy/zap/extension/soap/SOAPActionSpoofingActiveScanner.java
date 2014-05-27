/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.soap;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.AbstractAppPlugin;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;

/**
 * SOAP Action Spoofing Active Scanner
 * @author Alberto
 */
public class SOAPActionSpoofingActiveScanner extends AbstractAppPlugin {

	private static final String MESSAGE_PREFIX = "soap.soapactionspoofing.";

	private static Logger log = Logger.getLogger(SOAPActionSpoofingActiveScanner.class);
	

	
	
	@Override
	public int getId() {
		/*
		 * This should be unique across all active and passive rules.
		 * The master list is http://code.google.com/p/zaproxy/source/browse/trunk/src/doc/alerts.xml
		 */
		return 90026;
	}

	@Override
	public String getName() {
		return Constant.messages.getString(MESSAGE_PREFIX + "name");
	}

	public String getDescription() {
		return Constant.messages.getString(MESSAGE_PREFIX + "desc");
	}

	public String getSolution() {
		return Constant.messages.getString(MESSAGE_PREFIX + "soln");
	}

	public String getReference() {
		return Constant.messages.getString(MESSAGE_PREFIX + "refs");
	}
	
	@Override
	public String[] getDependency() {
		return null;
	}

	@Override
	public int getCategory() {
		return Category.MISC;
	}

	@Override
	public void init() {

	}

	/*
	 * This method is called by the active scanner for each GET and POST parameter for every page 
	 * @see org.parosproxy.paros.core.scanner.AbstractAppParamPlugin#scan(org.parosproxy.paros.network.HttpMessage, java.lang.String, java.lang.String)
	 */
	@Override
	public void scan() {
		try {
			/* Retrieves a good request. */
			HttpMessage msg = getNewMsg();
			
			/* Modifies the request to try an attack. */
			
			/*ExtensionImportWSDL wsdlHandler = (ExtensionImportWSDL) Control.getSingleton().getExtensionLoader().getExtension(ExtensionImportWSDL.NAME);		
			if(wsdlHandler == null){
				log.warn(this.getName()+" could not load the wsdlHandler extension");
				return;
			}
			String[][] soapOperations = wsdlHandler.getSoapOperations();*/	
			
			String[][] soapOperations = ImportWSDL.getInstance().getSoapOperations();
			
			for(int i = 0; i < soapOperations.length; i++){
				boolean vulnerable = false;
				for(int j = 0; j < soapOperations[i].length && !vulnerable; j++){
					HttpRequestHeader header = msg.getRequestHeader();
					/* Available ops should be known here from the imported WSDL file. */
					header.setHeader("SOAPAction", soapOperations[i][j]);
					
					/* Sends the modified request. */
					sendAndReceive(msg);
					
					/* Checks the response. */
					String responseContent = new String(msg.getResponseBody().getBytes());
					
					/* Raises an alert when necessary. */
					if (responseContent.contains("soapenv")) {
				   		bingo(Alert.RISK_LOW, Alert.WARNING, null, null, "soapenv", null, msg);
						vulnerable = true;
					}
				}
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}	
	}

	@Override
	public int getRisk() {
		return Alert.RISK_HIGH;
	}

	@Override
	public int getCweId() {
		// The CWE id
		return 0;
	}

	@Override
	public int getWascId() {
		// The WASC ID
		return 0;
	}
}