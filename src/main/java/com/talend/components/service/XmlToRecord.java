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

    public XmlToRecord(RecordBuilderFactory factory) {
        this.factory = factory;
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
            System.out.println("only one child");
            Node onlyChild = childNodes.get(0);
            if(onlyChild.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                mapXmlText(onlyChild.getNodeName(), onlyChild.getFirstChild().getTextContent(), builder);
            }
            else
                builder.withRecord(onlyChild.getNodeName(), toRecord(onlyChild));
        }

        // Multiple child
        if(childNodesSize > 1) {

            // In case we have an array
            int it = 0;
            boolean isArray = false;
            String arrayName = null;
            ArrayList<Record> nodeArray = new ArrayList<Record>();

            for(Node n: childNodes) {

                it = 0;

                for(Node o: childNodes) {
                    if(n.getNodeName().equals(o.getNodeName())) {
                      it++;
                    }
                }

                if(it == 1) {
                    System.out.println(n.getNodeName() + "is unique");
                    if(n.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                        mapXmlText(n.getNodeName(), n.getFirstChild().getTextContent(), builder);
                    }
                    else
                        builder.withRecord(n.getNodeName(), toRecord(n));

                } else {
                    System.out.println(n.getNodeName() + "isn't unique");
                    isArray = true;
                    arrayName = n.getNodeName();
                    System.out.println("Add record to array");
                    nodeArray.add(toRecord(n));
                }
            }

            if(isArray) {
                System.out.println("isArray is true: build with array");
                builder.withArray(factory.newEntryBuilder().withName(arrayName).withType(Schema.Type.ARRAY)
                        .withElementSchema(nodeArray.get(0).getSchema()).build(), nodeArray);
            }

        }


        return builder.build();
    }

    private void mapXmlText(final String name, final String value, Record.Builder builder) {
        // For text nodes
        System.out.println("mapXml text");

                System.out.println("Node name: " + n.getNodeName());
                System.out.println("Node text content: " + n.getTextContent());
                try {
                    Number number = NumberFormat.getInstance().parse(n.getTextContent());
                    if(Double.class.isInstance(number)){
                        System.out.println("Double");
                        builder.withDouble(n.getNodeName(), number.doubleValue());
                    }
                    if (Long.class.isInstance(number)) {
                        System.out.println("Long");
                        builder.withLong(n.getNodeName(), number.longValue());
                    }
                } catch (ParseException e) {
                    System.out.println("Parse not number : " + e);
                    System.out.println("build with string");
                    builder.withString(n.getNodeName(), n.getTextContent());
                }
    }


    /**
        private Schema getArrayElementSchema(final RecordBuilderFactory factory, final List<Object> items) {
            if (items.isEmpty()) {
                return factory.newSchemaBuilder(Schema.Type.STRING).build();
            }
            final Schema firstSchema = toSchema(items.get(0));
            if (firstSchema.getType() == Schema.Type.RECORD) {
                // This code merges schema of all record of the array [{aaa, bbb}, {aaa, ccc}] => {aaa, bbb, ccc}
                return items.stream().skip(1).map(this::toSchema).reduce(firstSchema, (Schema s1, Schema s2) -> {
                    if (s1 == null) {
                        return s2;
                    }
                    if (s2 == null) { // unlikely
                        return s1;
                    }
                    final List<Schema.Entry> entries1 = s1.getEntries();
                    final List<Schema.Entry> entries2 = s2.getEntries();
                    final Set<String> names1 = entries1.stream().map(Schema.Entry::getName).collect(toSet());
                    final Set<String> names2 = entries2.stream().map(Schema.Entry::getName).collect(toSet());
                    if (!names1.equals(names2)) {
                        // here we are not good since values will not be right anymore,
                        // forbidden for current version anyway but potentially supported later
                        final Schema.Builder builder = factory.newSchemaBuilder(Schema.Type.RECORD);
                        entries1.forEach(builder::withEntry);
                        entries2.stream().filter(it -> !names1.contains(it.getName())).forEach(builder::withEntry);
                        return builder.build();
                    }
                    return s1;
                });
            } else {
                return firstSchema;
            }
        }*/

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