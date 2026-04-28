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

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Applies an XML PATCH document (RFC 5261) to an XML target. The selector grammar uses XPath 1.0
 * via {@link javax.xml.xpath.XPath}, with namespace prefixes resolved against the in-scope
 * declarations of each patch operation element.
 * <p>
 * As a non-RFC convenience inherited from the {@code com.github.dnault:xml-patch} library this
 * replaces, multi-line text content in patch operations is trimmed by default; this can be
 * overridden with {@code trim="true"} or {@code trim="false"} on the operation element.
 * </p>
 */
public final class XmlPatcher {
	
	private static final String XMLNS_NS = "http://www.w3.org/2000/xmlns/";
	
	private XmlPatcher() {
	}
	
	/**
	 * Applies the patch in {@code diff} to the document in {@code target}, writing the result to
	 * {@code out}. Streams are read but not closed by this method.
	 */
	public static void patch(InputStream target, InputStream diff, OutputStream out) throws IOException {
		Document targetDoc = parse(target);
		Document diffDoc = parse(diff);
		
		Element diffRoot = diffDoc.getDocumentElement();
		if (diffRoot == null) {
			throw new XmlPatchException("Patch document has no root element");
		}
		
		NodeList ops = diffRoot.getChildNodes();
		List<Element> opElements = new ArrayList<>();
		for (int i = 0; i < ops.getLength(); i++) {
			Node n = ops.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				opElements.add((Element) n);
			}
		}
		
		for (Element op : opElements) {
			applyOperation(targetDoc, op);
		}
		
		write(targetDoc, out);
	}
	
	private static Document parse(InputStream in) throws IOException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			dbf.setExpandEntityReferences(false);
			dbf.setXIncludeAware(false);
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder builder = dbf.newDocumentBuilder();
			return builder.parse(new InputSource(in));
		}
		catch (ParserConfigurationException | SAXException e) {
			throw new XmlPatchException("Failed to parse XML: " + e.getMessage(), e);
		}
	}
	
	private static void write(Document doc, OutputStream out) throws IOException {
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			trySetAttribute(tf, XMLConstants.ACCESS_EXTERNAL_DTD, "");
			trySetAttribute(tf, XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			t.transform(new DOMSource(doc), new StreamResult(out));
		}
		catch (TransformerException e) {
			throw new XmlPatchException("Failed to serialize XML: " + e.getMessage(), e);
		}
	}
	
	private static void trySetAttribute(TransformerFactory tf, String name, Object value) {
		try {
			tf.setAttribute(name, value);
		}
		catch (IllegalArgumentException ignored) {
			// Some JDK XML implementations don't recognise these attributes; secure processing
			// already covers the same ground, so a missing attribute is not fatal.
		}
	}
	
	private static void applyOperation(Document target, Element op) {
		String name = op.getLocalName();
		if (name == null) {
			name = op.getTagName();
		}
		switch (name) {
			case "add":
				add(target, op);
				break;
			case "replace":
				replace(target, op);
				break;
			case "remove":
				remove(target, op);
				break;
			default:
				throw new XmlPatchException("Unknown patch operation: " + name);
		}
	}
	
	// -- selection -----------------------------------------------------------------------------
	
	private static List<Node> selectNodes(Document target, Element op) {
		boolean multi = false;
		String selector = op.getAttribute("sel");
		if (selector.isEmpty()) {
			selector = op.getAttribute("msel");
			multi = !selector.isEmpty();
		}
		if (selector.isEmpty()) {
			throw new XmlPatchException("Missing 'sel' attribute on <" + op.getTagName() + ">");
		}
		
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(buildNamespaceContext(op));
			NodeList nodes = (NodeList) xpath.evaluate(selector, target, XPathConstants.NODESET);
			
			if (nodes.getLength() == 0) {
				throw new XmlPatchException("No matches for selector: " + selector);
			}
			if (!multi && nodes.getLength() > 1) {
				throw new XmlPatchException(
				        "More than one match for 'sel' selector: " + selector + " (use 'msel' for multi-select)");
			}
			
			List<Node> result = new ArrayList<>(nodes.getLength());
			for (int i = 0; i < nodes.getLength(); i++) {
				result.add(nodes.item(i));
			}
			return result;
		}
		catch (XPathExpressionException e) {
			throw new XmlPatchException("Invalid XPath selector: " + selector, e);
		}
	}
	
	private static NamespaceContext buildNamespaceContext(final Element patchOp) {
		return new NamespaceContext() {
			
			@Override
			public String getNamespaceURI(String prefix) {
				if (prefix == null) {
					throw new IllegalArgumentException("prefix must not be null");
				}
				String uri = patchOp.lookupNamespaceURI(prefix.isEmpty() ? null : prefix);
				return uri != null ? uri : XMLConstants.NULL_NS_URI;
			}
			
			@Override
			public String getPrefix(String namespaceURI) {
				return patchOp.lookupPrefix(namespaceURI);
			}
			
			@Override
			public Iterator<String> getPrefixes(String namespaceURI) {
				String p = patchOp.lookupPrefix(namespaceURI);
				return p == null ? Collections.<String> emptyIterator() : Collections.singletonList(p).iterator();
			}
		};
	}
	
	// -- add -----------------------------------------------------------------------------------
	
	private static void add(Document target, Element op) {
		String position = nullIfEmpty(op.getAttribute("pos"));
		String type = nullIfEmpty(op.getAttribute("type"));
		
		for (Node node : selectNodes(target, op)) {
			doAdd(target, op, node, position, type);
		}
	}
	
	private static void doAdd(Document target, Element op, Node node, String position, String type) {
		if (type != null) {
			if (type.startsWith("@")) {
				addAttribute(op, type.substring(1), asElement(node, "add @attribute"));
				return;
			}
			if (type.startsWith("namespace::")) {
				addNamespaceDeclaration(op, type.substring("namespace::".length()),
				    asElement(node, "add namespace declaration"));
				return;
			}
			throw new XmlPatchException("Unrecognized 'type' value on <add>: " + type);
		}
		
		if ("before".equals(position) || "after".equals(position)) {
			Node parent = node.getParentNode();
			if (parent == null || parent.getNodeType() == Node.DOCUMENT_NODE) {
				NodeList kids = op.getChildNodes();
				for (int i = 0; i < kids.getLength(); i++) {
					Node k = kids.item(i);
					short kt = k.getNodeType();
					if (kt == Node.ELEMENT_NODE) {
						throw new XmlPatchException(
						        "Cannot add elements before/after the document root; only comments and processing instructions are allowed");
					}
					if (kt == Node.TEXT_NODE && !((Text) k).getData().trim().isEmpty()) {
						throw new XmlPatchException("Cannot add text before/after the document root");
					}
				}
			}
		}
		
		String contextDefaultNs = defaultNamespaceFor(node, position);
		List<Node> newContent = cloneContent(target, op, contextDefaultNs);
		
		if (position == null) {
			Element e = asElement(node, "default add (append)");
			for (Node n : newContent) {
				e.appendChild(n);
			}
		} else if ("prepend".equals(position)) {
			Element e = asElement(node, "prepend");
			Node first = e.getFirstChild();
			for (Node n : newContent) {
				if (first == null) {
					e.appendChild(n);
				} else {
					e.insertBefore(n, first);
				}
			}
		} else if ("before".equals(position)) {
			Node parent = node.getParentNode();
			for (Node n : newContent) {
				parent.insertBefore(n, node);
			}
		} else if ("after".equals(position)) {
			Node parent = node.getParentNode();
			Node ref = node.getNextSibling();
			for (Node n : newContent) {
				if (ref == null) {
					parent.appendChild(n);
				} else {
					parent.insertBefore(n, ref);
				}
			}
		} else {
			throw new XmlPatchException("Unrecognized position: " + position
			        + " (expected one of 'before', 'after', 'prepend', or omit for append)");
		}
	}
	
	private static void addAttribute(Element op, String name, Element targetElement) {
		String value = getTextMaybeTrim(op);
		int colon = name.indexOf(':');
		if (colon >= 0) {
			String prefix = name.substring(0, colon);
			String localName = name.substring(colon + 1);
			String nsUri = op.lookupNamespaceURI(prefix);
			if (nsUri == null) {
				throw new XmlPatchException(
				        "Could not resolve namespace prefix '" + prefix + "' for attribute '" + name + "'");
			}
			if (targetElement.hasAttributeNS(nsUri, localName)) {
				throw new XmlPatchException(
				        "Cannot add attribute '" + name + "': attribute already exists on target element");
			}
			targetElement.setAttributeNS(nsUri, name, value);
		} else {
			if (targetElement.hasAttribute(name)) {
				throw new XmlPatchException(
				        "Cannot add attribute '" + name + "': attribute already exists on target element");
			}
			targetElement.setAttribute(name, value);
		}
	}
	
	private static void addNamespaceDeclaration(Element op, String prefix, Element targetElement) {
		String uri = getTextMaybeTrim(op);
		String qname = prefix.isEmpty() ? "xmlns" : "xmlns:" + prefix;
		targetElement.setAttributeNS(XMLNS_NS, qname, uri);
	}
	
	// -- replace -------------------------------------------------------------------------------
	
	private static void replace(Document target, Element op) {
		for (Node node : selectNodes(target, op)) {
			doReplace(target, op, node);
		}
	}
	
	private static void doReplace(Document target, Element op, Node node) {
		short type = node.getNodeType();
		
		if (type == Node.ATTRIBUTE_NODE) {
			ensureOnlyText(op, "Attribute value replacement must contain text only");
			((Attr) node).setValue(getTextMaybeTrim(op));
			return;
		}
		
		if (type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE) {
			Node parent = node.getParentNode();
			if (parent == null) {
				throw new XmlPatchException("Cannot replace orphan text node");
			}
			ensureOnlyText(op, "Replacement of a Text node must contain only text content");
			String replacement = getTextMaybeTrim(op);
			if (replacement.isEmpty()) {
				parent.removeChild(node);
			} else {
				parent.replaceChild(target.createTextNode(replacement), node);
			}
			return;
		}
		
		if (type == Node.ELEMENT_NODE || type == Node.COMMENT_NODE || type == Node.PROCESSING_INSTRUCTION_NODE) {
			Node parent = node.getParentNode();
			if (parent == null) {
				throw new XmlPatchException("Cannot replace orphan node");
			}
			
			String contextDefaultNs;
			if (parent.getNodeType() == Node.DOCUMENT_NODE) {
				contextDefaultNs = null;
			} else {
				contextDefaultNs = ((Element) parent).lookupNamespaceURI(null);
			}
			
			List<Node> replacement = cloneContent(target, op, contextDefaultNs);
			
			if (replacement.size() != 1 || replacement.get(0).getNodeType() != type) {
				throw new XmlPatchException("Replacement must be a single content node of type " + nodeTypeName(node));
			}
			
			if (parent.getNodeType() == Node.DOCUMENT_NODE && type != Node.ELEMENT_NODE) {
				throw new XmlPatchException("Cannot replace prolog nodes");
			}
			
			parent.replaceChild(replacement.get(0), node);
			return;
		}
		
		throw new XmlPatchException("Unsupported node type for replace: " + nodeTypeName(node));
	}
	
	private static void ensureOnlyText(Element op, String errorMessage) {
		NodeList kids = op.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++) {
			short t = kids.item(i).getNodeType();
			if (t != Node.TEXT_NODE && t != Node.CDATA_SECTION_NODE) {
				throw new XmlPatchException(errorMessage);
			}
		}
	}
	
	// -- remove --------------------------------------------------------------------------------
	
	private static void remove(Document target, Element op) {
		for (Node node : selectNodes(target, op)) {
			doRemove(op, node);
		}
	}
	
	private static void doRemove(Element op, Node node) {
		String ws = nullIfEmpty(op.getAttribute("ws"));
		short type = node.getNodeType();
		
		if (type == Node.ELEMENT_NODE || type == Node.COMMENT_NODE || type == Node.PROCESSING_INSTRUCTION_NODE) {
			Node parent = node.getParentNode();
			if (parent == null) {
				throw new XmlPatchException("Cannot remove orphan " + nodeTypeName(node));
			}
			if (parent.getNodeType() == Node.DOCUMENT_NODE) {
				if (type == Node.ELEMENT_NODE) {
					throw new XmlPatchException("Cannot remove the document root element");
				}
				if (ws != null) {
					throw new XmlPatchException("'ws' is not valid when removing prolog nodes");
				}
				parent.removeChild(node);
				return;
			}
			
			List<Node> toDetach = new ArrayList<>();
			toDetach.add(node);
			
			if ("before".equals(ws) || "both".equals(ws)) {
				Node prev = node.getPreviousSibling();
				if (!isWhitespace(prev)) {
					throw new XmlPatchException("Sibling before is not a whitespace node");
				}
				toDetach.add(prev);
			}
			if ("after".equals(ws) || "both".equals(ws)) {
				Node next = node.getNextSibling();
				if (!isWhitespace(next)) {
					throw new XmlPatchException("Sibling after is not a whitespace node");
				}
				toDetach.add(next);
			}
			if (ws != null && !"before".equals(ws) && !"after".equals(ws) && !"both".equals(ws)) {
				throw new XmlPatchException("Unrecognized 'ws' value: " + ws);
			}
			
			for (Node d : toDetach) {
				d.getParentNode().removeChild(d);
			}
			return;
		}
		
		if (ws != null) {
			throw new XmlPatchException(
			        "'ws' attribute is only valid when removing Element, Comment, or Processing Instruction nodes");
		}
		
		if (type == Node.ATTRIBUTE_NODE) {
			Attr a = (Attr) node;
			a.getOwnerElement().removeAttributeNode(a);
			return;
		}
		
		if (type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE) {
			node.getParentNode().removeChild(node);
			return;
		}
		
		throw new XmlPatchException("Unsupported node type for remove: " + nodeTypeName(node));
	}
	
	// -- cloning + namespace canonicalization --------------------------------------------------
	
	private static List<Node> cloneContent(Document target, Element op, String contextDefaultNs) {
		List<Node> result = new ArrayList<>();
		NodeList kids = op.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++) {
			Node k = kids.item(i);
			if (isWhitespace(k)) {
				// Lenient: skip pretty-print whitespace between patch operation children.
				continue;
			}
			Node imported = target.importNode(k, true);
			if (imported.getNodeType() == Node.ELEMENT_NODE) {
				imported = canonicalizeDefaultNamespace(target, (Element) imported, contextDefaultNs);
			}
			result.add(imported);
		}
		return result;
	}
	
	/**
	 * Aligns imported patch content with the namespace context at the insertion point. Two adjustments:
	 * elements with no explicit namespace are promoted into the in-scope default namespace (so an
	 * unprefixed patch fragment doesn't serialize with an {@code xmlns=""} escape), and elements whose
	 * URI already matches the default namespace but were authored with a prefix in the patch get
	 * recreated unprefixed (so we don't drag in a redundant {@code xmlns:prefix=...} declaration).
	 */
	private static Element canonicalizeDefaultNamespace(Document target, Element element, String defaultNs) {
		String elementNs = element.getNamespaceURI();
		String elementPrefix = element.getPrefix();
		
		boolean promoteFromNoNs = elementNs == null && defaultNs != null;
		boolean dropPrefixToMatchDefault = elementNs != null && elementNs.equals(defaultNs) && elementPrefix != null;
		
		if (promoteFromNoNs || dropPrefixToMatchDefault) {
			String newNs = promoteFromNoNs ? defaultNs : elementNs;
			Element copy = target.createElementNS(newNs, element.getLocalName());
			NamedNodeMap attrs = element.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				Attr a = (Attr) attrs.item(i);
				String name = a.getName();
				if ("xmlns".equals(name) || name.startsWith("xmlns:")) {
					continue;
				}
				if (a.getNamespaceURI() == null) {
					copy.setAttribute(name, a.getValue());
				} else {
					copy.setAttributeNS(a.getNamespaceURI(), name, a.getValue());
				}
			}
			List<Node> childSnapshot = childSnapshot(element);
			for (Node child : childSnapshot) {
				element.removeChild(child);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					child = canonicalizeDefaultNamespace(target, (Element) child, newNs);
				}
				copy.appendChild(child);
			}
			return copy;
		}
		
		Attr explicitDefault = element.getAttributeNodeNS(XMLNS_NS, "xmlns");
		String inheritedForChildren;
		if (explicitDefault != null) {
			String declared = explicitDefault.getValue();
			inheritedForChildren = declared.isEmpty() ? null : declared;
		} else {
			// Reaching here means we did not promote, so when elementNs is null defaultNs is also
			// null; when elementNs is non-null, that is what children inherit.
			inheritedForChildren = elementNs;
		}
		
		List<Node> childSnapshot = childSnapshot(element);
		for (Node child : childSnapshot) {
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element childEl = (Element) child;
				Element canonical = canonicalizeDefaultNamespace(target, childEl, inheritedForChildren);
				if (canonical != childEl) {
					element.replaceChild(canonical, childEl);
				}
			}
		}
		return element;
	}
	
	private static List<Node> childSnapshot(Element e) {
		NodeList kids = e.getChildNodes();
		List<Node> snapshot = new ArrayList<>(kids.getLength());
		for (int i = 0; i < kids.getLength(); i++) {
			snapshot.add(kids.item(i));
		}
		return snapshot;
	}
	
	private static String defaultNamespaceFor(Node selectedNode, String position) {
		Node ctx;
		if (position == null || "prepend".equals(position)) {
			ctx = selectedNode;
		} else {
			ctx = selectedNode.getParentNode();
		}
		if (ctx == null) {
			return null;
		}
		if (ctx.getNodeType() == Node.DOCUMENT_NODE) {
			Element root = ((Document) ctx).getDocumentElement();
			return root != null ? root.lookupNamespaceURI(null) : null;
		}
		return ctx.lookupNamespaceURI(null);
	}
	
	// -- text + trim ---------------------------------------------------------------------------
	
	private static String getTextMaybeTrim(Element op) {
		String text = collectImmediateText(op);
		String override = op.getAttribute("trim");
		if ("true".equals(override)) {
			return text.trim();
		}
		if ("false".equals(override)) {
			return text;
		}
		if (!override.isEmpty()) {
			throw new XmlPatchException("Expected 'trim' attribute to be 'true' or 'false' but was: " + override);
		}
		return isMultiline(text) ? text.trim() : text;
	}
	
	private static String collectImmediateText(Element op) {
		StringBuilder sb = new StringBuilder();
		NodeList kids = op.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++) {
			Node k = kids.item(i);
			short t = k.getNodeType();
			if (t == Node.TEXT_NODE || t == Node.CDATA_SECTION_NODE) {
				sb.append(k.getNodeValue());
			}
		}
		return sb.toString();
	}
	
	private static boolean isMultiline(String s) {
		return s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
	}
	
	private static boolean isWhitespace(Node n) {
		return n != null && n.getNodeType() == Node.TEXT_NODE && ((Text) n).getData().trim().isEmpty();
	}
	
	// -- misc ----------------------------------------------------------------------------------
	
	private static String nullIfEmpty(String s) {
		return (s == null || s.isEmpty()) ? null : s;
	}
	
	private static Element asElement(Node node, String contextDescription) {
		if (node.getNodeType() != Node.ELEMENT_NODE) {
			throw new XmlPatchException(
			        "Operation '" + contextDescription + "' requires an element target, got " + nodeTypeName(node));
		}
		return (Element) node;
	}
	
	private static String nodeTypeName(Node n) {
		switch (n.getNodeType()) {
			case Node.ELEMENT_NODE:
				return "Element";
			case Node.ATTRIBUTE_NODE:
				return "Attribute";
			case Node.TEXT_NODE:
				return "Text";
			case Node.CDATA_SECTION_NODE:
				return "CDATA";
			case Node.COMMENT_NODE:
				return "Comment";
			case Node.PROCESSING_INSTRUCTION_NODE:
				return "ProcessingInstruction";
			case Node.DOCUMENT_NODE:
				return "Document";
			default:
				return "Node[type=" + n.getNodeType() + "]";
		}
	}
}
