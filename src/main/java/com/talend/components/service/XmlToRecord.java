package com.talend.components.service;

import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.json.*;
import javax.xml.xpath.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class XmlToRecord implements Serializable {

    private final RecordBuilderFactory factory;

    public XmlToRecord(RecordBuilderFactory factory) {
        this.factory = factory;
    }

    public Record toRecord(final Node node) throws XPathExpressionException {

        System.out.println("====> Node name: " + node.getNodeName());
        System.out.println("====> node value: " + node.getNodeValue());
        Record.Builder builder = factory.newRecordBuilder();

        if(node.hasChildNodes()) {

            System.out.println("====> Node has childrens!! ");
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            XPathExpression expr = xpath.compile("//*");

            NodeList doc = node.getChildNodes();
            NodeList elements = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            for(int i=0; i < elements.getLength(); ++i) {
                System.out.println("====> Node num " + i);
                System.out.println("====> Name " + elements.item(i).getNodeName());
                builder.withRecord(elements.item(i).getNodeName(), toRecord(elements.item(i)));
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