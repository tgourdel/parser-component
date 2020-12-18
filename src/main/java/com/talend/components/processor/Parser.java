package com.talend.components.processor;

import static org.talend.sdk.component.api.component.Icon.IconType.CUSTOM;

import java.io.Serializable;
import java.io.StringReader;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.json.*;
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

import com.talend.components.service.ParserProcessorService;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import org.talend.sdk.component.api.service.record.RecordService;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


@Version(1) // default version is 1, if some configuration changes happen between 2 versions you can add a migrationHandler
@Icon(value = CUSTOM, custom = "parser") // icon is located at src/main/resources/icons/parser.svg
@Processor(name = "parser")
@Documentation("Performs JSON or XML parsing on input fields.")
public class Parser implements Serializable {
    private final ParserConfiguration configuration;
    private final ParserProcessorService service;
    private RecordBuilderFactory builderFactory;
    private RecordService recordService;
    final JsonToRecord jsonToRecord;
    final XmlToRecord xmlToRecord;
    private String field;
    private Format format;

    public Parser(@Option("configuration") final ParserConfiguration configuration,
                  final ParserProcessorService service,
                  final RecordBuilderFactory builderFactory) {
        this.configuration = configuration;
        this.service = service;
        this.builderFactory = builderFactory;
        this.recordService = recordService;
        this.jsonToRecord = new JsonToRecord(builderFactory, false);
        this.xmlToRecord = new XmlToRecord(builderFactory);
    }

    @PostConstruct
    public void init() {
        // get field name
        field = this.configuration.getField();
        field = (field.startsWith(".") ? field.substring(1) : field);
        format = this.configuration.getFormat();
    }

    @ElementListener
    public void onNext(
            @Input final Record defaultInput,
            @Output final OutputEmitter<Record> defaultOutput) {

            // If a field hasn't been set
            if(field == null || field.isEmpty()) {
                defaultOutput.emit(defaultInput);
            } else { // If a field is set proceed

                // Used to read JSON or XML
                StringReader fieldReader = new StringReader(defaultInput.getString(field));

                // Record builder
                Record.Builder builder = builderFactory.newRecordBuilder();

                switch (format) {
                    case JSON:
                        JsonReader jsonReader = Json.createReader(fieldReader);
                        JsonObject jsonObjectRead = jsonReader.readObject();
                        jsonReader.close();

                        final Schema inputSchema = defaultInput.getSchema();

                        for (Schema.Entry entry : inputSchema.getEntries())
                        {
                            // For each entry except the one to parse
                            if (!entry.getName().equals(field)) {
                                // Add all field to the builder except the one to parse
                                recordService.forwardEntry(defaultInput, builder, entry.getName(),
                                        builderFactory.newEntryBuilder().withType(entry.getType()).withName(entry.getName()).build());
                            } else {
                                // Replace the one to parse by its record
                                builder.withRecord(
                                        entry.getName(),
                                        jsonToRecord.toRecord(jsonObjectRead));
                            }
                        }
                        break;
                    case XML:
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder Xmlbuilder;
                        try {
                            Xmlbuilder = factory.newDocumentBuilder();
                            Document document = Xmlbuilder.parse(new InputSource(fieldReader));
                            document.getDocumentElement().normalize();
                            builder.withRecord(field, xmlToRecord.toRecord(document));

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
            }
    }

    @PreDestroy
    public void release() {
        // this is the symmetric method of the init() one,
        // release potential connections you created or data you cached
        // Note: if you don't need it you can delete it
    }


}