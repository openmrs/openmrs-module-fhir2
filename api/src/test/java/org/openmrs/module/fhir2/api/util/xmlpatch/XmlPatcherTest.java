/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.util.xmlpatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XmlPatcherTest {
	
	private static String applyPatch(String target, String diff) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XmlPatcher.patch(new ByteArrayInputStream(target.getBytes(StandardCharsets.UTF_8)),
		    new ByteArrayInputStream(diff.getBytes(StandardCharsets.UTF_8)), out);
		return out.toString(StandardCharsets.UTF_8.name());
	}
	
	private static Document parseNamespaceAware(String xml) throws IOException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			return dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
		}
		catch (Exception e) {
			throw new IOException("Failed to parse result XML", e);
		}
	}
	
	@Test
	public void shouldReplaceAttributeValueWithTrimmedMultilineText() throws IOException {
		String target = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		        + "<Patient xmlns=\"http://hl7.org/fhir\"><gender value=\"male\"/></Patient>";
		String diff = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<diff xmlns:fhir=\"http://hl7.org/fhir\">"
		        + "<replace sel=\"/fhir:Patient/fhir:gender/@value\">\n\t\tfemale\n\t</replace>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("value=\"female\""));
		assertThat(result, not(containsString("\n\t\tfemale")));
	}
	
	@Test
	public void shouldNotTrimSingleLineTextByDefault() throws IOException {
		String target = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		        + "<Patient xmlns=\"http://hl7.org/fhir\"><gender value=\"male\"/></Patient>";
		String diff = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<diff xmlns:fhir=\"http://hl7.org/fhir\">"
		        + "<replace sel=\"/fhir:Patient/fhir:gender/@value\">female</replace>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("value=\"female\""));
	}
	
	@Test
	public void shouldHonorTrimFalseOverride() throws IOException {
		String target = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		        + "<Patient xmlns=\"http://hl7.org/fhir\"><gender value=\"male\"/></Patient>";
		String diff = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<diff xmlns:fhir=\"http://hl7.org/fhir\">"
		        + "<replace sel=\"/fhir:Patient/fhir:gender/@value\" trim=\"false\">\n  female\n</replace>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		// Newlines in attribute values must be preserved (here as character references).
		assertThat(result, containsString("&#10;  female&#10;"));
	}
	
	@Test
	public void shouldReplaceElementUnderDefaultNamespace() throws IOException {
		String target = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		        + "<Location xmlns=\"http://hl7.org/fhir\"><name value=\"Old\"/></Location>";
		String diff = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<diff xmlns:fhir=\"http://hl7.org/fhir\">"
		        + "<replace sel=\"/fhir:Location/fhir:name\"><name value=\"New\"/></replace>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("<name value=\"New\"/>"));
		// Replacement element must be in the FHIR default namespace, not bound to xmlns="".
		assertThat(result, not(containsString("xmlns=\"\"")));
	}
	
	@Test
	public void shouldAddElementUnderDefaultNamespaceWithoutXmlnsEscape() throws IOException {
		String target = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		        + "<Immunization xmlns=\"http://hl7.org/fhir\"><id value=\"1\"/></Immunization>";
		String diff = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<diff xmlns:fhir=\"http://hl7.org/fhir\">"
		        + "<add sel=\"/fhir:Immunization\"><expirationDate value=\"2023-07-30\"/></add>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("<expirationDate value=\"2023-07-30\"/>"));
		assertThat(result, not(containsString("xmlns=\"\"")));
	}
	
	@Test
	public void shouldAddElementWithPrepend() throws IOException {
		String target = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<r xmlns=\"http://example.org/r\"><b/></r>";
		String diff = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<diff xmlns:r=\"http://example.org/r\">"
		        + "<add sel=\"/r:r\" pos=\"prepend\"><a/></add>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		int aPos = result.indexOf("<a/>");
		int bPos = result.indexOf("<b/>");
		assertThat(aPos > 0, equalTo(true));
		assertThat(bPos > 0, equalTo(true));
		assertThat(aPos < bPos, equalTo(true));
	}
	
	@Test
	public void shouldAddElementBeforeSelectedNode() throws IOException {
		String target = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<r xmlns=\"http://example.org/r\"><b/></r>";
		String diff = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<diff xmlns:r=\"http://example.org/r\">"
		        + "<add sel=\"/r:r/r:b\" pos=\"before\"><a/></add>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		int aPos = result.indexOf("<a/>");
		int bPos = result.indexOf("<b/>");
		assertThat(aPos > 0, equalTo(true));
		assertThat(aPos < bPos, equalTo(true));
	}
	
	@Test
	public void shouldAddElementAfterSelectedNode() throws IOException {
		String target = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<r xmlns=\"http://example.org/r\"><b/></r>";
		String diff = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<diff xmlns:r=\"http://example.org/r\">"
		        + "<add sel=\"/r:r/r:b\" pos=\"after\"><c/></add>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		int bPos = result.indexOf("<b/>");
		int cPos = result.indexOf("<c/>");
		assertThat(cPos > 0, equalTo(true));
		assertThat(bPos < cPos, equalTo(true));
	}
	
	@Test
	public void shouldAddAttributeViaTypeShorthand() throws IOException {
		String target = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<r xmlns=\"http://example.org/r\"><thing/></r>";
		String diff = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<diff xmlns:r=\"http://example.org/r\">"
		        + "<add sel=\"/r:r/r:thing\" type=\"@id\">42</add>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("id=\"42\""));
	}
	
	@Test
	public void shouldRemoveSelectedElement() throws IOException {
		String target = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<r xmlns=\"http://example.org/r\"><a/><b/></r>";
		String diff = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<diff xmlns:r=\"http://example.org/r\">"
		        + "<remove sel=\"/r:r/r:a\"/>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, not(containsString("<a/>")));
		assertThat(result, containsString("<b/>"));
	}
	
	@Test
	public void shouldRemoveAttribute() throws IOException {
		String target = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		        + "<r xmlns=\"http://example.org/r\"><thing id=\"42\"/></r>";
		String diff = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<diff xmlns:r=\"http://example.org/r\">"
		        + "<remove sel=\"/r:r/r:thing/@id\"/>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, not(containsString("id=\"42\"")));
	}
	
	@Test
	public void shouldRemoveSurroundingWhitespaceWithWsBoth() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\">\n  <a/>\n  <b/>\n</r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<remove sel=\"/r:r/r:a\" ws=\"both\"/>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, not(containsString("<a/>")));
		assertThat(result, containsString("<b/>"));
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldThrowWhenRemovingDocumentRoot() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"/>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<remove sel=\"/r:r\"/>" + "</diff>";
		
		applyPatch(target, diff);
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldThrowWhenSelectorMatchesNothing() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"/>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<remove sel=\"/r:r/r:missing\"/>" + "</diff>";
		
		applyPatch(target, diff);
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldThrowWhenSelMatchesMultipleNodes() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><a/><a/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<remove sel=\"/r:r/r:a\"/>" + "</diff>";
		
		applyPatch(target, diff);
	}
	
	@Test
	public void shouldRemoveMultipleNodesWithMsel() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><a/><a/><b/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<remove msel=\"/r:r/r:a\"/>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, not(containsString("<a/>")));
		assertThat(result, containsString("<b/>"));
	}
	
	@Test
	public void shouldReplaceTextNode() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><note>old</note></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<replace sel=\"/r:r/r:note/text()\">new</replace>"
		        + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("<note>new</note>"));
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldRejectDoctypeInTargetInput() throws IOException {
		String target = "<?xml version=\"1.0\"?><!DOCTYPE r><r/>";
		String diff = "<diff><remove sel=\"/r\"/></diff>";
		applyPatch(target, diff);
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldRejectDoctypeInPatchInput() throws IOException {
		String target = "<r/>";
		String diff = "<?xml version=\"1.0\"?><!DOCTYPE diff><diff><remove sel=\"/r\"/></diff>";
		applyPatch(target, diff);
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldRejectInternalEntityDeclaration() throws IOException {
		String target = "<?xml version=\"1.0\"?>" + "<!DOCTYPE r [<!ENTITY xxe \"data\">]>" + "<r>&xxe;</r>";
		String diff = "<diff><remove sel=\"/r\"/></diff>";
		applyPatch(target, diff);
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldRejectExternalSystemDtdReference() throws IOException {
		String target = "<?xml version=\"1.0\"?>" + "<!DOCTYPE r SYSTEM \"http://example.invalid/evil.dtd\">" + "<r/>";
		String diff = "<diff><remove sel=\"/r\"/></diff>";
		applyPatch(target, diff);
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldRejectExternalGeneralEntity() throws IOException {
		String target = "<?xml version=\"1.0\"?>" + "<!DOCTYPE r [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
		        + "<r>&xxe;</r>";
		String diff = "<diff><remove sel=\"/r\"/></diff>";
		applyPatch(target, diff);
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldRejectAddingAttributeThatAlreadyExists() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><thing id=\"existing\"/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<add sel=\"/r:r/r:thing\" type=\"@id\">replacement</add>"
		        + "</diff>";
		applyPatch(target, diff);
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldRejectTextReplaceWithElementContent() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><note>old</note></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<replace sel=\"/r:r/r:note/text()\"><foo/></replace>"
		        + "</diff>";
		applyPatch(target, diff);
	}
	
	@Test
	public void shouldReplaceComment() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><!-- old --><a/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<replace sel=\"/r:r/comment()\"><!-- new --></replace>"
		        + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("<!-- new -->"));
		assertThat(result, not(containsString("<!-- old -->")));
	}
	
	@Test
	public void shouldRemoveComment() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><!-- to be removed --><a/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<remove sel=\"/r:r/comment()\"/>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, not(containsString("to be removed")));
		assertThat(result, containsString("<a/>"));
	}
	
	@Test
	public void shouldReplaceProcessingInstruction() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><?target old?><a/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">"
		        + "<replace sel=\"/r:r/processing-instruction('target')\"><?target new?></replace>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("<?target new?>"));
		assertThat(result, not(containsString("<?target old?>")));
	}
	
	@Test
	public void shouldRemoveProcessingInstruction() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><?target gone?><a/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<remove sel=\"/r:r/processing-instruction('target')\"/>"
		        + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, not(containsString("<?target gone?>")));
	}
	
	@Test
	public void shouldAddNamespaceDeclaration() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><a/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">"
		        + "<add sel=\"/r:r/r:a\" type=\"namespace::ext\">http://example.org/ext</add>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("xmlns:ext=\"http://example.org/ext\""));
	}
	
	@Test
	public void shouldApplyAddOperationToAllMatchesViaMsel() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><a/><a/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<add msel=\"/r:r/r:a\" type=\"@flag\">on</add>"
		        + "</diff>";
		
		String result = applyPatch(target, diff);
		
		// Both <a> elements should now carry the flag attribute.
		int firstMatch = result.indexOf("flag=\"on\"");
		int secondMatch = result.indexOf("flag=\"on\"", firstMatch + 1);
		assertThat(firstMatch > 0, equalTo(true));
		assertThat(secondMatch > firstMatch, equalTo(true));
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldFailWhenLaterOperationReferencesNodeRemovedByEarlierOperation() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><a/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<remove sel=\"/r:r/r:a\"/>"
		        + "<replace sel=\"/r:r/r:a\"><a value=\"x\"/></replace>" + "</diff>";
		applyPatch(target, diff);
	}
	
	@Test
	public void shouldAddCommentAsContent() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><a/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">"
		        + "<add sel=\"/r:r\" pos=\"prepend\"><!-- annotation --></add>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("<!-- annotation -->"));
	}
	
	@Test
	public void shouldAddElementWhenPatchDocHasDefaultNamespace() throws IOException {
		// The patch's own elements (<diff>, <add>, ...) sit in the patch's default namespace,
		// distinct from the target namespace. Operation dispatch uses local name so this works.
		String target = "<r xmlns=\"http://example.org/r\"><a/></r>";
		String diff = "<diff xmlns=\"http://example.org/patch\" xmlns:t=\"http://example.org/r\">"
		        + "<add sel=\"/t:r\"><t:b/></add>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("<b/>"));
		// The patch's own default namespace must not leak into the output.
		assertThat(result, not(containsString("\"http://example.org/patch\"")));
	}
	
	@Test
	public void shouldReplaceAttributeWhenPatchDocHasDefaultNamespace() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><a value=\"1\"/></r>";
		String diff = "<diff xmlns=\"http://example.org/patch\" xmlns:t=\"http://example.org/r\">"
		        + "<replace sel=\"/t:r/t:a/@value\">2</replace>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("value=\"2\""));
	}
	
	@Test
	public void shouldRemoveElementWhenPatchDocHasDefaultNamespace() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><a/><b/></r>";
		String diff = "<diff xmlns=\"http://example.org/patch\" xmlns:t=\"http://example.org/r\">"
		        + "<remove sel=\"/t:r/t:a\"/>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, not(containsString("<a/>")));
		assertThat(result, containsString("<b/>"));
	}
	
	@Test
	public void shouldOperateOnTargetUsingPrefixedNamespace() throws IOException {
		// Target uses prefix "f:" rather than a default xmlns; patch uses prefix "fhir:". XPath
		// matches by URI, so the differently-named prefixes that bind to the same URI line up.
		String target = "<f:Patient xmlns:f=\"http://hl7.org/fhir\"><f:gender value=\"male\"/></f:Patient>";
		String diff = "<diff xmlns:fhir=\"http://hl7.org/fhir\">"
		        + "<replace sel=\"/fhir:Patient/fhir:gender/@value\">female</replace>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("value=\"female\""));
	}
	
	@Test
	public void shouldReplaceDocumentRootElement() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><a/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">"
		        + "<replace sel=\"/r:r\"><newRoot xmlns=\"http://example.org/r\"><b/></newRoot></replace>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("<newRoot"));
		assertThat(result, containsString("<b/>"));
		assertThat(result, not(containsString("<r ")));
	}
	
	@Test
	public void shouldApplyMultipleOperationsInOrder() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><a value=\"1\"/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<replace sel=\"/r:r/r:a/@value\">2</replace>"
		        + "<add sel=\"/r:r\"><b/></add>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("value=\"2\""));
		assertThat(result, containsString("<b/>"));
	}
	
	@Test
	public void shouldAddElementWhenTargetAndPatchUseDifferentPrefixesForSameNamespace() throws IOException {
		// Target binds prefix "t:"; patch binds prefix "p:"; both refer to the same URI.
		String target = "<t:r xmlns:t=\"http://example.org/r\"><t:a/></t:r>";
		String diff = "<diff xmlns:p=\"http://example.org/r\">" + "<add sel=\"/p:r\"><p:b/></add>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		// Reparse and assert the new element is in the right namespace, regardless of which prefix
		// the serializer chose.
		Document resultDoc = parseNamespaceAware(result);
		NodeList bElements = resultDoc.getElementsByTagNameNS("http://example.org/r", "b");
		assertThat(bElements.getLength(), equalTo(1));
	}
	
	@Test
	public void shouldReplaceElementWhenTargetAndPatchUseDifferentPrefixesForSameNamespace() throws IOException {
		String target = "<t:r xmlns:t=\"http://example.org/r\"><t:thing value=\"old\"/></t:r>";
		String diff = "<diff xmlns:p=\"http://example.org/r\">"
		        + "<replace sel=\"/p:r/p:thing\"><p:thing value=\"new\"/></replace>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		Document resultDoc = parseNamespaceAware(result);
		NodeList things = resultDoc.getElementsByTagNameNS("http://example.org/r", "thing");
		assertThat(things.getLength(), equalTo(1));
		assertThat(things.item(0).getAttributes().getNamedItem("value").getNodeValue(), equalTo("new"));
	}
	
	@Test
	public void shouldRemoveElementWhenTargetAndPatchUseDifferentPrefixesForSameNamespace() throws IOException {
		String target = "<t:r xmlns:t=\"http://example.org/r\"><t:a/><t:b/></t:r>";
		String diff = "<diff xmlns:p=\"http://example.org/r\">" + "<remove sel=\"/p:r/p:a\"/>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		Document resultDoc = parseNamespaceAware(result);
		assertThat(resultDoc.getElementsByTagNameNS("http://example.org/r", "a").getLength(), equalTo(0));
		assertThat(resultDoc.getElementsByTagNameNS("http://example.org/r", "b").getLength(), equalTo(1));
	}
	
	@Test
	public void shouldAddCommentBeforeDocumentRoot() throws IOException {
		String target = "<r/>";
		String diff = "<diff><add sel=\"/r\" pos=\"before\"><!-- prolog --></add></diff>";
		
		String result = applyPatch(target, diff);
		
		int commentPos = result.indexOf("<!-- prolog -->");
		int rootPos = result.indexOf("<r/>");
		assertThat(commentPos > 0, equalTo(true));
		assertThat(commentPos < rootPos, equalTo(true));
	}
	
	@Test
	public void shouldAddCommentAfterDocumentRoot() throws IOException {
		String target = "<r/>";
		String diff = "<diff><add sel=\"/r\" pos=\"after\"><!-- epilog --></add></diff>";
		
		String result = applyPatch(target, diff);
		
		int rootPos = result.indexOf("<r/>");
		int commentPos = result.indexOf("<!-- epilog -->");
		assertThat(rootPos > 0, equalTo(true));
		assertThat(commentPos > rootPos, equalTo(true));
	}
	
	@Test
	public void shouldAddProcessingInstructionBeforeDocumentRoot() throws IOException {
		String target = "<r/>";
		String diff = "<diff><add sel=\"/r\" pos=\"before\"><?xml-stylesheet href=\"a.xsl\"?></add></diff>";
		
		String result = applyPatch(target, diff);
		
		int piPos = result.indexOf("<?xml-stylesheet");
		int rootPos = result.indexOf("<r/>");
		assertThat(piPos > 0, equalTo(true));
		assertThat(piPos < rootPos, equalTo(true));
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldRejectAddingElementBeforeDocumentRoot() throws IOException {
		String target = "<r/>";
		String diff = "<diff><add sel=\"/r\" pos=\"before\"><intruder/></add></diff>";
		
		applyPatch(target, diff);
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldRejectAddingElementAfterDocumentRoot() throws IOException {
		String target = "<r/>";
		String diff = "<diff><add sel=\"/r\" pos=\"after\"><intruder/></add></diff>";
		
		applyPatch(target, diff);
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldRejectAddingTextBeforeDocumentRoot() throws IOException {
		String target = "<r/>";
		String diff = "<diff><add sel=\"/r\" pos=\"before\">stray text</add></diff>";
		
		applyPatch(target, diff);
	}
	
	@Test
	public void shouldAddNamespacedAttribute() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\" xmlns:x=\"http://example.org/x\"><thing/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\" xmlns:x=\"http://example.org/x\">"
		        + "<add sel=\"/r:r/r:thing\" type=\"@x:id\">42</add>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		Document resultDoc = parseNamespaceAware(result);
		Element thing = (Element) resultDoc.getElementsByTagNameNS("http://example.org/r", "thing").item(0);
		assertThat(thing.getAttributeNS("http://example.org/x", "id"), equalTo("42"));
	}
	
	@Test
	public void shouldReplaceNamespacedAttribute() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\" xmlns:x=\"http://example.org/x\">" + "<thing x:id=\"old\"/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\" xmlns:x=\"http://example.org/x\">"
		        + "<replace sel=\"/r:r/r:thing/@x:id\">new</replace>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		Document resultDoc = parseNamespaceAware(result);
		Element thing = (Element) resultDoc.getElementsByTagNameNS("http://example.org/r", "thing").item(0);
		assertThat(thing.getAttributeNS("http://example.org/x", "id"), equalTo("new"));
	}
	
	@Test
	public void shouldRemoveNamespacedAttribute() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\" xmlns:x=\"http://example.org/x\">"
		        + "<thing x:id=\"removeMe\" keep=\"1\"/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\" xmlns:x=\"http://example.org/x\">"
		        + "<remove sel=\"/r:r/r:thing/@x:id\"/>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		Document resultDoc = parseNamespaceAware(result);
		Element thing = (Element) resultDoc.getElementsByTagNameNS("http://example.org/r", "thing").item(0);
		assertThat(thing.hasAttributeNS("http://example.org/x", "id"), equalTo(false));
		assertThat(thing.getAttribute("keep"), equalTo("1"));
	}
	
	@Test
	public void shouldHonorTrimTrueOverrideOnSingleLineText() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><a value=\"old\"/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">"
		        + "<replace sel=\"/r:r/r:a/@value\" trim=\"true\">  padded  </replace>" + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("value=\"padded\""));
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldRejectInvalidTrimValue() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><a value=\"old\"/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<replace sel=\"/r:r/r:a/@value\" trim=\"yes\">x</replace>"
		        + "</diff>";
		
		applyPatch(target, diff);
	}
	
	@Test
	public void shouldTrimMultilineTextWhenAddingAttribute() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><thing/></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<add sel=\"/r:r/r:thing\" type=\"@id\">\n\t42\n\t</add>"
		        + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("id=\"42\""));
	}
	
	@Test
	public void shouldTrimMultilineTextWhenReplacingTextNode() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><note>old</note></r>";
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<replace sel=\"/r:r/r:note/text()\">\n\tnew\n\t</replace>"
		        + "</diff>";
		
		String result = applyPatch(target, diff);
		
		assertThat(result, containsString("<note>new</note>"));
	}
	
	@Test(expected = XmlPatchException.class)
	public void shouldRejectAddingNamespacedAttributeWithUnknownPrefix() throws IOException {
		String target = "<r xmlns=\"http://example.org/r\"><thing/></r>";
		// Patch does NOT declare the "x" prefix that the type attribute references.
		String diff = "<diff xmlns:r=\"http://example.org/r\">" + "<add sel=\"/r:r/r:thing\" type=\"@x:id\">42</add>"
		        + "</diff>";
		
		applyPatch(target, diff);
	}
}
