package com.talend.components.service;

import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.json.*;
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

    public Record toRecord(final Node node) {

        System.out.println("====> Node name: " + node.getNodeName());
        System.out.println("====> node value: " + node.getNodeValue());
        Record.Builder builder = factory.newRecordBuilder();

        // get all child nodes
        NodeList childrens = node.getChildNodes();

        if(node.hasChildNodes()) {
            System.out.println("====> Node has childrens!! ");
            for (int i=0; i<childrens.getLength(); i++) {
                // get child node
                Node childNode = childrens.item(i);
                System.out.println("====> Child node found!");
                System.out.println("====> Child node name: " + childNode.getNodeName());
                System.out.println("====> Child node value: " + childNode.getNodeValue());

                builder.withRecord(childNode.getNodeName(), toRecord(childNode));
            }
        }
        else if (node.getNodeType() == Node.TEXT_NODE) {
            System.out.println("====> Node is text ");

            builder.withString(node.getNodeName(), node.getNodeValue());
        }
        // visit child node
        return builder.build();
    }
}