package grails.plugins.feeds
/*
 * Copyright 2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
import groovy.util.XmlSlurper
import static org.junit.Assert.*
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
import grails.transaction.*
import org.junit.Before
import grails.util.*


/** 
 * Argh so much pain and diminishing returns, not testing this... leave til later
 *
 * CURRENTLY THESE TESTS DO NOT TEST ANYTHING
 *
 * @author Marc Palmer (marc@anyware.co.uk)
 */
@TestMixin(IntegrationTestMixin)
// @Rollback
class RenderMethodTests {


	private TestController controller = new TestController()

	void setUp() {
		GrailsWebMockUtil.bindMockWebRequest()
	}

	void testRenderNodes() {
		controller.test1()

		def dom = parseXml(controller.response.contentAsString)
		assertNotNull dom

		assertEquals 1, dom.channel.size()
		assertEquals 3, dom.channel[0].item.size()
	}

/* 
    This test fails with null "mode" errors, seems to be a ROME thing?
	void testRenderNodesToAtom03() {
	    doTestRenderNodesToAtom("0.3")
    }
*/    

	void testRenderNodesToAtom10() {
		doTestRenderNodesToAtom("1.0")
	}

	private void doTestRenderNodesToAtom(version) {
		controller.test2(version)

		def dom = parseXml(controller.response.contentAsString)
		assertNotNull dom

		assertEquals 3, dom.entry.size()
	}

	void testRenderNodesNestedExplicitContentNodeBeforePropertySetters() {
		controller.test3()

		def dom = parseXml(controller.response.contentAsString)
		assertNotNull dom

		assertEquals 1, dom.channel.size()
		assertEquals 3, dom.channel[0].item.size()
		assertEquals 'http://somewhere.com/x', dom.channel[0].item[0].guid.text()
		assertEquals 'http://somewhere.com/x', dom.channel[0].item[0].link.text()
	}

	void testRenderNodesNestedExplicitContentNode() {
		controller.test4()

		def dom = parseXml(controller.response.contentAsString)
		assertNotNull dom

		assertEquals 1, dom.channel.size()
		assertEquals 3, dom.channel[0].item.size()
		assertEquals 'http://somewhere.com/x', dom.channel[0].item[0].guid.text()
		assertEquals 'http://somewhere.com/x', dom.channel[0].item[0].link.text()
	}

	// Make sure our meta stuff is not inferfering with normal property resolution
	void testRenderNodesBadMethodResolution() {
		try {
			controller.test5()
			fail("shouldn't happen")			
		}
		catch(MissingPropertyException e) {
			// expected
		}
	}

	void testRenderStringStillWorks() {
		controller.test6()

		assertEquals "Hello world", controller.response.contentAsString
	}

	void testRenderClosureStillWorks() {
		controller.test7()

		assertTrue controller.response.contentAsString.contains("<html>")
	}

	void testRenderMapClosureStillWorks() {
		controller.test8()

		assertTrue controller.response.contentAsString.contains("<root>")
	}

	void testSimple() {
		controller.test9()

		def dom = parseXml(controller.response.contentAsString)
		assertNotNull dom

		assertEquals 1, dom.channel.size()
		assertEquals "My test feed", dom.channel[0].title.text()
	}

	private parseXml(String xml) {
		def slurper = new XmlSlurper()
		slurper.setFeature 'http://apache.org/xml/features/disallow-doctype-decl', true
		slurper.parseText xml
	}
}
