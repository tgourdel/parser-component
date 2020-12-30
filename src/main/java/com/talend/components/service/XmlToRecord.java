package com.talend.components.service;

import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.json.*;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.xpath.*;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class XmlToRecord implements Serializable {

    private final RecordBuilderFactory factory;

    private final boolean enforceNumberAsString;

    public XmlToRecord(final RecordBuilderFactory factory) {
        this(factory, false);
    }

    public XmlToRecord(RecordBuilderFactory factory, boolean enforceNumberAsString) {
        this.factory = factory;
        this.enforceNumberAsString = enforceNumberAsString;
    }

    public Record toRecord(final Node node) throws XPathExpressionException, ParseException {

        // Initialization
        Record.Builder builder = factory.newRecordBuilder();
        List<Node> childNodes = asList(node.getChildNodes());
        int childNodesSize = childNodes.size();

        // No data
        if(childNodesSize == 0) {
            return builder.withString(node.getNodeName(), null).build();
        }

        // Only one child
        if(childNodesSize == 1) {
            // System.out.println("only one child");
            Node onlyChild = childNodes.get(0);
            if(onlyChild.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                mapXmlText(onlyChild.getNodeName(), onlyChild.getFirstChild().getTextContent(), builder);
            } else if (onlyChild.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE) {
                builder.withString(onlyChild.getNodeName(), onlyChild.getFirstChild().getTextContent());
            }
            else {
                builder.withRecord(onlyChild.getNodeName(), toRecord(onlyChild));
            }
        }

        // Multiple child
        if(childNodesSize > 1) {

            // In case we have an array
            int it = 0;
            boolean isArray = false;
            String arrayName = null;
            ArrayList<Record> nodeArray = new ArrayList<Record>();
            ArrayList<String> stringArray = new ArrayList<String>();

            for(Node n: childNodes) {

                it = 0;

                for(Node o: childNodes) {
                    if(n.getNodeName().equals(o.getNodeName())) {
                      it++;
                    }
                }

                if(it == 1) {
                    // System.out.println(n.getNodeName() + " is unique");
                    if(n.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                        mapXmlText(n.getNodeName(), n.getFirstChild().getTextContent(), builder);
                    }
                    else
                        builder.withRecord(n.getNodeName(), toRecord(n));

                } else {
                    // System.out.println(n.getNodeName() + " isn't unique");
                    isArray = true;
                    arrayName = n.getNodeName();

                    if(n.getFirstChild().getNodeType() == Node.TEXT_NODE)
                        stringArray.add(n.getTextContent());
                    else
                        nodeArray.add(toRecord(n));
                }
            }

            if(isArray) {

                if(nodeArray.size() > 0) {
                    // System.out.println("isArray is true: build with array");
                    builder.withArray(factory.newEntryBuilder().withName(arrayName).withType(Schema.Type.ARRAY)
                            .withElementSchema(nodeArray.get(0).getSchema()).build(), nodeArray);
                }

                if(stringArray.size() > 0) {
                    // System.out.println("isArray is true: build with string array");
                    builder.withArray(factory.newEntryBuilder().withName(arrayName).withType(Schema.Type.ARRAY)
                            .withElementSchema(factory.newSchemaBuilder(Schema.Type.STRING).build()).build(), stringArray);
                }


            }
        }
        return builder.build();
    }

    private void mapXmlText(final String name, final String value, Record.Builder builder) {
        // For text nodes

                if(this.enforceNumberAsString) {
                    builder.withString(name, value);
                } else {
                    try {
                        Number number = NumberFormat.getInstance().parse(value);
                        if(Double.class.isInstance(number)){
                            builder.withDouble(name, number.doubleValue());
                        }
                        if (Long.class.isInstance(number)) {
                            builder.withLong(name, number.longValue());
                        }
                    } catch (ParseException e) {
                        builder.withString(name, value);
                    }
                }
    }

    public List<Node> asList(NodeList n) {
        return n.getLength()==0?
            Collections.<Node>emptyList(): new NodeListWrapper(n);
    }

    final class NodeListWrapper extends AbstractList<Node> implements RandomAccess {
        private final NodeList list;

        NodeListWrapper(NodeList l) {
            list = l;
        }

        public Node get(int index) {
            return list.item(index);
        }

        public int size() {
            return list.getLength();
        }
    }

}