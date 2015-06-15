/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.esb.nhttp.transport.test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import org.apache.axiom.om.OMElement;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.ESBTestCaseUtils;

/**
 * Class for test the content length header of the response in NHTTP transport
 */
public class ESBJAVA3774HeadMethodContentLengthTestCase extends ESBIntegrationTest {

	private ServerConfigurationManager serverConfigurationManager;
	private HttpServer server = null;

	@BeforeClass(alwaysRun = true)
	public void setEnvironment() throws Exception {
		super.init();
		serverConfigurationManager = new ServerConfigurationManager(context);
		serverConfigurationManager.applyConfiguration(new File(getESBResourceLocation() + File.separator
		                                                       + "nhttp" + File.separator + "transport" +
		                                                       File.separator + "ESBJAVA3774" + File.separator +
		                                                       "axis2.xml"));
		super.init();
		// load the proxy config
		String relativePath = "artifacts" + File.separator + "ESB" +
		                      File.separator + "nhttp" + File.separator + "transport" +
		                      File.separator + "ESBJAVA3774" +
		                      File.separator + "ESBJAVA3774HeadMethodContentLengthTestSynapse.xml";
		ESBTestCaseUtils util = new ESBTestCaseUtils();
		relativePath = relativePath.replaceAll("[\\\\/]", File.separator);
		OMElement apiConfig = util.loadResource(relativePath);
		addApi(apiConfig);
	}

	@Test(groups = "wso2.esb", description = " Checking Http HEAD method response content length in NHTTP")
	public void testContentOfHEADRequest() throws Exception {

		server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.createContext("/headertest", new TestHTTPHandler());
		server.setExecutor(null); // creates a default executor
		server.start();

		String restURL = "http://localhost:8280/wheather/test";
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpHead httpHead = new HttpHead(restURL);
		HttpResponse response = httpclient.execute(httpHead);
		Header contentLength = response.getFirstHeader(HTTP.CONTENT_LEN);
		Assert.assertEquals(Integer.parseInt(contentLength.getValue()), 2500,
		                    "There is problem with obtaining Content length");

		server.stop(0);
	}

	/**
	 * Inner class for handle the given request HEAD and generate an appropriate response.
	 */
	private class TestHTTPHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			String response = "";
			t.sendResponseHeaders(200, 2500);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	@AfterClass(alwaysRun = true)
	public void destroy() throws Exception {
		super.cleanup();
		serverConfigurationManager.restoreToLastConfiguration();

	}
}
