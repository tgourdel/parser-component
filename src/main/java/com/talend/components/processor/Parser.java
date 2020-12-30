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

import com.talend.components.ParserRuntimeException;
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

import com.talend.components.service.ParserService;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;


@Version(1) // default version is 1, if some configuration changes happen between 2 versions you can add a migrationHandler
@Icon(value = CUSTOM, custom = "parser") // icon is located at src/main/resources/icons/parser.svg
@Processor(name = "parser")
@Documentation("Performs JSON or XML parsing on input fields.")
public class Parser implements Serializable {
    private final ParserConfiguration configuration;
    private final ParserService service;
    private RecordBuilderFactory builderFactory;
    private JsonToRecord jsonToRecord;
    private XmlToRecord xmlToRecord;
    private String field;
    private Format format;

    public Parser(@Option("configuration") final ParserConfiguration configuration,
                  final ParserService service,
                  final RecordBuilderFactory builderFactory) {
        this.configuration = configuration;
        this.service = service;
        this.builderFactory = builderFactory;

    }

    @PostConstruct
    public void init() {
        // get field name
        field = this.configuration.getField();
        field = (field.startsWith(".") ? field.substring(1) : field);
        format = this.configuration.getFormat();

        switch (format) {
            case JSON:
                jsonToRecord = new JsonToRecord(builderFactory, this.configuration.isEnforceNumbersAsDouble());
                break;
            case XML:
                xmlToRecord = new XmlToRecord(builderFactory);
                break;
        }



    }

    @ElementListener
    public void onNext(
            @Input final Record defaultInput,
            @Output final OutputEmitter<Record> defaultOutput) {

             final Schema inputSchema = defaultInput.getSchema();

            if(field == null || field.isEmpty()) {
                defaultOutput.emit(defaultInput); //
            } else { // If a field is set proceed

                StringReader fieldReader = new StringReader(defaultInput.getString(field));
                Record.Builder builder = builderFactory.newRecordBuilder();

                for (Schema.Entry entry : inputSchema.getEntries()) {
                    if (!entry.getName().equals(field)) { // For each exact the one to parse
                        service.forwardEntry(defaultInput, builder, entry.getName(), entry); // Forward all others
                    } else {
                        switch (format) {
                            case JSON:
                                try {
                                JsonReader jsonReader = Json.createReader(fieldReader);
                                JsonObject jsonObjectRead = jsonReader.readObject();
                                jsonReader.close();

                                builder.withRecord(entry.getName(),
                                        jsonToRecord.toRecord(jsonObjectRead, this.configuration.isEnforceNumbersAsDouble()));                                    );
                                } catch (Exception e) {
                                    throw new ParserRuntimeException("JSON Parsing failed: " + e.getMessage());
                                }

                                break;
                            case XML:
                                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                                DocumentBuilder Xmlbuilder;
                                try {
                                    Xmlbuilder = factory.newDocumentBuilder();
                                    Document document = Xmlbuilder.parse(new InputSource(fieldReader));
                                    document.getDocumentElement().normalize();
                                    builder.withRecord(entry.getName(),
                                            xmlToRecord.toRecord(document,
                                            this.configuration.isEnforceNumbersAsString()));

                                } catch (Exception e) {
                                    throw new ParserRuntimeException("XML Parsing failed: " + e.getMessage());
                                }

                                break;
                            default:
                                throw new ParserRuntimeException("Format is not available.");
                        }
                    }
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