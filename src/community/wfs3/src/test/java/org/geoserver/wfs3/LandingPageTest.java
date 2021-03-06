/* (c) 2018 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.jayway.jsonpath.DocumentContext;
import java.util.List;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;

public class LandingPageTest extends WFS3TestSupport {

    @Test
    public void testLandingPageNoSlash() throws Exception {
        DocumentContext json = getAsJSONPath("wfs3", 200);
        checkJSONLandingPage(json);
    }

    @Test
    public void testLandingPageSlash() throws Exception {
        DocumentContext json = getAsJSONPath("wfs3/", 200);
        checkJSONLandingPage(json);
    }

    @Test
    public void testLandingPageJSON() throws Exception {
        DocumentContext json = getAsJSONPath("wfs3?f=json", 200);
        checkJSONLandingPage(json);
    }

    private void checkJSONLandingPage(DocumentContext json) {
        assertEquals(13, (int) json.read("links.length()", Integer.class));
        // check landing page links
        assertJSONList(
                json,
                "links[?(@.type == 'application/json' && @.href =~ /.*wfs3\\/\\?.*/)].rel",
                "self");
        assertJSONList(
                json,
                "links[?(@.type != 'application/json' && @.href =~ /.*wfs3\\/\\?.*/)].rel",
                "service",
                "service",
                "service");
        // check API links
        assertJSONList(
                json, "links[?(@.href =~ /.*wfs3\\/api.*/)].rel", "service", "service", "service");
        // check conformance links
        assertJSONList(
                json,
                "links[?(@.href =~ /.*wfs3\\/conformance.*/)].rel",
                "service",
                "service",
                "service");
        // check collection links
        assertJSONList(
                json,
                "links[?(@.href =~ /.*wfs3\\/collections.*/)].rel",
                "service",
                "service",
                "service");
    }

    private <T> void assertJSONList(DocumentContext json, String path, T... expected) {
        List<T> selfRels = json.read(path);
        assertThat(selfRels, Matchers.containsInAnyOrder(expected));
    }

    @Test
    public void testLandingPageWorkspaceSpecific() throws Exception {
        DocumentContext json = getAsJSONPath("cdf/wfs3/", 200);
        checkJSONLandingPage(json);
    }

    @Test
    public void testLandingPageXML() throws Exception {
        Document dom = getAsDOM("wfs3/?f=text/xml");
        print(dom);
        // TODO: add actual tests in here
    }

    @Test
    public void testLandingPageYaml() throws Exception {
        String yaml = getAsString("wfs3/?f=application/x-yaml");
        System.out.println(yaml);
        // TODO: add actual tests in here
    }

    @Test
    public void testLandingPageHTML() throws Exception {
        MockHttpServletResponse response = getAsServletResponse("wfs3?f=html");
        assertEquals(200, response.getStatus());
        assertEquals("text/html", response.getContentType());

        System.out.println(response.getContentAsString());

        // parse the HTML
        org.jsoup.nodes.Document document = Jsoup.parse(response.getContentAsString());
        // check a couple of links
        assertEquals(
                "" /* Not encoded yet "http://localhost:8080/geoserver/wfs3/collections?f=text/html"  */,
                document.select("#collectionsHtmlLink").attr("href"));
        assertEquals(
                "http://localhost:8080/geoserver/wfs3/api?f=application%2Fjson",
                document.select("#jsonApiLink").attr("href"));
    }
}
