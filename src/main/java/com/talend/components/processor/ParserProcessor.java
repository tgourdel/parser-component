package com.talend.components.processor;

import static java.util.stream.Collectors.toList;
import static org.talend.sdk.component.api.component.Icon.IconType.CUSTOM;

import java.io.Serializable;
import java.io.StringReader;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.json.*;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.talend.components.service.JsonToRecord;

import com.talend.components.ParserProcessorRuntimeException;
import com.talend.components.service.Format;
import com.talend.components.service.XmlToRecord;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Input;
import org.talend.sdk.component.api.processor.Output;
import org.talend.sdk.component.api.processor.OutputEmitter;
import org.talend.sdk.component.api.processor.Processor;
import org.talend.sdk.component.api.record.Record;

import com.talend.components.service.ParserComponentService;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


@Version(1) // default version is 1, if some configuration changes happen between 2 versions you can add a migrationHandler
@Icon(value = CUSTOM, custom = "parser") // icon is located at src/main/resources/icons/parser.svg
@Processor(name = "parser")
@Documentation("Performs JSON or XML parsing on input fields.")
public class ParserProcessor implements Serializable {
    private final ParserProcessorConfiguration configuration;
    private final ParserComponentService service;
    private RecordBuilderFactory builderFactory;
    final JsonToRecord jsonToRecord;
    final XmlToRecord xmlToRecord;
    private String field;
    private Format format;

    public ParserProcessor(@Option("configuration") final ParserProcessorConfiguration configuration,
                           final ParserComponentService service,
                           final RecordBuilderFactory builderFactory) {
        this.configuration = configuration;
        this.service = service;
        this.builderFactory = builderFactory;
        this.jsonToRecord = new JsonToRecord(builderFactory, false);
        this.xmlToRecord = new XmlToRecord(builderFactory);
    }

    @PostConstruct
    public void init() {
        // get field name
        field = this.configuration.getField();
        format = this.configuration.getFormat();
    }

    @ElementListener
    public void onNext(
            @Input final Record defaultInput,
            @Output final OutputEmitter<Record> defaultOutput) {

        if(field != null) {

            // Record Initialization
            Record.Builder builder = builderFactory.newRecordBuilder();

            switch (format) {
                case JSON:
                    JsonReader jsonReader = Json.createReader(new StringReader(defaultInput.getString(field)));
                    JsonObject jsonObjectRead = jsonReader.readObject();
                    jsonReader.close();

                    final Schema schema = defaultInput.getSchema();

                    for (Schema.Entry entry : schema.getEntries()) {
                        System.out.println(("====> entry.getName : " + entry.getName()));
                        System.out.println(("====> field : " + (field.startsWith(".") ? field.substring(1) : field)));
                       if(entry.getName().equals((field.startsWith(".") ? field.substring(1) : field))) {
                           builder.withRecord(field, jsonToRecord.toRecord(jsonObjectRead));
                           System.out.println(("====> field found!!!!!!"));
                       } else {
                           switch (entry.getType()) {
                               case DATETIME:
                                   builder.withDateTime(entry.getName(), defaultInput.getDateTime(entry.getName()));
                                   break;
                               case BOOLEAN:
                                   builder.withBoolean(entry.getName(), defaultInput.getBoolean(entry.getName()));
                                   break;
                               case DOUBLE:
                                   builder.withDouble(entry.getName(), defaultInput.getDouble(entry.getName()));
                                   break;
                               case INT:
                                   builder.withInt(entry.getName(), defaultInput.getInt(entry.getName()));
                                   break;
                               case LONG:
                                   builder.withLong(entry.getName(), defaultInput.getLong(entry.getName()));
                                   break;
                               case FLOAT:
                                   builder.withFloat(entry.getName(), defaultInput.getFloat(entry.getName()));
                                   break;
                               case STRING:
                                   builder.withString(entry.getName(), defaultInput.getString(entry.getName()));
                                   break;
                               case BYTES:
                                   builder.withBytes(entry.getName(), defaultInput.getBytes(entry.getName()));
                                   break;
                           }
                       }
                    }
                    break;
                case XML:

                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder Xmlbuilder;
                    try {
                        Xmlbuilder = factory.newDocumentBuilder();
                        Document document = Xmlbuilder.parse(new InputSource(new StringReader(defaultInput.getString(field))));
                        document.getDocumentElement().normalize();
                        builder.withRecord("root", xmlToRecord.toRecord(document));

                    } catch (Exception e) {
                        throw new ParserProcessorRuntimeException("XML Parsing failed: " + e.getMessage());
                    }
                    break;
                default:
                    throw new ParserProcessorRuntimeException("Format is not available.");
            }

            // Emit record built earlier
            Record record = builder.build();
            defaultOutput.emit(record);
        } else {
            defaultOutput.emit(null);
        }
    }

    @PreDestroy
    public void release() {
        // this is the symmetric method of the init() one,
        // release potential connections you created or data you cached
        // Note: if you don't need it you can delete it
    }


}