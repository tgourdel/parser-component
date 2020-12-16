package com.talend.components.processor;

import static org.talend.sdk.component.api.component.Icon.IconType.CUSTOM;

import java.io.Serializable;
import java.io.StringReader;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.json.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;

import com.talend.components.ParserProcessorRuntimeException;
import com.talend.components.service.Format;
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
import org.talend.sdk.component.api.service.record.RecordBuilderFactory;

@Version(1) // default version is 1, if some configuration changes happen between 2 versions you can add a migrationHandler
@Icon(value = CUSTOM, custom = "parser") // icon is located at src/main/resources/icons/parser.svg
@Processor(name = "parser")
@Documentation("Performs JSON or XML parsing on input fields.")
public class ParserProcessor implements Serializable {
    private final ParserProcessorConfiguration configuration;
    private final ParserComponentService service;
    private JsonBuilderFactory builderFactory;
    private String field;
    private Format format;

    public ParserProcessor(@Option("configuration") final ParserProcessorConfiguration configuration,
                           final ParserComponentService service,
                           final JsonBuilderFactory builderFactory) {
        this.configuration = configuration;
        this.service = service;
        this.builderFactory = builderFactory;
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
            @Output final OutputEmitter<JsonObject> defaultOutput) {

        if(defaultInput.getString(field) != null) {
            switch (format) {

                case JSON:
                    JsonReader jsonReader = Json.createReader(new StringReader(defaultInput.getString(field)));
                    JsonObject jsonObjectRead = jsonReader.readObject();
                    jsonReader.close();
                    JsonObject record = builderFactory.createObjectBuilder().add(field, jsonObjectRead).build();
                    defaultOutput.emit(record);
                    break;
                case XML:

                    // DocumentBuilderFactory factory =
                    // DocumentBuilderFactory.newInstance();
                    // DocumentBuilder builder = factory.newDocumentBuilder();

                    break;
                default:
                    throw new ParserProcessorRuntimeException("Format is not available.");
            }
        }
    }

    @PreDestroy
    public void release() {
        // this is the symmetric method of the init() one,
        // release potential connections you created or data you cached
        // Note: if you don't need it you can delete it
    }
}