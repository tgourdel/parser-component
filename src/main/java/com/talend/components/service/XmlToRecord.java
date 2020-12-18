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
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class XmlToRecord implements Serializable {

    private final RecordBuilderFactory factory;

    public XmlToRecord(RecordBuilderFactory factory) {
        this.factory = factory;
    }

    public Record toRecord(final Node node) throws XPathExpressionException {

        Record.Builder builder = factory.newRecordBuilder();

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        XPathExpression expr = xpath.compile(".//child::*");

        System.out.println("====> Node name: " + node.getNodeName());
        System.out.println("====> node value: " + node.getNodeValue());

        if(node.hasChildNodes()) {
            System.out.println("====> Node has childrens!! ");
            NodeList elements = (NodeList) expr.evaluate(node.getChildNodes(), XPathConstants.NODESET);

            ArrayList<String> listNodes = new ArrayList<String>();

            for(int i=0; i < elements.getLength(); ++i) {
                listNodes.add(elements.item(i).getNodeName());
            }

            List<Object> items = new ArrayList<Object>();
            String listname = "";

            for(int i=0; i < elements.getLength(); ++i) {
                System.out.println("====> Node num " + i);
                System.out.println("====> Name " + elements.item(i).getNodeName());
                if(listNodes.indexOf(elements.item(i).getNodeName()) > 1)  {

                    items.add(toRecord(elements.item(i)));
                } else {
                    builder.withRecord(elements.item(i).getNodeName(), toRecord(elements.item(i)));
                }
            }
            if(!listname.equals("")) {
                builder.withArray(factory.newEntryBuilder().withName(listname).withType(Schema.Type.ARRAY)
                        .build(), items);
            }
        }
        else if (node.getNodeType() == Node.TEXT_NODE) {
            System.out.println("====> Node is text ");

            String name = node.getNodeName();
            String value = node.getTextContent();

            System.out.println("====>Name : " + name);
            System.out.println("====>Value : " + value);

            builder.withString(name, value);
        } else {
            System.out.println("====> Nothing to do");
        }
        // visit child node
        return builder.build();
    }

}