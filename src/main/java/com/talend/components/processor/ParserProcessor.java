package com.talend.components.processor;

import static org.talend.sdk.component.api.component.Icon.IconType.CUSTOM;

import java.io.Serializable;
import java.io.StringReader;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.json.*;

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
@Icon(value = CUSTOM, custom = "jsonparser") // icon is located at src/main/resources/icons/jsonparser.svg
@Processor(name = "jsonparser")
@Documentation("Parses data")
public class ParserProcessor implements Serializable {
    private final ParserProcessorConfiguration configuration;
    private final ParserComponentService service;
    private JsonBuilderFactory builderFactory;

    public ParserProcessor(@Option("configuration") final ParserProcessorConfiguration configuration,
                           final ParserComponentService service,
                           final JsonBuilderFactory builderFactory) {
        this.configuration = configuration;
        this.service = service;
        this.builderFactory = builderFactory;
    }

    @PostConstruct
    public void init() {
        // this method will be executed once for the whole component execution,
        // this is where you can establish a connection for instance
        // Note: if you don't need it you can delete it
    }

    @ElementListener
    public void onNext(
            @Input final Record defaultInput,
            @Output final OutputEmitter<JsonObject> defaultOutput) {

        String name = defaultInput.getString(this.configuration.getField());
        JsonReader jsonReader = Json.createReader(new StringReader(defaultInput.getString(this.configuration.getField())));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();

        defaultOutput.emit(object);
    }

    @PreDestroy
    public void release() {
        // this is the symmetric method of the init() one,
        // release potential connections you created or data you cached
        // Note: if you don't need it you can delete it
    }
}